#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifdef ANDROID
	#include <android/log.h>
	#define  LOG_TAG    "gmeplayer"
	#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
	#define  LOGD(...) printf("[gmeplayer] "); printf(__VA_ARGS__); fflush(stdout);
#endif

#include "xmp.h"
#include "xtvapps_simusplayer_core_GMEPlayer.h"

#include "gme/gme.h"

#define WAVE_SIZE 128
#define MAX_HANDLES 10

Music_Emu* handles[MAX_HANDLES];
gme_info_t* infos[MAX_HANDLES];

static int find_free_handle() {
	for(int i=0; i<MAX_HANDLES; i++) {
		if (!handles[i]) return i;
	}
	return -1;
}

static Music_Emu* get_emu(int handle) {
	if (handle < 0 || handle >= MAX_HANDLES) return NULL;
	return handles[handle];
}

static void handle_error(const char *str) {
	// if (str != NULL) LOGD("Error %s\n", str);
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeOpen
  (JNIEnv *env, jclass clazz, jstring sPath, jint track, jint sample_rate, jfloat depth, jboolean accurate) {
	int handle = find_free_handle();
	if (handle < 0) {
		LOGD("No more free handles\n");
		return -1;
	}

	char* path = (char *)env->GetStringUTFChars(sPath, 0);

	const char *result = gme_open_file(path, &handles[handle], sample_rate);

	if (result) {
		LOGD("Cannot open %s : %s\n", path, result);
	} else {
		LOGD("Opened %s\n", path);
		Music_Emu* emu = handles[handle];

		handle_error(gme_track_info(emu, &infos[handle], track));
		gme_info_t* info = infos[handle];

		gme_set_stereo_depth(emu, depth);
		gme_enable_accuracy(emu, accurate ? 1 : 0);

		handle_error(gme_start_track(emu, track));
		if (info->length <= 0) {
			gme_set_fade(emu, info->play_length - 3000, 3000);
		}

		gme_set_tempo(emu, 1.0f);

		LOGD("Track %i started for %s\n", track, path);
	}

	env->ReleaseStringUTFChars(sPath, path);

	return result ? -1 : handle;
}

JNIEXPORT jobjectArray JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeGetMetadata
  (JNIEnv *env, jclass clazz, jstring sPath) {

	Music_Emu *handle;
	gme_info_t *info;

	int open_sample_rate = 44100;
	int open_track = 0;

	jobjectArray result = NULL;

	const char* path = (char *)env->GetStringUTFChars(sPath, 0);

	const char *open_error = gme_open_file(path, &handle, open_sample_rate);

	if (open_error) {
		LOGD("Cannot open %s : %s\n", path, open_error);
	} else {
		LOGD("Opened %s\n", path);

		static char *metadata[5];

		handle_error(gme_track_info(handle, &info, open_track));

		metadata[0] = strdup(info->system);
		metadata[1] = strdup(info->game);
		metadata[2] = strdup(info->song);
		metadata[3] = strdup(info->author);
		metadata[4] = strdup(info->copyright);

	    jclass jStringClasss = env->FindClass("java/lang/String");
		result = env->NewObjectArray(5, jStringClasss, NULL);
		for(int i=0; i<5; i++) {
			jobject value = env->NewStringUTF(metadata[i]);
			env->SetObjectArrayElement(result, i, value);
		}
	}

	env->ReleaseStringUTFChars(sPath, path);
	return result;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeSetTempo
  (JNIEnv *env, jclass clazz, jint handle, jdouble tempo){
	Music_Emu* emu = get_emu(handle);
	if (!emu) return;

	gme_set_tempo(emu, tempo);
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeFillBuffer
  (JNIEnv *env, jclass clazz, jint handle, jbyteArray jBuffer) {
	Music_Emu* emu = get_emu(handle);
	if (!emu) return -1;

	jbyte* buffer = env->GetByteArrayElements(jBuffer, NULL);
	jsize length = env->GetArrayLength(jBuffer);

	handle_error(gme_play(emu, length / 2, (short int *)buffer));

	env->ReleaseByteArrayElements(jBuffer, buffer, 0);

	jboolean ended = gme_track_ended(emu) || gme_tell(emu) >= infos[handle]->play_length;

	return ended ? -1 : length;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeFillWave
  (JNIEnv *env, jclass clazz, jint handle, jint channel, jintArray jSamples) {
	Music_Emu* emu = get_emu(handle);
	if (!emu) return;

	jint* samples = env->GetIntArrayElements(jSamples, NULL);
	jsize length = env->GetArrayLength(jSamples);

	gme_get_wave(channel, samples, length);

	env->ReleaseIntArrayElements(jSamples, samples, 0);
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeGetWavesCount
  (JNIEnv *env, jclass clazz, jint handle) {
	Music_Emu* emu = get_emu(handle);
	if (!emu) return 0;

	return gme_get_waves_count();
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeClose
  (JNIEnv *env, jclass clazz, jint handle){
	Music_Emu* emu = get_emu(handle);
	if (!emu) return;

	gme_delete(emu);
	gme_free_info(infos[handle]);

	handles[handle] = NULL;
	infos[handle] = NULL;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeSeek
  (JNIEnv *env, jclass clazz, jint handle, jlong position){
	Music_Emu* emu = get_emu(handle);
	if (!emu) return;

	handle_error(gme_seek(emu, position));
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeTimeElapsed
  (JNIEnv *env, jclass clazz, jint handle){
	Music_Emu* emu = get_emu(handle);
	if (!emu) return 0;

	return gme_tell(emu);
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeTimeTotal
  (JNIEnv *env, jclass clazz, jint handle){
	Music_Emu* emu = get_emu(handle);
	if (!emu) return 0;

	return infos[handle]->play_length;
}


#ifdef __cplusplus
}
#endif


