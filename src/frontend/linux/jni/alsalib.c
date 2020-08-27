#include <alsa/asoundlib.h>
#include <common.h>
#include "alsalib.h"

static snd_seq_t *seq;
static int client;

#define MAX_PORTS 128
struct port_info ports[MAX_PORTS];
int ports_total = 0;

#define MIDI_BYTES_PER_SEC (31250 / (1 + 8 + 2))

/* error handling for ALSA functions */
static void check_snd(const char *operation, int err) {
	if (err < 0)
		log_fatal("Cannot %s - %s", operation, snd_strerror(err));
}

static void init_seq(void) {
	int err;

	/* open sequencer */
	err = snd_seq_open(&seq, "default", SND_SEQ_OPEN_DUPLEX, 0);
	check_snd("open sequencer", err);

	/* set our name (otherwise it's "Client-xxx") */
	err = snd_seq_set_client_name(seq, "simusplayer");
	check_snd("set client name", err);

	/* find out who we actually are */
	client = snd_seq_client_id(seq);
	check_snd("get client id", client);
}

static void load_ports() {

	snd_seq_client_info_t *cinfo;
	snd_seq_port_info_t *pinfo;

	snd_seq_client_info_alloca(&cinfo);
	snd_seq_port_info_alloca(&pinfo);

	memset(ports, MAX_PORTS, sizeof(struct port_info));

	int port_index = 0;

	snd_seq_client_info_set_client(cinfo, -1);
	while (snd_seq_query_next_client(seq, cinfo) >= 0) {
		int client = snd_seq_client_info_get_client(cinfo);

		snd_seq_port_info_set_client(pinfo, client);
		snd_seq_port_info_set_port(pinfo, -1);
		while (snd_seq_query_next_port(seq, pinfo) >= 0) {
			/* port must understand MIDI messages */
			if (!(snd_seq_port_info_get_type(pinfo)
			      & SND_SEQ_PORT_TYPE_MIDI_GENERIC))
				continue;
			/* we need both WRITE and SUBS_WRITE */
			if ((snd_seq_port_info_get_capability(pinfo)
			     & (SND_SEQ_PORT_CAP_WRITE | SND_SEQ_PORT_CAP_SUBS_WRITE))
			    != (SND_SEQ_PORT_CAP_WRITE | SND_SEQ_PORT_CAP_SUBS_WRITE))
				continue;

			struct port_info *port_info = &ports[port_index++];
			port_info->client      = snd_seq_port_info_get_client(pinfo);
			port_info->port        = snd_seq_port_info_get_port(pinfo);
			port_info->client_name = strdup(snd_seq_client_info_get_name(cinfo));
			port_info->port_name   = strdup(snd_seq_port_info_get_name(pinfo));
		}
	}

	ports_total = port_index;
}

int alsa_get_ports_count() {
	return ports_total;
}

bool alsa_connect_port(int index) {
	struct port_info *port_info = alsa_get_port_info(index);
	if (port_info == NULL) return false;

	int err = snd_seq_connect_to(seq, 0, port_info->client, port_info->port);

	if (err < 0) {
		log_error("Cannot connect to port index %d %d:%d %s:%s - %s",
				index,
				port_info->client, port_info->port,
				port_info->client_name, port_info->port_name,
				snd_strerror(err));
		return false;
	}
	return true;
}

void create_source_port() {
	snd_seq_port_info_t *pinfo;
	int err;

	snd_seq_port_info_alloca(&pinfo);

	/* the first created port is 0 anyway, but let's make sure ... */
	snd_seq_port_info_set_port(pinfo, 0);
	snd_seq_port_info_set_port_specified(pinfo, 1);

	snd_seq_port_info_set_name(pinfo, "simusplayer");

	snd_seq_port_info_set_capability(pinfo, 0); /* sic */
	snd_seq_port_info_set_type(pinfo,
				   SND_SEQ_PORT_TYPE_MIDI_GENERIC |
				   SND_SEQ_PORT_TYPE_APPLICATION);

	err = snd_seq_create_port(seq, pinfo);
	check_snd("create port", err);
}

struct port_info *alsa_get_port_info(int index) {
	if (index < 0 || index >= ports_total) {
		log_error("Invalid port index %d", index);
		return NULL;
	}
	return &ports[index];
}

snd_seq_t *alsa_get_seq() {
	return seq;
}

void alsa_init() {
	init_seq();
	load_ports();
	create_source_port();
}

void alsa_done() {
	for(int i=0; i<ports_total; i++) {
		struct port_info *port_info = &ports[i];
		free(port_info->client_name);
		free(port_info->port_name);
	}
	snd_seq_close(seq);
}

snd_seq_event_t ev;

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

void alsa_seq_init(struct port_info *port_info) {
	snd_seq_ev_clear(&ev);
	snd_seq_ev_set_direct(&ev);
	snd_seq_ev_set_dest(&ev, port_info->client, port_info->port);
	ev.source.port = 0;
}

void alsa_set_event_note(uint8 type, uint8 channel, uint8 note, uint8 velocity) {
	ev.type = type;
	snd_seq_ev_set_fixed(&ev);

	ev.data.note.channel  = channel;
	ev.data.note.note     = note;
	ev.data.note.velocity = velocity;
}

void alsa_set_event_controller(uint8 channel, uint8 param, uint8 value) {
	ev.type = SND_SEQ_EVENT_CONTROLLER;
	snd_seq_ev_set_fixed(&ev);

	ev.data.control.channel = channel;
	ev.data.control.param   = param;
	ev.data.control.value   = value;
}

void alsa_set_event_change(uint8 type, uint8 channel, uint8 value) {
	ev.type = type;
	snd_seq_ev_set_fixed(&ev);

	ev.data.control.channel = channel;
	ev.data.control.value   = value;
}

void alsa_set_event_pitch_bend(uint8 channel, uint8 value) {
	ev.type = SND_SEQ_EVENT_PITCHBEND;
	snd_seq_ev_set_fixed(&ev);

	ev.data.control.channel = channel;
	ev.data.control.value = value;
}

void alsa_set_event_sysex(snd_seq_t *seq, int len, uint8 *sysex) {
	ev.type = SND_SEQ_EVENT_SYSEX;
	snd_seq_ev_set_variable(&ev, len, sysex);
	handle_big_sysex(seq, &ev);
}

void alsa_send_event(snd_seq_t *seq, uint32 tick) {
	ev.time.tick = tick;
	int err = snd_seq_event_output_direct(seq, &ev);
	check_snd("output event", err);
}

void alsa_seq_finish(snd_seq_t *seq, uint32 tick) {
	snd_seq_ev_set_fixed(&ev);
	ev.type = SND_SEQ_EVENT_STOP;
	ev.time.tick = tick;
	ev.dest.client = SND_SEQ_CLIENT_SYSTEM;
	ev.dest.port = SND_SEQ_PORT_SYSTEM_TIMER;
	int err = snd_seq_event_output(seq, &ev);
	check_snd("output event", err);
}

