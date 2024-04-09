import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
import info.openrocket.core.util.Coordinate;

/**
 * Simulation listener that launches a rocket from a specific altitude.
 */
public class AirStart extends AbstractSimulationListener {

	/** Launch altitude */
	private static final double ALTITUDE = 1000.0;

	@Override
	public void startSimulation(SimulationStatus status) throws SimulationException {
		Coordinate position = status.getRocketPosition();
		position = position.add(0, 0, ALTITUDE);
		status.setRocketPosition(position);
	}

}
