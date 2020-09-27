package xtvapps.simusplayer.core;

import java.io.File;
import java.io.IOException;

import fts.core.Utils;
import xtvapps.simusplayer.core.audio.AudioPlayerThread;
import xtvapps.simusplayer.core.audio.AudioRenderThread;

public abstract class MediaPlayer {
	protected WaveDevice waveDevice;
	protected boolean isPlaying = false;
	protected boolean isPaused = false;
	protected boolean isStopped = true;
	
	public MediaPlayer(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
		onInit();
	}
	
	public abstract void play(File file, final AudioRenderThread audioRenderThread, final AudioPlayerThread audioPlayerThread) throws IOException;
	
	public void stop() {
		isPlaying = false;
	}
	
	public void waitForStop() {
		Utils.sleep(1000);
		while (!isStopped) {
			Utils.sleep(100);
		}
	}
	
	public void shutdown() {
		stop();
		waitForStop();
		onRelease();
	}
	
	public void onInit() {}
	public void onRelease() {}
}
