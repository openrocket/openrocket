package net.sf.openrocket.simulation.exception;

/**
 * An exception signifying that a problem occurred at launch, for example
 * that no motors were defined or no motors ignited.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationLaunchException extends SimulationException {
	
	public SimulationLaunchException(String message) {
		super(message);
	}
	
	public SimulationLaunchException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
