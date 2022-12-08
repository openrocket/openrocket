package net.sf.openrocket.simulation.extension.example;

import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;

/**
 * A simulation listener that stops the simulation after a specified number of steps or
 * after a specified amount of simulation time.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class StopSimulation extends AbstractSimulationExtension {

	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		conditions.getSimulationListenerList().add(new StopSimulationListener());
	}

	@Override
	public String getName() {
		return "Stop Simulation";
	}

	@Override
	public String getDescription() {
		return "Stop simulafter at a configurable simulation time or step count";
	}

	public int getReportRate() {
		return config.getInt("reportRate", 500);
	}
	
	public void setReportRate(int reportRate) {
		config.put("reportRate", reportRate);
	}

	public int getStopStep() {
		return config.getInt("stopStep", 5000);
	}

	public void setStopStep(int stopStep) {
		config.put("stopStep", stopStep);
	}

	public int getStopTime() {
		return config.getInt("stopTime", 10);
	}

	public void setStopTime(int stopTime) {
		config.put("stopTime", stopTime);
	}
	
	private class StopSimulationListener extends AbstractSimulationListener {
		
		private int step = 0;
		
		private long startTime = -1;
		private long time = -1;
		
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
			if ((step % getReportRate()) == 0) {
				long t = System.nanoTime();
				
				System.out.printf("Step %4d, time=%.3f, took %d us/step (avg. %d us/step)\n",
								  step, status.getSimulationTime(), (t - time) / 1000 / getReportRate(), (t - startTime) / 1000 / step);
				time = t;
			}
			if (status.getSimulationTime() >= getStopTime() || step >= getStopStep()) {
				System.out.printf("Stopping simulation, step=%d time=%.3f\n", step, status.getSimulationTime());
				status.getEventQueue().add(new FlightEvent(FlightEvent.Type.SIMULATION_END,
														   status.getSimulationTime(), null));
			}
		}
	}
		
}
