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
	public void onWindowCreate() {
		Widget rootView = inflate("main");
		setContentView(rootView);
	}
	
	@Override
	public void onFrame() {
		int[][] notes = midiPlayer.getNotes();
		KeyboardView keyboard = (KeyboardView)findWidget("keyboard");
		keyboard.setNotes(notes[0]);
	}
	
	@Override
	public void onWindowStop() {
		NativeInterface.midiStop();
	}

	@Override
	public void onWindowStart() {
		NativeInterface.alsaInit();
		dumpAlsaPorts();
		
		try {
			byte[] songData = CoreUtils.loadBytes(new File("/home/fcatrin/tmp/a_bridge.mid"));
	
			final MidiSong song = MidiSong.load(new SimpleStream(songData));
			for(MidiTrack track : song.getTracks()) {
				System.out.println(track.getName());
			}
	
			playSong(song);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void playSong(final MidiSong song) {
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
	}
	
	private void dumpAlsaPorts() {
		System.out.println(" Port    Client name                      Port name");
		int ports = NativeInterface.alsaGetPortsCount();
		for(int port=0; port<ports; port++) {
			int    ids[]   = NativeInterface.alsaGetPortIds(port);
			String names[] = NativeInterface.alsaGetPortNames(port);
			
			System.out.println(String.format("%3d:%-3d  %-32.32s %s", ids[0], ids[1], names[0], names[1]));
		}
		System.out.flush();
	}
	
	public static void main(String[] args) throws IOException {
		SimusPlayer player = new SimusPlayer("Simus Player", (7*8 + 1)*14, 64);
		player.run();
	}

}
