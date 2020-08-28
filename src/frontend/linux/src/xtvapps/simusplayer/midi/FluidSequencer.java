package xtvapps.simusplayer.midi;

import xtvapps.simusplayer.core.FluidPlayer;

public class FluidSequencer implements MidiSequencer {

	private FluidPlayer fluidPlayer;

	public FluidSequencer(FluidPlayer fluidPlayer) {
		this.fluidPlayer = fluidPlayer;
	}
	
	@Override
	public void reset() {
	}

	@Override
	public void sendEvent(MidiEvent event) {
		int[] data = event.getData();
		int type = event.getType().ordinal();
		
		switch(event.getType()) {
		case KEYPRESS:
		case NOTEON:
		case NOTEOFF: {
			int channel  = data[0];
			int note     = data[1];
			int velocity = data[2];
			FluidPlayer.fluidSendEventNote(type, channel, note, velocity);
			break;
		}
		case CONTROLLER: {
			int channel = data[0];
			int param   = data[1];
			int value   = data[2];
			FluidPlayer.fluidSendEventController(channel, param, value);
			break;
		}
		case PGMCHANGE:
		case CHANPRESS: {
			int channel = data[0];
			int value   = data[1];
			FluidPlayer.fluidSendEventChange(type, channel, value);
			break;
		}
		case PITCHBEND: {
			int channel = data[0];
			int value   = data[1];
			FluidPlayer.fluidSendEventPitchBend(channel, value);
			break;
		}
		case SYSEX: {
			FluidPlayer.fluidSendEventSysex(event.getSysex());
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
	}

}
