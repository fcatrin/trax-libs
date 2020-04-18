#include "common.h"
#include "fileutils.h"

static uint32 file_offset;

int read_byte(FILE *file) {
	++file_offset;
	return getc(file);
}

int32 read_32_le(FILE *file) {
	int32 value;
	value = read_byte(file);
	value |= read_byte(file) << 8;
	value |= read_byte(file) << 16;
	value |= read_byte(file) << 24;
	return !feof(file) ? value : EOF;
}

int32 read_int(FILE *file, uint32 bytes) {
	int32 c, value = 0;

	do {
		c = read_byte(file);
		if (c == EOF)
			return EOF;
		value = (value << 8) | c;
	} while (--bytes);
	return value;
}

int32 read_var(FILE *file) {
	int32 value, c;

	c = read_byte(file);
	value = c & 0x7f;
	if (c & 0x80) {
		c = read_byte(file);
		value = (value << 7) | (c & 0x7f);
		if (c & 0x80) {
			c = read_byte(file);
			value = (value << 7) | (c & 0x7f);
			if (c & 0x80) {
				c = read_byte(file);
				value = (value << 7) | c;
				if (c & 0x80)
					return EOF;
			}
		}
	}
	return !feof(file) ? value : EOF;
}

void skip(FILE *file, uint32 bytes) {
	while (bytes > 0) {
		read_byte(file);
		--bytes;
	}
}


