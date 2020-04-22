package xtvapps.simusplayer;

public class NativeInterface {
	static {
		System.loadLibrary("simusplayer");
	}
	
	public static native void alsaInit();
	public static native void alsaDumpPorts();
	
	public static native int midiLoad(String filename);
	public static native int midiUnload(int handle);
	public static native int midiGetTracksCount(int handle);
	public static native String modoGetTrackName(int handle, int track);
}
