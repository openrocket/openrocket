/*
 * PrintSimulationWorker.java
 */
package info.openrocket.swing.gui.print;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.FlightData;

import info.openrocket.swing.gui.simulation.SimulationWorker;

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