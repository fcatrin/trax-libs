Rebuild header for NativeInterface:

cd jni
javah -classpath ../bin xtvapps.simusplayer.NativeInterface
javah -classpath ../bin:../../../corelib/bin xtvapps.simusplayer.midi.AlsaSequencer
