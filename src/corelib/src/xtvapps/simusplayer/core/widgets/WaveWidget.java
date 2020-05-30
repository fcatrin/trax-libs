package xtvapps.simusplayer.core.widgets;

import fts.core.Widget;
import fts.core.Window;
import fts.events.PaintEvent;
import fts.graphics.Canvas;
import fts.graphics.Color;
import fts.graphics.Point;

public class WaveWidget extends Widget {

	int wave[];
	private Color lineColor;
	public WaveWidget(Window window) {
		super(window);
		lineColor = new Color("#333B50");
	}

	public void setWave(int wave[]) {
		this.wave = wave;
	}
	
	@Override
	public void redraw() {
	}

	@Override
	protected void onPaint(PaintEvent e) {
		Canvas canvas = e.canvas;
		if (background != null) {
			background.setBounds(bounds);
			background.draw(canvas);
		}
		
		if (wave== null) return;
		canvas.setForeground(lineColor);
		int px = bounds.x+2;
		int py = bounds.y+2 + (bounds.height-4) / 2;
		for(int x=0; x<64 && x<wave.length; x++) {
			int sample = wave[x] / 256 / 8;
			canvas.drawLine(px, py - sample, px, py - sample+1);
			px++;
		}

	}

	@Override
	public Point getContentSize(int width, int height) {
		return new Point(68, 68);
	}

}
