#ifdef __cplusplus
extern "C" {
#endif

#include <alsa/asoundlib.h>
#include <formats/midi/loader.h>

#include "xtvapps_simusplayer_NativeInterface.h"

#include "alsalib.h"
#include "midiplayer.h"

#define MAX_OPENED_SONGS 10
song *songs[MAX_OPENED_SONGS];

int current_port = -1;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaInit
  (JNIEnv *env, jclass thiz) {
	alsa_init();
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaDone
  (JNIEnv *env, jclass thiz) {
	alsa_done();
}


JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaGetPortsCount
  (JNIEnv *env, jclass thiz) {
	return alsa_get_ports_count();
}

JNIEXPORT jintArray JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaGetPortIds
  (JNIEnv *env, jclass thiz, jint port_index) {

	struct port_info *port_info = alsa_get_port_info(port_index);
	if (port_info == NULL) return NULL;

	int ids[2] = {
			port_info->client,
			port_info->port
	};

	jintArray result = env->NewIntArray(2);
	env->SetIntArrayRegion(result, 0, 2, ids);

	return result;
}

JNIEXPORT jobjectArray JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaGetPortNames
  (JNIEnv *env, jclass thiz, jint port_index) {
	struct port_info *port_info = alsa_get_port_info(port_index);
	if (port_info == NULL) return NULL;

	const char *names[2] = {
			port_info->client_name,
			port_info->port_name
	};

	jobjectArray result = env->NewObjectArray(2,
			env->FindClass("java/lang/String"),
	        env->NewStringUTF(""));

	for(int i=0;i<2;i++) {
		env->SetObjectArrayElement(result, i, env->NewStringUTF(names[i]));
	}

	return result;
}

JNIEXPORT jboolean JNICALL Java_xtvapps_simusplayer_NativeInterface_alsaConnectPort
  (JNIEnv *env, jclass thiz, jint port) {
	current_port = -1;
	if (alsa_connect_port(port)) {
		current_port = port;
	}
	return current_port >= 0;
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


JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_midiPlay
  (JNIEnv *env, jclass thiz, jint handle) {
	struct song *song = get_song_by_handle(handle);
	if (song == NULL) return;

	snd_seq_t * seq = alsa_get_seq();

	struct port_info *port_info = alsa_get_port_info(current_port);
	if (port_info == NULL) return;

	midi_play(seq, song, port_info);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_midiStop
	(JNIEnv *env, jclass thiz) {
	midi_play_stop();
}

JNIEXPORT jintArray JNICALL Java_xtvapps_simusplayer_NativeInterface_midiGetNotes
  (JNIEnv *env, jclass thiz, jint handle, jint track) {

	struct song *song = get_song_by_handle(handle);
	if (song == NULL) return NULL;

	uint8 *notes = midi_get_notes(song, track);
	if (notes == NULL) return NULL;

	static int notesResult[LAST_NOTE];
	for(int i=0;i<LAST_NOTE;i++) {
		notesResult[i] = notes[i];
	}

	jintArray result = env->NewIntArray(LAST_NOTE);
	env->SetIntArrayRegion(result, 0, LAST_NOTE, notesResult);

	return result;
}


JNIEXPORT void JNICALL Java_xtvapps_simusplayer_midi_AlsaSequencer_alsaReset
  (JNIEnv *env, jclass thiz) {
	struct port_info *port_info = alsa_get_port_info(current_port);
	alsa_seq_init(port_info);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_midi_AlsaSequencer_alsaSendEventNote
  (JNIEnv *env, jclass thiz, jlong tick, jint type, jint channel, jint note, jint velocity) {
	snd_seq_t * seq = alsa_get_seq();

	alsa_set_event_note(type, channel, note, velocity);
	alsa_send_event(seq, tick);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_midi_AlsaSequencer_alsaSendEventController
  (JNIEnv *env, jclass thiz, jlong tick, jint channel, jint param, jint value) {
	snd_seq_t * seq = alsa_get_seq();

	alsa_set_event_controller(channel, param, value);
	alsa_send_event(seq, tick);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_midi_AlsaSequencer_alsaSendEventChange
  (JNIEnv *env, jclass thiz, jlong tick, jint type, jint channel, jint value) {
	snd_seq_t * seq = alsa_get_seq();

	alsa_set_event_change(type, channel, value);
	alsa_send_event(seq, tick);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_midi_AlsaSequencer_alsaSendEventPitchBend
  (JNIEnv *env, jclass thiz, jlong tick, jint channel, jint value) {
	snd_seq_t * seq = alsa_get_seq();

	alsa_set_event_pitch_bend(channel, value);
	alsa_send_event(seq, tick);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_midi_AlsaSequencer_alsaSendEventSysex
  (JNIEnv *env, jclass thiz, jlong tick, jintArray jsysex) {
	snd_seq_t * seq = alsa_get_seq();

	jsize in_len = env->GetArrayLength(jsysex);
	jint *in_sysex = env->GetIntArrayElements(jsysex, 0);

	uint8 sysex[in_len];
	for(int i=0; i<in_len; i++) {
		sysex[i] = in_sysex[i];
	}

	alsa_set_event_sysex(seq, in_len, sysex);
	alsa_send_event(seq, tick);

	env->ReleaseIntArrayElements(jsysex, in_sysex, 0);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_midi_AlsaSequencer_alsaFinish
  (JNIEnv *env, jclass thiz, jlong tick) {
	snd_seq_t * seq = alsa_get_seq();
	alsa_send_event(seq, tick);

	alsa_seq_finish(seq, tick);
}


#ifdef __cplusplus
}
#endif


