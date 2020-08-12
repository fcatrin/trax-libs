package xtvapps.simusplayer.core;

import java.io.IOException;

import fts.core.Log;

public class ModPlayer {
	private static final String LOGTAG = ModPlayer.class.getSimpleName();
	private static final int SLEEP_TIME = 10;

	static {
		System.loadLibrary("simusplayer-corelib");
	}

	private WaveDevice waveDevice;
	private Thread audioThread;

	private ModInfo modInfo = new ModInfo();
	private FrameInfo frameInfo = new FrameInfo();

	private ModPlayerListener modPlayerListener;
	
	boolean isPlaying = false;
	boolean isPaused = false;
	boolean isStopped = true;
	
	boolean mutedChannels[];
	
	public ModPlayer(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
	}
	
	public void setModPlayerListener(ModPlayerListener modPlayerListener) {
		this.modPlayerListener = modPlayerListener;
	}
	
	public void play(String path) throws IOException {
		xmpInit(path, waveDevice.getFreq());

		loadModInfo();
		
		mutedChannels = new boolean[modInfo.tracks];

		audioThread = new Thread() {
			@Override
			public void run() {
				
				if (modPlayerListener!=null) modPlayerListener.onStart();
				
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
					
						loadFrameInfo();
						waveDevice.write(buffer.samplesOut, buffer.samplesOut.length);

						buffer.processed = false;
					}
				} while (isPlaying);
				Log.d(LOGTAG, "play stop");
				waveDevice.close();
			    xmpRelease();
			    if (modPlayerListener!=null) modPlayerListener.onEnd();
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
	
	private int wave[][] = new int[24][128];
	
	public int[] getWave(int channel) {
		int[] w = wave[channel];
		xmpFillWave(w, channel);
		return w;
	}
	
	protected void sleep() {
		try {Thread.sleep(SLEEP_TIME);} catch (Exception e) {};
	}
	
	public void toggleChannel(int channel) {
		boolean mute = !mutedChannels[channel];
		xmpMuteChannel(channel, mute);
		mutedChannels[channel] = mute;
		
		Log.d(LOGTAG, "toggle channel " + channel);
	}
	
	private void loadModInfo() {
		modInfo.modName = xmpGetModuleName();
		modInfo.modFormat = xmpGetModuleFormat();

		int[] modInfoData = xmpGetModuleInfo();
		modInfo.tracks = modInfoData[0];
		modInfo.patterns = modInfoData[1];
		modInfo.samples = modInfoData[2];
		modInfo.speed = modInfoData[3];
		modInfo.bpm = modInfoData[4];

		frameInfo.position = 0;
		frameInfo.speed = modInfo.speed;
		frameInfo.bpm = modInfo.bpm;
	}
	
	private void loadFrameInfo() {
		int[] playingInfo = xmpGetPlayingInfo();
		frameInfo.position     = playingInfo[0];
		frameInfo.speed        = playingInfo[1];
		frameInfo.bpm          = playingInfo[2];
		frameInfo.time         = playingInfo[3];
		frameInfo.totalTime    = playingInfo[4];
		frameInfo.virtChannels = playingInfo[5];
		frameInfo.virtUsed     = playingInfo[6];
	}
	
	public ModInfo getModInfo() {
		return modInfo;
	}
	
	public FrameInfo getFrameInfo() {
		return frameInfo;
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
	public native void    xmpMuteChannel(int channel, boolean mute);
	
	public class ModInfo {
		public String modName;
		public String modFormat;
		public int samples;
		public int patterns;
		public int tracks;
		public int speed;
		public int bpm;
	}
	
	public class FrameInfo {
		public int position;
		public int speed;
		public int bpm;
		public int time;
		public int totalTime;
		public int virtChannels;
		public int virtUsed;
	}
	
	public interface ModPlayerListener {
		public void onStart();
		public void onEnd();
	}
}
