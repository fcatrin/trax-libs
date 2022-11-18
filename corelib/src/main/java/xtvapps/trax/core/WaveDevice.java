package xtvapps.trax.core;

public abstract class WaveDevice {
	int freq;
	int bufferSize;
	
	public WaveDevice(int freq, int bufferSize) {
		this.freq = freq;
		this.bufferSize = bufferSize;
	}
	
	public abstract void open();
	public abstract void write(short samples[], int size);
	public abstract void close();
	
	public int getFreq() {
		return freq;
	}
	public int getBufferSize() {
		return bufferSize;
	}
	
}
