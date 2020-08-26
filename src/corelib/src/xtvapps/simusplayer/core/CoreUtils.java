package xtvapps.simusplayer.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CoreUtils {
	private static final int BUF_SIZE = 0x10000;
	
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

}
