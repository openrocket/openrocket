package net.sf.openrocket.simulation;

import java.util.Collection;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.simulation.exception.SimulationException;



public interface SimulationListener {

	
	public void flightConditions(SimulationStatus status, FlightConditions conditions)
		throws SimulationException;
	
	
	public void forceCalculation(SimulationStatus status, FlightConditions conditions,
			AerodynamicForces forces) throws SimulationException;
	
	
	/**
	 * Called every time a simulation step has been taken.  The parameter contains the
	 * simulation status.  This method may abort the simulation by returning a
	 * <code>SIMULATION_END</code> event.  Note that this event and all others within
	 * the current time are still handled, so be careful not to create an infinite loop
	 * of events.
	 * 
	 * @param status	the current flight status.
	 * @return			new flight events to handle, or <code>null</code> for none.
	 */
	public Collection<FlightEvent> stepTaken(SimulationStatus status)
		throws SimulationException;
	
	
	/**
	 * Called every time an event is handled by the simulation system.  The parameters
	 * contain the event and current simulation status.  This method may abort the 
	 * simulation by returning a <code>SIMULATION_END</code> event.  Note that this 
	 * event and all others within the current time are still handled, so be careful 
	 * not to create an infinite loop of events.
	 * 
	 * @param event		the event that triggered this call.
	 * @param status	the current flight status.
	 * @return			new flight events to handle, or <code>null</code> for none.
	 */
	public Collection<FlightEvent> handleEvent(FlightEvent event, SimulationStatus status)
		throws SimulationException;
	
}
