package info.openrocket.core.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.simulation.exception.SimulationException;

public class GroundStepper extends AbstractSimulationStepper {
	private static final Logger log = LoggerFactory.getLogger(GroundStepper.class);

	DataStore store = new DataStore();

	@Override
	public SimulationStatus initialize(SimulationStatus status) {
		log.trace("initializing GroundStepper");
		
		return status;
	}

	@Override
	public void step(SimulationStatus status, double timeStep) throws SimulationException {
		log.trace("step:  position=" + status.getRocketPosition() + ", velocity=" + status.getRocketVelocity());
		
		// Set to values sitting on the ground
		landedValues(status, store);
		
		// Put in a step to immediately go to landed status
		double time = status.getSimulationTime();
		if (timeStep > 2 * MIN_TIME_STEP) {
			// Record timeStep in *current* flightDataBranch record, replacing NaN which was there
			// previously
			status.getFlightDataBranch().setValue(FlightDataType.TYPE_TIME_STEP, MIN_TIME_STEP);
			timeStep = timeStep - MIN_TIME_STEP;
			
			status.setSimulationTime(time + MIN_TIME_STEP);
			status.storeData();
			store.storeData(status);
		}
		
		// Set status to reflect sitting on the ground ever since the last step
		status.setSimulationTime(status.getSimulationTime() + timeStep);
		status.getFlightDataBranch().setValue(FlightDataType.TYPE_TIME_STEP, timeStep);
		
		status.storeData();
		store.storeData(status);
	}

	@Override
	void calculateAcceleration(SimulationStatus status, DataStore store) throws SimulationException {
		// empty
	}
}
