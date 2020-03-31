#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>

#define EXIT_FAILURE 1

void fatal(const char *msg, ...) {
	va_list ap;

	va_start(ap, msg);
	vfprintf(stderr, msg, ap);
	va_end(ap);
	fputc('\n', stderr);
	exit(EXIT_FAILURE);
}
