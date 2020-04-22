package xtvapps.simusplayer;

public class NativeInterface {
	static {
		System.loadLibrary("simusplayer");
		System.loadLibrary("simusplayer-core");
	}
	
	public static native void init();
	public static native void dump_ports();
}
