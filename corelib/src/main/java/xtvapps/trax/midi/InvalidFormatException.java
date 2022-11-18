package xtvapps.trax.midi;

import java.io.IOException;

public class InvalidFormatException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public InvalidFormatException() {
		this("Invalid format");
	}

	public InvalidFormatException(String msg) {
		super(msg);
	}

}
