package xtvapps.simusplayer.core;

import java.io.File;
import java.io.IOException;

import fts.core.Log;
import fts.core.Utils;
import xtvapps.simusplayer.core.audio.AudioBuffer;
import xtvapps.simusplayer.core.audio.AudioRenderThread;
import xtvapps.simusplayer.core.audio.AudioRenderer;
import xtvapps.simusplayer.core.audio.AudioBuffer.Format;
import xtvapps.simusplayer.core.audio.AudioBuffer.Status;
import xtvapps.simusplayer.core.audio.AudioPlayerThread;

public class FluidPlayer extends MediaPlayer {
	private static final String LOGTAG = FluidPlayer.class.getSimpleName();

	static {
		System.loadLibrary("simusplayer-corelib");
	}

	public FluidPlayer(WaveDevice waveDevice) {
		super(waveDevice);
	}

	@Override
	public void onInit() {
		fluidInit(waveDevice.freq);
		fluidLoadSoundFontFile("/usr/share/sounds/sf2/FluidR3_GM.sf2");
	}
	
	@Override
	public void onRelease() {
		fluidRelease();
	}
	
	@Override
	public void play(File midiFile, final AudioRenderThread audioRenderThread, final AudioPlayerThread audioPlayerThread) throws IOException {
		
		final AudioRenderer audioRenderer = new AudioRenderer() {

			@Override
			public void fillBuffer(byte[] buffer) {
				fluidFillBuffer(buffer);		
			}
		};

		final FluidMidiThread midiThread = new FluidMidiThread(midiFile.getAbsolutePath());
		
		Thread controllerThread = new Thread("FluidControllerThread") {
			@Override
			public void run() {
				audioRenderThread.setAudioRenderer(audioRenderer);
				
				while (isPlaying) {
					CoreUtils.shortSleep();
				}

				audioRenderThread.setAudioRenderer(null);

				midiThread.shutdown();
				try {
					midiThread.join();
				} catch (InterruptedException e) {}
				
			    isStopped = true;
			}			
		};
		
		isPlaying = true;
		
		midiThread.start();
		controllerThread.start();
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
