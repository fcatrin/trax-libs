package xtvapps.trax.core;

import java.io.File;

import fts.core.Log;
import fts.core.Utils;
import xtvapps.trax.core.audio.AudioRenderer;

public class ModPlayer extends MediaPlayer {
	private static final String LOGTAG = ModPlayer.class.getSimpleName();
	private static final int SLEEP_TIME = 10;
	private static final int MAX_CHANNELS = 64;

	static {
		System.loadLibrary("trax-corelib");
	}

	private ModInfo modInfo = new ModInfo();
	private FrameInfo frameInfo = new FrameInfo();
	
	private ModPlayerListener modPlayerListener;
	
	boolean mutedChannels[] = new boolean[MAX_CHANNELS];
	
	public ModPlayer(WaveDevice waveDevice) {
		super(waveDevice);
	}
	
	public void setModPlayerListener(ModPlayerListener modPlayerListener) {
		this.modPlayerListener = modPlayerListener;
	}
	
	@Override
	public void onInit() {}

	@Override
	public void onRelease() {}

	@Override
	public AudioRenderer onPrepare(File songFile) {
		boolean successOnInit = xmpInit(songFile.getAbsolutePath(), waveDevice.getFreq());
		Log.d(LOGTAG, "successOnInit " + successOnInit);
		if (!successOnInit) return null;

		loadModInfo(songFile);

		for(int i=0; i<modInfo.tracks; i++) {
			xmpMuteChannel(i, mutedChannels[i]);
		}
		
		if (modPlayerListener!=null) modPlayerListener.onStart();

		return new AudioRenderer() {
			
			@Override
			public void fillBuffer(byte[] buffer) {
				int result = xmpFillBuffer(buffer, 1);
				hasEnded = result < 0;
				loadFrameInfo();
			}
		};
	}

	@Override
	public void onFinish() {
	    xmpRelease();
	    if (modPlayerListener!=null) modPlayerListener.onEnd();
	}

	private int wave[][] = new int[MAX_CHANNELS][128];
	
	public int[] getWave(int channel) {
		if (!isPrepared) return null;

		int[] w = wave[channel];
		xmpFillWave(w, channel);
		return w;
	}
	
	public void toggleChannel(int channel) {
		boolean mute = !mutedChannels[channel];
		muteChannel(channel, mute);
	}

	public void muteChannel(int channel, boolean mute) {
		xmpMuteChannel(channel, mute);
		mutedChannels[channel] = mute;
	}

	public boolean[] getWaveStatus() {
		return mutedChannels;
	}
	
	private void loadModInfo(File modFile) {
		modInfo.modName = xmpGetModuleName();
		modInfo.modFormat = xmpGetModuleFormat();
		
		if (Utils.isEmptyString(modInfo.modName)) modInfo.modName = CoreUtils.nameNoExt(modFile);

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
		
		setTimeTotal(frameInfo.totalTime);
		setTimeElapsed(frameInfo.time); 
	}
	
	@Override
	public void seek(long position) {
		int totalTime = frameInfo.totalTime;
		if (totalTime == 0) return;
		
		// position comes relative to time, we need to convert it to patterns
		float relative = (float)position / totalTime;
		int pattern = (int)(relative * modInfo.patterns);
		xmpSeek(pattern);
	}
	
	public ModInfo getModInfo() {
		return modInfo;
	}
	
	public FrameInfo getFrameInfo() {
		return frameInfo;
	}

	@Override
	public void doForward() {
		xmpForward();
	}

	@Override
	public void doRewind() {
		xmpRewind();
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
	public native void    xmpForward();
	public native void    xmpRewind();
	public native void    xmpSeek(int pattern);
	
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
