#include <alsa/asoundlib.h>
#include <formats/midi/common.h>
#include "alsalib.h"

static snd_seq_t *seq;
static int client;

#define MAX_PORTS 128
struct port_info ports[MAX_PORTS];
int ports_total = 0;

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

struct port_info *alsa_get_port_info(int index) {
	if (index < 0 || index >= ports_total) {
		log_error("Invalid port index %d", index);
		return NULL;
	}
	return &ports[index];
}

void alsa_init() {
	init_seq();
	load_ports();
}

void alsa_done() {
	for(int i=0; i<ports_total; i++) {
		struct port_info *port_info = &ports[i];
		free(port_info->client_name);
		free(port_info->port_name);
	}
	snd_seq_close(seq);
}

