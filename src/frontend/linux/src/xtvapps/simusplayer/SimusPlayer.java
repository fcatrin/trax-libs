package xtvapps.simusplayer;

import java.io.File;
import java.io.IOException;

import fts.core.Widget;
import fts.linux.Window;
import xtvapps.simusplayer.core.CoreUtils;
import xtvapps.simusplayer.core.widgets.KeyboardView;
import xtvapps.simusplayer.midi.AlsaSequencer;
import xtvapps.simusplayer.midi.MidiPlayer;
import xtvapps.simusplayer.midi.MidiSong;
import xtvapps.simusplayer.midi.MidiTrack;
import xtvapps.simusplayer.midi.SimpleStream;

public class SimusPlayer extends Window {

	private int songHandle;

	private MidiPlayer midiPlayer;

	public SimusPlayer(String title, int width, int height) {
		super(title, width, height);
	}
	
	@Override
	public void onCreate() {
		Widget rootView = inflate("main");
		setContentView(rootView);
	}
	
	@Override
	public void onFrame() {
		int[] notes = midiPlayer.getNotes();
		KeyboardView keyboard = (KeyboardView)findWidget("keyboard");
		keyboard.setNotes(notes);
	}
	
	@Override
	public void onStop() {
		NativeInterface.midiStop();
	}

	@Override
	public void onStart() {
		NativeInterface.alsaInit();

		songHandle = NativeInterface.midiLoad("/opt/songs/midi/a_bridge.mid");
		System.out.println(songHandle);
		
		int nTracks = NativeInterface.midiGetTracksCount(songHandle);
		for(int i=0; i<nTracks; i++) {
			String trackName = NativeInterface.midiGetTrackName(songHandle, i);
			System.out.println(String.format("Track %d: %s", i, trackName));
		}

		System.out.println(" Port    Client name                      Port name");
		int ports = NativeInterface.alsaGetPortsCount();
		for(int port=0; port<ports; port++) {
			int    ids[]   = NativeInterface.alsaGetPortIds(port);
			String names[] = NativeInterface.alsaGetPortNames(port);
			
			System.out.println(String.format("%3d:%-3d  %-32.32s %s", ids[0], ids[1], names[0], names[1]));
			
		}

		System.out.flush();
		
		// byte[] songData = CoreUtils.loadBytes(new File("/home/fcatrin/tmp/canyon.mid"));
		try {
			byte[] songData = CoreUtils.loadBytes(new File("/home/fcatrin/tmp/a_bridge.mid"));
	
			final MidiSong song = MidiSong.load(new SimpleStream(songData));
			for(MidiTrack track : song.getTracks()) {
				System.out.println(track.getName());
			}
			
	
			Thread t = new Thread() {
				@Override
				public void run() {
					NativeInterface.alsaConnectPort(2);
					
					AlsaSequencer seq = new AlsaSequencer();
					midiPlayer = new MidiPlayer(seq);
					midiPlayer.play(song);
	
					NativeInterface.alsaDone();
				}
			};
			
			t.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		SimusPlayer player = new SimusPlayer("Simus Player", (7*8 + 1)*14, 64);
		player.run();
	}

}
