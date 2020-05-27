package xtvapps.simusplayer;

import fts.core.Application;
import fts.core.Context;
import fts.core.DesktopResourceLocator;
import fts.core.SimpleCallback;
import fts.core.Widget;
import fts.core.Window;
import fts.linux.ComponentFactory;
import xtvapps.simusplayer.core.widgets.KeyboardView;

public class SimusPlayer {

	private static int songHandle;

	private static Window window;
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("missing midi file name");
			return;
		}
		
		Application app = new Application(new ComponentFactory(), new DesktopResourceLocator(), new Context());
		window = Application.createWindow("Simus Player", (7*8 + 1)*14, 80);
		window.setOnFrameCallback(getOnFrameCallback());
		
		Widget rootView = app.inflate(window, "main");
		window.setContentView(rootView);
		
		NativeInterface.alsaInit();

		songHandle = NativeInterface.midiLoad(args[0]);
		System.out.println(songHandle);
		
		int nTracks = NativeInterface.midiGetTracksCount(songHandle);
		for(int i=0; i<nTracks; i++) {
			String trackName = NativeInterface.midiGetTrackName(songHandle, i);
			System.out.println(String.format("Track %d: %s", i, trackName));
		}

		System.out.println(" Port    Client name                      Port name");
		int ports = NativeInterface.alsaGetPortsCount();
		for(int port=0; port<ports; port++) {
			int    ids[]   = NativeInterface.alsaGetPortIds(port);
			String names[] = NativeInterface.alsaGetPortNames(port);
			
			System.out.println(String.format("%3d:%-3d  %-32.32s %s", ids[0], ids[1], names[0], names[1]));
			
		}

		System.out.flush();
		
		Thread t = new Thread() {
			@Override
			public void run() {
				NativeInterface.alsaConnectPort(2);
				NativeInterface.midiPlay(songHandle);
				NativeInterface.alsaDone();
			}
		};
		
		t.start();

		window.open();
		window.mainLoop();
		NativeInterface.midiStop();

	}
	
	private static void onFrame() {
		int[] notes = NativeInterface.midiGetNotes(songHandle, 1);
		KeyboardView keyboard = (KeyboardView)window.findWidget("keyboard");
		keyboard.setNotes(notes);
	}
	
	private static SimpleCallback getOnFrameCallback() {
		return new SimpleCallback() {

			@Override
			public void onResult() {
				onFrame();
			}
		};
	}

}
