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

		SimulationStatus status = new SimulationStatus(config, new SimulationConditions());
		for (MotorClusterState clusterState : status.getMotors()) {
			clusterState.ignite(0.0);
		}
		
		RK4SimulationStepper stepper = new RK4SimulationStepper();
		status.setSimulationTime(0.4);
		// Thrust of a single C6 at time 0.4 is 5 (from TestRockets.java, not actual thrustcurve)
		double c6Thrust = 5.0;
		
		// Two motors in sustainer
		config.setOnlyStage(0);
		double thrust = stepper.calculateThrust(status, new RK4SimulationStepper.DataStore());
		assertEquals(2.0 * c6Thrust, thrust, MathUtil.EPSILON, "Sustainer thrust incorrect");

		// Three side boosters with four motors in each
		config.setOnlyStage(1);
		thrust = stepper.calculateThrust(status, new RK4SimulationStepper.DataStore());
		assertEquals(12.0 * c6Thrust, thrust, MathUtil.EPSILON, "side booster thrust incorrect");

		// All 14 motors now
		config.setAllStages();
		thrust = stepper.calculateThrust(status, new RK4SimulationStepper.DataStore());
		assertEquals(14.0 * c6Thrust, thrust, MathUtil.EPSILON, "Total thrust incorrect");
	}
}
