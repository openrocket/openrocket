package net.sf.openrocket.simulation.listeners.system;

import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;


/**
 * A simulation listeners that ends the simulation when the ground is hit.
 * 
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class GroundHitListener extends AbstractSimulationListener {
	
	public static final GroundHitListener INSTANCE = new GroundHitListener();
	
	@Override
	public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) {
		if (event.getType() == FlightEvent.Type.GROUND_HIT) {
			status.getEventQueue().add(new FlightEvent(FlightEvent.Type.SIMULATION_END, status.getSimulationTime()));
		}
		return true;
	}
	
	@Override
	public boolean isSystemListener() {
		return true;
	}
}
