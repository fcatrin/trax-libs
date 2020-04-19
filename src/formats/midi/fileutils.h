#ifndef _FILEUTILS_H
#define _FILEUTILS_H

#include <stdio.h>

struct stream_t {
	FILE *file;
	uint32 offset;
	char *filename;
};

typedef struct stream_t stream;

stream *open(const char *filename);
void close(stream *stream);

int   read_byte(stream *stream);
int32 read_32_le(stream *stream);
int32 read_int(stream *stream, uint32 bytes);
int32 read_var(stream *stream);
void  skip(stream *stream, uint32 bytes);
void unread_byte(stream *stream, int c);

#endif
