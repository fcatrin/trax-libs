package xtvapps.simusplayer;

public class SimusPlayer {

	public static void main(String[] args) {
		NativeInterface.alsaInit();
		NativeInterface.alsaDumpPorts();

		int handle = NativeInterface.midiLoad("/home/fcatrin/test.mid");
		System.out.println(handle);
		
		int nTracks = NativeInterface.midiGetTracksCount(handle);
		for(int i=0; i<nTracks; i++) {
			String trackName = NativeInterface.midiGetTrackName(handle, i);
			System.out.println(String.format("Track %d: %s", i, trackName));
		}
	}

}
