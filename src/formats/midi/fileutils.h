#ifndef _FILEUTILS_H
#define _FILEUTILS_H

#include <stdio.h>

int   read_byte(FILE *file);
int32 read_32_le(FILE *file);
int32 read_int(FILE *file, uint32 bytes);
int32 read_var(FILE *file);
void  skip(FILE *file, uint32 bytes);

#endif
