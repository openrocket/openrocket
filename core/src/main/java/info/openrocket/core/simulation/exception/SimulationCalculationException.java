package info.openrocket.core.simulation.exception;

import info.openrocket.core.simulation.FlightDataBranch;

/**
 * An exception that indicates that a computation problem has occurred during
 * the simulation, for example that some values have exceed reasonable bounds.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationCalculationException extends SimulationException {

	private FlightDataBranch flightDataBranch;
	
	public SimulationCalculationException() {
	}

	public SimulationCalculationException(String message, FlightDataBranch dataBranch) {
		super(message);
		flightDataBranch = dataBranch;
	}

	public SimulationCalculationException(Throwable cause, FlightDataBranch dataBranch) {
		super(cause);
		flightDataBranch = dataBranch;
	}

	public SimulationCalculationException(String message, Throwable cause, FlightDataBranch dataBranch) {
		super(message, cause);
		flightDataBranch = dataBranch;
	}

	public FlightDataBranch getFlightDataBranch() {
		return flightDataBranch;
	}
}
