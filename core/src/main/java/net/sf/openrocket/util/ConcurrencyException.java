package net.sf.openrocket.util;

/**
 * An exception that indicates a concurrency bug in the software.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ConcurrencyException extends FatalException {
	
	public ConcurrencyException() {
		super();
	}
	
	public ConcurrencyException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ConcurrencyException(String message) {
		super(message);
	}
	
	public ConcurrencyException(Throwable cause) {
		super(cause);
	}
	
}
