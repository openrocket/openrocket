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
	 * When the step() is called, a new point is added to the flight data branch,
	 * and the current simulation status is saved to that point.
	 *
	 * The sim parameters to update the status are calculated, and saved to
	 * the new point (it's a little flaky that only the sim parameters at the
	 * start of an RK4 step are saved, since they vary throughout the step)
	 *
	 * Upon ground hit, one extra step is take to save the simulation status
	 * and parameters at the moment of impact. The updated status at the end
	 * of this step isn't saved, as the rocket actually stops moving at this
	 * point.
	 */
	public void step(SimulationStatus status, double maxTimeStep) throws SimulationException;

}
