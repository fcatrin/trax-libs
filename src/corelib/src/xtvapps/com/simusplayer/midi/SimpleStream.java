package xtvapps.com.simusplayer.midi;

import java.io.IOException;

public class SimpleStream {
	
	private byte[] data;
	int offset = 0;

	public SimpleStream(byte data[]) {
		this.data = data;
	}
	
	public int readByte() throws IOException {
		 if (offset >= data.length) throw new IOException("Unexpected EOF");
		 int value = data[offset];
		 if (value < 0) value += 256;
		 
		 offset ++;
		 
		 return value;
	}
	
	public void unreadByte() {
		offset--;
		checkOffset();
	}
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
		checkOffset();
	}
	
	public void skip(long n) {
		offset += (int)n;
		checkOffset();
	}
	
	private void checkOffset() {
		if (offset < 0) offset = 0;
	}
}
