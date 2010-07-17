package net.sf.openrocket.simulation.listeners.system;

import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationCancelledException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;


/**
 * A simulation listener that throws a {@link SimulationCancelledException} if
 * this thread has been interrupted.  The conditions is checked every time a step
 * is taken.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class InterruptListener extends AbstractSimulationListener {
	
	public static final InterruptListener INSTANCE = new InterruptListener();
	
	@Override
	public void postStep(SimulationStatus status) throws SimulationException {
		if (Thread.interrupted()) {
			throw new SimulationCancelledException("The simulation was interrupted.");
		}
	}
	
	@Override
	public boolean isSystemListener() {
		return true;
	}
}
