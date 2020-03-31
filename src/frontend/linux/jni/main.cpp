#include "alsalib.h"
#include "xtvapps_simusplayer_NativeInterface.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_init
  (JNIEnv *env, jclass thiz) {
	alsa_init();
}


JNIEXPORT void JNICALL Java_xtvapps_simusplayer_NativeInterface_dump_1ports
	(JNIEnv *env, jclass thiz) {
	alsa_list_ports();
}

#ifdef __cplusplus
}
#endif


