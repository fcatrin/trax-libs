package xtvapps.simusplayer.midi;

import java.util.HashMap;
import java.util.Map;

import xtvapps.simusplayer.midi.MidiEvent.EventType;

public class AlsaSequencer implements MidiSequencer {

	private static final Map<EventType, Integer> eventMap = new HashMap<EventType, Integer>();
	
	private static final int SND_SEQ_EVENT_NOTEOFF    = 7; 
	private static final int SND_SEQ_EVENT_NOTEON     = 6; 
	private static final int SND_SEQ_EVENT_KEYPRESS   = 8; 
	private static final int SND_SEQ_EVENT_CONTROLLER = 10; 
	private static final int SND_SEQ_EVENT_PGMCHANGE  = 11; 
	private static final int SND_SEQ_EVENT_CHANPRESS  = 12; 
	private static final int SND_SEQ_EVENT_PITCHBEND  = 13; 
	private static final int SND_SEQ_EVENT_TEMPO      = 35; 
	private static final int SND_SEQ_EVENT_SYSEX      = 130; 
	
	static {
		eventMap.put(EventType.NOTEOFF,    SND_SEQ_EVENT_NOTEOFF);
		eventMap.put(EventType.NOTEON,     SND_SEQ_EVENT_NOTEON);
		eventMap.put(EventType.KEYPRESS,   SND_SEQ_EVENT_KEYPRESS);
		eventMap.put(EventType.CONTROLLER, SND_SEQ_EVENT_CONTROLLER);
		eventMap.put(EventType.PGMCHANGE,  SND_SEQ_EVENT_PGMCHANGE);
		eventMap.put(EventType.CHANPRESS,  SND_SEQ_EVENT_CHANPRESS);
		eventMap.put(EventType.PITCHBEND,  SND_SEQ_EVENT_PITCHBEND);
		eventMap.put(EventType.TEMPO,      SND_SEQ_EVENT_TEMPO);
		eventMap.put(EventType.SYSEX,      SND_SEQ_EVENT_SYSEX);
	}
	
	public AlsaSequencer() {}
	
	@Override
	public void reset() {
		alsaReset();
	}

	@Override
	public void sendEvent(MidiEvent event) {
		int alsaType = eventMap.get(event.getType());
		long tick = event.getTick();
		int[] data = event.getData();
		
		switch(event.getType()) {
		case KEYPRESS:
		case NOTEON:
		case NOTEOFF: {
			int channel  = data[0];
			int note     = data[1];
			int velocity = data[2];
			alsaSendEventNote(tick, alsaType, channel, note, velocity);
			break;
		}
		case CONTROLLER: {
			int channel = data[0];
			int param   = data[1];
			int value   = data[2];
			alsaSendEventController(tick, channel, param, value);
			break;
		}
		case PGMCHANGE:
		case CHANPRESS: {
			int channel = data[0];
			int value   = data[1];
			alsaSendEventChange(tick, alsaType, channel, value);
			break;
		}
		case PITCHBEND: {
			int channel = data[0];
			int value   = data[1];
			alsaSendEventPitchBend(tick, channel, value);
			break;
		}
		case SYSEX: {
			alsaSendEventSysex(tick, event.getSysex());
			break;
		}
		case TEMPO:
			break;
		default:
			break;
		}
	}

	@Override
	public void finish(long tick) {
		alsaFinish(tick);
	}
	
	private static native void alsaReset();
	private static native void alsaSendEventNote(long tick, int type, int channel, int note, int velocity);
	private static native void alsaSendEventController(long tick, int channel, int param, int value);
	private static native void alsaSendEventChange(long tick, int type, int channel, int value);
	private static native void alsaSendEventPitchBend(long tick, int channel, int value);
	private static native void alsaSendEventSysex(long tick, int sysex[]);
	private static native void alsaFinish(long tick);

}
