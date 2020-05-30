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
		lineColor = new Color("#FFFFFF");
	}

	public void setWave(int wave[]) {
		this.wave = wave;
	}
	
	@Override
	public void redraw() {
	}

	@Override
	protected void onPaint(PaintEvent e) {
		if (wave== null) return;
		Canvas canvas = e.canvas;
		canvas.setForeground(lineColor);
		int px = bounds.x;
		int py = bounds.y + bounds.height / 2;
		for(int x=0; x<64 && x<wave.length; x++) {
			int sample = wave[x] / 256 / 4;
			canvas.drawLine(px, py - sample, px, py + sample);
			px++;
		}

	}

	@Override
	public Point getContentSize(int width, int height) {
		return new Point(64, 64);
	}

}
