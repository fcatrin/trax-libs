#include <stdlib.h>
#include <string.h>
#include "common.h"
#include "fileutils.h"

stream *open(const char *filename) {
	FILE *f = fopen(filename, "rb");
	if (!f) return NULL;

	stream *stream = malloc(sizeof(stream));
	stream->file = f;
	stream->offset = 0;
	stream->filename = strdup(filename);
	return stream;
}

void close(stream *stream) {
	fclose(stream->file);
	free(stream->filename);
	free(stream);
}

int read_byte(stream *stream) {
	stream->offset++;
	return getc(stream->file);
}

void unread_byte(stream *stream, int c) {
	stream->offset--;
	ungetc(c, stream->file);
}

int32 read_32_le(stream *stream) {
	int32 value;
	value = read_byte(stream);
	value |= read_byte(stream) << 8;
	value |= read_byte(stream) << 16;
	value |= read_byte(stream) << 24;
	return !feof(stream->file) ? value : EOF;
}

int32 read_int(stream *stream, uint32 bytes) {
	int32 c, value = 0;

	do {
		c = read_byte(stream);
		if (c == EOF)
			return EOF;
		value = (value << 8) | c;
	} while (--bytes);
	return value;
}

int32 read_var(stream *stream) {
	int32 value, c;

	uint8 i = 0;
	value = 0;
	do {
		c = read_byte(stream);
		value = (value << 7) | (c & 0x7f);
	} while ( (c & 0x80) && i++ < 4 && !feof(stream->file));

	return !feof(stream->file) ? value : EOF;
}

void skip(stream *stream, uint32 bytes) {
	while (bytes > 0) {
		read_byte(stream);
		--bytes;
	}
}


