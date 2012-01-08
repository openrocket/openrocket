package net.sf.openrocket.simulation.listeners.system;

import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;


/**
 * A simulation listeners that ends the simulation when apogee is reached.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ApogeeEndListener extends AbstractSimulationListener {
	
	public static final ApogeeEndListener INSTANCE = new ApogeeEndListener();
	
	@Override
	public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) {
		if (event.getType() == FlightEvent.Type.APOGEE) {
			status.getEventQueue().add(new FlightEvent(FlightEvent.Type.SIMULATION_END, status.getSimulationTime()));
		}
		return true;
	}
	
	@Override
	public boolean isSystemListener() {
		return true;
	}
}
