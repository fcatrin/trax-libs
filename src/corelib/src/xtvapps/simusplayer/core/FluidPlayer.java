package xtvapps.simusplayer.core;

import java.io.File;
import java.io.IOException;

import fts.core.Log;
import xtvapps.simusplayer.core.audio.AudioBuffer;
import xtvapps.simusplayer.core.audio.AudioRenderThread;
import xtvapps.simusplayer.core.audio.AudioRenderer;
import xtvapps.simusplayer.core.audio.AudioBuffer.Format;
import xtvapps.simusplayer.core.audio.AudioBuffer.Status;
import xtvapps.simusplayer.core.audio.AudioPlayerThread;

public class FluidPlayer {
	private static final String LOGTAG = FluidPlayer.class.getSimpleName();

	static {
		System.loadLibrary("simusplayer-corelib");
	}

	private WaveDevice waveDevice;
	
	boolean isPlaying = false;
	boolean isPaused = false;
	boolean isStopped = true;
	
	public FluidPlayer(WaveDevice waveDevice) {
		this.waveDevice = waveDevice;
	}
	
	public void play(File midiFile) throws IOException {
		fluidInit(waveDevice.freq);
		fluidLoadSoundFontFile("/usr/share/sounds/sf2/FluidR3_GM.sf2");
		
		AudioRenderer audioRenderer = new AudioRenderer() {

			@Override
			public void fillBuffer(byte[] buffer) {
				fluidFillBuffer(buffer);		
			}
		};

		final FluidMidiThread midiThread = new FluidMidiThread(midiFile.getAbsolutePath());
		final AudioRenderThread renderThread = new AudioRenderThread(waveDevice.getFreq(), 100, 4);
		renderThread.setAudioRenderer(audioRenderer);
		
		final AudioPlayerThread audioPlayerThread = new AudioPlayerThread(waveDevice, renderThread);
		
		Thread controllerThread = new Thread("FluidControllerThread") {
			@Override
			public void run() {
				while(isPlaying) {
					CoreUtils.shortSleep();
				}
				midiThread.shutdown();
				renderThread.shutdown();
				audioPlayerThread.shutdown();
				try {
					midiThread.join();
					renderThread.join();
					audioPlayerThread.join();
				} catch (InterruptedException e) {}
				
				fluidRelease();
			    isStopped = true;
			}			
		};
		
		isPlaying = true;
		
		renderThread.start();
		audioPlayerThread.start();
		midiThread.start();
		controllerThread.start();
	}
	
	public void stop() {
		isPlaying = false;
	}
	
	public void waitForStop() {
		try {
			Thread.sleep(1000);
			while (!isStopped) {
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			
		}
	}
	
	
	public native boolean fluidInit(int freq);
	public native void    fluidLoadSoundFontFile(String path);
	public native void    fluidRelease();
	public native int     fluidFillBuffer(byte[] buffer);
	
	public static native void fluidSendEventNote(int type, int channel, int note, int velocity);
	public static native void fluidSendEventController(int channel, int param, int value);
	public static native void fluidSendEventChange(int type, int channel, int value);
	public static native void fluidSendEventPitchBend(int channel, int value);
	public static native void fluidSendEventSysex(int sysex[]);

}
