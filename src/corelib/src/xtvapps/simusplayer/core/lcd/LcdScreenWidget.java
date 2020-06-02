package xtvapps.simusplayer.core.lcd;

import fts.core.Container;
import fts.core.LayoutInfo;
import fts.core.Widget;
import fts.core.Window;
import fts.graphics.Color;
import fts.graphics.Point;

public class LcdScreenWidget extends Container {

	private LcdSegmentWidget lcdName;

	public LcdScreenWidget(Window w) {
		super(w);
		
		lcdName = new LcdSegmentWidget(w);
		lcdName.setLen(20);
		add(lcdName);
	}

	public void setOnColor(Color color) {
		LcdChar.setOnColor(color);
	}
	
	public void setOffColor(Color color) {
		LcdChar.setOffColor(color);
	}
	
	public void setName(String name) {
		lcdName.setText(name);
	}
	
	@Override
	public void layout() {
		int x = bounds.x + padding.left;
		int y = bounds.y + padding.top;
		
		LayoutInfo layoutInfo = lcdName.getLayoutInfo();
		lcdName.setBounds(x, y, layoutInfo.measuredWidth, layoutInfo.measuredHeight);
	}

	@Override
	public void onMeasureChildren(MeasureSpec wspec, MeasureSpec hspec) {
		for (Widget child : getChildren()) {
			child.onMeasure(wspec.value, hspec.value);
		}
	}

	@Override
	public Point getContentSize(int width, int height) {
		int paddingWidth = padding.left + padding.right;
		int paddingHeight = padding.top + padding.bottom;
		return lcdName.getContentSize(width + paddingWidth, height + paddingHeight);
	}

	@Override
	protected Object resolvePropertyValue(String propertyName, String value) {
		if (propertyName.equals("onColor") || propertyName.equals("offColor")) {
			return resolvePropertyValueColor(propertyName, value);
		}
		return super.resolvePropertyValue(propertyName, value);
	}

}
