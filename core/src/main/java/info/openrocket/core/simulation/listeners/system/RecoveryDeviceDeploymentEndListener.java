package info.openrocket.core.simulation.listeners.system;

import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;

/**
 * A simulation listeners that ends the simulation when apogee is reached.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RecoveryDeviceDeploymentEndListener extends AbstractSimulationListener {

	public static final RecoveryDeviceDeploymentEndListener INSTANCE = new RecoveryDeviceDeploymentEndListener();

	@Override
	public boolean recoveryDeviceDeployment(SimulationStatus status, RecoveryDevice recoveryDevice)
			throws SimulationException {
		status.getEventQueue().add(new FlightEvent(FlightEvent.Type.SIMULATION_END, status.getSimulationTime()));
		return true;
	}

	@Override
	public boolean isSystemListener() {
		return true;
	}
}
