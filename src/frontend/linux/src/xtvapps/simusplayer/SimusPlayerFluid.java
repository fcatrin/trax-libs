package xtvapps.simusplayer;

import java.io.File;
import java.io.IOException;

import fts.core.Application;
import fts.core.Context;
import fts.core.DesktopLogger;
import fts.core.DesktopResourceLocator;
import fts.core.SimpleCallback;
import fts.core.Widget;
import fts.core.Window;
import fts.linux.ComponentFactory;
import xtvapps.simusplayer.core.CoreUtils;
import xtvapps.simusplayer.core.FluidPlayer;
import xtvapps.simusplayer.midi.MidiSong;
import xtvapps.simusplayer.midi.MidiTrack;
import xtvapps.simusplayer.midi.SimpleStream;

public class SimusPlayerFluid {
	private static final String LOGTAG = SimusPlayer.class.getSimpleName();
	private static Window window;
	
	private static FluidPlayer fluidPlayer;

	public static void main(String[] args) throws IOException {
		Application app = new Application(new ComponentFactory(), new DesktopResourceLocator(), new DesktopLogger(), new Context());
		
		byte[] songData = CoreUtils.loadBytes(new File("/home/fcatrin/tmp/canyon.mid"));
		MidiSong song = MidiSong.load(new SimpleStream(songData));
		for(MidiTrack track : song.getTracks()) {
			System.out.println(track.getName());
		}
		
		/*

		window = Application.createWindow("Simus Midi Player", 480, 272);
		window.setOnFrameCallback(getOnFrameCallback());
		
		final Widget rootView = app.inflate(window, "modplayer");
		window.setContentView(rootView);
		
		fluidPlayer = new FluidPlayer(new DesktopWaveDevice(44100, 1024));
		
		play();
		
		window.open();
		window.mainLoop();
		fluidPlayer.stop();
		*/
	}
	
	private static void play() {
		Thread t = new Thread() {
			public void run() {
				try {
					fluidPlayer.play("");
					Thread.sleep(5000);
					fluidPlayer.fluidNoteOn(0, 64, 100);
					Thread.sleep(20);
					fluidPlayer.fluidNoteOn(0, 71, 100);
					Thread.sleep(1000);
					fluidPlayer.fluidNoteOff(0, 64);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		t.start();
	}
	
	protected static void onFrameCallback() {
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
