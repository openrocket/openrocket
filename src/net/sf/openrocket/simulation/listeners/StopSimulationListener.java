package net.sf.openrocket.simulation.listeners;

import java.util.Collection;
import java.util.Collections;

import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;


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
	public Collection<FlightEvent> handleEvent(FlightEvent event,
			SimulationStatus status) {

		if (event.getType() == FlightEvent.Type.LAUNCH) {
			System.out.println("Simulation starting.");
			time = System.nanoTime();
			startTime = System.nanoTime();
		}
		
		return null;
	}

	@Override
	public Collection<FlightEvent> stepTaken(SimulationStatus status) {
		step ++;
		if ((step%REPORT) == 0) {
			long t = System.nanoTime();
			
			System.out.printf("Step %4d, time=%.3f, took %d us/step (avg. %d us/step)\n",
					step,status.time,(t-time)/1000/REPORT,(t-startTime)/1000/step);
			time = t;
		}
		if (status.time >= stopTime || step >= stopStep) {
			System.out.printf("Stopping simulation, step=%d time=%.3f\n",step,status.time);
			return Collections.singleton(new FlightEvent(FlightEvent.Type.SIMULATION_END,
					status.time, null));
		}
		return null;
	}

}
