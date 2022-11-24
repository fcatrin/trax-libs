package xtvapps.trax.core.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import fts.core.Utils;
import xtvapps.trax.core.TraXCoreUtils;
import xtvapps.trax.core.audio.AudioBuffer.Format;
import xtvapps.trax.core.audio.AudioBuffer.Status;

public class AudioRenderThread extends Thread {
	private static final String LOGTAG = AudioRenderThread.class.getSimpleName();
	
	private AudioBuffer audioBuffers[];
	private long lastTime = 0;
	private int resolution = 0;
	private int currentBufferIndex = 0;
	private int nextBufferIndex = 0;
	
	private boolean running;
	private boolean isPaused;
	
	private AudioRenderer audioRenderer;
	private AudioProcessor audioProcessor;

	private String audioRendererLock = "";
	
	private static boolean debugEnabled = false;	
	FileOutputStream fosDebugPre;
	FileOutputStream fosDebugPost;
	
	public AudioRenderThread(int freq, int resolution, int buffers) {
		this.resolution = resolution;

		int requestedBufferSize = freq / resolution;
		int bufferSize = TraXCoreUtils.findNextPowerOfTwo(requestedBufferSize);
		
		audioBuffers = new AudioBuffer[buffers];
		for(int i=0; i<buffers; i++) {
			audioBuffers[i] = new AudioBuffer(bufferSize, 0, Format.S16);
		}

	}
	
	public void setAudioRenderer(AudioRenderer audioRenderer) {
		synchronized (audioRendererLock) {
			this.audioRenderer = audioRenderer;
		}
	}
	
	public void setAudioProcessor(AudioProcessor audioProcessor) {
		this.audioProcessor = audioProcessor;
	}
	
	public void render() {
		long t0 = System.currentTimeMillis();
		long waitmsec = lastTime + resolution - t0;
		if (waitmsec<=0) {
			renderBuffer();
		} else if (waitmsec > resolution / 2) {
			Utils.sleep(resolution / 2);
		}
	}
	
	public void renderBuffer() {
		AudioBuffer audioBuffer = audioBuffers[currentBufferIndex];
		while (audioBuffer.getStatus() != Status.Free && running) {
			Utils.sleep(1);
		}

		synchronized (audioRendererLock) {
			if (audioRenderer!=null && !isPaused) {
				audioRenderer.fillBuffer(audioBuffer.samplesIn);
			} else {
				Arrays.fill(audioBuffer.samplesIn, (byte)0);
				Utils.sleep(10);
			}
		}

		audioBuffer.render();
		if (debugEnabled) debugWrite(fosDebugPre, audioBuffer.samplesOut);
		if (audioProcessor != null) {
			audioProcessor.process(audioBuffer);
			if (debugEnabled) debugWrite(fosDebugPost, audioBuffer.samplesOut);
		}
		audioBuffer.setStatus(Status.Filled);

		currentBufferIndex = ++currentBufferIndex % audioBuffers.length;

		pushProcessedWave(audioBuffer);
	}
	
	int waveLeft[]  = new int[240];
	int waveRight[] = new int[240]; 
	
	private void pushProcessedWave(AudioBuffer audioBuffer) {
		for(int i=0; i<waveLeft.length; i++) {
			waveLeft[i]  = audioBuffer.samplesOut[i*2];
			waveRight[i] = audioBuffer.samplesOut[i*2 + 1];
		}
	}
	
	public int[] getWaveLeft() {
		return waveLeft;
	}
	
	public int[] getWaveRight() {
		return waveRight;
	}

	public AudioBuffer getNextBuffer() {
		AudioBuffer audioBuffer = audioBuffers[nextBufferIndex];
		if (audioBuffer.getStatus() != Status.Filled) return null;
		
		nextBufferIndex = ++nextBufferIndex % audioBuffers.length;
		return audioBuffer;
	}
	
	@Override
	public void run() {
		debugInit();
		running = true;
		while (running) {
			render();
		}
		debugDone();
	}
	
	private void debugWrite(OutputStream os, short samples[]) {
		try {
			os.write(TraXCoreUtils.toByteArray(samples));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void debugInit() {
		if (!debugEnabled) return;
		try {
			File homeDir = new File(System.getProperty("user.home"));
			fosDebugPre  = new FileOutputStream(new File(homeDir, "samplesPre.raw"));
			fosDebugPost = new FileOutputStream(new File(homeDir, "samplesPost.raw"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void debugDone() {
		if (!debugEnabled) return;
		try {
			fosDebugPre.close();
			fosDebugPost.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		running = false;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}
	
}
