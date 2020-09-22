package xtvapps.simusplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import fts.android.AndroidUtils;
import fts.android.FtsActivity;
import fts.core.Context;
import fts.core.Log;
import fts.core.SimpleCallback;
import fts.core.Utils;
import fts.core.Window;
import xtvapps.simusplayer.core.ModPlayer;
import xtvapps.simusplayer.core.ModPlayer.FrameInfo;
import xtvapps.simusplayer.core.ModPlayer.ModInfo;
import xtvapps.simusplayer.core.audio.AudioPlayerThread;
import xtvapps.simusplayer.core.audio.AudioRenderThread;
import xtvapps.simusplayer.core.lcd.LcdSegmentWidget;
import xtvapps.simusplayer.core.widgets.WaveContainer;

public class MainActivity extends FtsActivity {
	private static final String LOGTAG = MainActivity.class.getSimpleName();

	private static WaveContainer waveContainer;
	private static LcdSegmentWidget lcdTime;
	private static LcdSegmentWidget lcdLength;
	private static LcdSegmentWidget lcdPosition;
	private static LcdSegmentWidget lcdTempo;

	private static ModPlayer modPlayer;
	private static ModInfo modInfo;

	private static List<File> songs = new ArrayList<File>();
	private static int currentSong = 0;

	private AndroidWaveDevice waveDevice;

	private AudioPlayerThread audioPlayerThread;

	private AudioRenderThread audioRenderThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Context.pointsPerPixel = 2;
		super.onCreate(savedInstanceState);

		songs.add(new File(getFilesDir(), "test/elimination.mod"));
		
		final Window window = getFtsWindow();
		
		window.setOnFrameCallback(new SimpleCallback() {
			
			@Override
			public void onResult() {
				onFrameCallback();
			}
		});
		
		waveContainer = (WaveContainer)window.findWidget("waves");
		
		lcdTime     = (LcdSegmentWidget)window.findWidget("lcdTime");
		lcdLength   = (LcdSegmentWidget)window.findWidget("lcdLength");
		lcdPosition = (LcdSegmentWidget)window.findWidget("lcdPosition");
		lcdTempo    = (LcdSegmentWidget)window.findWidget("lcdTempo");
		
		try {
			AndroidUtils.unpackAssets(this, "test", getFilesDir());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		waveDevice = new AndroidWaveDevice(44100, 4096);
		modPlayer = new ModPlayer(waveDevice);
		modPlayer.setModPlayerListener(new ModPlayer.ModPlayerListener() {
			
			@Override
			public void onStart() {
				modInfo = modPlayer.getModInfo();
				System.out.println("name: " + modInfo.modName);
				System.out.println("format: " + modInfo.modFormat);
				System.out.println("tracks: " + modInfo.tracks);
				System.out.println("patterns: " + modInfo.patterns);
				System.out.println("samples: " + modInfo.samples);
				System.out.println("speed: " + modInfo.speed);
				System.out.println("bpm: " + modInfo.bpm);
				for(int i=0; i<modInfo.samples; i++) {
					System.out.println(String.format("%02X : %s", i, modPlayer.xmpGetSampleName(i)));
				}
				
				String modName = Utils.isEmptyString(modInfo.modName) ? "" : modInfo.modName;
				
				LcdSegmentWidget lcdModName = (LcdSegmentWidget)window.findWidget("lcdModName");
				lcdModName.setText(toFirstLetterUppercase(modName));
				waveContainer.setWaves(modInfo.tracks);
			}
			
			@Override
			public void onEnd() {
				Log.d(LOGTAG, "player ends");
			}
		});
	}

	@Override
	protected String getRootLayout() {
		return "modplayer";
	}
	
	private void play() {
		File songFile = songs.get(currentSong);
		try {
			modPlayer.play(songFile, audioRenderThread, audioPlayerThread);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void playPrev() {
		modPlayer.stop();
		modPlayer.waitForStop();
		currentSong--;
		if (currentSong<0) currentSong = songs.size()-1;
		
		play();
	}

	private void playNext() {
		modPlayer.stop();
		modPlayer.waitForStop();
		currentSong++;
		if (currentSong>=songs.size()) currentSong = 0;
		
		play();
	}
	
	private static String padz(int n) {
		if (n>99) n = 99;
		return n < 10 ? "0" + n : "" + n;
	}

	private static String pads(String s, int n) {
		s = "      " + s;
		return s.substring(s.length()-n);
	}

	private static String buildTime(int t) {
		t = t / 1000;
		int seconds = t % 60;
		int minutes = t / 60;
		
		return padz(minutes) + ":" + padz(seconds);
	}
	
	protected static void onFrameCallback() {
		if (modInfo == null) return;

		for(int i=0; i<modInfo.tracks; i++) {
			waveContainer.setWave(i, modPlayer.getWave(i));
		}
		
		FrameInfo frameInfo = modPlayer.getFrameInfo();
		
		String elapsed = buildTime(frameInfo.time);
		String length  = buildTime(frameInfo.totalTime);
		lcdTime.setText(elapsed);
		lcdLength.setText(length);
		
		String position = padz(frameInfo.position) + "/" + padz(modInfo.patterns);
		lcdPosition.setText(position);
		
		String tempo = pads(String.valueOf(frameInfo.bpm), 5);
		lcdTempo.setText(tempo);
	}
	
	private static String toFirstLetterUppercase(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}


	@Override
	protected void onStart() {
		super.onStart();
		
		audioPlayerThread = new AudioPlayerThread(waveDevice);
		audioRenderThread = new AudioRenderThread(waveDevice.getFreq(), 100, 4);
		audioPlayerThread.setAudioRenderThread(audioRenderThread);
		
		audioPlayerThread.start();
		audioRenderThread.start();
		
		play();
	}

	@Override
	protected void onStop() {
		super.onStop();
		modPlayer.stop();
		modPlayer.waitForStop();
		
		audioPlayerThread.shutdown();
		audioRenderThread.shutdown();
		try {
			audioPlayerThread.join();
			audioRenderThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
