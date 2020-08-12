package xtvapps.simusplayer.core;

import java.io.IOException;

import fts.core.Log;

public class FluidPlayer {
	private static final String LOGTAG = FluidPlayer.class.getSimpleName();
	private static final int SLEEP_TIME = 10;

	static {
		System.loadLibrary("simusplayer-fluid");
	}

	private WaveDevice waveDevice;
	private Thread audioThread;
	
	boolean isPlaying = false;
	boolean isPaused = false;
	boolean isStopped = true;
	
	public FluidPlayer(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
		fluidInit(waveDevice.freq);
		fluidLoadSoundFontFile("/home/fcatrin/Descargas/Gravis_Ultrasound_Classic_PachSet_v1.6.sf2");
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

				AudioBuffer audioBuffer1 = new AudioBuffer(bufferSize, 0);
				AudioBuffer audioBuffer2 = new AudioBuffer(bufferSize, 1);
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
	
	public native void fluidNoteOn(int channel, int note, int velocity);
	public native void fuildNoteOff(int channel, int note);
	
}
