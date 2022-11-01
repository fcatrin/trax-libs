package xtvapps.simusplayer.midi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fts.core.Log;
import fts.core.Utils;
import xtvapps.simusplayer.midi.MidiEvent.EventType;

public class MidiPlayer {
	
	public static final int CHANNELS = 16;
	
	private MidiSequencer sequencer;
	boolean playing = false;
	boolean isPaused = false;
	boolean hasEnded = false;
	
	Map<Integer, Integer> channelMap = new HashMap<Integer, Integer>();
	int notes[][];

	public MidiPlayer(MidiSequencer sequencer) {
		this.sequencer = sequencer;
	}
	
	public void play(MidiSong song) {
		hasEnded = false;
		
		scanNotes(song);
		
		long maxTick = 0;
		for(MidiTrack track : song.getTracks()) {
			maxTick = Math.max(maxTick, track.getEndTick());
			track.setCurrentEventIndex(0);
			track.resetNotes();
		}
		
		sequencer.reset();
		
		long t0 = System.currentTimeMillis();
		long tempo = song.getTempo();
		long ticksElapsed = 0;
		
		playing = true;
		while (playing) {
			if (isPaused) {
				Utils.sleep(10);
				t0 = System.currentTimeMillis();
				continue;
			}
			long minTick = maxTick + 1;
			long now = System.currentTimeMillis();
			long elapsed = now - t0;
			
			long ticksPerMinute = (60000000 / tempo) * song.ppq;
			long ticks = (elapsed * ticksPerMinute) / 60000;

			MidiEvent event = null;
			MidiTrack eventTrack = null;
			for(MidiTrack track : song.getTracks()) {
				MidiEvent currentEvent = track.getCurrentEvent();
				if (currentEvent == null) continue;
				
				if (currentEvent.getTick() < minTick) {
					event = currentEvent;
					eventTrack = track;
					minTick = event.getTick();
				}
				
			}
		
			// no more events, song is finished
			if (event == null) break;
			
			// wait if the event is still in the future
			long waitTicks = minTick - (ticksElapsed + ticks);
			if (waitTicks > 0) {
				Utils.sleep(waitTicks / 2);
				continue;
			}
			
			t0 = System.currentTimeMillis();
			ticksElapsed += ticks;
			
			eventTrack.nextEvent();
			
			if (event.getType() == EventType.TEMPO) {
				tempo = event.getTempo();
			} else {
				sequencer.sendEvent(event);
				
				if (event.getType() == EventType.NOTEON || event.getType() == EventType.NOTEOFF) {
					int[] data = event.getData();
					int channel  = data[0];
					int note     = data[1];
					int velocity = data[2];
					notes[channelMap.get(channel)][note] = event.getType() == EventType.NOTEON ? velocity : 0;
					
				}
			}
		}
		
		sequencer.finish(maxTick);
		hasEnded = true;
	}
	
	private void scanNotes(MidiSong song) {
		Set<Integer> channels = new HashSet<Integer>();
		for(MidiTrack track : song.getTracks()) {
			for(MidiEvent event : track.getEvents()) {
				if (event.getType() != EventType.NOTEON) continue;
				int[] data = event.getData();
				int channel  = data[0];
				channels.add(channel);
				
			}
		}
		
		channelMap.clear();
		int channelIndex = 0;
		for(int i=0; i<CHANNELS; i++) {
			if (channels.contains(i)) {
				channelMap.put(i, channelIndex++);
			}
		}
		if (channelIndex > 0) {
			notes = new int[channelIndex][MidiEvent.LAST_NOTE];
		} else {
			notes = null;
		}
	}

	public int[][] getNotes() {
		return notes;
	}
	
	public void stop() {
		playing = false;
	}
	
	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}
	
	public boolean hasEnded() {
		return hasEnded;
	}
}
