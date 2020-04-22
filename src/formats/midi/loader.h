#ifndef __LOADER_H
#define __LOADER_H

#include "song.h"

song *load(const char *filename);
void unload(song *song);

#endif
