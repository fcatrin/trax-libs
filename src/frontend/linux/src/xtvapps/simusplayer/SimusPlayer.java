package xtvapps.simusplayer;

public class SimusPlayer {

	public static void main(String[] args) {
		NativeInterface.init();
		NativeInterface.dump_ports();
	}

}
