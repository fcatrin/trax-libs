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
	private final static int NOTE_ON_MARGIN = 2;

	Color backgroundColor;
	Color whiteKeysColor;
	Color blackKeysColor;
	Color noteColor;
	
	float notes[] = {};
	
	public KeyboardView(Window window) {
		super(window);
		backgroundColor = new Color("#000000");
		whiteKeysColor  = new Color("#F0F0F0");
		blackKeysColor  = new Color("#202020");
		noteColor = new Color("#F0A0A0");
		notes = new float[] {1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1};
	}
	
	public void setNotes(float notes[]) {
		this.notes = notes;
	}

	@Override
	public void redraw() {
	}

	@Override
	protected void onPaint(PaintEvent e) {
		Canvas c = e.canvas;
		int widthPerNote = bounds.width / WHITE_NOTES;
		int left = bounds.x;

		c.setForeground(backgroundColor);
		c.drawFilledRect(bounds.x, bounds.y, bounds.width, bounds.height);
		
		int whiteNoteWidth = (widthPerNote - 1 - NOTE_ON_MARGIN * 2);
		for(int i=0; i<OCTAVES*12+1; i++) {
			int note_in_octave = i % 12;
			if (note_in_octave == 0 || note_in_octave == 2 || note_in_octave == 4 || note_in_octave == 5 || note_in_octave == 7 ||
				note_in_octave == 9 || note_in_octave == 11) {
				c.setForeground(whiteKeysColor);
				c.drawFilledRect(left, bounds.y, widthPerNote-1, bounds.height);
				
				boolean isNoteOn = i < notes.length && notes[i] > 0;
				if (isNoteOn) {
					c.setForeground(noteColor);
					c.drawFilledRect(left + NOTE_ON_MARGIN, bounds.y + bounds.height - NOTE_ON_MARGIN - whiteNoteWidth, whiteNoteWidth, whiteNoteWidth);
				}
				left += widthPerNote;
			}
		}
		
		left = bounds.x + widthPerNote / 2;
		int blackKeyWidth = (int)(widthPerNote * 0.8);
		int blackKeyHeight = (int)(bounds.height * 0.6);
		int blackNoteWidth = (blackKeyWidth - NOTE_ON_MARGIN * 2);
		for(int i=0; i<OCTAVES*12; i++) {
			int note_in_octave = i % 12;
			if (note_in_octave == 1 || note_in_octave == 3 || note_in_octave == 6 || note_in_octave == 8 || note_in_octave == 10) {
				c.setForeground(blackKeysColor);
				c.drawFilledRect(left, bounds.y, blackKeyWidth, blackKeyHeight);
				
				boolean isNoteOn = i < notes.length && notes[i] > 0;
				if (isNoteOn) {
					c.setForeground(noteColor);
					c.drawFilledRect(left + NOTE_ON_MARGIN, bounds.y + blackKeyHeight - NOTE_ON_MARGIN - blackNoteWidth, blackNoteWidth, blackNoteWidth);
				}
				
				left += widthPerNote * ((note_in_octave == 3 || note_in_octave == 10) ? 2 : 1);
			}
			
		}
		/*
		if (notes!=null) {
			int index = 0;
			int margin = (widthPerNote - NOTE_ON_SIZE) / 2;
			left = bounds.x + margin;
			
			for(int i=0; i<WHITE_NOTES && index < notes.length; i++) {
				if (notes[index] > 0) {
					c.drawFilledRect(left, bounds.y + bounds.height - NOTE_ON_SIZE - margin, NOTE_ON_SIZE, NOTE_ON_SIZE);
				}
				int note_in_octave = index % 12;
				index += note_in_octave == 4 || note_in_octave == 11 ? 1 : 2;
				i++;
				left += widthPerNote;
			}
		}
		*/
	}

	@Override
	public Point getContentSize(int width, int height) {
		// find int size within width
		int widthPerNote = width / WHITE_NOTES;
		int requiredWith = WHITE_NOTES * widthPerNote;
		return new Point(requiredWith, MIN_HEIGHT);
	}

}
