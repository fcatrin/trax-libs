#ifndef _SONG_H_
#define _SONG_H_

#include "../../common.h"

struct event {
	struct event *next; /* linked list */

	uint8  type;		/* SND_SEQ_EVENT_xxx */
	uint8  port;		/* port index */
	uint32 tick;
	union {
		uint8 d[3];	    /* channel and data bytes */
		uint32 tempo;
		uint32 length;	/* length of sysex data */
	} data;
	uint8 sysex[0];
};

struct track {
	struct event *first_event;	/* list of all events in this track */
	uint32 end_tick;			/* length of this track */

	struct event *current_event;	/* used while loading and playing */
	char *name;
};

struct song {
	uint16 num_tracks;
	struct track  *tracks;
	int32  smpte_timing;

	uint32 tempo;
	uint32 ppq;
};

enum Event {
	EVENT_NOTEOFF = 1,
	EVENT_NOTEON,
	EVENT_KEYPRESS,
	EVENT_CONTROLLER,
	EVENT_PGMCHANGE,
	EVENT_CHANPRESS,
	EVENT_PITCHBEND,
	EVENT_TEMPO,
	EVENT_SYSEX
};

#endif
