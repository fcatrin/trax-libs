package xtvapps.com.simusplayer.midi;

public class MidiEvent {
	
	public enum Event {
		EVENT_NOTEOFF,
		EVENT_NOTEON,
		EVENT_KEYPRESS,
		EVENT_CONTROLLER,
		EVENT_PGMCHANGE,
		EVENT_CHANPRESS,
		EVENT_PITCHBEND,
		EVENT_TEMPO,
		EVENT_SYSEX
	};
	
	public static final int LAST_NOTE = 128;
	
	short type;
	short port;
	long  tick;
	
	short data[] = new short[3];
	long  tempo;
	long  length;
	
	short sysex[];
}
