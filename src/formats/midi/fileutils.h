#ifndef _FILEUTILS_H
#define _FILEUTILS_H

typedef struct stream {
	FILE *file;
	uint32 offset;
	char *filename;
} stream;

stream *stream_open(const char *filename);
void    stream_close(stream *stream);

int   read_byte(stream *stream);
int32 read_32_le(stream *stream);
int32 read_int(stream *stream, uint32 bytes);
int32 read_var(stream *stream);
char *read_string(stream *stream, int bytes);
void  skip(stream *stream, uint32 bytes);
void unread_byte(stream *stream, int c);

#endif
