package info.openrocket.core.simulation.listeners.system;

import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationCancelledException;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;

/**
 * A simulation listener that throws a {@link SimulationCancelledException} if
 * this thread has been interrupted. The conditions is checked every time a step
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
