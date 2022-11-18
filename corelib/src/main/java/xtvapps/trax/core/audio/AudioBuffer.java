package xtvapps.trax.core.audio;


public class AudioBuffer {
	public enum Status {Free, Filled, Processing}
	public enum Format {U16, S16}
	private static final String LOGTAG = AudioBuffer.class.getSimpleName();
	
	public short samplesOut[];
	public byte samplesIn[];
	boolean hasMore = true;
	int id;
	long markOffset = 0;
	Format format = Format.U16;
	private Status status = Status.Free;

	public AudioBuffer(int length, int id) {
		this(length, id, Format.U16);
	}
	
	public AudioBuffer(int length, int id, Format format) {
		samplesOut = new short[length*4];
		samplesIn = new byte[length*8];
		this.id = id;
		this.format = format;
	}

	public void render() {
		if (format == Format.U16) {
			for(int i=0; i<samplesOut.length; i++) {
				short a = samplesIn[i * 2];
				short b = samplesIn[i * 2 + 1];

				short value = (short)(a + (b << 8));
				samplesOut[i] = value;
			}
		} else {
			for(int i=0; i<samplesOut.length; i++) {
				short a = samplesIn[i * 2];
				short b = samplesIn[i * 2 + 1];
				
				if (a<0) a+= 256;
				if (b<0) b+= 256;
				
				short value = (short)(a + (b << 8));
				samplesOut[i] = value;
			}
		}
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
