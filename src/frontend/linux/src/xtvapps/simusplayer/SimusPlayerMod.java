package xtvapps.simusplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fts.core.Application;
import fts.core.Callback;
import fts.core.Context;
import fts.core.DesktopLogger;
import fts.core.DesktopResourceLocator;
import fts.core.Log;
import fts.core.SimpleCallback;
import fts.core.Utils;
import fts.core.Widget;
import fts.core.Window;
import fts.events.OnClickListener;
import fts.linux.ComponentFactory;
import fts.widgets.ButtonWidget;
import xtvapps.simusplayer.core.ModPlayer;
import xtvapps.simusplayer.core.ModPlayer.FrameInfo;
import xtvapps.simusplayer.core.ModPlayer.ModInfo;
import xtvapps.simusplayer.core.lcd.LcdScreenWidget;
import xtvapps.simusplayer.core.lcd.LcdSegmentWidget;
import xtvapps.simusplayer.core.widgets.WaveContainer;

public class SimusPlayerMod {
	private static final String LOGTAG = SimusPlayer.class.getSimpleName();
	private static final int SLEEP_TIME = 10;
	private static Window window;
	
	private static ModPlayer modPlayer;
	private static ModInfo modInfo;
	private static WaveContainer waveContainer;
	private static LcdSegmentWidget lcdTime;
	private static LcdSegmentWidget lcdLength;
	private static LcdSegmentWidget lcdPosition;
	private static LcdSegmentWidget lcdTempo;

	private static List<String> songs = new ArrayList<String>();
	private static int currentSong = 0;

	public static void main(String[] args) throws IOException {
		songs.add("/home/fcatrin/tmp/mods/elimination.mod");
		songs.add("/home/fcatrin/tmp/mods/bloodm.mod");
		songs.add("/home/fcatrin/tmp/mods/bananasplit.mod");
		songs.add("/home/fcatrin/tmp/mods/devlpr94.xm");
		songs.add("/home/fcatrin/tmp/mods/m4v-fasc.it");
		
		Application app = new Application(new ComponentFactory(), new DesktopResourceLocator(), new DesktopLogger(), new Context());

		// final File modFile = new File("/home/fcatrin/tmp/mods/bananasplit.mod"); // 4 channels
		// final File modFile = new File("/home/fcatrin/tmp/mods/beyond_music.mod"); // 4 channels
		// final File modFile = new File("/home/fcatrin/tmp/mods/bloodm.mod"); // 6 channels
		// final File modFile = new File("/home/fcatrin/tmp/mods/chi.mod"); // 4 chn
		// final File modFile = new File("/home/fcatrin/tmp/mods/devlpr94.xm");  // 10 chn
		// final File modFile = new File("/home/fcatrin/tmp/mods/dirt.mod"); // 4 chn
		// final File modFile = new File("/home/fcatrin/tmp/mods/elimination.mod"); // 4 chn
		// final File modFile = new File("/home/fcatrin/tmp/mods/greenochrome.xm"); // 8 chn
		// final File modFile = new File("/home/fcatrin/tmp/mods/m4v-fasc.it"); // 18 chn
		// final File modFile = new File("/home/fcatrin/tmp/mods/mindstorm.mod"); // 4 chn
		/*
		final File modFile = new File("/home/fcatrin/tmp/mods/satellite.s3m"); // 8 chn
		
		if (!modFile.exists()) {
			Log.d(LOGTAG, modFile + " not found");
			return;
		}
		*/
		
		window = Application.createWindow("Simus Mod Player", 480, 272);
		window.setOnFrameCallback(getOnFrameCallback());
		
		final Widget rootView = app.inflate(window, "modplayer");
		window.setContentView(rootView);
		
		waveContainer = (WaveContainer)rootView.findWidget("waves");
		
		lcdTime     = (LcdSegmentWidget)rootView.findWidget("lcdTime");
		lcdLength   = (LcdSegmentWidget)rootView.findWidget("lcdLength");
		lcdPosition = (LcdSegmentWidget)rootView.findWidget("lcdPosition");
		lcdTempo    = (LcdSegmentWidget)rootView.findWidget("lcdTempo");
		
		ButtonWidget btnPrev = (ButtonWidget)rootView.findWidget("btnPrev");
		ButtonWidget btnNext = (ButtonWidget)rootView.findWidget("btnNext");
		
		btnPrev.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(Widget w) {
				playPrev();
			}
		});

		btnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(Widget w) {
				playNext();
			}
		});

		modPlayer = new ModPlayer(new DesktopWaveDevice(44100, 4096));
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
				
				LcdSegmentWidget lcdModName = (LcdSegmentWidget)rootView.findWidget("lcdModName");
				lcdModName.setText(toFirstLetterUppercase(modName));
				waveContainer.setWaves(modInfo.tracks);
			}
			
			@Override
			public void onEnd() {
				Log.d(LOGTAG, "player ends");
			}
		});
		

		waveContainer.setMuteChannelCallback(new Callback<Integer>() {
			
			@Override
			public void onResult(Integer channel) {
				modPlayer.toggleChannel(channel);
			}
		});
		
		play();
		
		window.open();
		window.mainLoop();
		modPlayer.stop();
	}
	
	private static void play() {
		play(songs.get(currentSong));
	}
	
	private static void play(final String fileName) {
		Thread t = new Thread() {
			public void run() {
				try {
					modPlayer.play(fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		t.start();
	}
	
	private static void playPrev() {
		modPlayer.stop();
		modPlayer.waitForStop();
		currentSong--;
		if (currentSong<0) currentSong = songs.size()-1;
		
		play();
	}

	private static void playNext() {
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
	
	static int frameskip = 2;
	static int frames = 0;
	
	protected static void onFrameCallback() {
		if (modInfo == null) return;

		if (frames != frameskip) {
			frames++;
			return;
		}
		
		frames = 0;
		
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
	
	private static SimpleCallback getOnFrameCallback() {
		return new SimpleCallback() {
			
			@Override
			public void onResult() {
				onFrameCallback();
			}
		};
	}

}
