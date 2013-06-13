package net.sf.openrocket.simulation;

import net.sf.openrocket.simulation.exception.SimulationException;

public interface SimulationStepper {
	
	/**
	 * Initialize a simulation using this simulation stepper based on the provided
	 * current simulation status and launch conditions.
	 * 
	 * @param status		the current simulation status.
	 * @return				a SimulationStatus suitable for simulating with this simulation stepper.
	 */
	public SimulationStatus initialize(SimulationStatus status);
	
	/**
	 * Perform one simulation time step.
	 * 
	 * @param status		the current simulation status, of a type returned by {@link #initialize(SimulationStatus)}.
	 * @param maxTimeStep	the maximum time step to take.  This is an upper bound and can be used to limit a stepper
	 * 						from stepping over upcoming flight events (motor ignition etc).
	 */
	public void step(SimulationStatus status, double maxTimeStep) throws SimulationException;
	
}
