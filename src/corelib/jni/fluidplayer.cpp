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
#include "xtvapps_simusplayer_core_FluidPlayer.h"

static fluid_settings_t*     settings;
static fluid_synth_t*        synth;
static fluid_audio_driver_t* adriver;

JNIEXPORT jboolean JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidInit
  (JNIEnv *env, jobject thiz, jint sample_rate) {
	char setting_sample_rate[] = "synth.sample-rate";

    settings = new_fluid_settings();

    fluid_settings_setnum(settings, setting_sample_rate, sample_rate);

    synth    = new_fluid_synth(settings);
    adriver  = new_fluid_audio_driver(settings, synth);
	return true;
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidRelease
  (JNIEnv *env, jobject thiz) {
	if (settings == NULL) return;

	delete_fluid_audio_driver(adriver);
	delete_fluid_synth(synth);
	delete_fluid_settings(settings);

	adriver  = NULL;
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


JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fluidNoteOn
  (JNIEnv *env, jobject thiz, jint channel, jint note, jint velocity) {
	fluid_synth_noteon(synth, channel, note, velocity);
}

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_FluidPlayer_fuildNoteOff
  (JNIEnv *env, jobject thiz, jint channel, jint note) {
	fluid_synth_noteoff(synth, channel, note);
}


#ifdef __cplusplus
}
#endif


