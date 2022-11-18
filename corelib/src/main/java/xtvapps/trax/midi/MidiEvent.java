package xtvapps.trax.midi;

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
	
	private EventType type;
	private int  port;
	private long tick;
	
	private int  data[];
	private long tempo;
	
	private int sysex[];
	
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

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getTick() {
		return tick;
	}

	public void setTick(long tick) {
		this.tick = tick;
	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}

	public long getTempo() {
		return tempo;
	}

	public void setTempo(long tempo) {
		this.tempo = tempo;
	}

	public int[] getSysex() {
		return sysex;
	}

	public void setSysex(int[] sysex) {
		this.sysex = sysex;
	}
	
}
