#ifndef _ALSALIB_H_
#define _ALSALIB_H_

struct port_info {
	int client;
	int port;
	char *client_name;
	char *port_name;
};

void alsa_init();
void alsa_done();

int alsa_get_ports_count();
struct port_info *alsa_get_port_info(int index);
bool alsa_connect_port(int index);

snd_seq_t *alsa_get_seq();

void alsa_seq_init(struct port_info *port_info);
void alsa_set_event_note(uint8 type, uint8 channel, uint8 note, uint8 velocity);
void alsa_set_event_controller(uint8 channel, uint8 param, uint8 value);
void alsa_set_event_change(uint8 type, uint8 channel, uint8 value);
void alsa_set_event_pitch_bend(uint8 channel, uint8 value);
void alsa_set_event_sysex(snd_seq_t *seq, int len, uint8 *sysex);
void alsa_send_event(snd_seq_t *seq, uint32 tick);
void alsa_seq_finish(snd_seq_t *seq, uint32 tick);

#endif
