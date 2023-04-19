package net.sf.openrocket.util;

/**
 * Exception indicating an invalid expression.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class InvalidExpressionException extends Exception {
	
	public InvalidExpressionException(String message) {
		super(message);
	}
	
	public InvalidExpressionException(Throwable cause) {
		super(cause);
	}
	
	public InvalidExpressionException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
