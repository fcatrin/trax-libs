#ifdef __cplusplus
extern "C" {
#endif

#include "formats/midi/loader.h"

#include "alsalib.h"
#include "xtvapps_simusplayer_NativeInterface.h"

#define MAX_OPENED_SONGS 10
song *songs[MAX_OPENED_SONGS];

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaInit
  (JNIEnv *env, jclass thiz) {
	alsa_init();
}


JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaDumpPorts
	(JNIEnv *env, jclass thiz) {
	alsa_list_ports();
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_NativeInterface_midiLoad
  (JNIEnv *env, jclass thiz, jstring sFilename) {

	// find empty slot for song
	int index = 0;
	while (index < MAX_OPENED_SONGS && songs[index]) index++;
	if (index == MAX_OPENED_SONGS) return -1;

	const char* filename = env->GetStringUTFChars(sFilename, 0);

	song *song = song_load(filename);
	if (song == NULL) {
		index = -1;
	} else {
		songs[index] = song;
	}

	env->ReleaseStringUTFChars(sFilename, filename);
	return index;
}

static song *get_song_by_handle(int handle) {
	if (handle < 0 || handle >= MAX_OPENED_SONGS || songs[handle] == NULL) {
		log_error("invalid song handle %d", handle);
		return NULL;
	}
	return songs[handle];
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_midiUnload
  (JNIEnv *env, jclass thiz, jint handle) {

	struct song *song = get_song_by_handle(handle);
	if (song != NULL) {
		song_unload(song);
		songs[handle] = NULL;
	}
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_NativeInterface_midiGetTracksCount
  (JNIEnv *env, jclass thiz, jint handle) {
	struct song *song = get_song_by_handle(handle);
	if (song == NULL) return 0;

	return song->num_tracks;
}

JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_NativeInterface_midiGetTrackName
  (JNIEnv *env, jclass clazz, jint handle, jint track) {
	struct song *song = get_song_by_handle(handle);
	if (song == NULL) return NULL;

	if (track < 0 || track >= song->num_tracks) {
		log_error("invalid number of tracks %d for song %d with %d tracks",
				track, handle, song->num_tracks);
		return NULL;
	}

	return env->NewStringUTF(song->tracks[track].name);
}

#ifdef __cplusplus
}
#endif


