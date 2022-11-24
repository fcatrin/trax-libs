package xtvapps.trax.core;

import java.io.File;

import fts.core.SimpleCallback;
import fts.core.Utils;
import xtvapps.trax.core.audio.AudioPlayerThread;
import xtvapps.trax.core.audio.AudioRenderThread;
import xtvapps.trax.core.audio.AudioRenderer;

public abstract class MediaPlayer {
	static {
		System.loadLibrary("trax-corelib");
	}

	protected WaveDevice waveDevice;
	protected boolean isPlaying = false;
	protected boolean isStopped = true;
	protected boolean isPaused = false;
	protected boolean hasEnded = false;
	protected boolean isPrepared = false;

	private int timeTotal;
	private int timeElapsed;
	private File file;
	private AudioRenderThread audioRenderThread;
	private AudioPlayerThread audioPlayerThread;
	private SimpleCallback onEndedCallback;

	public MediaPlayer(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
		onInit();
	}
	
	public void setOnEndedCallback(SimpleCallback onEndedCallback) {
		this.onEndedCallback = onEndedCallback;
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
		isPrepared = false;
		
		Thread controllerThread = new Thread("MediaPlayerControllerThread") {
			@Override
			public void run() {
				AudioRenderer audioRenderer = onPrepare(file);
				if (audioRenderer != null) {
					isPrepared = true;
					audioRenderThread.setAudioRenderer(audioRenderer);
					while (isPlaying && !hasEnded) {
						audioRenderThread.setPaused(isPaused);
						TraXCoreUtils.shortSleep();
					}
					isPrepared = false;
					audioRenderThread.setAudioRenderer(null);
					onFinish();
				} else {
					onFinish();
				}
				isPlaying = false;
				isStopped = true;
				if (hasEnded && onEndedCallback!=null) onEndedCallback.onResult();
			}
		};
		
		hasEnded  = false;
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
	
	public void seek(long position){}

	public abstract void onInit();
	public abstract void onRelease();
	public abstract AudioRenderer onPrepare(File songFile);
	public abstract void onFinish();
	public abstract void doForward();
	public abstract void doRewind();
}
