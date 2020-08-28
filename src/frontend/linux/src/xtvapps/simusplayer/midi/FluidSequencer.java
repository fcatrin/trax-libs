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
		
		switch (event.getType()) {
		case NOTEON: {
			int channel  = data[0];
			int note     = data[1];
			int velocity = data[2];
			System.out.println("send note c:" + channel + ", n:" + note + "v:" + velocity);
			fluidPlayer.fluidNoteOn(channel, note, velocity);
			break;
		}
		case NOTEOFF: {
			int channel  = data[0];
			int note     = data[1];
			fluidPlayer.fluidNoteOff(channel, note);
			break;
		}
		}
	}

	@Override
	public void finish(long tick) {
	}

}
