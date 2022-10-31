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

#include <fluidsynth.h>
#include "common.h"
#include "xtvapps_simusplayer_core_FluidPlayer.h"

static fluid_settings_t*     settings;
static fluid_synth_t*        synth;

JNIEXPORT jboolean JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidInit
  (JNIEnv *env, jobject thiz, jint sample_rate) {

	settings = new_fluid_settings();

    fluid_settings_setnum(settings, "synth.sample-rate", sample_rate);
    fluid_settings_setnum(settings, "synth.gain", 0.6);

    synth    = new_fluid_synth(settings);
	return true;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidRelease
  (JNIEnv *env, jobject thiz) {
	if (settings == NULL) return;

	delete_fluid_synth(synth);
	delete_fluid_settings(settings);

	synth    = NULL;
	settings = NULL;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidLoadSoundFontFile
  (JNIEnv *env, jobject thiz, jstring sPath) {
	if (synth == NULL) return;

	char* path = (char *)env->GetStringUTFChars(sPath, 0);

	if (fluid_is_soundfont(path)) fluid_synth_sfload(synth, path, 1);

	env->ReleaseStringUTFChars(sPath, path);
}


JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidFillBuffer
  (JNIEnv *env, jobject thiz, jbyteArray jBuffer) {
	jbyte* buffer = env->GetByteArrayElements(jBuffer, NULL);
	jsize length = env->GetArrayLength(jBuffer);

	fluid_synth_write_s16(synth, length/4,  // length in bytes, 1 stereo 16 bit frame = 4 bytes
						    buffer, 0, 2,
						    buffer, 1, 2);

	env->ReleaseByteArrayElements(jBuffer, buffer, 0);

	return 0;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidSendEventNote
(JNIEnv *env, jclass clazz, jint type, jint channel, jint note, jint velocity) {
	switch(type) {
	case NOTEON:
		fluid_synth_noteon(synth, channel, note, velocity);
		break;
	case NOTEOFF:
		fluid_synth_noteoff(synth, channel, note);
		break;
	}
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidSendEventController
  (JNIEnv *env, jclass clazz, jint channel, jint param, jint value) {
	fluid_synth_cc(synth, channel, param, value);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidSendEventChange
  (JNIEnv *env, jclass clazz, jint type, jint channel, jint value) {
	switch(type) {
	case PGMCHANGE:
		fluid_synth_program_change(synth, channel, value);
		break;
	case CHANPRESS:
		fluid_synth_channel_pressure(synth, channel, value);
		break;
	}
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidSendEventPitchBend
  (JNIEnv *env, jclass clazz, jint channel, jint value) {
	fluid_synth_pitch_bend(synth, channel, value + 8192);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidSendEventSysex
  (JNIEnv *env, jclass clazz, jintArray jsysex) {

}

int main(int argc, char **argv) {}

#ifdef __cplusplus
}
#endif


