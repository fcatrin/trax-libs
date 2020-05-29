package xtvapps.simusplayer.core;

public class ModPlayer extends Thread {
	
	static {
		System.loadLibrary("simusplayer-mod");
	}
	
	private native boolean xmpInit(String path, int freq);
	private native void    xmpRelease();
	private native int     xmpFillBuffer(byte[] buffer, int loop);
	private native static String xmpGetModuleName(String path);
	private native void    xmpSetVolume(int volume);
}
