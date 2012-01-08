package net.sf.openrocket.optimization.general;

/**
 * An exception that prevents optimization from continuing.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OptimizationException extends Exception {
	
	public OptimizationException(String message) {
		super(message);
	}
	
	public OptimizationException(Throwable cause) {
		super(cause);
	}
	
	public OptimizationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
