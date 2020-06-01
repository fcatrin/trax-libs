package xtvapps.simusplayer.core;

import java.io.IOException;

import fts.core.Log;

public class ModPlayer {
	private static final String LOGTAG = ModPlayer.class.getSimpleName();
	private static final int SLEEP_TIME = 10;
	
	static {
		System.loadLibrary("simusplayer-mod");
	}

	private WaveDevice waveDevice;
	private Thread audioThread;
	
	boolean isPlaying = false;
	boolean isPaused = false;
	
	public ModPlayer(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
	}
	
	public void play(String path) throws IOException {
		xmpInit(path, waveDevice.getFreq());
		
		System.out.println("mod name: " + xmpGetModuleName());
		System.out.println("mod format: " + xmpGetModuleFormat());
		
		
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
						ModPlayer.this.sleep();
					} else {
						AudioBuffer buffer = audioBuffers[frontBuffer];
						xmpFillBuffer(audioBuffers[frontBuffer].samplesIn, 0);
						buffer.render();
						
						frontBuffer++;
						if (frontBuffer>=audioBuffers.length) frontBuffer = 0;
					
						waveDevice.write(buffer.samplesOut, buffer.samplesOut.length);

						buffer.processed = false;
					}
				} while (isPlaying);
				Log.d(LOGTAG, "play stop");
				waveDevice.close();
			    xmpRelease();
			    isPlaying = false;
			}
		};
		isPlaying = true;
		isPaused = false;
		audioThread.start();
	}
	
	public void stop() {
		isPlaying = false;
	}
	
	private int wave[][] = new int[24][64];
	
	public int[] getWave(int channel) {
		int[] w = wave[channel];
		xmpFillWave(w, channel);
		return w;
	}
	
	protected void sleep() {
		try {Thread.sleep(SLEEP_TIME);} catch (Exception e) {};
	}

	public native boolean xmpInit(String path, int freq);
	public native void    xmpRelease();
	public native int     xmpFillBuffer(byte[] buffer, int loop);
	public native static String xmpGetModuleNameFromPath(String path);
	public native void    xmpSetVolume(int volume);
	public native void    xmpFillWave(int[] wave, int channel);
	public native String  xmpGetModuleName();
	public native String  xmpGetModuleFormat();
	public native int[]   xmpGetModuleInfo();
	public native String  xmpGetSampleName(int sample);
	public native int[]   xmpGetPlayingInfo();
	
}
