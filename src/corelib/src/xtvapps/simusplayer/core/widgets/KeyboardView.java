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

	Color backgroundColor;
	Color whiteKeysColor;
	Color blackKeysColor;
	
	public KeyboardView(Window window) {
		super(window);
		backgroundColor = new Color("#000000");
		whiteKeysColor  = new Color("#F0F0F0");
		blackKeysColor  = new Color("#202020");
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
		
		c.setForeground(whiteKeysColor);
		for(int i=0; i<WHITE_NOTES; i++) {
			c.drawFilledRect(left, bounds.y, widthPerNote-1, bounds.height);
			left += widthPerNote;
		}
		
		c.setForeground(blackKeysColor);
		left = bounds.x + widthPerNote / 2;
		int blackKeyWidth = (int)(widthPerNote * 0.8);
		int blackKeyHeight = (int)(bounds.height * 0.6);
		for(int i=0; i<OCTAVES; i++) {
			c.drawFilledRect(left, bounds.y, blackKeyWidth, blackKeyHeight);
			left += widthPerNote;
			c.drawFilledRect(left, bounds.y, blackKeyWidth, blackKeyHeight);
			left += widthPerNote*2;
			c.drawFilledRect(left, bounds.y, blackKeyWidth, blackKeyHeight);
			left += widthPerNote;
			c.drawFilledRect(left, bounds.y, blackKeyWidth, blackKeyHeight);
			left += widthPerNote;
			c.drawFilledRect(left, bounds.y, blackKeyWidth, blackKeyHeight);
			left += widthPerNote*2;
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
