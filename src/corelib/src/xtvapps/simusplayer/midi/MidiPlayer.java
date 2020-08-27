package xtvapps.simusplayer.midi;

import xtvapps.simusplayer.midi.MidiEvent.EventType;

public class MidiPlayer {
	
	private MidiSequencer sequencer;
	boolean playing = false;

	public MidiPlayer(MidiSequencer sequencer) {
		this.sequencer = sequencer;
	}
	
	public void play(MidiSong song) {
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
				sleep(waitTicks / 2);
				continue;
			}
			
			t0 = System.currentTimeMillis();
			ticksElapsed += ticks;
			
			eventTrack.nextEvent();
			
			if (event.getType() == EventType.TEMPO) {
				tempo = event.getTempo();
			} else {
				sequencer.sendEvent(event);
			}
		}
		
		sequencer.finish();
	}

	private void sleep(long msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
