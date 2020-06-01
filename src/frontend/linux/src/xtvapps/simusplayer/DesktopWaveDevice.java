package xtvapps.simusplayer;

import javax.sound.sampled.LineUnavailableException;

import xtvapps.simusplayer.core.WaveDevice;

public class DesktopWaveDevice extends WaveDevice {
	private static final int CHANNELS = 2;

	private DesktopAudioTrack audioTrack;

	public DesktopWaveDevice(int freq, int bufferSize) {
		super(freq, bufferSize);
	}

	@Override
	public void open() {
		audioTrack = new DesktopAudioTrack(getFreq(), CHANNELS, getBufferSize());
		try {
			audioTrack.play();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(short[] samples, int len) {
		audioTrack.write(samples, 0, len);
	}

	@Override
	public void close() {
		audioTrack.stop();
	    audioTrack.release();
	}

}
