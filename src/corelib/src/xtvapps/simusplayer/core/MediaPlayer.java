package xtvapps.simusplayer.core;

import java.io.File;
import java.io.IOException;

import fts.core.Utils;
import xtvapps.simusplayer.core.audio.AudioPlayerThread;
import xtvapps.simusplayer.core.audio.AudioRenderThread;
import xtvapps.simusplayer.core.audio.AudioRenderer;

public abstract class MediaPlayer {
	protected WaveDevice waveDevice;
	protected boolean isPlaying = false;
	protected boolean isPaused = false;
	protected boolean isStopped = true;
	
	public MediaPlayer(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
		onInit();
	}
	
	public void play(final File file, final AudioRenderThread audioRenderThread, final AudioPlayerThread audioPlayerThread) {
		Thread controllerThread = new Thread("MediaPlayerControllerThread") {
			@Override
			public void run() {
				AudioRenderer audioRenderer = onPrepare(file);
				if (audioRenderer != null) {
					audioRenderThread.setAudioRenderer(audioRenderer);
					while (isPlaying) {
						CoreUtils.shortSleep();
					}
					audioRenderThread.setAudioRenderer(null);
					onFinish();
				}
				isPlaying = false;
				isStopped = true;
			}
		};
		
		isPaused  = false;
		isStopped = false;
		isPlaying = true;

		controllerThread.start();
	}
	
	public void stop() {
		isPlaying = false;
	}
	
	public void waitForStop() {
		Utils.sleep(500);
		while (!isStopped) {
			Utils.sleep(100);
		}
	}
	
	public void shutdown() {
		stop();
		waitForStop();
		onRelease();
	}
	
	public abstract void onInit();
	public abstract void onRelease();
	public abstract AudioRenderer onPrepare(File songFile);
	public abstract void onFinish();
}
