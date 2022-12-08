package net.sf.openrocket.simulation.extension.example;

import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;

public class PrintSimulation extends AbstractSimulationExtension {

	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		conditions.getSimulationListenerList().add(new PrintSimulationListener());
	}

	@Override
	public String getName() {
		return "Print Simulation Values";
	}

	@Override
	public String getDescription() {
		return "Print summary of simulation progress to standard output after every step";
	}
	
	private class PrintSimulationListener extends AbstractSimulationListener {
		
		@Override
		public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
			System.out.println("*** handleEvent *** " + event.toString() +
							   " position=" + status.getRocketPosition() + " velocity=" + status.getRocketVelocity());
			return true;
		}
		
		@Override
		public void postStep(SimulationStatus status) throws SimulationException {
			FlightDataBranch data = status.getFlightData();
			System.out.printf("*** stepTaken *** time=%.3f position=" + status.getRocketPosition() +
							  " velocity=" + status.getRocketVelocity() + "=%.3f\n", status.getSimulationTime(), status.getRocketVelocity().length());
			System.out.printf("                  thrust=%.3fN drag==%.3fN mass=%.3fkg " +
							  "accZ=%.3fm/s2 acc=%.3fm/s2\n",
							  data.getLast(FlightDataType.TYPE_THRUST_FORCE),
							  data.getLast(FlightDataType.TYPE_DRAG_FORCE),
							  data.getLast(FlightDataType.TYPE_MASS),
							  data.getLast(FlightDataType.TYPE_ACCELERATION_Z),
							  data.getLast(FlightDataType.TYPE_ACCELERATION_TOTAL));
		}
	}
	
}
