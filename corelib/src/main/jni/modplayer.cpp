#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifdef ANDROID
	#include <android/log.h>
	#define  LOG_TAG    "modplayer"
	#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
	#define  LOGD(...) printf(__VA_ARGS__)
#endif

#include "xmp.h"
#include "xtvapps_simusplayer_core_ModPlayer.h"

#define WAVE_SIZE 128

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

	LOGD("loading module %s", path);
	if (ret != 0) {
		LOGD("cannot load module %s err:%d", path, ret);
		return false;
	}

	xmp_get_module_info(ctx, &modInfo);

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

JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetModuleNameFromPath
  (JNIEnv *env, jclass thiz, jstring sPath) {
	static struct xmp_module_info modInfo;

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

	struct xmp_frame_info::xmp_channel_info *channel_info = &frame_info.channel_info[channel];

	jint* waveBuffer = env->GetIntArrayElements(wave, NULL);

	jsize resultSize = env->GetArrayLength(wave);
	if (channel_info->wave != NULL) { // there is no reliable method to know if the channel is being mixed or not
		int checksum = 0;
		for(int i=0; i< WAVE_SIZE / 4; i++) checksum = checksum ^ channel_info->wave[i];
		if (checksum == channel_info->wave_checksum) {
			channel_info->wave = NULL;
		} else {
			channel_info->wave_checksum = checksum;
		}
	}
	if (channel_info->wave == NULL) {
		memset(waveBuffer, 0, resultSize * sizeof(jint));
	} else {
		jsize arraySize = resultSize < WAVE_SIZE ? resultSize : WAVE_SIZE;
		int pos = channel_info->wave_pos;
		for(int i=0; i<arraySize; i++) {
		    waveBuffer[i] = channel_info->wave[pos++];
		    pos = pos & (WAVE_SIZE-1);
		}
	}

	env->ReleaseIntArrayElements(wave, waveBuffer, 0);
}

JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetModuleName
  (JNIEnv *env, jobject thiz) {
	return env->NewStringUTF(modInfo.mod->name);
}

JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetModuleFormat
  (JNIEnv *env, jobject thiz) {
	return env->NewStringUTF(modInfo.mod->type);
}

JNIEXPORT jintArray JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetModuleInfo
  (JNIEnv *env, jobject thiz) {

	xmp_module *mod = modInfo.mod;
	int modInfoData[] = {
			mod->chn,
			mod->len,
			mod->ins,
			mod->spd,
			mod->bpm
	};

	jintArray result = env->NewIntArray(5);
	env->SetIntArrayRegion(result, 0, 5, modInfoData);
	return result;
}

JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetSampleName
  (JNIEnv *env, jobject thiz, jint sampleIndex) {
	xmp_get_module_info(ctx, &modInfo);

	if (sampleIndex < 0 || sampleIndex >= modInfo.mod->smp) return NULL;

	char *sampleName = modInfo.mod->xxi[sampleIndex].name;
	return env->NewStringUTF(sampleName);
}

JNIEXPORT jintArray JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetPlayingInfo
  (JNIEnv *env, jobject thiz) {
	static struct xmp_frame_info frame_info;
	xmp_get_frame_info(ctx, &frame_info);

	int playingInfoData[] = {
			frame_info.pos,
			frame_info.speed,
			frame_info.bpm,
			frame_info.time,
			frame_info.total_time,
			frame_info.virt_channels,
			frame_info.virt_used
	};

	jintArray result = env->NewIntArray(7);
	env->SetIntArrayRegion(result, 0, 7, playingInfoData);
	return result;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpMuteChannel
  (JNIEnv *env, jobject thiz, jint channel, jboolean mute) {

	if (channel < 0 || channel >= modInfo.mod->chn) return;

	xmp_channel_mute(ctx, channel, mute);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpForward
  (JNIEnv *env, jobject thiz){
	if (ctx) xmp_next_position(ctx);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpRewind
  (JNIEnv *env, jobject thiz) {
	if (ctx) xmp_prev_position(ctx);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpSeek
  (JNIEnv *env, jobject thiz, jint pattern) {
	if (ctx) xmp_set_position(ctx, pattern);
}

#ifdef __cplusplus
}
#endif


