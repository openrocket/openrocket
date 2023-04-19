package net.sf.openrocket.simulation.exception;

/**
 * An exception signifying that the simulation failed because no motors were
 * defined or ignited in the rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MotorIgnitionException extends SimulationLaunchException {
	
	public MotorIgnitionException(String message) {
		super(message);
	}
	
	public MotorIgnitionException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
