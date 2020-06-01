package xtvapps.simusplayer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class DesktopAudioTrack {

	private int bufferSize;
	private Info info;
	private AudioFormat format;
	private SourceDataLine audioLine;
	private byte byteSamples[] = null;

	public DesktopAudioTrack(int freq, int channels, int bufferSize) {
		this.bufferSize = bufferSize;
		format = new AudioFormat(freq, 16, channels, true, false); // 16bits, signed, little endian
		info = new DataLine.Info(SourceDataLine.class, format);
	}

	public void play() throws LineUnavailableException {
		audioLine = (SourceDataLine) AudioSystem.getLine(info);
		audioLine.open(format, bufferSize);
		audioLine.start();
	}

	public void write(short[] samples, int start, int len) {
		if (byteSamples == null || byteSamples.length != samples.length*2) {
			byteSamples = new byte[samples.length*2];
		}
		for(int i=0; i<samples.length; i++) {
			byteSamples[i*2+0] = (byte)(samples[i] & 0xff);
			byteSamples[i*2+1] = (byte)(samples[i] >> 8);
		}
		audioLine.write(byteSamples, start, len*2);
	}

	public void stop() {
		audioLine.drain();
        audioLine.close();
	}

	public void release() {
		audioLine = null;
	}

}
