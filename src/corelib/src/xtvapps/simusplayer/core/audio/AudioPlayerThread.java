package xtvapps.simusplayer.core.audio;

import fts.core.Log;
import xtvapps.simusplayer.core.CoreUtils;
import xtvapps.simusplayer.core.FluidPlayer;
import xtvapps.simusplayer.core.WaveDevice;
import xtvapps.simusplayer.core.audio.AudioBuffer.Status;

public class AudioPlayerThread extends Thread{
	private static final String LOGTAG = AudioPlayerThread.class.getSimpleName();

	private boolean isPaused;
	private boolean isPlaying;
	
	private WaveDevice waveDevice;
	private AudioRenderThread audioRenderThread;

	public AudioPlayerThread(WaveDevice waveDevice, AudioRenderThread audioRenderThread) {
		this.waveDevice = waveDevice;
		this.audioRenderThread = audioRenderThread;
	}
	
	@Override
	public void run() {
		Log.d(LOGTAG, "play start");

		isPaused  = false;
		isPlaying = true;
		
		waveDevice.open();
		
		do {
			if (isPaused) {
				CoreUtils.shortSleep();
			} else {
				AudioBuffer buffer = audioRenderThread.getNextBuffer();
				if (buffer != null) {
					buffer.setStatus(Status.Processing);
					waveDevice.write(buffer.samplesOut, buffer.samplesOut.length);
					buffer.setStatus(Status.Free);
				} else {
					CoreUtils.shortSleep();
				}
			}
		} while (isPlaying);
		
		waveDevice.close();

		Log.d(LOGTAG, "play stop");
	}
	
	public void shutdown() {
		isPlaying = false;
	}
	
	public void pause() {
		isPaused = true;
	}

}
