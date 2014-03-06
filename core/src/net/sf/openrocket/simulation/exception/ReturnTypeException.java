package net.sf.openrocket.simulation.exception;


/**
 * An for the OpenRocketAPI such that an interface for the API
 * Need not handle invalid return values.
 * 
 * @author Bejon <nubjub@gmail.com>
 */
public class ReturnTypeException extends SimulationException {
	
	public ReturnTypeException() {
		
	}
	
	public ReturnTypeException(String message) {
		super(message);
	}
	
	public ReturnTypeException(Throwable cause) {
		super(cause);
	}
	
	public ReturnTypeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
