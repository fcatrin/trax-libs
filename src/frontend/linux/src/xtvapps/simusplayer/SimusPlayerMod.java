package xtvapps.simusplayer;

import java.io.File;
import java.io.IOException;

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
import fts.linux.ComponentFactory;
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

	public static void main(String[] args) throws IOException {

		Application app = new Application(new ComponentFactory(), new DesktopResourceLocator(), new DesktopLogger(), new Context());

		// final File modFile = new File("/home/fcatrin/tmp/mods/greenochrome.xm");
		// final File modFile = new File("/home/fcatrin/tmp/mods/m4v-fasc.it");
		// final File modFile = new File("/home/fcatrin/tmp/mods/mindstorm.mod");
		final File modFile = new File("/home/fcatrin/tmp/mods/satellite.s3m");
		
		// modPlayer.play("/home/fcatrin/tmp/mods/bananasplit.mod");
		// modPlayer.play("/home/fcatrin/tmp/mods/beyond_music.mod");
		// modPlayer.play("/home/fcatrin/tmp/mods/bloodm.mod");
		// modPlayer.play("/home/fcatrin/tmp/mods/chi.mod");
		// modPlayer.play("/home/fcatrin/tmp/mods/devlpr94.xm");
		// modPlayer.play("/home/fcatrin/tmp/mods/dirt.mod");
		// modPlayer.play("/home/fcatrin/tmp/mods/elimination.mod");
		if (!modFile.exists()) {
			Log.d(LOGTAG, modFile + " not found");
			return;
		}
		
		window = Application.createWindow("Simus Mod Player", 480, 272);
		window.setOnFrameCallback(getOnFrameCallback());
		
		final Widget rootView = app.inflate(window, "modplayer");
		window.setContentView(rootView);
		
		waveContainer = (WaveContainer)rootView.findWidget("waves");
		
		lcdTime     = (LcdSegmentWidget)rootView.findWidget("lcdTime");
		lcdLength   = (LcdSegmentWidget)rootView.findWidget("lcdLength");
		lcdPosition = (LcdSegmentWidget)rootView.findWidget("lcdPosition");
		lcdTempo    = (LcdSegmentWidget)rootView.findWidget("lcdTempo");

		modPlayer = new ModPlayer(new DesktopWaveDevice(44100, 1024));
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
				
				String modName = Utils.isEmptyString(modInfo.modName) ? modFile.getName() : modInfo.modName;
				
				LcdSegmentWidget lcdModName = (LcdSegmentWidget)rootView.findWidget("lcdModName");
				lcdModName.setText(toFirstLetterUppercase(modName));
				waveContainer.setWaves(modInfo.tracks);
			}
			
			@Override
			public void onEnd() {
				Log.d(LOGTAG, "player ends");
			}
		});
		
		Thread t = new Thread() {
			public void run() {
				try {
					modPlayer.play(modFile.getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		t.start();

		waveContainer.setMuteChannelCallback(new Callback<Integer>() {
			
			@Override
			public void onResult(Integer channel) {
				modPlayer.toggleChannel(channel);
			}
		});
		
		window.open();
		window.mainLoop();
		modPlayer.stop();
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
	
	private static SimpleCallback getOnFrameCallback() {
		return new SimpleCallback() {
			
			@Override
			public void onResult() {
				onFrameCallback();
			}
		};
	}

}
