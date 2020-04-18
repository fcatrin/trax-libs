#include <stdlib.h>
#include "common.h"
#include "fileutils.h"
#include "song.h"

static struct event *event_new(struct track *track, int32 sysex_length)
{
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

