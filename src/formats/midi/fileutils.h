#ifndef _FILEUTILS_H
#define _FILEUTILS_H

struct stream {
	FILE *file;
	uint32 offset;
	char *filename;
};

struct stream *stream_open(const char *filename);
void    stream_close(struct stream *stream);

int   read_byte(struct stream *stream);
int32 read_32_le(struct stream *stream);
int32 read_int(struct stream *stream, uint32 bytes);
int32 read_var(struct stream *stream);
char *read_string(struct stream *stream, int bytes);
void  skip(struct stream *stream, uint32 bytes);
void unread_byte(struct stream *stream, int c);

#endif
