package net.sf.openrocket.simulation.listeners.system;

import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;


/**
 * A simulation listeners that ends the simulation when apogee is reached.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RecoveryDeviceDeploymentEndListener extends AbstractSimulationListener {
	
	public static final RecoveryDeviceDeploymentEndListener INSTANCE = new RecoveryDeviceDeploymentEndListener();
	
	@Override
	public boolean recoveryDeviceDeployment(SimulationStatus status, RecoveryDevice recoveryDevice) throws SimulationException {
		status.getEventQueue().add(new FlightEvent(FlightEvent.Type.SIMULATION_END, status.getSimulationTime()));
		return true;
	}
	
	@Override
	public boolean isSystemListener() {
		return true;
	}
}
