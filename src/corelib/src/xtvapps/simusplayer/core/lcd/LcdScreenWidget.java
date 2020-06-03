package xtvapps.simusplayer.core.lcd;

import fts.core.Window;
import fts.graphics.Color;
import fts.widgets.LinearContainer;

public class LcdScreenWidget extends LinearContainer {

	public LcdScreenWidget(Window w) {
		super(w);
	}

	public void setOnColor(Color color) {
		LcdChar.setOnColor(color);
	}
	
	public void setOffColor(Color color) {
		LcdChar.setOffColor(color);
	}
	
	@Override
	protected Object resolvePropertyValue(String propertyName, String value) {
		if (propertyName.equals("onColor") || propertyName.equals("offColor")) {
			return resolvePropertyValueColor(propertyName, value);
		}
		return super.resolvePropertyValue(propertyName, value);
	}

}
