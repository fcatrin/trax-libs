package xtvapps.simusplayer;

public class AudioBuffer {
	private static final String LOGTAG = AudioBuffer.class.getSimpleName();
	
	short samplesOut[];
	byte samplesIn[];
	boolean hasMore = true;
	boolean processed = false;
	int id;
	long markOffset = 0;

	public AudioBuffer(int length, int id) {
		samplesOut = new short[length*4];
		samplesIn = new byte[length*8];
		this.id = id;
	}

	public void render() {
		for(int i=0; i<samplesOut.length; i++) {
			short a = samplesIn[i * 2];
			short b = samplesIn[i * 2 + 1];
			
			short value = (short)(a + (b << 8));
			samplesOut[i] = value;
		}
	}
	
}
