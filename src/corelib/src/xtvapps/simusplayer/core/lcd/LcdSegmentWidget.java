package xtvapps.simusplayer.core.lcd;

import fts.core.Widget;
import fts.core.Window;
import fts.events.PaintEvent;
import fts.graphics.Canvas;
import fts.graphics.Point;

public class LcdSegmentWidget extends Widget {
	
	String text;
	int len;
	
	public LcdSegmentWidget(Window window) {
		super(window);
	}
	
	public void setLen(int len) {
		this.len = len;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	@Override
	protected void onPaint(PaintEvent e) {
		Canvas canvas = e.canvas;
		int x = bounds.x;
		int y = bounds.y;
		LcdChar.drawString(canvas, x, y, text);
	}

	@Override
	public Point getContentSize(int width, int height) {
		return LcdChar.getSize(len);
	}

	@Override
	public void redraw() {}

}
