package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;

public class MotorClusterPodsTest extends BaseTestCase {
	private static final double C6_MOTOR_RADIUS = 0.018 / 2;

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

	/**
	 * Verify that flight conditions include only the projected area of motor
	 * clusters that are both on active stages and currently thrusting.
	 */
	@Test
	public void testThrustingMotorBaseAreaTracksMotorAndStageState() throws SimulationException {
		Rocket rocket = TestRockets.makeClusterPods();
		FlightConfiguration selectedConfiguration = rocket.getFlightConfigurationByIndex(0);
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(selectedConfiguration.getFlightConfigurationID());
		SimulationConditions simulationConditions = simulation.getOptions().toSimulationConditions();
		simulationConditions.setSimulation(simulation);
		FlightConfiguration configuration = simulation.getActiveConfiguration();
		SimulationStatus status = new SimulationStatus(configuration, simulationConditions);
		RK4SimulationStepper stepper = new RK4SimulationStepper();
		RK4SimulationStepper.DataStore store = new RK4SimulationStepper.DataStore();
		ComponentAssembly sustainerAssembly = (ComponentAssembly) rocket.getChild(0);
		ComponentAssembly sideBoosterAssembly =
				(ComponentAssembly) rocket.getChild(0).getChild(0).getChild(1);

		stepper.calculateFlightConditions(status, store);
		assertEquals(0, store.flightConditions.getThrustingMotorBaseArea(), MathUtil.EPSILON,
				"Armed motors must not reduce base drag before ignition");

		for (MotorClusterState clusterState : status.getMotors()) {
			clusterState.ignite(0);
		}
		status.setSimulationTime(0.4);
		stepper.calculateFlightConditions(status, store);
		double singleMotorArea = Math.PI * MathUtil.pow2(C6_MOTOR_RADIUS);
		assertEquals(14 * singleMotorArea, store.flightConditions.getThrustingMotorBaseArea(), MathUtil.EPSILON,
				"All fourteen thrusting motors should contribute their projected area");
		assertEquals(2 * singleMotorArea,
				store.flightConditions.getThrustingMotorBaseArea(sustainerAssembly), MathUtil.EPSILON,
				"The two sustainer motors should belong only to the sustainer wake");
		assertEquals(12 * singleMotorArea,
				store.flightConditions.getThrustingMotorBaseArea(sideBoosterAssembly), MathUtil.EPSILON,
				"The twelve side-booster motors should belong only to the booster wakes");

		configuration.setOnlyStage(0);
		stepper.calculateFlightConditions(status, store);
		assertEquals(2 * singleMotorArea, store.flightConditions.getThrustingMotorBaseArea(), MathUtil.EPSILON,
				"Motors on inactive booster stages must not affect the active rocket base");
		assertEquals(0, store.flightConditions.getThrustingMotorBaseArea(sideBoosterAssembly), MathUtil.EPSILON,
				"An inactive booster assembly must not retain stale motor area");

		for (MotorClusterState clusterState : status.getMotors()) {
			clusterState.burnOut(1);
		}
		stepper.calculateFlightConditions(status, store);
		assertEquals(0, store.flightConditions.getThrustingMotorBaseArea(), MathUtil.EPSILON,
				"Motor area correction must end at burnout");
	}

	/**
	 * Verify that simultaneously burning motors of different diameters remain
	 * associated with their own core and side-booster wakes.
	 */
	@Test
	public void testDifferentMotorSizesRemainInTheirOwnWakes() throws SimulationException {
		Rocket rocket = TestRockets.makeMultiStageEventTestRocket();
		FlightConfiguration selectedConfiguration = rocket.getSelectedConfiguration();
		Simulation simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(selectedConfiguration.getFlightConfigurationID());
		SimulationConditions simulationConditions = simulation.getOptions().toSimulationConditions();
		simulationConditions.setSimulation(simulation);
		FlightConfiguration configuration = simulation.getActiveConfiguration();
		SimulationStatus status = new SimulationStatus(configuration, simulationConditions);

		for (MotorClusterState motorState : status.getMotors()) {
			if (motorState.getIgnitionEvent() == IgnitionEvent.LAUNCH) {
				motorState.ignite(0);
			}
		}

		RK4SimulationStepper stepper = new RK4SimulationStepper();
		RK4SimulationStepper.DataStore store = new RK4SimulationStepper.DataStore();
		stepper.calculateFlightConditions(status, store);

		ComponentAssembly sustainerAssembly = (ComponentAssembly) rocket.getChild(0);
		ComponentAssembly coreBoosterAssembly = (ComponentAssembly) rocket.getChild(1);
		ComponentAssembly sideBoosterAssembly =
				(ComponentAssembly) rocket.getChild(1).getChild(0).getChild(1);
		double coreMotorArea = Math.PI * MathUtil.pow2(0.018 / 2);
		double sideMotorArea = 2 * Math.PI * MathUtil.pow2(0.013 / 2);

		assertEquals(0, store.flightConditions.getThrustingMotorBaseArea(sustainerAssembly), MathUtil.EPSILON,
				"The unignited sustainer motor must not affect any powered base area");
		assertEquals(coreMotorArea,
				store.flightConditions.getThrustingMotorBaseArea(coreBoosterAssembly), MathUtil.EPSILON,
				"The 18 mm center motor should belong to the core-booster wake");
		assertEquals(sideMotorArea,
				store.flightConditions.getThrustingMotorBaseArea(sideBoosterAssembly), MathUtil.EPSILON,
				"Both 13 mm side motors should belong to the side-booster wakes");
		assertEquals(coreMotorArea + sideMotorArea, store.flightConditions.getThrustingMotorBaseArea(),
				MathUtil.EPSILON, "The reported total should remain the sum of all independent wakes");
	}
}
