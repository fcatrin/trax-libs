#ifdef __cplusplus
extern "C" {
#endif

#include "xmp.h"
#include "xtvapps_simusplayer_core_ModPlayer.h"

#define WAVE_SIZE 64

xmp_context ctx;
struct xmp_frame_info frameInfo;
struct xmp_module_info modInfo;

JNIEXPORT jboolean JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpInit
  (JNIEnv *env, jobject thiz, jstring sPath, jint freq) {
	int ret;

	char* path = (char *)env->GetStringUTFChars(sPath, 0);

	ctx = xmp_create_context();
	ret = xmp_load_module(ctx, path);

	env->ReleaseStringUTFChars(sPath, path);

	if (ret != 0) {
		return false;
	}

	xmp_start_player(ctx, freq, 0);
	xmp_set_player(ctx, XMP_PLAYER_MIX, 75);
	xmp_set_player(ctx, XMP_PLAYER_INTERP, XMP_INTERP_SPLINE);

	return true;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpRelease
  (JNIEnv *env, jobject thiz) {
	xmp_end_player(ctx);
	xmp_release_module(ctx);
	xmp_free_context(ctx);

	ctx = NULL;
}

JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpFillBuffer
  (JNIEnv *env, jobject thiz, jbyteArray jBuffer, jint loop) {
	jbyte* buffer = env->GetByteArrayElements(jBuffer, NULL);
	jsize length = env->GetArrayLength(jBuffer);

	int result = xmp_play_buffer(ctx, buffer, length, loop);

	env->ReleaseByteArrayElements(jBuffer, buffer, 0);

	return result;
}

JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetModuleName
  (JNIEnv *env, jclass thiz, jstring sPath) {
	jstring name = NULL;
	int ret = 0;

	char* path = (char *)env->GetStringUTFChars(sPath, 0);

	ctx = xmp_create_context();
	ret = xmp_load_module(ctx, path);
	if (ret == 0) {
		xmp_get_module_info(ctx, &modInfo);
		name = env->NewStringUTF(modInfo.mod->name);
	}

	xmp_release_module(ctx);
	xmp_free_context(ctx);

	ctx = NULL;

	env->ReleaseStringUTFChars(sPath, path);

	return name;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpSetVolume
  (JNIEnv *env, jobject thiz, jint vol) {
	if (ctx == NULL) return;

	xmp_set_player(ctx, XMP_PLAYER_VOLUME, vol);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpFillWave
  (JNIEnv *env, jobject thiz, jintArray wave, jint channel) {
	if (ctx == NULL) return;

	static struct xmp_frame_info frame_info;
	xmp_get_frame_info(ctx, &frame_info);

	if (channel >= frame_info.virt_used) return;

	struct xmp_frame_info::xmp_channel_info *channel_info = &frame_info.channel_info[channel];

	jsize resultSize = env->GetArrayLength(wave);
	jsize arraySize = resultSize < WAVE_SIZE ? resultSize : WAVE_SIZE;
	env->SetIntArrayRegion(wave, 0, arraySize, channel_info->wave);
}

#ifdef __cplusplus
}
#endif

