package xtvapps.simusplayer.core;

public class ModPlayer {
	
	static {
		System.loadLibrary("simusplayer-mod");
	}
	
	public native boolean xmpInit(String path, int freq);
	public native void    xmpRelease();
	public native int     xmpFillBuffer(byte[] buffer, int loop);
	public native static String xmpGetModuleName(String path);
	public native void    xmpSetVolume(int volume);
	
	public native void    xmpFillWave(int[] wave, int channel);
}
