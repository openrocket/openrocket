package net.sf.openrocket.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Coordinate;

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
	}
}
