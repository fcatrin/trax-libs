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
import fts.core.NativeWindow;
import fts.events.OnClickListener;
import fts.linux.ComponentFactory;
import fts.widgets.ButtonWidget;
import xtvapps.simusplayer.core.FluidPlayer;
import xtvapps.simusplayer.core.audio.AudioPlayerThread;
import xtvapps.simusplayer.core.audio.AudioRenderThread;
import xtvapps.simusplayer.core.lcd.LcdSegmentWidget;

public class SimusPlayerFluid {
	private static final String LOGTAG = SimusPlayer.class.getSimpleName();
	private static NativeWindow window;
	
	private static FluidPlayer fluidPlayer;

	private static List<File> songs = new ArrayList<File>();
	private static int currentSong = 0;
	private static LcdSegmentWidget lcdModName;
	
	private static AudioPlayerThread audioPlayerThread;
	private static AudioRenderThread audioRenderThread;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		File dir = new File("/opt/songs/midi");
		File[] modFiles = dir.listFiles();
		for(File modFile : modFiles) {
			if (modFile.isFile()) songs.add(modFile);
		}
		
		Application app = new Application(new ComponentFactory(), new DesktopResourceLocator(), new DesktopLogger(), new Context());
		

		window = Application.createNativeWindow("Simus Midi Player", 480, 272);
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

		
		window.open();
		
		onStart();
		window.mainLoop();
		onStop();
	}
	
	private static void onStart() {
		DesktopWaveDevice waveDevice = new DesktopWaveDevice(44100, 4096);
		fluidPlayer = new FluidPlayer(waveDevice);
		
		audioPlayerThread = new AudioPlayerThread(waveDevice);
		audioRenderThread = new AudioRenderThread(waveDevice.getFreq(), 100, 4);
		audioPlayerThread.setAudioRenderThread(audioRenderThread);
		
		audioPlayerThread.start();
		audioRenderThread.start();
		
		play();

	}

	private static void onStop() {
		fluidPlayer.stop();
		audioPlayerThread.shutdown();
		audioRenderThread.shutdown();
		try {
			audioPlayerThread.join();
			audioRenderThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		fluidPlayer.shutdown();
	}

	private static void play() {
		try {
			File songFile = songs.get(currentSong);
			fluidPlayer.play(songFile, audioRenderThread, audioPlayerThread);
			
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
