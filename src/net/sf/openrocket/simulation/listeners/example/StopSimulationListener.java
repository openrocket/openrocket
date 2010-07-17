package net.sf.openrocket.simulation.listeners.example;

import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;

/**
 * A simulation listener that stops the simulation after a specified number of steps or
 * after a specified abount of simulation time.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class StopSimulationListener extends AbstractSimulationListener {
	
	private final int REPORT = 500;
	
	private final double stopTime;
	private final int stopStep;
	
	private int step = 0;
	
	private long startTime = -1;
	private long time = -1;
	
	public StopSimulationListener(double t, int n) {
		stopTime = t;
		stopStep = n;
	}
	
	@Override
	public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) {
		
		if (event.getType() == FlightEvent.Type.LAUNCH) {
			System.out.println("Simulation starting.");
			time = System.nanoTime();
			startTime = System.nanoTime();
		}
		
		return true;
	}
	
	
	@Override
	public void postStep(SimulationStatus status) throws SimulationException {
		step++;
		if ((step % REPORT) == 0) {
			long t = System.nanoTime();
			
			System.out.printf("Step %4d, time=%.3f, took %d us/step (avg. %d us/step)\n",
					step, status.getSimulationTime(), (t - time) / 1000 / REPORT, (t - startTime) / 1000 / step);
			time = t;
		}
		if (status.getSimulationTime() >= stopTime || step >= stopStep) {
			System.out.printf("Stopping simulation, step=%d time=%.3f\n", step, status.getSimulationTime());
			status.getEventQueue().add(new FlightEvent(FlightEvent.Type.SIMULATION_END,
					status.getSimulationTime(), null));
		}
	}
	
}
