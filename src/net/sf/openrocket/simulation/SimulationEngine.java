package net.sf.openrocket.simulation;

import net.sf.openrocket.simulation.exception.SimulationException;

/**
 * A simulation engine that controls the flow of a simulation.  This typically maintains
 * flight events and related actions, while continuously calling a SimulationStepper to
 * move the rocket forward step by step.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface SimulationEngine {
	
	/**
	 * Simulate the flight of a rocket.
	 * 
	 * @param simulation	the simulation conditions which to simulate.
	 * @return				a FlightData object containing the simulated data.
	 * @throws SimulationException		if an error occurs during simulation
	 */
	public FlightData simulate(SimulationConditions simulation)
		throws SimulationException;
	
}
