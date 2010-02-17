package net.sf.openrocket.simulation.listeners;

import java.util.Collection;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationListener;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;


public class AbstractSimulationListener implements SimulationListener {

	@Override 
	public void flightConditions(SimulationStatus status, FlightConditions conditions)
		throws SimulationException {
		// No-op
	}
	
	@Override
	public void forceCalculation(SimulationStatus status, FlightConditions conditions,
			AerodynamicForces forces) throws SimulationException {
		// No-op
	}

	@Override
	public Collection<FlightEvent> handleEvent(FlightEvent event,
			SimulationStatus status) throws SimulationException {
		// No-op
		return null;
	}

	@Override
	public Collection<FlightEvent> stepTaken(SimulationStatus status)
		throws SimulationException {
		// No-op
		return null;
	}

}
