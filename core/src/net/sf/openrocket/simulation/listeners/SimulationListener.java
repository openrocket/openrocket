package net.sf.openrocket.simulation.listeners;

import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;



public interface SimulationListener {
	
	/**
	 * Get the name of this simulation listener.  Ideally this should be localized, as
	 * it can be displayed in the UI.
	 * 
	 * @return	the name of this simulation listener.
	 */
	public String getName();
	
	
	/**
	 * Get the menu position of this simulation listener.  This should be an array
	 * of localized submenu names in descending order, or an empty array for positioning
	 * in the base menu.
	 * 
	 * @return	the menu position of this simulation listener.
	 */
	public String[] getMenuPosition();
	
	
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
	 * Return a list of any flight data types this listener creates.
	 */
	public FlightDataType[] getFlightDataTypes();
	
	
}
