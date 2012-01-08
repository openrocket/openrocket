package net.sf.openrocket.simulation.exception;


public class SimulationListenerException extends SimulationException {

	public SimulationListenerException() {
	}

	public SimulationListenerException(String message) {
		super(message);
	}

	public SimulationListenerException(Throwable cause) {
		super(cause);
	}

	public SimulationListenerException(String message, Throwable cause) {
		super(message, cause);
	}

}
