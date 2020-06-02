package xtvapps.simusplayer.core.lcd;

import java.util.HashMap;
import java.util.Map;

import fts.graphics.Canvas;
import fts.graphics.Color;
import fts.graphics.Point;

public class LcdChar {
	private static final Map<String, int[]> font = new HashMap<String, int[]>();
	
	static Color offColor;
	static Color onColor;
	
	static int pixel_cols = 5;
	static int pixel_rows = 7;
	
	static int pixel_size = 4;
	static int pixel_spacing = 1;
	
	static {
		initFont();
	}
	
	public static void setOnColor(Color onColor) {
		LcdChar.onColor  = onColor;
	}

	public static void setOffColor(Color offColor) {
		LcdChar.offColor = offColor;
	}

	public static void drawChar(Canvas c, int x, int y, int bitmap[]) {
		int px = x;
		
		for(int col = 0; col<pixel_cols; col++) {
			int bits = bitmap[col];
			int bit = 1;
			int py = y;
			
			for(int row = 0; row<pixel_rows; row++) {
				boolean on = (bits & bit) != 0;
				Color color = on ? onColor : offColor;
				c.setColor(color);
				c.drawFilledRect(px, py, pixel_size, pixel_size);
				py += pixel_size + pixel_spacing;
				
				bit = bit << 1;
			}
			px += pixel_size + pixel_spacing;
		}
	}
	
	public static void drawString(Canvas c, int x, int y, String s, int offset, int len) {
		int emptyBitmap[] = font.get(" ");
		for(int i=0; i<len; i++) {
			int index = offset + i;
			int bitmap[] = emptyBitmap;
			if (0 <= index && index < s.length()) {
				bitmap = font.get(s.substring(index, index+1));
				if (bitmap == null) bitmap = emptyBitmap;
			}
			
			drawChar(c, x, y, bitmap);
			x += (pixel_size + pixel_spacing) * (pixel_cols+1);
		}
	}
	
	public static Point getSize(int chars) {
		int pixelSize  = pixel_size + pixel_spacing;
		int charWidth  = pixel_cols * pixelSize - pixel_spacing;
		int charHeight = pixel_rows * pixelSize - pixel_spacing;
		
		int width = chars * (charWidth + pixelSize) - pixelSize;
		return new Point(width, charHeight);
	}
	
	
	private static void initFont() {
		// font definition from https://github.com/shingo45endo/sound-canvas-lcd
		
		font.put(" ", new int[]  {0b0000000, 0b0000000, 0b0000000, 0b0000000, 0b0000000});
		font.put("!", new int[]  {0b0000000, 0b0000000, 0b1011111, 0b0000000, 0b0000000});
		font.put("\"", new int[] {0b0000000, 0b0000111, 0b0000000, 0b0000111, 0b0000000});
		font.put("#", new int[]  {0b0010100, 0b1111111, 0b0010100, 0b1111111, 0b0010100});
		font.put("$", new int[]  {0b0100100, 0b0101010, 0b1111111, 0b0101010, 0b0010010});
		font.put("%", new int[]  {0b0100011, 0b0010011, 0b0001000, 0b1100100, 0b1100010});
		font.put("&", new int[]  {0b0110110, 0b1001001, 0b1010101, 0b0100010, 0b1010000});
		font.put("'", new int[]  {0b0000000, 0b0000101, 0b0000011, 0b0000000, 0b0000000});
		font.put("(", new int[]  {0b0000000, 0b0011100, 0b0100010, 0b1000001, 0b0000000});
		font.put(")", new int[]  {0b0000000, 0b1000001, 0b0100010, 0b0011100, 0b0000000});
		font.put("*", new int[]  {0b0010100, 0b0001000, 0b0111110, 0b0001000, 0b0010100});
		font.put("+", new int[]  {0b0001000, 0b0001000, 0b0111110, 0b0001000, 0b0001000});
		font.put(",", new int[]  {0b0000000, 0b1010000, 0b0110000, 0b0000000, 0b0000000});
		font.put("-", new int[]  {0b0001000, 0b0001000, 0b0001000, 0b0001000, 0b0001000});
		font.put(".", new int[]  {0b0000000, 0b1100000, 0b1100000, 0b0000000, 0b0000000});
		font.put("/", new int[]  {0b0100000, 0b0010000, 0b0001000, 0b0000100, 0b0000010});
		font.put("0", new int[]  {0b0111110, 0b1010001, 0b1001001, 0b1000101, 0b0111110});
		font.put("1", new int[]  {0b0000000, 0b1000010, 0b1111111, 0b1000000, 0b0000000});
		font.put("2", new int[]  {0b1000010, 0b1100001, 0b1010001, 0b1001001, 0b1000110});
		font.put("3", new int[]  {0b0100001, 0b1000001, 0b1000101, 0b1001011, 0b0110001});
		font.put("4", new int[]  {0b0011000, 0b0010100, 0b0010010, 0b1111111, 0b0010000});
		font.put("5", new int[]  {0b0100111, 0b1000101, 0b1000101, 0b1000101, 0b0111001});
		font.put("6", new int[]  {0b0111100, 0b1001010, 0b1001001, 0b1001001, 0b0110000});
		font.put("7", new int[]  {0b0000001, 0b1110001, 0b0001001, 0b0000101, 0b0000011});
		font.put("8", new int[]  {0b0110110, 0b1001001, 0b1001001, 0b1001001, 0b0110110});
		font.put("9", new int[]  {0b0000110, 0b1001001, 0b1001001, 0b0101001, 0b0011110});
		font.put(":", new int[]  {0b0000000, 0b0110110, 0b0110110, 0b0000000, 0b0000000});
		font.put(";", new int[]  {0b0000000, 0b1010110, 0b0110110, 0b0000000, 0b0000000});
		font.put("<", new int[]  {0b0000000, 0b0001000, 0b0010100, 0b0100010, 0b1000001});
		font.put("=", new int[]  {0b0010100, 0b0010100, 0b0010100, 0b0010100, 0b0010100});
		font.put(">", new int[]  {0b1000001, 0b0100010, 0b0010100, 0b0001000, 0b0000000});
		font.put("?", new int[]  {0b0000010, 0b0000001, 0b1010001, 0b0001001, 0b0000110});
		font.put("@", new int[]  {0b0110010, 0b1001001, 0b1111001, 0b1000001, 0b0111110});
		font.put("A", new int[]  {0b1111110, 0b0010001, 0b0010001, 0b0010001, 0b1111110});
		font.put("B", new int[]  {0b1111111, 0b1001001, 0b1001001, 0b1001001, 0b0110110});
		font.put("C", new int[]  {0b0111110, 0b1000001, 0b1000001, 0b1000001, 0b0100010});
		font.put("D", new int[]  {0b1111111, 0b1000001, 0b1000001, 0b0100010, 0b0011100});
		font.put("E", new int[]  {0b1111111, 0b1001001, 0b1001001, 0b1001001, 0b1000001});
		font.put("F", new int[]  {0b1111111, 0b0001001, 0b0001001, 0b0001001, 0b0000001});
		font.put("G", new int[]  {0b0111110, 0b1000001, 0b1001001, 0b1001001, 0b1111010});
		font.put("H", new int[]  {0b1111111, 0b0001000, 0b0001000, 0b0001000, 0b1111111});
		font.put("I", new int[]  {0b0000000, 0b1000001, 0b1111111, 0b1000001, 0b0000000});
		font.put("J", new int[]  {0b0100000, 0b1000000, 0b1000001, 0b0111111, 0b0000001});
		font.put("K", new int[]  {0b1111111, 0b0001000, 0b0010100, 0b0100010, 0b1000001});
		font.put("L", new int[]  {0b1111111, 0b1000000, 0b1000000, 0b1000000, 0b1000000});
		font.put("M", new int[]  {0b1111111, 0b0000010, 0b0001100, 0b0000010, 0b1111111});
		font.put("N", new int[]  {0b1111111, 0b0000100, 0b0001000, 0b0010000, 0b1111111});
		font.put("O", new int[]  {0b0111110, 0b1000001, 0b1000001, 0b1000001, 0b0111110});
		font.put("P", new int[]  {0b1111111, 0b0001001, 0b0001001, 0b0001001, 0b0000110});
		font.put("Q", new int[]  {0b0111110, 0b1000001, 0b1010001, 0b0100001, 0b1011110});
		font.put("R", new int[]  {0b1111111, 0b0001001, 0b0011001, 0b0101001, 0b1000110});
		font.put("S", new int[]  {0b1000110, 0b1001001, 0b1001001, 0b1001001, 0b0110001});
		font.put("T", new int[]  {0b0000001, 0b0000001, 0b1111111, 0b0000001, 0b0000001});
		font.put("U", new int[]  {0b0111111, 0b1000000, 0b1000000, 0b1000000, 0b0111111});
		font.put("V", new int[]  {0b0011111, 0b0100000, 0b1000000, 0b0100000, 0b0011111});
		font.put("W", new int[]  {0b0111111, 0b1000000, 0b0111000, 0b1000000, 0b0111111});
		font.put("X", new int[]  {0b1100011, 0b0010100, 0b0001000, 0b0010100, 0b1100011});
		font.put("Y", new int[]  {0b0000011, 0b0000100, 0b1111000, 0b0000100, 0b0000011});
		font.put("Z", new int[]  {0b1100001, 0b1010001, 0b1001001, 0b1000101, 0b1000011});
		font.put("[", new int[]  {0b0000000, 0b0000000, 0b1111111, 0b1000001, 0b1000001});
		font.put("\\", new int[] {0b0000010, 0b0000100, 0b0001000, 0b0010000, 0b0100000});
		font.put("]", new int[]  {0b1000001, 0b1000001, 0b1111111, 0b0000000, 0b0000000});
		font.put("^", new int[]  {0b0000100, 0b0000010, 0b0000001, 0b0000010, 0b0000100});
		font.put("_", new int[]  {0b1000000, 0b1000000, 0b1000000, 0b1000000, 0b1000000});
		font.put("`", new int[]  {0b0000000, 0b0000001, 0b0000010, 0b0000100, 0b0000000});
		font.put("a", new int[]  {0b0100000, 0b1010100, 0b1010100, 0b1010100, 0b1111000});
		font.put("b", new int[]  {0b1111111, 0b1001000, 0b1000100, 0b1000100, 0b0111000});
		font.put("c", new int[]  {0b0111000, 0b1000100, 0b1000100, 0b1000100, 0b0100000});
		font.put("d", new int[]  {0b0111000, 0b1000100, 0b1000100, 0b1001000, 0b1111111});
		font.put("e", new int[]  {0b0111000, 0b1010100, 0b1010100, 0b1010100, 0b0011000});
		font.put("f", new int[]  {0b0001000, 0b1111110, 0b0001001, 0b0000001, 0b0000010});
		font.put("g", new int[]  {0b0001100, 0b1010010, 0b1010010, 0b1010010, 0b0111110});
		font.put("h", new int[]  {0b1111111, 0b0001000, 0b0000100, 0b0000100, 0b1111000});
		font.put("i", new int[]  {0b0000000, 0b1000100, 0b1111101, 0b1000000, 0b0000000});
		font.put("j", new int[]  {0b0100000, 0b1000000, 0b1000100, 0b0111101, 0b0000000});
		font.put("k", new int[]  {0b1111111, 0b0010000, 0b0101000, 0b1000100, 0b0000000});
		font.put("l", new int[]  {0b0000000, 0b1000001, 0b1111111, 0b1000000, 0b0000000});
		font.put("m", new int[]  {0b1111100, 0b0000100, 0b0011000, 0b0000100, 0b1111000});
		font.put("n", new int[]  {0b1111100, 0b0001000, 0b0000100, 0b0000100, 0b1111000});
		font.put("o", new int[]  {0b0111000, 0b1000100, 0b1000100, 0b1000100, 0b0111000});
		font.put("p", new int[]  {0b1111100, 0b0010100, 0b0010100, 0b0010100, 0b0001000});
		font.put("q", new int[]  {0b0001000, 0b0010100, 0b0010100, 0b0011000, 0b1111100});
		font.put("r", new int[]  {0b1111100, 0b0001000, 0b0000100, 0b0000100, 0b0001000});
		font.put("s", new int[]  {0b1001000, 0b1010100, 0b1010100, 0b1010100, 0b0100000});
		font.put("t", new int[]  {0b0000100, 0b0111111, 0b1000100, 0b1000000, 0b0100000});
		font.put("u", new int[]  {0b0111100, 0b1000000, 0b1000000, 0b0100000, 0b1111100});
		font.put("v", new int[]  {0b0011100, 0b0100000, 0b1000000, 0b0100000, 0b0011100});
		font.put("w", new int[]  {0b0111100, 0b1000000, 0b0110000, 0b1000000, 0b0111100});
		font.put("x", new int[]  {0b1000100, 0b0101000, 0b0010000, 0b0101000, 0b1000100});
		font.put("y", new int[]  {0b0001100, 0b1010000, 0b1010000, 0b1010000, 0b0111100});
		font.put("z", new int[]  {0b1000100, 0b1100100, 0b1010100, 0b1001100, 0b1000100});
		font.put("{", new int[]  {0b0000000, 0b0001000, 0b0110110, 0b1000001, 0b0000000});
		font.put("|", new int[]  {0b0000000, 0b0000000, 0b1111111, 0b0000000, 0b0000000});
		font.put("}", new int[]  {0b0000000, 0b1000001, 0b0110110, 0b0001000, 0b0000000});
		font.put("~", new int[]  {0b0010000, 0b0001000, 0b0001000, 0b0010000, 0b0001000});
		font.put("\u00a5", new int[] {0b0010101, 0b0010110, 0b1111100, 0b0010110, 0b0010101}); // YEN SIGN
		font.put("\u00b1", new int[] {0b1000100, 0b1000100, 0b1011111, 0b1000100, 0b1000100}); // PLUS-MINUS SIGN
		font.put("\u2161", new int[] {0b1000001, 0b1111111, 0b1000001, 0b1111111, 0b1000001}); // ROMAN NUMERAL TWO
	}
}
