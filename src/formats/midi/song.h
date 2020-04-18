#ifndef _SONG_H_
#define _SONG_H_

struct event {
	struct event *next;		/* linked list */

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
};


#endif
