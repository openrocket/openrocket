package info.openrocket.core.simulation;

import info.openrocket.core.simulation.exception.SimulationException;

public interface SimulationStepper {

	/**
	 * Initialize a simulation using this simulation stepper based on the provided
	 * current simulation status and launch conditions.
	 * 
	 * @param status the current simulation status.
	 * @return a SimulationStatus suitable for simulating with this simulation
	 *         stepper.
	 */
	public SimulationStatus initialize(SimulationStatus status);

	/**
	 * Perform one simulation time step.
	 * 
	 * @param status      the current simulation status, of a type returned by
	 *                    {@link #initialize(SimulationStatus)}.
	 * @param maxTimeStep the maximum time step to take. This is an upper bound and
	 *                    can be used to limit a stepper
	 *                    from stepping over upcoming flight events (motor ignition
	 *                    etc).
	 *
	 * When the step() is called, a new record has been started for the simulation step, and
	 * the SimulationStatus at the start time of the step has been saved to the FlightDataBranch.
	 * The stepper's DataStore is saved to the FlightDataBranch as soon as it has been calculated.
	 *
	 * At the end of the step, the simulation time is updated, a new record is created in the FlightDataBranch,
	 * and the updated SimulationStatus is saved to the FlightDataBranch.
	 *
	 * When the simulation terminates, the DataStore has to be computed one more time and saved to the
	 * FlightDataBranch to complete the last record.
	 */
	public void step(SimulationStatus status, double maxTimeStep) throws SimulationException;

	/*
	 * clean up at end of simulation. Calculates DataStore values one last time and
	 * writes them to FlightDataBranch
	 */
	void cleanup(SimulationStatus status) throws SimulationException;
}
