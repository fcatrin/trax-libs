package xtvapps.simusplayer.core;

import java.io.IOException;

import fts.core.Log;
import xtvapps.simusplayer.core.AudioBuffer.Format;

public class FluidPlayer {
	private static final String LOGTAG = FluidPlayer.class.getSimpleName();
	private static final int SLEEP_TIME = 10;

	static {
		System.loadLibrary("simusplayer-corelib");
	}

	private WaveDevice waveDevice;
	private Thread audioThread;
	
	boolean isPlaying = false;
	boolean isPaused = false;
	boolean isStopped = true;
	
	public FluidPlayer(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
		fluidInit(waveDevice.freq);
		fluidLoadSoundFontFile("/usr/share/sounds/sf2/FluidR3_GM.sf2");
	}
	
	public void play(String path) throws IOException {
		audioThread = new Thread() {
			@Override
			public void run() {
				int minbuffsize = 1024;
				int bufferSize = waveDevice.getBufferSize();

				Log.d(LOGTAG, "buffersize: " + bufferSize + ", minbuffersize: "  + minbuffsize);
				if (minbuffsize > bufferSize) {
					bufferSize = minbuffsize;
				}
				
				waveDevice.open();

				AudioBuffer audioBuffer1 = new AudioBuffer(bufferSize, 0, Format.S16);
				AudioBuffer audioBuffer2 = new AudioBuffer(bufferSize, 1, Format.S16);
				AudioBuffer audioBuffers[] = {audioBuffer1, audioBuffer2};
				
				
				int frontBuffer = 0;
				
				Log.d(LOGTAG, "play start");
				do {
					if (isPaused) {
						FluidPlayer.this.sleep();
					} else {
						AudioBuffer buffer = audioBuffers[frontBuffer];
						fluidFillBuffer(buffer.samplesIn);
						buffer.render();
						waveDevice.write(buffer.samplesOut, buffer.samplesOut.length);
						buffer.processed = false;
					}
				} while (isPlaying);
				Log.d(LOGTAG, "play stop");
				fluidRelease();
				waveDevice.close();
			    isStopped = true;
			}
		};
		isPlaying = true;
		isPaused = false;
		isStopped = false;
		audioThread.start();
	}
	
	public void stop() {
		isPlaying = false;
	}
	
	public void waitForStop() {
		while (!isStopped) {
			sleep();
		}
	}
	
	protected void sleep() {
		try {Thread.sleep(SLEEP_TIME);} catch (Exception e) {};
	}
	
	public native boolean fluidInit(int freq);
	public native void    fluidLoadSoundFontFile(String path);
	public native void    fluidRelease();
	public native int     fluidFillBuffer(byte[] buffer);
	
	public static native void fluidSendEventNote(int type, int channel, int note, int velocity);
	public static native void fluidSendEventController(int channel, int param, int value);
	public static native void fluidSendEventChange(int type, int channel, int value);
	public static native void fluidSendEventPitchBend(int channel, int value);
	public static native void fluidSendEventSysex(int sysex[]);
	
}
