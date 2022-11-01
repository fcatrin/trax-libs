package xtvapps.simusplayer.core.lcd;

import fts.core.Widget;
import fts.core.NativeWindow;
import fts.events.PaintEvent;
import fts.graphics.Canvas;
import fts.graphics.Point;

public class LcdSegmentWidget extends Widget {
	
	String text;
	int len;
	
	public LcdSegmentWidget(NativeWindow window) {
		super(window);
	}
	
	public void setLen(int len) {
		this.len = len;
	}
	
	public void setText(String text) {
		if (this.text == text) return;  // fast check for null values on both states
		
		boolean mustInvalidate = 
				(text!=null && !text.equals(this.text)) ||
				(this.text!=null && !this.text.equals(text));		
		this.text = text;
		if (mustInvalidate)	invalidate();
	}

	@Override
	protected void onPaint(PaintEvent e) {
		Canvas canvas = e.canvas;
		LcdChar.drawString(canvas, 0, 0, text, 0, len);
	}

	@Override
	public Point getContentSize(int width, int height) {
		return LcdChar.getSize(len);
	}

	@Override
	protected Object resolvePropertyValue(String propertyName, String value) {
		if (propertyName.equals("len")) {
			return resolvePropertyValueInt(propertyName, value);
		}
		return super.resolvePropertyValue(propertyName, value);
	}
}
