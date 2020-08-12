#ifdef __cplusplus
extern "C" {
#endif

#ifdef ANDROID
	#include <android/log.h>
	#define  LOG_TAG    "fluidplayer"
	#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
	#define  LOGD(...) printf(__VA_ARGS__)
#endif

#include "xtvapps_simusplayer_core_FluidPlayer.h"

JNIEXPORT jboolean JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidInit
  (JNIEnv *env, jobject thiz, jstring sPath, jint freq) {
	return true;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidRelease
  (JNIEnv *env, jobject thiz) {
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidFillBuffer
  (JNIEnv *env, jobject thiz, jbyteArray jBuffer, jint loop) {
	jbyte* buffer = env->GetByteArrayElements(jBuffer, NULL);
	jsize length = env->GetArrayLength(jBuffer);

	int result = xmp_play_buffer(ctx, buffer, length, loop);

	env->ReleaseByteArrayElements(jBuffer, buffer, 0);

	return result;
}


#ifdef __cplusplus
}
#endif


