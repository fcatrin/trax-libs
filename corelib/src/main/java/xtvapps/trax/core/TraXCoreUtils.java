package xtvapps.trax.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import fts.core.CoreUtils;

public class TraXCoreUtils {
	private static final int BUF_SIZE = 0x10000;
	private static final int SHORT_SLEEP_TIME = 50;
	private static final int SHORTEST_SLEEP_TIME = 10;
	
	public static final int MSEC = 1000;
	
	private TraXCoreUtils(){}
	
	public static byte[] loadBytes(File f) throws IOException {
		InputStream is = new FileInputStream(f);
		return loadBytes(is);
	}
	public static byte[] loadBytes(InputStream is) throws IOException {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buf = new byte[BUF_SIZE];
			while (true) {
				int rc = is.read(buf);
				if (rc <= 0)
					break;
				else
					bout.write(buf, 0, rc);
			}
			return bout.toByteArray();
		} finally {
			if (is!=null) is.close();
		}
	}
	
	public static void shortSleep() {
		CoreUtils.sleep(SHORT_SLEEP_TIME);
	}

	public static void shortestSleep() {
		CoreUtils.sleep(SHORTEST_SLEEP_TIME);
	}

	public static byte[] toByteArray(short input[]) {
		ByteBuffer byteBuf = ByteBuffer.allocate(2*input.length);
		for(int i=0; i<input.length; i++) {
		    byteBuf.putShort(input[i]);
		}
		return byteBuf.array();
	}

	public static int findNextPowerOfTwo(int requestedBufferSize) {
		int n = 2;
		while (n < requestedBufferSize) n = 2*n;
		return n;
	}
	
	public static String nameNoExt(File file) {
		String name = file.getName();
		int p = name.lastIndexOf(".");
		if (p>0) {
			name = name.substring(0, p);
		}
		return name;
	}

}
