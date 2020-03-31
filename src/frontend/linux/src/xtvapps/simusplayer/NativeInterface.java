package xtvapps.simusplayer;

public class NativeInterface {
	static {
		System.loadLibrary("simusplayer");
	}
	
	public static native void init();
	public static native void dump_ports();
}
