package xtvapps.simusplayer.midi;

import java.util.List;

public class MidiTrack {
	List<MidiEvent> events;
	long endTick;
	int currentEvent;
	String name;

	short notes[] = new short[MidiEvent.LAST_NOTE];

	public List<MidiEvent> getEvents() {
		return events;
	}

	public long getEndTick() {
		return endTick;
	}

	public int getCurrentEvent() {
		return currentEvent;
	}

	public String getName() {
		return name;
	}

	public short[] getNotes() {
		return notes;
	}
	
	
}
