package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.extension.AbstractSimulationExtension;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;

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
			FlightDataBranch dataBranch = status.getFlightDataBranch();
			System.out.printf("*** stepTaken *** time=%.3f position=" + status.getRocketPosition() +
					" velocity=" + status.getRocketVelocity() + "=%.3f\n", status.getSimulationTime(),
					status.getRocketVelocity().length());
			System.out.printf("                  thrust=%.3fN drag==%.3fN mass=%.3fkg " +
					"accZ=%.3fm/s2 acc=%.3fm/s2\n",
					dataBranch.getLast(FlightDataType.TYPE_THRUST_FORCE),
					dataBranch.getLast(FlightDataType.TYPE_DRAG_FORCE),
					dataBranch.getLast(FlightDataType.TYPE_MASS),
					dataBranch.getLast(FlightDataType.TYPE_ACCELERATION_Z),
					dataBranch.getLast(FlightDataType.TYPE_ACCELERATION_TOTAL));
		}
	}

}
