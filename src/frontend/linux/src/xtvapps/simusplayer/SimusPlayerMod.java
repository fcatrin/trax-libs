package xtvapps.simusplayer;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import fts.core.Application;
import fts.core.Context;
import fts.core.DesktopLogger;
import fts.core.DesktopResourceLocator;
import fts.core.Log;
import fts.linux.ComponentFactory;
import xtvapps.simusplayer.core.ModPlayer;

public class SimusPlayerMod {
	private static final String LOGTAG = SimusPlayer.class.getSimpleName();
	private static final int SLEEP_TIME = 10;
	
	private Thread audioThread;
	private int bufferSize;
	private int freq;
	private int channels;
	private boolean isPaused;
	private boolean isPlaying;

	public SimusPlayerMod(int freq, int channels, int bufferSize) {
		this.bufferSize = bufferSize;
		this.freq = freq;
		this.channels = channels;
	}

	public void play(String path) throws IOException {
		final ModPlayer modPlayer = new ModPlayer();
		modPlayer.xmpInit(path, freq);
		
		audioThread = new Thread() {
			@Override
			public void run() {
				
				int minbuffsize = 8192;

				Log.d(LOGTAG, "buffersize: " + bufferSize + ", minbuffersize: "  + minbuffsize);
				if (minbuffsize > bufferSize) {
					bufferSize = minbuffsize;
				}
				AudioTrack audioTrack = new AudioTrack(freq, channels, bufferSize);

				AudioBuffer audioBuffer1 = new AudioBuffer(bufferSize, 0);
				AudioBuffer audioBuffer2 = new AudioBuffer(bufferSize, 1);
				AudioBuffer audioBuffers[] = {audioBuffer1, audioBuffer2};
				
				
				int frontBuffer = 0;
				try {
					audioTrack.play();
				} catch (LineUnavailableException e1) {
					e1.printStackTrace();
				}
				
				boolean hasMore = true;
				Log.d(LOGTAG, "play start");
				do {
					if (isPaused) {
						SimusPlayerMod.this.sleep();
					} else {
						AudioBuffer buffer = audioBuffers[frontBuffer];
						modPlayer.xmpFillBuffer(audioBuffers[frontBuffer].samplesIn, 0);
						buffer.render();
						
						frontBuffer++;
						if (frontBuffer>=audioBuffers.length) frontBuffer = 0;
					
						audioTrack.write(buffer.samplesOut, 0, buffer.samplesOut.length);

						buffer.processed = false;
					}
				} while (isPlaying && hasMore);
				Log.d(LOGTAG, "play stop");
				audioTrack.stop();
			    audioTrack.release();
			    modPlayer.xmpRelease();
			    isPlaying = false;
			}
		};
		isPlaying = true;
		isPaused = false;
		audioThread.start();
	}
	
	protected void sleep() {
		try {Thread.sleep(SLEEP_TIME);} catch (Exception e) {};
	}
	

	public static void main(String[] args) throws IOException {
		Application app = new Application(new ComponentFactory(), new DesktopResourceLocator(), new DesktopLogger(), new Context());
		
		SimusPlayerMod player = new SimusPlayerMod(44100, 2, 8192);
		player.play("/home/fcatrin/git/retrobox/RetroBoxDroid/assets/music/chi.mod");

	}

}
