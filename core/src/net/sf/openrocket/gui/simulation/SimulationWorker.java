package net.sf.openrocket.gui.simulation;

import java.util.Arrays;

import javax.swing.SwingWorker;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationCancelledException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.simulation.listeners.SimulationListener;



/**
 * A SwingWorker that runs a simulation in a background thread.  The simulation
 * always includes a listener that checks whether this SwingWorked has been cancelled,
 * and throws a {@link SimulationCancelledException} if it has.  This allows the
 * {@link #cancel(boolean)} method to be used to cancel the simulation.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class SimulationWorker extends SwingWorker<FlightData, SimulationStatus> {
	
	protected final Simulation simulation;
	private Throwable throwable = null;
	
	public SimulationWorker(Simulation sim) {
		this.simulation = sim;
	}
	
	
	/**
	 * Runs the simulation.
	 */
	@Override
	protected FlightData doInBackground() {
		if (isCancelled()) {
			throwable = new SimulationCancelledException("The simulation was interrupted.");
			return null;
		}
		
		SimulationListener[] listeners = getExtraListeners();
		
		if (listeners != null) {
			listeners = Arrays.copyOf(listeners, listeners.length + 1);
		} else {
			listeners = new SimulationListener[1];
		}
		
		listeners[listeners.length - 1] = new CancelListener();
		
		try {
			simulation.simulate(listeners);
		} catch (Throwable e) {
			throwable = e;
			return null;
		}
		return simulation.getSimulatedData();
	}
	
	
	/**
	 * Return additional listeners to use during the simulation.  The default
	 * implementation returns an empty array.
	 * 
	 * @return	additional listeners to use, or <code>null</code>.
	 */
	protected SimulationListener[] getExtraListeners() {
		return new SimulationListener[0];
	}
	
	
	/**
	 * Called after a simulation is successfully simulated.  This method is not
	 * called if the simulation ends in an exception.
	 */
	protected abstract void simulationDone();
	
	/**
	 * Called if the simulation is interrupted due to an exception.
	 * 
	 * @param t		the Throwable that caused the interruption
	 */
	protected abstract void simulationInterrupted(Throwable t);
	
	
	
	/**
	 * Marks this simulation as done and calls the progress update.
	 */
	@Override
	protected final void done() {
		if (throwable == null)
			simulationDone();
		else
			simulationInterrupted(throwable);
	}
	
	
	
	/**
	 * A simulation listener that throws a {@link SimulationCancelledException} if
	 * this SwingWorker has been cancelled.  The conditions is checked every time a step
	 * is taken.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	private class CancelListener extends AbstractSimulationListener {
		
		@Override
		public void postStep(SimulationStatus status) throws SimulationCancelledException {
			
			if (isCancelled()) {
				throw new SimulationCancelledException("The simulation was interrupted.");
			}
			
		}
	}
}
