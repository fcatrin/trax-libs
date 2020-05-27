package xtvapps.simusplayer;

public class NativeInterface {
	static {
		System.loadLibrary("simusplayer");
	}
	
	public static native void alsaInit();
	public static native void alsaDone();
	public static native int  alsaGetPortsCount();
	
	public static native int[]    alsaGetPortIds(int port);
	public static native String[] alsaGetPortNames(int port);
	
	public static native boolean alsaConnectPort(int port);
	
	public static native int    midiLoad(String filename);
	public static native void   midiUnload(int handle);
	public static native int    midiGetTracksCount(int handle);
	public static native String midiGetTrackName(int handle, int track);
	public static native int[]  midiGetNotes(int handle, int track);
	
	public static native void midiPlay(int handle);
	
}
