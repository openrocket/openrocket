package info.openrocket.core.simulation.listeners.example;

import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
import info.openrocket.core.util.Coordinate;

/**
 * Simulation listener that launches a rocket from a specific altitude.
 * <p>
 * The altitude is read from the system property "openrocket.airstart.altitude"
 * if defined, otherwise a default altitude of 1000 meters is used.
 */
public class AirStart extends AbstractSimulationListener {

	/** Default launch altitude */
	private static final double DEFAULT_ALTITUDE = 1000.0;

	@Override
	public void startSimulation(SimulationStatus status) throws SimulationException {

		// Get the launch altitude
		double altitude;
		String arg = System.getProperty("openrocket.airstart.altitude");
		try {
			altitude = Double.parseDouble(arg);
		} catch (RuntimeException e) {
			altitude = DEFAULT_ALTITUDE;
		}

		// Modify launch position
		Coordinate position = status.getRocketPosition();
		position = position.add(0, 0, altitude);
		status.setRocketPosition(position);

	}

}
