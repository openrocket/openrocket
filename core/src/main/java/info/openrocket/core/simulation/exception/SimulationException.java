package info.openrocket.core.simulation.exception;

public class SimulationException extends Exception {

	public SimulationException() {
		super();
	}

	public SimulationException(String message) {
		super(message);
	}

	public SimulationException(Throwable cause) {
		super(cause);
	}

	public SimulationException(String message, Throwable cause) {
		super(message, cause);
	}
}
