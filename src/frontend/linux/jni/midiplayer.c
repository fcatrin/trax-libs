#include <alsa/asoundlib.h>
#include <formats/midi/song.h>
#include "sys/time.h"
#include "alsalib.h"

/*
 * 31.25 kbaud, one start bit, eight data bits, two stop bits.
 * (The MIDI spec says one stop bit, but every transmitter uses two, just to be
 * sure, so we better not exceed that to avoid overflowing the output buffer.)
 */
#define MIDI_BYTES_PER_SEC (31250 / (1 + 8 + 2))

int event_map[] = {
	EVENT_NOTEOFF,    SND_SEQ_EVENT_NOTEOFF,
	EVENT_NOTEON,     SND_SEQ_EVENT_NOTEON,
	EVENT_KEYPRESS,   SND_SEQ_EVENT_KEYPRESS,
	EVENT_CONTROLLER, SND_SEQ_EVENT_CONTROLLER,
	EVENT_PGMCHANGE,  SND_SEQ_EVENT_PGMCHANGE,
	EVENT_CHANPRESS,  SND_SEQ_EVENT_CHANPRESS,
	EVENT_PITCHBEND,  SND_SEQ_EVENT_PITCHBEND,
	EVENT_TEMPO,      SND_SEQ_EVENT_TEMPO,
	EVENT_SYSEX,      SND_SEQ_EVENT_SYSEX,
	0
};

int end_delay = 2;

static void check_snd(const char *operation, int err) {
	if (err < 0)
		log_error("Cannot %s - %s", operation, snd_strerror(err));
}

static int get_alsa_event(int event) {
	for(int i=0; event_map[i]; i+=2) {
		if (event_map[i] == event) return event_map[i+1];
	}
	return -1;
}

static void handle_big_sysex(snd_seq_t *seq, snd_seq_event_t *ev) {
	unsigned int length;
	ssize_t event_size;
	int err;

	length = ev->data.ext.len;
	if (length > MIDI_BYTES_PER_SEC)
		ev->data.ext.len = MIDI_BYTES_PER_SEC;
	event_size = snd_seq_event_length(ev);
	if (event_size + 1 > snd_seq_get_output_buffer_size(seq)) {
		err = snd_seq_drain_output(seq);
		check_snd("drain output", err);
		err = snd_seq_set_output_buffer_size(seq, event_size + 1);
		check_snd("set output buffer size", err);
	}
	while (length > MIDI_BYTES_PER_SEC) {
		err = snd_seq_event_output(seq, ev);
		check_snd("output event", err);
		err = snd_seq_drain_output(seq);
		check_snd("drain output", err);
		err = snd_seq_sync_output_queue(seq);
		check_snd("sync output", err);
		if (sleep(1))
			log_error("aborted");
		ev->data.ext.ptr += MIDI_BYTES_PER_SEC;
		length -= MIDI_BYTES_PER_SEC;
	}
	ev->data.ext.len = length;
}

void midi_play(snd_seq_t *seq, struct song *song, struct port_info *port_info) {
	snd_seq_event_t ev;
	struct timeval tv;
	int i, max_tick, err;

	/* calculate length of the entire file */
	max_tick = 0;
	for (i = 0; i < song->num_tracks; ++i) {
		if (song->tracks[i].end_tick > max_tick) {
			max_tick = song->tracks[i].end_tick;
		}
	}

	/* initialize current position in each track */
	for (i = 0; i < song->num_tracks; ++i)
		song->tracks[i].current_event = song->tracks[i].first_event;

	/* common settings for all our events */
	snd_seq_ev_clear(&ev);
	snd_seq_ev_set_direct(&ev);
	ev.source.port = 0;
	snd_seq_ev_set_dest(&ev, port_info->client, port_info->port);

	gettimeofday(&tv, NULL);
	uint32 start_seconds = tv.tv_sec;
	uint32 start_usec    = tv.tv_usec;

	int tempo = song->tempo;
	int ticks_elapsed = 0;

	for (;;) {
		struct event* event = NULL;
		struct track* event_track = NULL;
		int i, min_tick = max_tick + 1;

		gettimeofday(&tv, NULL);
		int elapsed_seconds = tv.tv_sec  - start_seconds;
		int elapsed_usec    = tv.tv_usec - start_usec;

		int miliseconds = (elapsed_seconds * 1000) + (elapsed_usec / 1000);

		int ticks_per_minute = (60000000 / tempo) * song->ppq;
		int ticks = (miliseconds * ticks_per_minute) / 60000;

		/* search next event */
		for (i = 0; i < song->num_tracks; ++i) {
			struct track *track = &song->tracks[i];
			struct event *e2 = track->current_event;

			if (e2 && e2->tick < min_tick) {
				min_tick = e2->tick;
				event = e2;
				event_track = track;
			}

		}

		if (!event)
			break; /* end of song reached */

		//log_debug("ticks/minute:%d ms:%d ticks:%d next_tick:%d", ticks_per_minute, miliseconds, ticks_elapsed, min_tick);

		if ((ticks_elapsed + ticks) < min_tick) {
			usleep(100);
			continue;
		}

		start_seconds  = tv.tv_sec;
		start_usec     = tv.tv_usec;
		ticks_elapsed += ticks;

		/* advance pointer to next event */
		event_track->current_event = event->next;

		/* output the event */
		ev.type = get_alsa_event(event->type);
		ev.time.tick = event->tick;
		// ev.dest = port_info->port; // ports[event->port];
		switch (ev.type) {
		case SND_SEQ_EVENT_NOTEON:
		case SND_SEQ_EVENT_NOTEOFF:
		case SND_SEQ_EVENT_KEYPRESS:
			snd_seq_ev_set_fixed(&ev);
			ev.data.note.channel = event->data.d[0];
			ev.data.note.note = event->data.d[1];
			ev.data.note.velocity = event->data.d[2];
			break;
		case SND_SEQ_EVENT_CONTROLLER:
			snd_seq_ev_set_fixed(&ev);
			ev.data.control.channel = event->data.d[0];
			ev.data.control.param = event->data.d[1];
			ev.data.control.value = event->data.d[2];
			break;
		case SND_SEQ_EVENT_PGMCHANGE:
		case SND_SEQ_EVENT_CHANPRESS:
			snd_seq_ev_set_fixed(&ev);
			ev.data.control.channel = event->data.d[0];
			ev.data.control.value = event->data.d[1];
			break;
		case SND_SEQ_EVENT_PITCHBEND:
			snd_seq_ev_set_fixed(&ev);
			ev.data.control.channel = event->data.d[0];
			ev.data.control.value =
				((event->data.d[1]) |
				 ((event->data.d[2]) << 7)) - 0x2000;
			break;
		case SND_SEQ_EVENT_SYSEX:
			snd_seq_ev_set_variable(&ev, event->data.length,
						event->sysex);
			handle_big_sysex(seq, &ev);
			break;
		case SND_SEQ_EVENT_TEMPO:
			tempo = event->data.tempo;
			continue;
		default:
			log_error("Invalid event type %d!", ev.type);
		}

		err = snd_seq_event_output_direct(seq, &ev);
		check_snd("output event", err);
	}

	/* schedule queue stop at end of song */

	snd_seq_ev_set_fixed(&ev);
	ev.type = SND_SEQ_EVENT_STOP;
	ev.time.tick = max_tick;
	ev.dest.client = SND_SEQ_CLIENT_SYSTEM;
	ev.dest.port = SND_SEQ_PORT_SYSTEM_TIMER;
	err = snd_seq_event_output(seq, &ev);
	check_snd("output event", err);

	/* give the last notes time to die away */
	if (end_delay > 0)
		sleep(end_delay);

}
