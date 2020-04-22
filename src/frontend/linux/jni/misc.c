#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <misc.h>

#define EXIT_FAILURE 1

void log_fatal(const char *msg, ...) {
	va_list ap;

	va_start(ap, msg);
	vfprintf(stderr, msg, ap);
	va_end(ap);
	fputc('\n', stderr);
	exit(EXIT_FAILURE);
}
