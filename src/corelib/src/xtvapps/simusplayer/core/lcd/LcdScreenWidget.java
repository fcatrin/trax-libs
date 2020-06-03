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
	
	public void setPixelSize(int size) {
		LcdChar.pixel_size = size;
	}
	
	public void setPixelSpacing(int spacing) {
		LcdChar.pixel_spacing = spacing;
	}
	
	@Override
	protected Object resolvePropertyValue(String propertyName, String value) {
		if (propertyName.equals("onColor") || propertyName.equals("offColor")) {
			return resolvePropertyValueColor(propertyName, value);
		}
		
		if (propertyName.equals("pixelSize") || propertyName.equals("pixelSpacing")) {
			return resolvePropertyValueDimen(propertyName, value);
		}
		return super.resolvePropertyValue(propertyName, value);
	}

}
