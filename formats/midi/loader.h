#ifndef __LOADER_H
#define __LOADER_H

#include <formats/midi/song.h>

struct song *song_load(const char *filename);
void song_unload(struct song *song);

#endif
