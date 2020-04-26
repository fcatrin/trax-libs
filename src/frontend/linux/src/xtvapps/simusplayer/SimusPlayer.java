package xtvapps.simusplayer;

public class SimusPlayer {

	public static void main(String[] args) {
		NativeInterface.alsaInit();

		int handle = NativeInterface.midiLoad("/home/fcatrin/test.mid");
		System.out.println(handle);
		
		int nTracks = NativeInterface.midiGetTracksCount(handle);
		for(int i=0; i<nTracks; i++) {
			String trackName = NativeInterface.midiGetTrackName(handle, i);
			System.out.println(String.format("Track %d: %s", i, trackName));
		}

		System.out.println(" Port    Client name                      Port name");
		int ports = NativeInterface.alsaGetPortsCount();
		for(int port=0; port<ports; port++) {
			int    ids[]   = NativeInterface.alsaGetPortIds(port);
			String names[] = NativeInterface.alsaGetPortNames(port);
			
			System.out.println(String.format("%3d:%-3d  %-32.32s %s", ids[0], ids[1], names[0], names[1]));
			
		}
		
		NativeInterface.alsaDone();
	}

}
