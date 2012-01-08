package net.sf.openrocket.simulation.exception;


/**
 * An exception signifying that a simulation was cancelled.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationCancelledException extends SimulationException {

	public SimulationCancelledException() {

	}

	public SimulationCancelledException(String message) {
		super(message);
	}

	public SimulationCancelledException(Throwable cause) {
		super(cause);
	}

	public SimulationCancelledException(String message, Throwable cause) {
		super(message, cause);
	}

}
