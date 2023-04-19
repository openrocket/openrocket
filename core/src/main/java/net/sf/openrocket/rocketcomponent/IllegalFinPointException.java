package net.sf.openrocket.rocketcomponent;

/**
 * An exception signifying that an operation on the freeform fin set points was
 * illegal (segments intersect, removing first or last point, etc).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class IllegalFinPointException extends Exception {

	public IllegalFinPointException() {

	}

	public IllegalFinPointException(String message) {
		super(message);
	}

	public IllegalFinPointException(Throwable cause) {
		super(cause);
	}

	public IllegalFinPointException(String message, Throwable cause) {
		super(message, cause);
	}

}
