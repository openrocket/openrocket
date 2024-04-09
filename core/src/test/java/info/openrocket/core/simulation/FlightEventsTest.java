package info.openrocket.core.simulation;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;;

/**
 * Tests to verify that simulations contain all the expected flight events.
 */
public class FlightEventsTest extends BaseTestCase {
	private static final double EPSILON = 0.005;

	/**
	 * Tests for a single stage design.
	 */
	@Test
	public void testSingleStage() throws SimulationException {
		final Rocket rocket = TestRockets.makeEstesAlphaIII();
		final AxialStage stage = rocket.getStage(0);
		final InnerTube motorMountTube = (InnerTube) stage.getChild(1).getChild(2);
		final Parachute parachute = (Parachute) stage.getChild(1).getChild(3);

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
				new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 2.001, parachute),
				new FlightEvent(FlightEvent.Type.APOGEE, 2.48, rocket),
				new FlightEvent(FlightEvent.Type.GROUND_HIT, 42.97, null),
				new FlightEvent(FlightEvent.Type.SIMULATION_END, 42.97, null)
		};

		checkEvents(expectedEvents, sim, 0);
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
		rocket.getSelectedConfiguration().setAllStages();
		FlightConfigurationId fcid = rocket.getSelectedConfiguration().getFlightConfigurationID();
		sim.setFlightConfigurationId(fcid);

		sim.simulate();

		/*
		for (int b = 0; b < sim.getSimulatedData().getBranchCount(); b++) {
			System.out.println("branch " + b);
			for (FlightEvent event : sim.getSimulatedData().getBranch(b).getEvents()) {
				System.out.println("    " + event);
			}
		}
		*/

		// Test branch count
		final int branchCount = sim.getSimulatedData().getBranchCount();
		assertEquals(3, branchCount, " Multi-stage simulation invalid branch count ");

		final AxialStage sustainer = rocket.getStage(0);
		final BodyTube sustainerBody = (BodyTube) sustainer.getChild(1);
		final Parachute sustainerChute = (Parachute) sustainerBody.getChild(0);

		final AxialStage centerBooster = rocket.getStage(1);
		final BodyTube centerBoosterBody = (BodyTube) centerBooster.getChild(0);

		final ParallelStage sideBoosters = (ParallelStage) centerBoosterBody.getChild(1);
		final BodyTube sideBoosterBodies = (BodyTube) sideBoosters.getChild(1);

		// events whose time is too variable to check are given a time of 1200
		for (int b = 0; b < 2; b++) {
			FlightEvent[] expectedEvents;
			switch (b) {
				// Sustainer
				case 0:
					expectedEvents = new FlightEvent[] {
							new FlightEvent(FlightEvent.Type.LAUNCH, 0.0, rocket),
							new FlightEvent(FlightEvent.Type.IGNITION, 0.0, sideBoosterBodies),
							new FlightEvent(FlightEvent.Type.IGNITION, 0.01, centerBoosterBody),
							new FlightEvent(FlightEvent.Type.LIFTOFF, 0.8255, null),
							new FlightEvent(FlightEvent.Type.LAUNCHROD, 0.828, null),
							new FlightEvent(FlightEvent.Type.BURNOUT, 0.85, sideBoosterBodies),
							new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 0.85, sideBoosters),
							new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 0.85, sideBoosters),
							new FlightEvent(FlightEvent.Type.BURNOUT, 2.01, centerBoosterBody),
							new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.01, centerBooster),
							new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.01, centerBooster),
							new FlightEvent(FlightEvent.Type.IGNITION, 2.01, sustainerBody),
							new FlightEvent(FlightEvent.Type.BURNOUT, 4.01, sustainerBody),
							new FlightEvent(FlightEvent.Type.APOGEE, 8.5, rocket),
							new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 9.01, sustainer),
							new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 9.01, sustainerChute),
							new FlightEvent(FlightEvent.Type.GROUND_HIT, 83.27, null),
							new FlightEvent(FlightEvent.Type.SIMULATION_END, 83.27, null)
					};
					break;

				// Center Booster
				case 1:
					expectedEvents = new FlightEvent[] {
							new FlightEvent(FlightEvent.Type.IGNITION, 0.01, centerBoosterBody),
							new FlightEvent(FlightEvent.Type.BURNOUT, 2.01, centerBoosterBody),
							new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.01, centerBooster),
							new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.01, centerBooster),
							new FlightEvent(FlightEvent.Type.TUMBLE, 2.85, null),
							new FlightEvent(FlightEvent.Type.APOGEE, 3.78, rocket),
							new FlightEvent(FlightEvent.Type.GROUND_HIT, 9.0, null),
							new FlightEvent(FlightEvent.Type.SIMULATION_END, 9.0, null)
					};
					break;

				// Side Boosters
				case 2:
					expectedEvents = new FlightEvent[] {
							new FlightEvent(FlightEvent.Type.IGNITION, 0.0, sideBoosterBodies),
							new FlightEvent(FlightEvent.Type.BURNOUT, 0.85, sideBoosterBodies),
							new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 0.85, sideBoosters),
							new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 0.85, sideBoosters),
							new FlightEvent(FlightEvent.Type.TUMBLE, 1.0, null),
							new FlightEvent(FlightEvent.Type.APOGEE, 1.01, rocket),
							new FlightEvent(FlightEvent.Type.GROUND_HIT, 1.21, null),
							new FlightEvent(FlightEvent.Type.SIMULATION_END, 1.21, null)
					};
					break;

				default:
					throw new IllegalStateException("Invalid branch number " + b);
			}

			checkEvents(expectedEvents, sim, b);
		}
	}

	private void checkEvents(FlightEvent[] expectedEvents, Simulation sim, int branchNo) {

		FlightEvent[] actualEvents = sim.getSimulatedData().getBranch(branchNo).getEvents().toArray(new FlightEvent[0]);

		// Test event count
		assertEquals(expectedEvents.length, actualEvents.length, "Branch " + branchNo + " invalid number of events ");

		// Test that all expected events are present, in the right order, at the right
		// time, from the right sources
		for (int i = 0; i < actualEvents.length; i++) {
			final FlightEvent expected = expectedEvents[i];
			final FlightEvent actual = actualEvents[i];
			assertSame(expected.getType(), actual.getType(),
					"Branch " + branchNo + " FlightEvent " + i + " type " + expected.getType()
							+ " not found; FlightEvent " + actual.getType() + " found instead");

			if (1200 != expected.getTime()) {
				// event times that are dependent on simulation step time shouldn't be held to
				// tighter bounds than that
				double epsilon = (actual.getType() == FlightEvent.Type.TUMBLE) ||
						(actual.getType() == FlightEvent.Type.APOGEE) ||
						(actual.getType() == FlightEvent.Type.GROUND_HIT) ||
						(actual.getType() == FlightEvent.Type.SIMULATION_END) ? sim.getOptions().getTimeStep()
						: EPSILON;
				assertEquals(expected.getTime(), actual.getTime(), epsilon,
						"Branch " + branchNo + " FlightEvent " + i + " type " + expected.getType() + " has wrong time ");
			}

			// Test that the event sources are correct
			assertEquals(expected.getSource(), actual.getSource(),
					"Branch " + branchNo + " FlightEvent " + i + " type " + expected.getType() + " has wrong source ");
		}
	}
}
