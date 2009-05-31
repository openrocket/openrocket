package net.sf.openrocket.simulation.listeners;

import java.util.Collection;

import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationCancelledException;


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
	public Collection<FlightEvent> stepTaken(SimulationStatus status) 
	throws SimulationCancelledException {

		if (Thread.interrupted()) {
			throw new SimulationCancelledException("The simulation was interrupted.");
		}

		return null;
	}
}
