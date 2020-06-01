package xtvapps.simusplayer;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import fts.core.Application;
import fts.core.Context;
import fts.core.DesktopLogger;
import fts.core.DesktopResourceLocator;
import fts.core.Log;
import fts.core.SimpleCallback;
import fts.core.Widget;
import fts.core.Window;
import fts.linux.ComponentFactory;
import xtvapps.simusplayer.core.AudioBuffer;
import xtvapps.simusplayer.core.ModPlayer;
import xtvapps.simusplayer.core.ModPlayer.FrameInfo;
import xtvapps.simusplayer.core.ModPlayer.ModInfo;
import xtvapps.simusplayer.core.ModPlayer.ModPlayerListener;
import xtvapps.simusplayer.core.widgets.WaveWidget;

public class SimusPlayerMod {
	private static final String LOGTAG = SimusPlayer.class.getSimpleName();
	private static final int SLEEP_TIME = 10;
	private static Window window;
	
	private static ModPlayer modPlayer;

	public static void main(String[] args) throws IOException {
		Application app = new Application(new ComponentFactory(), new DesktopResourceLocator(), new DesktopLogger(), new Context());
		
		window = Application.createWindow("Simus Mod Player", 640, 360);
		window.setOnFrameCallback(getOnFrameCallback());
		
		Widget rootView = app.inflate(window, "modplayer");
		window.setContentView(rootView);

		modPlayer = new ModPlayer(new DesktopWaveDevice(44100, 1024));
		modPlayer.setModPlayerListener(new ModPlayer.ModPlayerListener() {
			
			@Override
			public void onStart() {
				ModPlayer.ModInfo modInfo = modPlayer.getModInfo();
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
			}
			
			@Override
			public void onEnd() {
				Log.d(LOGTAG, "player ends");
			}
		});
		
		Thread t = new Thread() {
			public void run() {
				try {
					modPlayer.play("/home/fcatrin/git/retrobox/RetroBoxDroid/assets/music/bananasplit.mod");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		t.start();
		
		window.open();
		window.mainLoop();
		modPlayer.stop();
	}

	private static long t0 = 0;
	protected static void onFrameCallback() {
		for(int i=0; i<4; i++) {
			WaveWidget waveWidget = (WaveWidget)window.findWidget("waveBox" + i);
			waveWidget.setWave(modPlayer.getWave(i));
		}
		
		long t = System.currentTimeMillis();
		if (t - t0 > 200 ) {
			ModInfo modInfo = modPlayer.getModInfo();
			FrameInfo frameInfo = modPlayer.getFrameInfo();
			System.out.println("pos: " + frameInfo.position + "/" + modInfo.patterns + " spd:" + frameInfo.speed + " bpm:" + frameInfo.bpm);
			t0 = t;
		}
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
