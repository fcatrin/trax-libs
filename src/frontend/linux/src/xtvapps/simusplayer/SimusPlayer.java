package xtvapps.simusplayer;

public class SimusPlayer {

	public static void main(String[] args) {
		NativeInterface.alsaInit();
		NativeInterface.alsaDumpPorts();
		
		int handle = NativeInterface.midiLoad("/home/fcatrin/test.mid");
		System.out.println(handle);
	}

}
