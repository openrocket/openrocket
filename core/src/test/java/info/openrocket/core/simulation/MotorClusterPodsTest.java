package info.openrocket.core.simulation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.MotorClusterState;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;
import info.openrocket.core.util.BaseTestCase;

public class MotorClusterPodsTest extends BaseTestCase {
	@Test
	public void testMotorClusterPods() throws SimulationException {
		final Rocket rocket = TestRockets.makeClusterPods();
		FlightConfiguration config = rocket.getFlightConfigurationByIndex(0);
		config.setAllStages();

		SimulationStatus status = new SimulationStatus(config, new SimulationConditions());
		for (MotorClusterState clusterState : status.getMotors()) {
			clusterState.ignite(0.0);
		}

		RK4SimulationStepper stepper = new RK4SimulationStepper();
		status.setSimulationTime(0.4);
		double thrust = stepper.calculateThrust(status, new RK4SimulationStepper.DataStore());

		// Thrust of a single C6 at time 0.4 is 5 (from TestRockets.java, not actual thrustcurve)
		// Two in the sustainer, three pods, four in each pod gives a total of 14 motors so total is 70
		assertEquals(70.0, thrust, MathUtil.EPSILON, "Calculated thrust incorrect");
	}
}
