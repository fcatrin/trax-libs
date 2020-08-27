package xtvapps.simusplayer.midi;

import java.util.List;

public class MidiTrack {
	private List<MidiEvent> events;
	private long endTick;
	private int currentEventIndex;
	private String name;

	short notes[] = new short[MidiEvent.LAST_NOTE];

	public List<MidiEvent> getEvents() {
		return events;
	}

	public long getEndTick() {
		return endTick;
	}

	public int getCurrentEventIndex() {
		return currentEventIndex;
	}
	
	public void setCurrentEventIndex(int index) {
		currentEventIndex = index;
	}
	
	public MidiEvent getCurrentEvent() {
		return currentEventIndex < events.size() ? events.get(currentEventIndex) : null;
	}
	
	public void nextEvent() {
		currentEventIndex++;
	}

	public String getName() {
		return name;
	}

	public short[] getNotes() {
		return notes;
	}
	
	public void resetNotes() {
		for(int i=0; i<notes.length; i++) {
			notes[i] = 0;
		}
	}
	
}
