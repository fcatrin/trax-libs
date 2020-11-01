#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifdef ANDROID
	#include <android/log.h>
	#define  LOG_TAG    "gmeplayer"
	#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
	#define  LOGD(...) printf(__VA_ARGS__)
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

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeOpen
  (JNIEnv *env, jclass clazz, jstring sPath, jint track, jint sample_rate, jfloat depth, jboolean accurate) {
	int handle = find_free_handle();
	if (handle < 0) {
		LOGD("No more free handles");
		return -1;
	}

	char* path = (char *)env->GetStringUTFChars(sPath, 0);

	const char *result = gme_open_file(path, &handles[handle], sample_rate);

	if (result) {
		LOGD("Cannot open %s : %s", path, result);
	} else {
		Music_Emu* emu = handles[handle];
		gme_info_t* info = infos[handle];

		gme_track_info(emu, &info, track);

		gme_set_stereo_depth(emu, depth);
		gme_enable_accuracy(emu, accurate ? 1 : 0);
	}

	env->ReleaseStringUTFChars(sPath, path);

	return result ? -1 : handle;
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeFillBuffer
  (JNIEnv *env, jclass clazz, jint handle, jbyteArray jBuffer) {
	Music_Emu* emu = get_emu(handle);
	if (!emu) return -1;

	jbyte* buffer = env->GetByteArrayElements(jBuffer, NULL);
	jsize length = env->GetArrayLength(jBuffer);

	gme_play(emu, length / 2, (short int *)buffer);

	env->ReleaseByteArrayElements(jBuffer, buffer, 0);

	return gme_track_ended(emu) ? -1 : length;
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

	gme_seek(emu, position);
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

	return infos[handle]->length;
}


#ifdef __cplusplus
}
#endif


