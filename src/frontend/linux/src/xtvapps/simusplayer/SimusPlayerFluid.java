package xtvapps.simusplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fts.core.Widget;
import fts.events.OnClickListener;
import fts.linux.Window;
import fts.widgets.ButtonWidget;
import xtvapps.simusplayer.core.FluidPlayer;
import xtvapps.simusplayer.core.audio.AudioPlayerThread;
import xtvapps.simusplayer.core.audio.AudioRenderThread;
import xtvapps.simusplayer.core.lcd.LcdSegmentWidget;

public class SimusPlayerFluid extends Window {

	private static final String LOGTAG = SimusPlayer.class.getSimpleName();
	
	private FluidPlayer fluidPlayer;

	private List<File> songs = new ArrayList<File>();
	private int currentSong = 0;
	private LcdSegmentWidget lcdModName;
	
	private AudioPlayerThread audioPlayerThread;
	private AudioRenderThread audioRenderThread;

	public SimusPlayerFluid(String title, int width, int height) {
		super(title, width, height);
	}
	
	@Override
	public void onCreate() {
		Widget rootView = inflate("modplayer");
		setContentView(rootView);

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

		loadSongs();		
	}

	private void loadSongs() {
		File dir = new File("/opt/songs/midi");
		File[] modFiles = dir.listFiles();
		for(File modFile : modFiles) {
			if (modFile.isFile()) songs.add(modFile);
		}
	}
	
	@Override
	public void onStart() {
		DesktopWaveDevice waveDevice = new DesktopWaveDevice(44100, 4096);
		fluidPlayer = new FluidPlayer(waveDevice);
		
		audioPlayerThread = new AudioPlayerThread(waveDevice);
		audioRenderThread = new AudioRenderThread(waveDevice.getFreq(), 100, 4);
		audioPlayerThread.setAudioRenderThread(audioRenderThread);
		
		audioPlayerThread.start();
		audioRenderThread.start();
		
		play();
	}

	@Override
	public void onStop() {
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

	private void play() {
		File songFile = songs.get(currentSong);
		fluidPlayer.play(songFile, audioRenderThread, audioPlayerThread);
		
		lcdModName.setText(songFile.getName());
	}
	
	
	private void playPrev() {
		fluidPlayer.stop();
		fluidPlayer.waitForStop();
		currentSong--;
		if (currentSong<0) currentSong = songs.size()-1;
		
		play();
	}

	private void playNext() {
		fluidPlayer.stop();
		fluidPlayer.waitForStop();
		currentSong++;
		if (currentSong>=songs.size()) currentSong = 0;
		
		play();
	}

	public static void main(String[] args) throws IOException, InterruptedException {
	
		SimusPlayerFluid fluidPlayer = new SimusPlayerFluid("Simus Midi Player", 480, 272);
		fluidPlayer.run();
	}

}
