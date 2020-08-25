package xtvapps.com.simusplayer.midi;

public class MidiEvent {
	public static final int LAST_NOTE = 128;
	
	short type;
	short port;
	long  tick;
	
	short data[] = new short[3];
	long  tempo;
	long  length;
	
	short sysex[];
}
