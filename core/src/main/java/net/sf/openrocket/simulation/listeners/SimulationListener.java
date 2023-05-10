package net.sf.openrocket.simulation.listeners;

import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;

/**
 * Listen to simulation events and possibly take action.
 * 
 * If the implementation maintains any state, it should be properly cloned.
 * 
 */
public interface SimulationListener {
	
	/**
	 * Called when starting a simulation.
	 * 
	 * @param status	the simulation status.
	 */
	public void startSimulation(SimulationStatus status) throws SimulationException;
	
	
	/**
	 * Called when ending a simulation.  This is called either when the simulation ends normally
	 * (due to an end simulation event) or when a SimulationException is thrown.
	 * <p>
	 * This method cannot throw a SimulationException, since the simulation is already being ended.
	 * 
	 * @param status	the simulation status.
	 * @param exception	the exception that caused ending the simulation, or <code>null</code> if ending normally.
	 */
	public void endSimulation(SimulationStatus status, SimulationException exception);
	
	
	/**
	 * Called before a simulation step is taken.  This method may also prevent the normal
	 * stepping method from being called.
	 * 
	 * @param status	the simulation status.
	 * @return			<code>true</code> to continue normally, <code>false</code> to skip taking the step
	 */
	public boolean preStep(SimulationStatus status) throws SimulationException;
	
	
	/**
	 * Called immediately after a simulation step has been taken.  This method is called whether the
	 * {@link #preStep(SimulationStatus)} aborted the step or not.
	 * 
	 * @param status	the simulation status.
	 */
	public void postStep(SimulationStatus status) throws SimulationException;
	
	
	/**
	 * Return whether this is a system listener.  System listeners are used internally for various
	 * purposes by OpenRocket.  User-written listeners should always return <code>false</code>.
	 * <p>
	 * System listeners do not cause warnings to be added to the simulation results when they affect
	 * the simulation.
	 * 
	 * @return		whether this is a system listener
	 */
	public boolean isSystemListener();
	
	
	/**
	 * Return a deep copy of this simulation listener including its state.
	 */
	public SimulationListener clone();
}
