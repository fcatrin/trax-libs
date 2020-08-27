package xtvapps.simusplayer.midi;

public class AlsaSequencer implements MidiSequencer {

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendEvent(MidiEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}
	
	private static native void alsaReset();
	private static native void alsaSendEvent();
	private static native void alsaFinish();

}
