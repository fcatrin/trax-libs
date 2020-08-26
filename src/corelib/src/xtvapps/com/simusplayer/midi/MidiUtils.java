package xtvapps.com.simusplayer.midi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MidiUtils {

	private static int readByte(SimpleStream is) throws IOException {
		int result = is.readByte();
		if (result < 0) throw new IOException("Unexpected EOF");
		
		return result;
	}
	
	public static long read32le(SimpleStream is) throws IOException {
		long value = readByte(is);
		value += readByte(is) << 8;
		value += readByte(is) << 16;
		value += readByte(is) << 24;
		return value;
	}
	
	public static long makeId(String id) {
		try {
			byte chars[] = id.getBytes("UTF-8");
			long value = chars[0];
			value += chars[1] << 8;
			value += chars[2] << 16;
			value += chars[3] << 24;
			return value;
		} catch (UnsupportedEncodingException e) {
			// should never happen
			e.printStackTrace();
			return 0;
		}
	}
	
	public static long readInt(SimpleStream is, int size) throws IOException {
		long value = 0;
		do {
			int c = readByte(is);
			value = value << 8 | c;
		} while (--size > 0);
		return value;
	}
	
	public static long readVar(SimpleStream is) throws IOException {
		long value = 0;
		int i = 0;

		int c;
		do {
			c = readByte(is);
			value = (value << 7) | (c & 0x7f);
		} while ((c & 0x80) != 0 && i++ < 4);
		return value;
	}
	
	public static String readString(SimpleStream is, int len) throws IOException {
		byte buffer[] = new byte[len];
		for(int i=0; i<len; i++) {
			buffer[i] = (byte)is.readByte();
		}
		return new String(buffer, "ASCII");
	}
	
}
