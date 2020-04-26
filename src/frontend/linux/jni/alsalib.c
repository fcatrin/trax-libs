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

			/*
			printf("%3d:%-3d  %-32.32s %s\n",
			       snd_seq_port_info_get_client(pinfo),
			       snd_seq_port_info_get_port(pinfo),
			       snd_seq_client_info_get_name(cinfo),
			       snd_seq_port_info_get_name(pinfo));
			*/
		}
	}

	ports_total = port_index;
}

int alsa_get_ports_count() {
	return ports_total;
}

struct port_info *alsa_get_port_info(int index) {
	if (index < 0 || index >= ports_total) return NULL;
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

