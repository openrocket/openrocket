package net.sf.openrocket.file;

import java.io.IOException;

/**
 * An exception marking that a file type was not supported.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class UnknownFileTypeException extends IOException {
	
	public UnknownFileTypeException() {
	}
	
	public UnknownFileTypeException(String message) {
		super(message);
	}
	
	public UnknownFileTypeException(Throwable cause) {
		super(cause);
	}
	
	public UnknownFileTypeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
