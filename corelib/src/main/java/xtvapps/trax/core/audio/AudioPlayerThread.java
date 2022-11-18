package xtvapps.trax.core.audio;

import fts.core.Log;
import xtvapps.trax.core.CoreUtils;
import xtvapps.trax.core.WaveDevice;
import xtvapps.trax.core.audio.AudioBuffer.Status;

public class AudioPlayerThread extends Thread{
	private static final String LOGTAG = AudioPlayerThread.class.getSimpleName();

	private boolean isPaused;
	private boolean isPlaying;
	
	private WaveDevice waveDevice;
	private AudioRenderThread audioRenderThread;

	public AudioPlayerThread(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
	}

	public void setAudioRenderThread(AudioRenderThread audioRenderThread) {
		synchronized (audioRenderThread) {
			this.audioRenderThread = audioRenderThread;
		}
	}
	
	@Override
	public void run() {
		Log.d(LOGTAG, "play start");

		isPaused  = false;
		isPlaying = true;
		
		waveDevice.open();
		
		AudioBuffer buffer = null;
		do {
			synchronized (audioRenderThread) {
				buffer = null;
				if (audioRenderThread!=null && !isPaused) {
					buffer = audioRenderThread.getNextBuffer(); 
				}
			}
			if (buffer != null) {
				buffer.setStatus(Status.Processing);
				waveDevice.write(buffer.samplesOut, buffer.samplesOut.length);
				buffer.setStatus(Status.Free);
			} else {
				CoreUtils.shortSleep();
			}
		} while (isPlaying);
		
		waveDevice.close();

		Log.d(LOGTAG, "play stop");
	}
	
	public void shutdown() {
		isPlaying = false;
	}

	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}
}
