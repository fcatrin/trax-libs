package xtvapps.simusplayer.core;

import java.io.File;

import fts.core.Utils;
import xtvapps.simusplayer.core.audio.AudioPlayerThread;
import xtvapps.simusplayer.core.audio.AudioRenderThread;
import xtvapps.simusplayer.core.audio.AudioRenderer;

public abstract class MediaPlayer {
	protected WaveDevice waveDevice;
	protected boolean isPlaying = false;
	protected boolean isStopped = true;
	protected boolean isPaused = false;

	private int timeTotal;
	private int timeElapsed;
	private File file;
	private AudioRenderThread audioRenderThread;
	private AudioPlayerThread audioPlayerThread;
	
	public MediaPlayer(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
		onInit();
	}
	
	public void restart() {
		play(file, audioRenderThread, audioPlayerThread);
	}
	
	public void play(final File file, final AudioRenderThread audioRenderThread, final AudioPlayerThread audioPlayerThread) {
		timeTotal   = 0;
		timeElapsed = 0;
		
		this.file = file;
		this.audioRenderThread = audioRenderThread;
		this.audioPlayerThread = audioPlayerThread;
		
		Thread controllerThread = new Thread("MediaPlayerControllerThread") {
			@Override
			public void run() {
				AudioRenderer audioRenderer = onPrepare(file);
				if (audioRenderer != null) {
					audioRenderThread.setAudioRenderer(audioRenderer);
					while (isPlaying) {
						audioRenderThread.setPaused(isPaused);
						CoreUtils.shortSleep();
					}
					audioRenderThread.setAudioRenderer(null);
					onFinish();
				}
				isPlaying = false;
				isStopped = true;
				// MediaPlayer.this.audioPlayerThread = null;
			}
		};
		
		isStopped = false;
		isPlaying = true;

		controllerThread.start();
	}
	
	public boolean isPaused() {
		return isPlaying && isPaused;
	}
	
	public void setPause(boolean pause) {
		isPaused = pause;
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

	public int getTimeTotal() {
		return timeTotal;
	}

	public void setTimeTotal(int timeTotal) {
		this.timeTotal = timeTotal;
	}

	public int getTimeElapsed() {
		return timeElapsed;
	}

	public void setTimeElapsed(int timeElapsed) {
		this.timeElapsed = timeElapsed;
	}

	public int[][] getNotes() {
		return null;
	}

	public abstract void onInit();
	public abstract void onRelease();
	public abstract AudioRenderer onPrepare(File songFile);
	public abstract void onFinish();
	public abstract void doForward();
	public abstract void doRewind();
}
