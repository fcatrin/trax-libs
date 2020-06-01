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

	protected static void onFrameCallback() {
		for(int i=0; i<4; i++) {
			WaveWidget waveWidget = (WaveWidget)window.findWidget("waveBox" + i);
			waveWidget.setWave(modPlayer.getWave(i));
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
