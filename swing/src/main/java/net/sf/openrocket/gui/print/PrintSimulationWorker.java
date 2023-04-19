/*
 * PrintSimulationWorker.java
 */
package net.sf.openrocket.gui.print;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.simulation.SimulationWorker;
import net.sf.openrocket.simulation.FlightData;

/**
 * A SimulationWorker that simulates the rocket flight in the background and sets the results to the extra text when
 * finished.  The worker can be cancelled if necessary.
 */
public class PrintSimulationWorker {
	
	public static FlightData doit(Simulation sim) {
		return new InnerPrintSimulationWorker(sim).doit();
	}
	
	static class InnerPrintSimulationWorker extends SimulationWorker {
		
		public InnerPrintSimulationWorker(Simulation sim) {
			super(sim);
		}
		
		public FlightData doit() {
			return doInBackground();
		}
		
		@Override
		protected void simulationDone() {
			// Do nothing if cancelled
			if (isCancelled()) {
				return;
			}
			
			simulation.getSimulatedData();
		}
		
		
		/**
		 * Called if the simulation is interrupted due to an exception.
		 *
		 * @param t the Throwable that caused the interruption
		 */
		@Override
		protected void simulationInterrupted(final Throwable t) {
		}
	}
}