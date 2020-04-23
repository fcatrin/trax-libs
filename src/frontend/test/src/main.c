#include <stdio.h>
#include <formats/midi/loader.h>

int main(int argc, char *argv[]) {

	song *song = song_load("/home/fcatrin/test.mid");
	if (song == NULL) {
		log_error("load failed");
	} else {
		log_error("load ok");
	}
}
