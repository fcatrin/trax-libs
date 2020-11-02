/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class xtvapps_simusplayer_core_GMEPlayer */

#ifndef _Included_xtvapps_simusplayer_core_GMEPlayer
#define _Included_xtvapps_simusplayer_core_GMEPlayer
#ifdef __cplusplus
extern "C" {
#endif
#undef xtvapps_simusplayer_core_GMEPlayer_TIME_RWND_FWD
#define xtvapps_simusplayer_core_GMEPlayer_TIME_RWND_FWD 5000L
/*
 * Class:     xtvapps_simusplayer_core_GMEPlayer
 * Method:    gmeOpen
 * Signature: (Ljava/lang/String;IIFZ)I
 */
JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeOpen
  (JNIEnv *, jclass, jstring, jint, jint, jfloat, jboolean);

/*
 * Class:     xtvapps_simusplayer_core_GMEPlayer
 * Method:    gmeSetTempo
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeSetTempo
  (JNIEnv *, jclass, jint, jdouble);

/*
 * Class:     xtvapps_simusplayer_core_GMEPlayer
 * Method:    gmeFillBuffer
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeFillBuffer
  (JNIEnv *, jclass, jint, jbyteArray);

/*
 * Class:     xtvapps_simusplayer_core_GMEPlayer
 * Method:    gmeFillWave
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeFillWave
  (JNIEnv *, jclass, jint, jint, jintArray);

/*
 * Class:     xtvapps_simusplayer_core_GMEPlayer
 * Method:    gmeGetWavesCount
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeGetWavesCount
  (JNIEnv *, jclass, jint);

/*
 * Class:     xtvapps_simusplayer_core_GMEPlayer
 * Method:    gmeClose
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeClose
  (JNIEnv *, jclass, jint);

/*
 * Class:     xtvapps_simusplayer_core_GMEPlayer
 * Method:    gmeSeek
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeSeek
  (JNIEnv *, jclass, jint, jlong);

/*
 * Class:     xtvapps_simusplayer_core_GMEPlayer
 * Method:    gmeTimeElapsed
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeTimeElapsed
  (JNIEnv *, jclass, jint);

/*
 * Class:     xtvapps_simusplayer_core_GMEPlayer
 * Method:    gmeTimeTotal
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_GMEPlayer_gmeTimeTotal
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
