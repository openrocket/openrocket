package net.sf.openrocket.simulation.exception;

/**
 * An exception that indicates that a computation problem has occurred during
 * the simulation, for example that some values have exceed reasonable bounds.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationCalculationException extends SimulationException {
	
	public SimulationCalculationException() {
		// TODO Auto-generated constructor stub
	}
	
	public SimulationCalculationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	public SimulationCalculationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	public SimulationCalculationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
}
