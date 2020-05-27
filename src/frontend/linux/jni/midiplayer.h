#ifndef __MIDI_PLAYER_H
#define __MIDI_PLAYER_H

void midi_play(snd_seq_t *seq, struct song *song, struct port_info *port_info);
void midi_play_stop();

uint8 *midi_get_notes(struct song *song, int track);

#endif
