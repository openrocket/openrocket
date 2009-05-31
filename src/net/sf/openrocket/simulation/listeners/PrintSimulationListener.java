package net.sf.openrocket.simulation.listeners;

import java.util.Collection;

import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;


public class PrintSimulationListener extends AbstractSimulationListener {

	@Override
	public Collection<FlightEvent> handleEvent(FlightEvent event,
			SimulationStatus status) {

		System.out.println("*** handleEvent *** "+event.toString() + 
				" position="+status.position + " velocity="+status.velocity);
		return null;
	}

	@Override
	public Collection<FlightEvent> stepTaken(SimulationStatus status) {

		FlightDataBranch data = status.flightData;
		System.out.printf("*** stepTaken *** time=%.3f position="+status.position+
				" velocity="+status.velocity+"=%.3f\n", status.time, status.velocity.length());
		System.out.printf("                  thrust=%.3fN drag==%.3fN mass=%.3fkg " +
				"accZ=%.3fm/s2 acc=%.3fm/s2\n", 
				data.getLast(FlightDataBranch.TYPE_THRUST_FORCE),
				data.getLast(FlightDataBranch.TYPE_DRAG_FORCE),
				data.getLast(FlightDataBranch.TYPE_MASS),
				data.getLast(FlightDataBranch.TYPE_ACCELERATION_Z),
				data.getLast(FlightDataBranch.TYPE_ACCELERATION_TOTAL));
		
		return null;
	}

}
