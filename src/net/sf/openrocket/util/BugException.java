package net.sf.openrocket.util;

/**
 * Thrown when a bug is noticed.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class BugException extends FatalException {
	
	public BugException(String message) {
		super(message);
	}
	
	public BugException(Throwable cause) {
		super(cause);
	}
	
	public BugException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
