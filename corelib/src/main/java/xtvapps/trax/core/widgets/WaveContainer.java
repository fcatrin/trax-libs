package xtvapps.trax.core.widgets;

import java.util.List;

import fts.core.Callback;
import fts.core.Container;
import fts.core.LayoutInfo;
import fts.core.Log;
import fts.core.Widget;
import fts.core.NativeWindow;
import fts.events.OnClickListener;
import fts.graphics.Color;
import fts.graphics.ColorListSelector;
import fts.graphics.Drawable;
import fts.graphics.Point;

public class WaveContainer extends Container {
	private static final int layouts[] = {
			2, 2, 1,
			4, 2, 2,
			6, 3, 2,
			8, 4, 2,
			10, 5, 2,
			12, 4, 3,
			14, 7, 2,
			16, 4, 4,
			18, 6, 3,
			20, 5, 4,
			22, 5, 4, // hope it doesn't happen
			24, 8, 3,
			26, 8, 3, // hope it doesn't happen
			28, 7, 4,
			30, 5, 6,
			32, 8, 4
	};

	int waves = 2;
	int spacing = 0;

	private Drawable waveBackground;
	
	private Callback<Integer> muteChannelCallback;

	private Color waveLinesColor;
	
	public WaveContainer(NativeWindow w) {
		super(w);
	}

	public void setWaves(int waves) {
		if (this.waves == waves) return;
		this.waves = waves;
		requestLayout();
	}
	
	public void setWaveBackground(Drawable waveBackground) {
		this.waveBackground = waveBackground;
	}

	public void setWaveLinesColor(ColorListSelector waveLinesColor) {
		this.waveLinesColor = waveLinesColor.getSelectedItem();
	}

	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}
	
	@Override
	public void layout() {
		int layoutBase = 0;
		for(int i=0; i<layouts.length; i+=3) {
			if (layouts[i] == waves) {
				layoutBase = i;
				break;
			}
		}
		
		removeAllChildren();
		
		int cols = layouts[layoutBase+1];
		int rows = layouts[layoutBase+2];
		
		Log.d("WaveContainer", String.format("waves:%d cols:%d rows:%d", waves, cols, rows));
		
		int availableWidth  = getInternalWidth()  - spacing * (cols-1);
		int availableHeight = getInternalHeight() - spacing * (rows-1);
		
		int waveWidth = availableWidth / cols;
		int waveHeight = availableHeight / rows;
		
		int y = bounds.y + padding.top;
		
		for(int row = 0; row < rows; row++) {
			int x = bounds.x + padding.left;
			for(int col = 0; col < cols; col++) {
				WaveWidget w = new WaveWidget(getWindow());
				w.setBackground(waveBackground);
				w.setWaveLinesColor(waveLinesColor);
				w.setBounds(x, y, waveWidth, waveHeight);
				add(w);

				final int channel = row * cols + col;

				w.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(Widget w) {
						if (muteChannelCallback!=null) muteChannelCallback.onResult(channel);
					}
				});
				
				x+= waveWidth + spacing;
				
			}
			y += waveHeight + spacing;
		}
	}
	
	public void setMuteChannelCallback(Callback<Integer> muteChannelCallback) {
		this.muteChannelCallback = muteChannelCallback;
	}

	public void setWave(int waveIndex, int wave[], boolean muted) {
		List<Widget> children = getChildren();
		
		if (waveIndex < children.size()) {
			WaveWidget w = (WaveWidget)children.get(waveIndex);
			w.setMuted(muted);
			w.setWave(wave);
		}
	}

	@Override
	public void onMeasureChildren(MeasureSpec w, MeasureSpec h) {
	}

	@Override
	public Point getContentSize(int width, int height) {
		LayoutInfo layoutInfo = getLayoutInfo();
		return new Point(layoutInfo.width, layoutInfo.height);
	}

	@Override
	protected Object resolvePropertyValue(String propertyName, String value) {
		if (propertyName.equals("spacing")) return resolvePropertyValueDimen(propertyName, value);
		if (propertyName.equals("waveBackground")) return resolveBackground(value);
		if (propertyName.equals("waveLinesColor")) return resolvePropertyValueColor(propertyName, value);
		return super.resolvePropertyValue(propertyName, value);
	}

}
