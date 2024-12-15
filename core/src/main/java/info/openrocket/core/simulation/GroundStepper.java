package info.openrocket.core.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.simulation.exception.SimulationException;

public class GroundStepper extends AbstractSimulationStepper {
	private static final Logger log = LoggerFactory.getLogger(GroundStepper.class);

	@Override
	public SimulationStatus initialize(SimulationStatus status) {
		log.trace("initializing GroundStepper");
		return status;
	}

	@Override
	public void step(SimulationStatus status, double timeStep) throws SimulationException {
		log.trace("step:  position=" + status.getRocketPosition() + ", velocity=" + status.getRocketVelocity());
		status.setSimulationTime(status.getSimulationTime() + timeStep);
		status.storeData();
	}

	@Override
	void calculateAcceleration(SimulationStatus status, DataStore store) throws SimulationException {
		// empty
	}
}
