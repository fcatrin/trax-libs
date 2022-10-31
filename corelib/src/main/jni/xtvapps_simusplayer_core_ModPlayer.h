/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class xtvapps_simusplayer_core_ModPlayer */

#ifndef _Included_xtvapps_simusplayer_core_ModPlayer
#define _Included_xtvapps_simusplayer_core_ModPlayer
#ifdef __cplusplus
extern "C" {
#endif
#undef xtvapps_simusplayer_core_ModPlayer_SLEEP_TIME
#define xtvapps_simusplayer_core_ModPlayer_SLEEP_TIME 10L
/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpInit
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpInit
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpRelease
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpRelease
  (JNIEnv *, jobject);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpFillBuffer
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpFillBuffer
  (JNIEnv *, jobject, jbyteArray, jint);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpGetModuleNameFromPath
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetModuleNameFromPath
  (JNIEnv *, jclass, jstring);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpSetVolume
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpSetVolume
  (JNIEnv *, jobject, jint);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpFillWave
 * Signature: ([II)V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpFillWave
  (JNIEnv *, jobject, jintArray, jint);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpGetModuleName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetModuleName
  (JNIEnv *, jobject);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpGetModuleFormat
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetModuleFormat
  (JNIEnv *, jobject);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpGetModuleInfo
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetModuleInfo
  (JNIEnv *, jobject);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpGetSampleName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetSampleName
  (JNIEnv *, jobject, jint);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpGetPlayingInfo
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpGetPlayingInfo
  (JNIEnv *, jobject);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpMuteChannel
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpMuteChannel
  (JNIEnv *, jobject, jint, jboolean);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpForward
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpForward
  (JNIEnv *, jobject);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpRewind
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpRewind
  (JNIEnv *, jobject);

/*
 * Class:     xtvapps_simusplayer_core_ModPlayer
 * Method:    xmpSeek
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_xtvapps_simusplayer_core_ModPlayer_xmpSeek
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif