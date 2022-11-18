package xtvapps.trax.core.widgets;

import fts.core.Widget;
import fts.core.NativeWindow;
import fts.events.PaintEvent;
import fts.graphics.Canvas;
import fts.graphics.Color;
import fts.graphics.Point;
import fts.graphics.Rectangle;
import xtvapps.trax.core.lcd.LcdChar;

public class WaveWidget extends Widget {

	int wave[];
	private Color waveLinesColor;
	boolean muted;
	
	public WaveWidget(NativeWindow window) {
		super(window);
		setClickable(true);
	}

	public void setWave(int wave[]) {
		this.wave = wave;
		invalidate();
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
		invalidate();
	}

	public void setWaveLinesColor(Color waveLinesColor) {
		this.waveLinesColor = waveLinesColor;
	}

	@Override
	protected void onPaint(PaintEvent e) {
		Canvas canvas = e.canvas;
		Rectangle paintBounds = getPaintBounds();
		if (background != null) {
			background.setBounds(paintBounds);
			background.draw(canvas);
		}

		if (wave == null) return;
		canvas.setColor(waveLinesColor);
		int px = 0;
		int py = bounds.height / 2;
		
		float points = (bounds.width + LcdChar.pixel_size + LcdChar.pixel_spacing) / (LcdChar.pixel_size + LcdChar.pixel_spacing);
		float steps = points > wave.length ? (float)wave.length / points : 1;
		
		if (muted) {
			for(int x=0; x<bounds.width && px < bounds.width; x++) {
				px += LcdChar.pixel_size + LcdChar.pixel_spacing;
			}
		} else {
			for(int x=0; x<bounds.width && px < bounds.width; x++) {
				float sample = wave[(int)(x * steps)] / 65536f;
				int h = (int)(bounds.height * sample);
				canvas.drawFilledRect(px, py - h, LcdChar.pixel_size, h);
				px += LcdChar.pixel_size + LcdChar.pixel_spacing;
			}
			
		}
	}

	@Override
	public Point getContentSize(int width, int height) {
		return new Point(0, 0);
	}

}
