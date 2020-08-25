package xtvapps.com.simusplayer.midi;

import java.util.List;

public class MidiTrack {
	List<MidiEvent> events;
	long endTick;
	int currentEvent;
	String name;

	short notes[] = new short[MidiEvent.LAST_NOTE];
}
