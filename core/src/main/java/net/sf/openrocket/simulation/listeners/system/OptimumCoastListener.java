package net.sf.openrocket.simulation.listeners.system;

import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;

/**
 * Simulation listener which ignores recovery deployment events and ends the simulation
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
	public boolean recoveryDeviceDeployment(SimulationStatus status, RecoveryDevice recoveryDevice) throws SimulationException {
		return false;
	}
	
	@Override
	public boolean isSystemListener() {
		return true;
	}
	
}
