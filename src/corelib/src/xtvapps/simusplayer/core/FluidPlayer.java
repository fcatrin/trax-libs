package xtvapps.simusplayer.core;

import java.io.IOException;

import fts.core.Log;
import xtvapps.simusplayer.core.AudioBuffer.Format;
import xtvapps.simusplayer.core.AudioBuffer.Status;

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
		final FluidMidiThread midiThread = new FluidMidiThread(path);
		final FluidRenderThread renderThread = new FluidRenderThread(this, waveDevice.getFreq(), 100, 4);
		audioThread = new Thread() {
			@Override
			public void run() {
				waveDevice.open();
				
				Log.d(LOGTAG, "play start");
				do {
					if (isPaused) {
						FluidPlayer.this.sleep();
					} else {
						AudioBuffer buffer = renderThread.getNextBuffer();
						if (buffer != null) {
							buffer.setStatus(Status.Processing);
							waveDevice.write(buffer.samplesOut, buffer.samplesOut.length);
							buffer.setStatus(Status.Free);
						} else {
							FluidPlayer.this.sleep();
						}
					}
				} while (isPlaying);
				
				Log.d(LOGTAG, "play stop");
				
				midiThread.shutdown();
				renderThread.shutdown();
				try {
					midiThread.join();
					renderThread.join();
				} catch (InterruptedException e) {}
				
				fluidRelease();
				waveDevice.close();
			    isStopped = true;
			}
		};
		isPlaying = true;
		isPaused = false;
		isStopped = false;
		
		renderThread.start();
		audioThread.start();
		midiThread.start();
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
