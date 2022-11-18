package xtvapps.trax.core;

import java.io.File;
import java.io.IOException;

import xtvapps.trax.midi.MidiPlayer;
import xtvapps.trax.midi.MidiSong;
import xtvapps.trax.midi.MidiTrack;
import xtvapps.trax.midi.SimpleStream;

public class FluidMidiThread extends Thread {
	private MidiPlayer midiPlayer;
	private MidiSong   song;

	public FluidMidiThread(String path) throws IOException {
		byte[] songData = CoreUtils.loadBytes(new File(path));
		song = MidiSong.load(new SimpleStream(songData));
		for(MidiTrack track : song.getTracks()) {
			System.out.println(track.getName());
		}
		
		FluidSequencer sequencer = new FluidSequencer();
		midiPlayer = new MidiPlayer(sequencer);
	}
	
	@Override
	public void run() {
		midiPlayer.play(song);
	}

	public void shutdown() {
		midiPlayer.stop();
	}

	public int[][] getNotes() {
		return midiPlayer.getNotes();
	}
	
	public void setPaused(boolean isPaused) {
		midiPlayer.setPaused(isPaused);
	}
	
	public boolean hasEnded() {
		return midiPlayer.hasEnded();
	}
}
