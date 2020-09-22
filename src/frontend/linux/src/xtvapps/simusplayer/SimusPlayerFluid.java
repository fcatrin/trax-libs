package xtvapps.simusplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fts.core.Application;
import fts.core.Context;
import fts.core.DesktopLogger;
import fts.core.DesktopResourceLocator;
import fts.core.SimpleCallback;
import fts.core.Widget;
import fts.core.Window;
import fts.events.OnClickListener;
import fts.linux.ComponentFactory;
import fts.widgets.ButtonWidget;
import xtvapps.simusplayer.core.FluidMidiThread;
import xtvapps.simusplayer.core.FluidPlayer;
import xtvapps.simusplayer.core.lcd.LcdSegmentWidget;

public class SimusPlayerFluid {
	private static final String LOGTAG = SimusPlayer.class.getSimpleName();
	private static Window window;
	
	private static FluidPlayer fluidPlayer;

	private static List<File> songs = new ArrayList<File>();
	private static int currentSong = 0;
	private static LcdSegmentWidget lcdModName;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		File dir = new File("/opt/songs/midi");
		File[] modFiles = dir.listFiles();
		for(File modFile : modFiles) {
			if (modFile.isFile()) songs.add(modFile);
		}
		
		Application app = new Application(new ComponentFactory(), new DesktopResourceLocator(), new DesktopLogger(), new Context());
		

		window = Application.createWindow("Simus Midi Player", 480, 272);
		window.setOnFrameCallback(getOnFrameCallback());
		
		final Widget rootView = app.inflate(window, "modplayer");
		window.setContentView(rootView);

		lcdModName = (LcdSegmentWidget)rootView.findWidget("lcdModName");

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

		
		fluidPlayer = new FluidPlayer(new DesktopWaveDevice(44100, 4096));
		
		window.open();
		
		play();
		window.mainLoop();
		fluidPlayer.stop();
	}
	
	private static void play() {
		try {
			File songFile = songs.get(currentSong);
			fluidPlayer.play(songFile);
			
			lcdModName.setText(songFile.getName());

		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	private static void playPrev() {
		fluidPlayer.stop();
		fluidPlayer.waitForStop();
		currentSong--;
		if (currentSong<0) currentSong = songs.size()-1;
		
		play();
	}

	private static void playNext() {
		fluidPlayer.stop();
		fluidPlayer.waitForStop();
		currentSong++;
		if (currentSong>=songs.size()) currentSong = 0;
		
		play();
	}

}
