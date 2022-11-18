package xtvapps.trax.core.lcd;

import fts.core.NativeWindow;
import fts.graphics.ColorListSelector;
import fts.widgets.LinearContainer;

public class LcdScreenWidget extends LinearContainer {

	public LcdScreenWidget(NativeWindow w) {
		super(w);
	}

	public void setOnColor(ColorListSelector color) {
		LcdChar.setOnColor(color.getSelectedItem());
	}
	
	public void setOffColor(ColorListSelector color) {
		LcdChar.setOffColor(color.getSelectedItem());
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
