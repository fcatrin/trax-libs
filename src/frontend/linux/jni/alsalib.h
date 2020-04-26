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


#endif
