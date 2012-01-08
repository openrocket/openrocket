package net.sf.openrocket.simulation.exception;

/**
 * An exception that indicates that a computation problem has occurred during
 * the simulation, for example that some values have exceed reasonable bounds.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationCalculationException extends SimulationException {
	
	public SimulationCalculationException() {
	}
	
	public SimulationCalculationException(String message) {
		super(message);
	}
	
	public SimulationCalculationException(Throwable cause) {
		super(cause);
	}
	
	public SimulationCalculationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
