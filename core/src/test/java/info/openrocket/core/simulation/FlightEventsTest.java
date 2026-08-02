package info.openrocket.core.simulation;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

/**
 * Tests to verify that simulations contain all the expected flight events.
 */
public class FlightEventsTest extends BaseTestCase {
	
	private static final double EPSILON = 0.005;

	/**
	 * find the first child component of the given type
	 */
	RocketComponent findComponent(RocketComponent parent, Class componentClass) {
		RocketComponent ret = null;
		for (RocketComponent child : parent.getChildren()) {
			if (child.getClass() == componentClass) {
				ret = child;
			}
		}
		return ret;
	}
	
	/**
	 * Tests for a single stage design.
	 */
	@Test
	public void testSingleStage() throws SimulationException {
		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		final AxialStage stage = rocket.getStage(0);
		final InnerTube motorMountTube = (InnerTube) stage.getChild(1).getChild(2);
		final Parachute parachute = (Parachute) stage.getChild(1).getChild(3);

		Warning warn = new Warning.HighSpeedDeployment(80.6, parachute);
		final Simulation sim = new Simulation(rocket);
		sim.getOptions().setISAAtmosphere(true);
		sim.getOptions().setTimeStep(0.05);
		sim.setFlightConfigurationId(TestRockets.TEST_FCID_0);

		sim.simulate();

		// Test branch count
		final int branchCount = sim.getSimulatedData().getBranchCount();
		assertEquals(1, branchCount, " Single stage simulation invalid branch count ");

		final FlightEvent[] expectedEvents = new FlightEvent[] {
				new FlightEvent(FlightEvent.Type.LAUNCH, 0.0, rocket),
				new FlightEvent(FlightEvent.Type.IGNITION, 0.0, motorMountTube),
				new FlightEvent(FlightEvent.Type.LIFTOFF, 0.1275, null),
				new FlightEvent(FlightEvent.Type.LAUNCHROD, 0.13, null),
				new FlightEvent(FlightEvent.Type.BURNOUT, 2.0, motorMountTube),
				new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.0, stage),
				new FlightEvent(FlightEvent.Type.SIM_WARN, 2.0, null, warn),
				new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 2.001, parachute),
				new FlightEvent(FlightEvent.Type.APOGEE, 2.48, rocket),
				new FlightEvent(FlightEvent.Type.GROUND_HIT, 42.97, null),
				new FlightEvent(FlightEvent.Type.SIMULATION_END, 42.97, null)
		};

		checkEvents(expectedEvents, sim, 0);
		checkLastRecord(sim, 0);
	}
																			   
	/**
	 * Should not get a sim abort if recovery device deploys when upper stage motor never fires
	 */
	@Test
	public void testDeployNoMotorEnabled() throws SimulationException {
		final Rocket rocket = TestRockets.makeBeta();
		final AxialStage sustainer = (AxialStage) rocket.getChild(0);
		
		BodyTube sustainerBody = (BodyTube) findComponent(sustainer, BodyTube.class);
		assertNotNull(sustainerBody, "Failed to find sustainer body tube");

		Parachute chute = (Parachute) findComponent(sustainerBody, Parachute.class);
		assertNotNull(chute, "Failed to find sustainer parachute");

		// Set parachute to deploy 1/2 second after stage separation
		DeploymentConfiguration deploymentConfig = new DeploymentConfiguration();
		deploymentConfig.setDeployEvent(DeploymentConfiguration.DeployEvent.LOWER_STAGE_SEPARATION);
		deploymentConfig.setDeployDelay(0.5);
		chute.getDeploymentConfigurations().setDefault(deploymentConfig);
		
		InnerTube sustainerMount = (InnerTube) findComponent(sustainerBody, InnerTube.class);
		assertNotNull(sustainerMount, "Failed to find sustainer motor mount");

		AxialStage booster = (AxialStage) rocket.getChild(1);
		assertNotNull(booster, "failed to find booster axial stage");

		BodyTube boosterBody = (BodyTube) findComponent(booster, BodyTube.class);
		assertNotNull(boosterBody, "failed to find booster body tube");

		InnerTube boosterMount = (InnerTube) findComponent(boosterBody, InnerTube.class);
		assertNotNull(boosterMount, "failed to find booster motor mount");
		
		final Simulation sim = new Simulation(rocket);
		sim.getOptions().setISAAtmosphere(true);
		sim.getOptions().setTimeStep(0.05);
		sim.getOptions ().getAverageWindModel().setAverage(0.1);
		rocket.getSelectedConfiguration().setAllStages();
		FlightConfigurationId fcid = rocket.getSelectedConfiguration().getFlightConfigurationID();
		sim.setFlightConfigurationId(fcid);

		// Set sustainer motor to never fire
		MotorConfiguration motorConfig = sustainerMount.getDefaultMotorConfig();
		motorConfig.setIgnitionEvent(IgnitionEvent.NEVER);
		sustainerMount.setMotorConfig(motorConfig, fcid);

		Warning warn = new Warning.HighSpeedDeployment(53.2, chute);

		sim.simulate();

		// Test branch count
		final int expectedBranchCount = 2;
		final int actualBranchCount = sim.getSimulatedData().getBranchCount();
		
		// events whose time is too variable to check are given a time of the max sim time
		for (int b = 0; b < actualBranchCount; b++) {
			FlightEvent[] expectedEvents = switch (b) {
				// Sustainer
				case 0 -> new FlightEvent[]{
						new FlightEvent(FlightEvent.Type.LAUNCH, 0.0, rocket),
						new FlightEvent(FlightEvent.Type.IGNITION, 0.0, boosterMount),
						new FlightEvent(FlightEvent.Type.LIFTOFF, 0.09, null),
						new FlightEvent(FlightEvent.Type.LAUNCHROD, 0.09, null),
						new FlightEvent(FlightEvent.Type.BURNOUT, 2.0, boosterMount),
						new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.0, booster),
						new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.0, booster),
						new FlightEvent(FlightEvent.Type.SIM_WARN, 2.5, null, warn),
						new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 2.5, chute),
						new FlightEvent(FlightEvent.Type.APOGEE, 2.87, rocket),
						new FlightEvent(FlightEvent.Type.GROUND_HIT, 1200, null),
						new FlightEvent(FlightEvent.Type.SIMULATION_END, 1200, null)
				};

				// Stage
				case 1 -> new FlightEvent[]{
					new FlightEvent(FlightEvent.Type.IGNITION, 0.0, boosterMount),
					new FlightEvent(FlightEvent.Type.BURNOUT, 2.0, boosterMount),
					new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.0, booster),
					new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.0, booster),
					new FlightEvent(FlightEvent.Type.TUMBLE, 2.1, null),
					new FlightEvent(FlightEvent.Type.APOGEE, 3.5, rocket),
					new FlightEvent(FlightEvent.Type.GROUND_HIT, 1200, null),
					new FlightEvent(FlightEvent.Type.SIMULATION_END, 1200, null)
				};
				
				default -> throw new IllegalStateException("Invalid branch number " + b);
			};

			checkEvents(expectedEvents, sim, b);
			checkLastRecord(sim, b);
		}
	}
	

	/**
	 * Tests for a multi-stage design.
	 */
	@Test
	public void testMultiStage() throws SimulationException {
		final Rocket rocket = TestRockets.makeMultiStageEventTestRocket();

		final Simulation sim = new Simulation(rocket);
		sim.getOptions().setISAAtmosphere(true);
		sim.getOptions().setTimeStep(0.05);
		sim.getOptions ().getAverageWindModel().setAverage(0.1);
		rocket.getSelectedConfiguration().setAllStages();
		FlightConfigurationId fcid = rocket.getSelectedConfiguration().getFlightConfigurationID();
		sim.setFlightConfigurationId(fcid);

		sim.simulate();

		// Test branch count
		final int expectedBranchCount = 3;
		final int actualBranchCount = sim.getSimulatedData().getBranchCount();
		assertEquals(expectedBranchCount, actualBranchCount, " Multi-stage simulation invalid branch count ");

		final AxialStage sustainer = rocket.getStage(0);
		final BodyTube sustainerBody = (BodyTube) sustainer.getChild(1);

		final AxialStage centerBooster = rocket.getStage(1);
		final BodyTube centerBoosterBody = (BodyTube) centerBooster.getChild(0);

		final ParallelStage sideBoosters = (ParallelStage) centerBoosterBody.getChild(1);
		final BodyTube sideBoosterBodies = (BodyTube) sideBoosters.getChild(1);
		final Parachute sideChutes = (Parachute) sideBoosterBodies.getChild(0);

		SimulationAbort simAbort = new SimulationAbort(SimulationAbort.Cause.TUMBLE_UNDER_THRUST);
		
		Warning warn = new Warning.HighSpeedDeployment(53.2, sideChutes);
		
		// events whose time is too variable to check are given a time of the max sim time
		for (int b = 0; b < actualBranchCount; b++) {
			FlightEvent[] expectedEvents = switch (b) {
				// Sustainer
				case 0 -> new FlightEvent[]{
						new FlightEvent(FlightEvent.Type.LAUNCH, 0.0, rocket),
						new FlightEvent(FlightEvent.Type.IGNITION, 0.0, sideBoosterBodies),
						new FlightEvent(FlightEvent.Type.IGNITION, 0.01, centerBoosterBody),
						new FlightEvent(FlightEvent.Type.LIFTOFF, 0.06, null),
						new FlightEvent(FlightEvent.Type.LAUNCHROD, 0.0625, null),
						new FlightEvent(FlightEvent.Type.BURNOUT, 1.05, sideBoosterBodies),
						new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 1.05, sideBoosters),
						new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 1.05, sideBoosters),
						new FlightEvent(FlightEvent.Type.BURNOUT, 2.11, centerBoosterBody),
						new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.11, centerBooster),
						new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.11, centerBooster),
						new FlightEvent(FlightEvent.Type.IGNITION, 2.11, sustainerBody),
						new FlightEvent(FlightEvent.Type.SIM_ABORT, RK4SimulationStepper.RECOMMENDED_MAX_TIME, null, simAbort)
				};

				// Center Booster
				case 1 -> new FlightEvent[]{
						new FlightEvent(FlightEvent.Type.IGNITION, 0.01, centerBoosterBody),
						new FlightEvent(FlightEvent.Type.BURNOUT, 2.11, centerBoosterBody),
						new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.11, centerBooster),
						new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.11, centerBooster),
						new FlightEvent(FlightEvent.Type.TUMBLE, 2.38, null),
						new FlightEvent(FlightEvent.Type.APOGEE, 3.89, rocket),
						new FlightEvent(FlightEvent.Type.GROUND_HIT, RK4SimulationStepper.RECOMMENDED_MAX_TIME, null),
						new FlightEvent(FlightEvent.Type.SIMULATION_END, RK4SimulationStepper.RECOMMENDED_MAX_TIME, null)
				};

				// Side Boosters
				case 2 -> new FlightEvent[]{
						new FlightEvent(FlightEvent.Type.IGNITION, 0.0, sideBoosterBodies),
						new FlightEvent(FlightEvent.Type.BURNOUT, 1.05, sideBoosterBodies),
						new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 1.05, sideBoosters),
						new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 1.05, sideBoosters),
						new FlightEvent(FlightEvent.Type.SIM_WARN, 1.05, null, warn),
						new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 1.051, sideChutes),
						new FlightEvent(FlightEvent.Type.APOGEE, 1.35, rocket),
						new FlightEvent(FlightEvent.Type.GROUND_HIT, RK4SimulationStepper.RECOMMENDED_MAX_TIME, null),
						new FlightEvent(FlightEvent.Type.SIMULATION_END, RK4SimulationStepper.RECOMMENDED_MAX_TIME, null)
				};
				default -> throw new IllegalStateException("Invalid branch number " + b);
			};

			checkEvents(expectedEvents, sim, b);

			// We don't save the sim step params on an abort, so the last record's saved types will
			// be missing a lot of them.
			if (expectedEvents[expectedEvents.length - 1].getType() != FlightEvent.Type.SIM_ABORT) {
				checkLastRecord(sim, b);
			}
		}
	}

	/**
	 * A motor in a pod fires its ejection charge inside the pod, so it must not deploy a recovery
	 * device in the airframe the pod is strapped to.
	 * See <a href="https://github.com/openrocket/openrocket/issues/2092">GitHub issue #2092</a>.
	 */
	@Test
	public void testPodMotorDoesNotDeployParentRecoveryDevice() throws SimulationException {
		final Rocket rocket = TestRockets.makeEstesAlphaIIIWithMotorPods();
		final BodyTube bodyTube = (BodyTube) rocket.getStage(0).getChild(1);
		final Parachute parachute = (Parachute) bodyTube.getChild(3);

		final Simulation sim = simulate(rocket);

		assertEquals(1, sim.getSimulatedData().getBranchCount(), "Motor pods should not create a new branch");

		// The pod A10-0s fire their ejection charges the instant they burn out, the sustainer's B4-3
		// only fires its charge three seconds after its own burnout.
		final List<FlightEvent> ejections = getEvents(sim, FlightEvent.Type.EJECTION_CHARGE);
		assertEquals(2, ejections.size(), "Expected an ejection charge from both the pod and the sustainer motors");
		assertEquals(1.05, ejections.get(0).getTime(), EPSILON, "Pod motor ejection charge at wrong time");
		assertEquals(5.0, ejections.get(1).getTime(), EPSILON, "Sustainer motor ejection charge at wrong time");

		// Only the sustainer's charge may deploy the sustainer's parachute
		final List<FlightEvent> deployments = getEvents(sim, FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT);
		assertEquals(1, deployments.size(), "Parachute should be deployed exactly once");
		assertEquals(parachute, deployments.get(0).getSource(), "Wrong recovery device deployed");
		assertEquals(5.001, deployments.get(0).getTime(), EPSILON, "Parachute deployed at wrong time");

		// Deploying on the pod motors' charges would have blown the parachute open under thrust
		assertTrue(getEvents(sim, FlightEvent.Type.SIM_ABORT).isEmpty(), "Simulation should not have aborted");
		assertEquals(3.19, sim.getSimulatedData().getDeploymentVelocity(), 0.05, "Wrong deployment velocity");
	}

	/**
	 * Only the <em>first</em> ejection charge in an airframe deploys its recovery devices; the charge
	 * of a second motor with a longer delay must not deploy an already deployed parachute again.
	 * See <a href="https://github.com/openrocket/openrocket/issues/2092">GitHub issue #2092</a>.
	 */
	@Test
	public void testOnlyFirstEjectionChargeDeploys() throws SimulationException {
		final Rocket rocket = TestRockets.makeEstesAlphaIIIWithSecondMotor();
		final BodyTube bodyTube = (BodyTube) rocket.getStage(0).getChild(1);
		final Parachute parachute = (Parachute) bodyTube.getChild(3);

		final Simulation sim = simulate(rocket);

		// The B4-3 fires its charge at t = 5.0 s, the A10-5 alongside it only at t = 6.05 s
		final List<FlightEvent> ejections = getEvents(sim, FlightEvent.Type.EJECTION_CHARGE);
		assertEquals(2, ejections.size(), "Expected an ejection charge from both motors");
		assertEquals(5.0, ejections.get(0).getTime(), EPSILON, "First ejection charge at wrong time");
		assertEquals(6.05, ejections.get(1).getTime(), EPSILON, "Second ejection charge at wrong time");

		final List<FlightEvent> deployments = getEvents(sim, FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT);
		assertEquals(1, deployments.size(), "Parachute should be deployed exactly once");
		assertEquals(parachute, deployments.get(0).getSource(), "Wrong recovery device deployed");
		assertEquals(5.001, deployments.get(0).getTime(), EPSILON, "Parachute deployed at wrong time");

		// The deployment velocity must be the one of the real deployment, not of the phantom second one
		assertEquals(10.21, sim.getSimulatedData().getDeploymentVelocity(), 0.05, "Wrong deployment velocity");
	}

	/*
	 * run a simulation of the rocket's TEST_FCID_1 configuration under the conditions the tests in
	 * this class share
	 */
	private Simulation simulate(Rocket rocket) throws SimulationException {
		final Simulation sim = new Simulation(rocket);
		sim.getOptions().setISAAtmosphere(true);
		sim.getOptions().setTimeStep(0.05);
		sim.getOptions().getAverageWindModel().setAverage(0.1);
		sim.setFlightConfigurationId(TestRockets.TEST_FCID_1);

		sim.simulate();

		return sim;
	}

	/*
	 * collect the events of a single type from the simulation's main branch, in the order they occurred
	 */
	private List<FlightEvent> getEvents(Simulation sim, FlightEvent.Type type) {
		return sim.getSimulatedData().getBranch(0).getEvents().stream()
				.filter(e -> e.getType() == type)
				.toList();
	}

	/*
	 * make sure expected and actual events match
	 *
	 */
	private void checkEvents(FlightEvent[] expectedEvents, Simulation sim, int branchNo) {

		FlightEvent[] actualEvents = sim.getSimulatedData().getBranch(branchNo).getEvents().toArray(new FlightEvent[0]);
			
		// Test that all expected events are present, in the right order, at the right
		// time, from the right sources
		for (int i = 0; i < Math.min(expectedEvents.length, actualEvents.length); i++) {
			final FlightEvent expected = expectedEvents[i];
			Warning expectedWarning = null;
			if (expected.getType() == FlightEvent.Type.SIM_WARN) {
				expectedWarning = (Warning) expected.getData();
			}
			
			final FlightEvent actual = actualEvents[i];
			Warning actualWarning = null;
			if (actual.getType() == FlightEvent.Type.SIM_WARN) {
				actualWarning = (Warning) actual.getData();
			}
			
			assertSame(expected.getType(), actual.getType(),
					   "Branch " + branchNo + " FlightEvent " + i);

			assertTrue(((expectedWarning == null) && (actualWarning == null)) ||
					   ((expectedWarning != null) && expectedWarning.equals(actualWarning)) ||
					   ((actualWarning != null) && actualWarning.equals(expectedWarning)),
					   "Branch " + branchNo + " FlightEvent " + i + ": " + expectedWarning
					   + " not found; " + actualWarning + " found instead");

			if (expected.getTime() != RK4SimulationStepper.RECOMMENDED_MAX_TIME) {
				// event times that are dependent on simulation step time shouldn't be held to
				// tighter bounds than that
				double epsilon = (actual.getType() == FlightEvent.Type.TUMBLE) ||
					(actual.getType() == FlightEvent.Type.APOGEE) ||
					(actual.getType() == FlightEvent.Type.GROUND_HIT) ||
					(actual.getType() == FlightEvent.Type.SIMULATION_END) ? (5 * sim.getOptions().getTimeStep())
						: EPSILON;
				assertEquals(expected.getTime(), actual.getTime(), epsilon,
						"Branch " + branchNo + " FlightEvent " + i + " type " + expected.getType() + " has wrong time ");
			}

			// Test that the event sources are correct
			assertEquals(expected.getSource(), actual.getSource(),
					"Branch " + branchNo + " FlightEvent " + i + " type " + expected.getType() + " has wrong source ");

			// If it's a warning event, make sure the warning types match
			if (expected.getType() == FlightEvent.Type.SIM_WARN) {
				assertInstanceOf(Warning.class, actual.getData(), "SIM_WARN event data is not a Warning");
				assertEquals((expected.getData()), actual.getData(), "Expected: " + expected.getData() + " but was: " + actual.getData());
			}
		}

		// Test event count.  I don't think it's possible to fail here without having failed earlier, but just in case.
		assertEquals(expectedEvents.length, actualEvents.length, "Branch " + branchNo + " incorrect number of events ");
	}

	/*
	 * make sure no flight data variables are present in next-to-last flight record, but not last record, except
	 * sim step time which should be NaN on the last step
	 */
	private void checkLastRecord(Simulation sim, int b) {
		FlightData data = sim.getSimulatedData();
		FlightDataBranch branch = data.getBranch(b);
		List<String> mismatches = new ArrayList<>();
		int length = branch.getLength();
		for (FlightDataType type : branch.getTypes()) {
			if (!branch.getByIndex(type, length-2).isNaN() &&
				Double.isNaN(branch.getLast(type)) &&
				(type != FlightDataType.TYPE_TIME_STEP)) {
				mismatches.add(type.getName());
			}
		}
		assertTrue(mismatches.isEmpty(), "Final flight data variables " + mismatches + " are NaN");
		assertTrue(Double.isNaN(branch.getLast(FlightDataType.TYPE_TIME_STEP)), "Sim branch " + b + " final FlightDataType.TYPE_TIME_STEP isn't NaN");
	}
}
