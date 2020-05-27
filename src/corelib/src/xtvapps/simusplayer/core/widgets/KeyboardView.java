package xtvapps.simusplayer.core.widgets;

import fts.core.Widget;
import fts.core.Window;
import fts.events.PaintEvent;
import fts.graphics.Canvas;
import fts.graphics.Color;
import fts.graphics.Point;

public class KeyboardView extends Widget {
	private final static int NOTES_PER_OCTAVE = 7;
	private final static int OCTAVES = 8;
	private final static int MIN_HEIGHT = 10;
	private final static int WHITE_NOTES = OCTAVES * NOTES_PER_OCTAVE + 1;
	private final static int FIRST_NOTE = 24;

	Color backgroundColor;
	Color whiteKeysColor;
	Color blackKeysColor;
	Color whiteNoteColor;
	Color blackNoteColor;
	
	int notes[] = {};
	
	public KeyboardView(Window window) {
		super(window);
		backgroundColor = new Color("#000000");
		whiteKeysColor  = new Color("#F0F0F0");
		blackKeysColor  = new Color("#202020");
		whiteNoteColor = new Color("#6E9DCD");
		blackNoteColor = new Color("#65BD1F");
	}
	
	public void setNotes(int notes[]) {
		this.notes = notes;
	}

	@Override
	public void redraw() {
	}

	private boolean isBlackNote(int note) {
		int note_in_octave = note % 12;
		return note_in_octave == 1 || note_in_octave == 3 || note_in_octave == 6 || note_in_octave == 8 || note_in_octave == 10;
	}
	
	@Override
	protected void onPaint(PaintEvent e) {
		Canvas c = e.canvas;
		int widthPerNote = bounds.width / WHITE_NOTES;
		int left = bounds.x;

		c.setForeground(backgroundColor);
		c.drawFilledRect(bounds.x, bounds.y, bounds.width, bounds.height);
		
		for(int i=0; i<OCTAVES*12+1; i++) {
			if (!isBlackNote(i)) {
				int noteIndex = i + FIRST_NOTE;
				boolean isNoteOn = notes!=null && noteIndex < notes.length && notes[noteIndex] > 0;
				c.setForeground(isNoteOn ? whiteNoteColor : whiteKeysColor);
				c.drawFilledRect(left, bounds.y, widthPerNote-1, bounds.height);
				left += widthPerNote;
			}
		}
		
		left = bounds.x + widthPerNote / 2;
		int blackKeyWidth = (int)(widthPerNote * 0.8);
		int blackKeyHeight = (int)(bounds.height * 0.6);
		for(int i=0; i<OCTAVES*12; i++) {
			if (isBlackNote(i)) {
				int noteIndex = i + FIRST_NOTE;
				boolean isNoteOn = notes!=null && noteIndex < notes.length && notes[noteIndex] > 0;
				c.setForeground(isNoteOn ? blackNoteColor : blackKeysColor);
				c.drawFilledRect(left, bounds.y, blackKeyWidth, blackKeyHeight);

				int note_in_octave = i % 12;
				left += widthPerNote * ((note_in_octave == 3 || note_in_octave == 10) ? 2 : 1);
			}
		}
	}

	@Override
	public Point getContentSize(int width, int height) {
		// find int size within width
		int widthPerNote = width / WHITE_NOTES;
		int requiredWith = WHITE_NOTES * widthPerNote;
		return new Point(requiredWith, MIN_HEIGHT);
	}

}
