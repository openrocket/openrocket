package net.sf.openrocket.simulation.listeners;

import java.util.Collection;
import java.util.Collections;

import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;


/**
 * A simulation listeners that ends the simulation when apogee is reached.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ApogeeEndListener extends AbstractSimulationListener {

	public static final ApogeeEndListener INSTANCE = new ApogeeEndListener();
	
	@Override
	public Collection<FlightEvent> handleEvent(FlightEvent event,
			SimulationStatus status) throws SimulationException {

		if (event.getType() == FlightEvent.Type.APOGEE) {
			return Collections.singleton(new FlightEvent(FlightEvent.Type.SIMULATION_END, 
					status.time));
		}
		return null;
	}

}
