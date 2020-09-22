package xtvapps.simusplayer.core;

import java.io.File;
import java.io.IOException;

import fts.core.Log;
import fts.core.Utils;
import xtvapps.simusplayer.core.audio.AudioPlayerThread;
import xtvapps.simusplayer.core.audio.AudioRenderThread;
import xtvapps.simusplayer.core.audio.AudioRenderer;

public class ModPlayer {
	private static final String LOGTAG = ModPlayer.class.getSimpleName();
	private static final int SLEEP_TIME = 10;

	static {
		System.loadLibrary("simusplayer-corelib");
	}

	private WaveDevice waveDevice;

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
	
	public void play(File modFile) throws IOException {
		xmpInit(modFile.getAbsolutePath(), waveDevice.getFreq());

		loadModInfo(modFile);
		
		mutedChannels = new boolean[modInfo.tracks];

		AudioRenderer audioRenderer = new AudioRenderer() {
			
			@Override
			public void fillBuffer(byte[] buffer) {
				xmpFillBuffer(buffer, 0);
				loadFrameInfo();
			}
		};
		
		final AudioRenderThread renderThread = new AudioRenderThread(waveDevice.getFreq(), 100, 4);
		renderThread.setAudioRenderer(audioRenderer);
		
		final AudioPlayerThread audioPlayerThread = new AudioPlayerThread(waveDevice, renderThread);
		Thread controllerThread = new Thread("ModPlayerControllerThread") {
			@Override
			public void run() {
				if (modPlayerListener!=null) modPlayerListener.onStart();

				while(isPlaying) {
					CoreUtils.shortSleep();
				}
				
				renderThread.shutdown();
				audioPlayerThread.shutdown();
				try {
					renderThread.join();
					audioPlayerThread.join();
				} catch (InterruptedException e) {}
				
			    xmpRelease();
			    if (modPlayerListener!=null) modPlayerListener.onEnd();

			    isStopped = true;
			}			
		};

		isPlaying = true;
		isPaused = false;
		isStopped = false;
		
		renderThread.start();
		audioPlayerThread.start();
		controllerThread.start();
	}
	
	public void stop() {
		isPlaying = false;
	}
	
	public void waitForStop() {
		try {
			Thread.sleep(1000);
			while (!isStopped) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
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
	
	private void loadModInfo(File modFile) {
		modInfo.modName = xmpGetModuleName();
		modInfo.modFormat = xmpGetModuleFormat();
		
		if (Utils.isEmptyString(modInfo.modName)) modInfo.modName = modFile.getName();

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
