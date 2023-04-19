package net.sf.openrocket.simulation.exception;

import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
	
public class SimulationException extends Exception {

	private FlightData flightData = null;
	private FlightDataBranch flightDataBranch = null;
	
	public SimulationException() {

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

	public void setFlightData(FlightData f) {
		flightData = f;
	}

	public FlightData getFlightData() {
		return flightData;
	}

	public void setFlightDataBranch(FlightDataBranch f) {
		flightDataBranch = f;
	}

	public FlightDataBranch getFlightDataBranch() {
		return flightDataBranch;
	}

}
