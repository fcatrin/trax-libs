package xtvapps.trax.core;

import java.io.File;

import fts.core.Log;
import xtvapps.trax.core.audio.AudioRenderer;

public class GMEPlayer extends MediaPlayer {
	public enum TVNorm {PAL, NTSC};
	
	private static final String LOGTAG = GMEPlayer.class.getSimpleName();
	private static final int TIME_RWND_FWD = 5000;
	
	private static final int MAX_CHANNELS = 24;
	boolean mutedChannels[] = new boolean[MAX_CHANNELS];

	private TVNorm tvNorm = TVNorm.NTSC;

	int handle;
	
	public GMEPlayer(WaveDevice waveDevice) {
		super(waveDevice);
	}

	public void setTvNorm(TVNorm tvNorm) {
		this.tvNorm = tvNorm;
	}

	@Override
	public void onInit() {}

	@Override
	public void onRelease() {}

	@Override
	public AudioRenderer onPrepare(File songFile) {
		handle = gmeOpen(songFile.getAbsolutePath(), 0, waveDevice.getFreq(), 0.2f, true);
		if (handle < 0) {
			Log.d(LOGTAG, "Cannot open fie " + songFile);
			return null;
		}
		
		if (songFile.getName().endsWith(".sap")) {
			gmeSetTempo(handle, tvNorm == TVNorm.PAL ? 1.0 : 1.08);
		}
		
		for(int i=0; i<gmeGetWavesCount(i); i++) {
			// gmeMuteChannel(i, mutedChannels[i]);
		}
		
		return new AudioRenderer() {
			
			@Override
			public void fillBuffer(byte[] buffer) {
				int result = gmeFillBuffer(handle, buffer);
				hasEnded = result < 0;
			}
		};
	}

	@Override
	public void onFinish() {
		gmeClose(handle);
		handle = -1;
	}

	@Override
	public void doForward() {
		move(TIME_RWND_FWD);
	}

	@Override
	public void doRewind() {
		move(-TIME_RWND_FWD);
	}
	
	private void move(int delta) {
		long msec = getTimeElapsed();
		long position = msec + delta;

		gmeSeek(handle, position);
	}

	@Override
	public int getTimeTotal() {
		return gmeTimeTotal(handle);
	}

	@Override
	public int getTimeElapsed() {
		return gmeTimeElapsed(handle);
	}

	@Override
	public void seek(long position) {
		gmeSeek(handle, position);
	}
	
	public void fillWave(int channel, int[] wave) {
		gmeFillWave(handle, channel, wave);
	}
	
	public int getWavesCount() {
		return gmeGetWavesCount(handle);
	}
	
	private int wave[][] = new int[24][128];
	
	public int[] getWave(int channel) {
		int[] w = wave[channel];
		gmeFillWave(handle, channel, w);
		return w;
	}
	
	public void toggleChannel(int channel) {
		boolean mute = !mutedChannels[channel];
		muteChannel(channel, mute);
	}

	public void muteChannel(int channel, boolean mute) {
		// gmeMuteChannel(channel, mute);
		mutedChannels[channel] = mute;
	}

	public boolean[] getWaveStatus() {
		return mutedChannels;
	}
	
	private static native int  gmeOpen(String path, int track, int freq, float depth, boolean accurate);
	private static native void gmeSetTempo(int handle, double tempo);
	private static native int  gmeFillBuffer(int handle, byte[] buffer);
	private static native void gmeFillWave(int handle, int channel, int[] wave);
	private static native int  gmeGetWavesCount(int handle);
	private static native void gmeClose(int handle);
	private static native void gmeSeek(int handle, long position);
	private static native int  gmeTimeElapsed(int handle);
	private static native int  gmeTimeTotal(int handle);
	
	public static native String[] gmeGetMetadata(String path);

}
