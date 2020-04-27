#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <common.h>
#include "fileutils.h"
#include "song.h"
#include "loader.h"

#define SYSEX_META_TRACK_NAME 0x03

#define MAKE_ID(c1, c2, c3, c4) ((c1) | ((c2) << 8) | ((c3) << 16) | ((c4) << 24))

static int32 read_id(struct stream *stream) {
	return read_32_le(stream);
}

static struct event *event_new(struct track *track, int32 sysex_length) {
	struct event *event;

	event = malloc(sizeof(struct event) + sysex_length);
	check_mem(event);

	event->next = NULL;

	/* append at the end of the track's linked list */
	if (track->current_event)
		track->current_event->next = event;
	else
		track->first_event = event;
	track->current_event = event;

	return event;
}

static int read_track(struct stream *stream, struct song *song, uint16 track_index, int track_end) {
	int tick = 0;
	unsigned char last_cmd = 0;
	unsigned char port = 0;

	struct track *track = &song->tracks[track_index];
	track->first_event = NULL;
	track->current_event = NULL;
	track->end_tick = 0;
	track->name = NULL;

	while (stream->offset < track_end) {
		unsigned char cmd;
		struct event *event;
		int delta_ticks, len, c;

		delta_ticks = read_var(stream);
		if (delta_ticks < 0) {
			break;
		}

		tick += delta_ticks;
		c = read_byte(stream);
		if (c < 0) {
			break;
		}

		if (c & 0x80) {
			/* have command */
			cmd = c;
			if (cmd < 0xf0)
				last_cmd = cmd;
		} else {
			/* running status */
			unread_byte(stream, c);
			cmd = last_cmd;
			if (!cmd)
				goto _error;
		}

		switch (cmd >> 4) {
			/* maps SMF events to Song events */
			static const unsigned char cmd_type[] = {
				[0x8] = EVENT_NOTEOFF,
				[0x9] = EVENT_NOTEON,
				[0xa] = EVENT_KEYPRESS,
				[0xb] = EVENT_CONTROLLER,
				[0xc] = EVENT_PGMCHANGE,
				[0xd] = EVENT_CHANPRESS,
				[0xe] = EVENT_PITCHBEND
			};

		case 0x8: /* channel msg with 2 parameter bytes */
		case 0x9:
		case 0xa:
		case 0xb:
		case 0xe:
			event = event_new(track, 0);
			event->type = cmd_type[cmd >> 4];
			event->port = port;
			event->tick = tick;
			event->data.d[0] = cmd & 0x0f;
			event->data.d[1] = read_byte(stream) & 0x7f;
			event->data.d[2] = read_byte(stream) & 0x7f;
			break;

		case 0xc: /* channel msg with 1 parameter byte */
		case 0xd:
			event = event_new(track, 0);
			event->type = cmd_type[cmd >> 4];
			event->port = port;
			event->tick = tick;
			event->data.d[0] = cmd & 0x0f;
			event->data.d[1] = read_byte(stream) & 0x7f;
			break;

		case 0xf:
			switch (cmd) {
			case 0xf0: /* sysex */
			case 0xf7: /* continued sysex, or escaped commands */
				len = read_var(stream);
				if (len < 0) {
					goto _error;
				}
				if (cmd == 0xf0)
					++len;
				event = event_new(track, len);
				event->type = EVENT_SYSEX;
				event->port = port;
				event->tick = tick;
				event->data.length = len;
				if (cmd == 0xf0) {
					event->sysex[0] = 0xf0;
					c = 1;
				} else {
					c = 0;
				}
				for (; c < len; ++c)
					event->sysex[c] = read_byte(stream);
				break;

			case 0xff: /* meta event */
				c = read_byte(stream);
				len = read_var(stream);
				if (len < 0)
					goto _error;

				switch (c) {
				case SYSEX_META_TRACK_NAME:
					track -> name = read_string(stream, len);
					break;
				case 0x21:	 /* port number */
					if (len < 1)
						goto _error;
					port = read_byte(stream); // Port Count is only available in the target system % port_count;
					skip(stream, len - 1);
					break;

				case 0x2f: /* end of track */
					track->end_tick = tick;
					skip(stream, track_end - stream->offset);
					return 1;

				case 0x51: /* tempo */
					if (len < 3)
						goto _error;
					if (song->smpte_timing) {
						/* SMPTE timing doesn't change */
						skip(stream, len);
					} else {
						event = event_new(track, 0);
						event->type = EVENT_TEMPO;
						event->port = port;
						event->tick = tick;
						event->data.tempo = read_byte(stream) << 16;
						event->data.tempo |= read_byte(stream) << 8;
						event->data.tempo |= read_byte(stream);

						// log_debug("tempo change %d", event->data.tempo);

						skip(stream, len - 3);
					}
					break;

				default: /* ignore all other meta events */
					skip(stream, len);
					break;
				}
				break;

			default: /* invalid Fx command */
				goto _error;
			}
			break;

		default: /* cannot happen */
			goto _error;
		}
	}
_error:
	log_error("%s: invalid MIDI data (offset %#x)", stream->filename, stream->offset);
	return 0;
}

/* reads an entire MIDI file */
static struct song *read_smf(struct stream *stream) {

	int header_len, type, i;

	/* the current position is immediately after the "MThd" id */
	header_len = read_int(stream, 4);
	if (header_len < 6) {
		goto invalid_format;
	}

	type = read_int(stream, 2);
	if (type != 0 && type != 1) {
		log_error("%s: type %d format is not supported", stream->filename, type);
		return NULL;
	}

	struct song *song = malloc(sizeof(struct song));

	song->num_tracks = read_int(stream, 2);
	if (song->num_tracks < 1 || song->num_tracks > 1000) {
		log_error("%s: invalid number of tracks (%d)", stream->filename, song->num_tracks);
		goto abort_song;
	}

	song->tracks = calloc(song->num_tracks, sizeof(struct track));
	check_mem(song->tracks);

	int32 time_division = read_int(stream, 2);
	if (time_division < 0)
		goto abort_song;

	/* interpret and set tempo */
	song->smpte_timing = time_division & 0x8000;
	if (!song->smpte_timing) {
		/* time_division is ticks per quarter */
		song->tempo = 500000; /* default: 120 bpm */
		song->ppq   = time_division;
		log_debug("smpt_timing off ppq %d", song->ppq);
	} else {
		/* upper byte is negative frames per second */
		i = 0x80 - ((time_division >> 8) & 0x7f);
		/* lower byte is ticks per frame */
		time_division &= 0xff;
		/* now pretend that we have quarter-note based timing */
		switch (i) {
		case 24:
			song->tempo = 500000;
			song->ppq   = 12 * time_division;
			break;
		case 25:
			song->tempo = 400000;
			song->ppq   = 10 * time_division;
			break;
		case 29: /* 30 drop-frame */
			song->tempo = 100000000;
			song->ppq   = 2997 * time_division;
			break;
		case 30:
			song->tempo = 500000;
			song->ppq   = 15 * time_division;
			break;
		default:
			log_error("%s: invalid number of SMPTE frames per second (%d)", stream->filename, i);
			goto abort_song;
		}
		log_debug("smpt_timing on ppq %d tempo %d division %d", song->ppq, song->tempo, i);
	}

	/* read tracks */
	for (i = 0; i < song->num_tracks; ++i) {
		int len;

		/* search for MTrk chunk */
		for (;;) {
			int id = read_id(stream);
			len = read_int(stream, 4);
			if (feof(stream->file)) {
				log_error("%s: unexpected end of file", stream->filename);
				goto abort_song;
			}
			if (len < 0 || len >= 0x10000000) {
				log_error("%s: invalid chunk length %d", stream->filename, len);
				goto abort_song;
			}

			if (id == MAKE_ID('M', 'T', 'r', 'k')) {
				break;
			}
			skip(stream, len);
		}
		if (!read_track(stream, song, i, stream->offset + len)) {
			goto abort_song;
		}
	}

	return song;

invalid_format:
	log_error("%s: invalid file format", stream->file);
	return  NULL;

abort_song:
	song_unload(song);
	return NULL;
}

void song_unload(struct song *song) {

	for (int i = 0; i < song->num_tracks; ++i) {
		struct track *track = &song->tracks[i];

		struct event *event = track->first_event;
		while (event) {
			struct event *next = event->next;
			free(event);
			event = next;
		}

		free(track->name);
	}

	free(song->tracks);
	free(song);
}

struct song *read_riff(struct stream *stream) {
	/* skip file length */
	read_byte(stream);
	read_byte(stream);
	read_byte(stream);
	read_byte(stream);

	/* check file type ("RMID" = RIFF MIDI) */
	if (read_id(stream) != MAKE_ID('R', 'M', 'I', 'D')) {
		goto invalid_format;
	}

	/* search for "data" chunk */
	for (;;) {
		int id  = read_id(stream);
		int len = read_32_le(stream);

		if (feof(stream->file)) {
			goto data_not_found;
		}

		if (id == MAKE_ID('d', 'a', 't', 'a'))
			break;
		if (len < 0)
			goto data_not_found;
		skip(stream, (len + 1) & ~1);
	}
	/* the "data" chunk must contain data in SMF format */
	if (read_id(stream) != MAKE_ID('M', 'T', 'h', 'd')) {
		goto invalid_format;
	}
	return read_smf(stream);

invalid_format:
	log_error("%s: invalid file format", stream->filename);
	return NULL;

data_not_found:
	log_error("%s: data chunk not found", stream->filename);
	return NULL;
}

struct song *song_load(const char *filename) {

	struct stream *stream = stream_open(filename);

	if (!stream) {
		log_error("Cannot open %s - %s", filename, strerror(errno));
		return NULL;
	}

	struct song *song = NULL;

	switch (read_id(stream)) {
	case MAKE_ID('M', 'T', 'h', 'd'):
		song = read_smf(stream);
		break;
	case MAKE_ID('R', 'I', 'F', 'F'):
		song = read_riff(stream);
		break;
	default:
		log_error("%s is not a Standard MIDI File", filename);
		break;
	}

	stream_close(stream);
	return song;
}
