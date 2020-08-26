package xtvapps.com.simusplayer.midi;

public class MidiEvent {
	
	public enum EventType {
		NOTEOFF,
		NOTEON,
		KEYPRESS,
		CONTROLLER,
		PGMCHANGE,
		CHANPRESS,
		PITCHBEND,
		TEMPO,
		SYSEX
	};
	
	public static final int LAST_NOTE = 128;
	
	EventType type;
	int  port;
	long tick;
	
	int  data[] = new int[3];
	long tempo;
	
	int sysex[];
	
	public static EventType eventMap[] = new EventType[0x10];
	
	static {
		eventMap[0x08] = EventType.NOTEOFF;
		eventMap[0x09] = EventType.NOTEON;
		eventMap[0x0a] = EventType.KEYPRESS;
		eventMap[0x0b] = EventType.CONTROLLER;
		eventMap[0x0c] = EventType.PGMCHANGE;
		eventMap[0x0d] = EventType.CHANPRESS;
		eventMap[0x0e] = EventType.PITCHBEND;
	}
	
}
