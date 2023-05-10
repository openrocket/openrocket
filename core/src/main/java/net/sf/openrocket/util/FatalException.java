package net.sf.openrocket.util;

/**
 * A superclass for all types of fatal error conditions.  This class is
 * abstract so only subclasses can be used.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 * @see BugException
 * @see ConfigurationException
 */
public abstract class FatalException extends RuntimeException {

	public FatalException() {
	}

	public FatalException(String message) {
		super(message);
	}

	public FatalException(Throwable cause) {
		super(cause);
	}

	public FatalException(String message, Throwable cause) {
		super(message, cause);
	}

}
