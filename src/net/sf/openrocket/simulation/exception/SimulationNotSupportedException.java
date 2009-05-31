package net.sf.openrocket.simulation.exception;


/**
 * A exception that signifies that the attempted simulation is not supported.  
 * The reason for not being supported may be due to unsupported combination of
 * simulator/calculator, unsupported rocket structure or other reasons.
 * <p>
 * This exception signifies a fatal problem in the simulation; for non-fatal conditions
 * add a warning to the simulation results.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationNotSupportedException extends SimulationException {

	public SimulationNotSupportedException() {
	}

	public SimulationNotSupportedException(String message) {
		super(message);
	}

	public SimulationNotSupportedException(Throwable cause) {
		super(cause);
	}

	public SimulationNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}
}
