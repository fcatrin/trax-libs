#ifndef __LOADER_H
#define __LOADER_H

#include <formats/midi/song.h>

song *song_load(const char *filename);
void song_unload(song *song);

#endif
