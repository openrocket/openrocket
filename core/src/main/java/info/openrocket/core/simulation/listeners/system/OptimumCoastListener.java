package info.openrocket.core.simulation.listeners.system;

import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;

/**
 * Simulation listener which ignores recovery deployment events and ends the
 * simulation
 * when apogee is reached.
 * 
 * @author kevin
 *
 */
public class OptimumCoastListener extends AbstractSimulationListener {

	public static final OptimumCoastListener INSTANCE = new OptimumCoastListener();

	@Override
	public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) {
		if (event.getType() == FlightEvent.Type.APOGEE) {
			status.getEventQueue().add(new FlightEvent(FlightEvent.Type.SIMULATION_END, status.getSimulationTime()));
		}
		return true;
	}

	@Override
	public boolean recoveryDeviceDeployment(SimulationStatus status, RecoveryDevice recoveryDevice)
			throws SimulationException {
		return false;
	}

	@Override
	public boolean isSystemListener() {
		return true;
	}

}
