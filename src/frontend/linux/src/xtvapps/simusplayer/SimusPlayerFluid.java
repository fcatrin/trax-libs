package xtvapps.simusplayer;

import java.io.IOException;

import fts.core.Application;
import fts.core.Context;
import fts.core.DesktopLogger;
import fts.core.DesktopResourceLocator;
import fts.core.SimpleCallback;
import fts.core.Widget;
import fts.core.Window;
import fts.linux.ComponentFactory;
import xtvapps.simusplayer.core.FluidMidiThread;
import xtvapps.simusplayer.core.FluidPlayer;

public class SimusPlayerFluid {
	private static final String LOGTAG = SimusPlayer.class.getSimpleName();
	private static Window window;
	
	private static FluidPlayer fluidPlayer;

	public static void main(String[] args) throws IOException, InterruptedException {
		Application app = new Application(new ComponentFactory(), new DesktopResourceLocator(), new DesktopLogger(), new Context());
		

		window = Application.createWindow("Simus Midi Player", 480, 272);
		window.setOnFrameCallback(getOnFrameCallback());
		
		final Widget rootView = app.inflate(window, "modplayer");
		window.setContentView(rootView);
		
		fluidPlayer = new FluidPlayer(new DesktopWaveDevice(44100, 4096));
		
		window.open();
		
		fluidPlayer.play();
		FluidMidiThread fluidMidiThread = new FluidMidiThread("/home/fcatrin/tmp/canyon.mid");
		fluidMidiThread.start();
		window.mainLoop();
		fluidMidiThread.shutdown();
		fluidMidiThread.join();
		fluidPlayer.stop();
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
