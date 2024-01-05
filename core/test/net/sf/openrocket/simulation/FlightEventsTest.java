package net.sf.openrocket.simulation;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.TestRockets;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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
        assertEquals(" Single stage simulation invalid branch count ", 1, branchCount);

        final FlightEvent[] expectedEvents = new FlightEvent[] {
			new FlightEvent(FlightEvent.Type.LAUNCH, 0.0, rocket),
			new FlightEvent(FlightEvent.Type.IGNITION, 0.0, motorMountTube),
			new FlightEvent(FlightEvent.Type.LIFTOFF, 0.1275, null),
			new FlightEvent(FlightEvent.Type.LAUNCHROD, 0.13, null),
			new FlightEvent(FlightEvent.Type.BURNOUT, 2.0, motorMountTube),
			new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.0, stage),
			new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 2.001, parachute),
			new FlightEvent(FlightEvent.Type.APOGEE, 2.48338, rocket),
			new FlightEvent(FlightEvent.Type.GROUND_HIT, 43.076, null),
			new FlightEvent(FlightEvent.Type.SIMULATION_END, 43.076, null)
		};
	
		checkEvents(expectedEvents, sim, 0);
	}

    /**
     * Tests for a multi-stage design.
     */
    @Test
    public void testMultiStage() throws SimulationException {
        final Rocket rocket = TestRockets.makeFalcon9Heavy();
		TestRockets.addCoreFins(rocket);
		
        final Simulation sim = new Simulation(rocket);
        sim.getOptions().setISAAtmosphere(true);
        sim.getOptions().setTimeStep(0.05);
        rocket.getSelectedConfiguration().setAllStages();
        FlightConfigurationId fcid = rocket.getSelectedConfiguration().getFlightConfigurationID();
        sim.setFlightConfigurationId(fcid);

        sim.simulate();

        // Test branch count
        final int branchCount = sim.getSimulatedData().getBranchCount();
        assertEquals(" Multi-stage simulation invalid branch count ", 3, branchCount);

		final AxialStage coreStage = rocket.getStage(1);
		final ParallelStage boosterStage = (ParallelStage) rocket.getStage(2);
		final InnerTube boosterMotorTubes = (InnerTube) boosterStage.getChild(1).getChild(0);
		final BodyTube coreBody = (BodyTube) coreStage.getChild(0);

		// events whose time is too variable to check are given a time of 1200
        for (int b = 0; b < 3; b++) {
			FlightEvent[] expectedEvents;
            switch (b) {
                // Sustainer (payload fairing stage)
                case 0:
					expectedEvents = new FlightEvent[] {
						new FlightEvent(FlightEvent.Type.LAUNCH, 0.0, rocket),
						new FlightEvent(FlightEvent.Type.IGNITION, 0.0, boosterMotorTubes),
						new FlightEvent(FlightEvent.Type.IGNITION, 0.0, coreBody),
						new FlightEvent(FlightEvent.Type.LIFTOFF, 0.1275, null),
						new FlightEvent(FlightEvent.Type.LAUNCHROD, 0.130, null),
						new FlightEvent(FlightEvent.Type.BURNOUT, 2.0, boosterMotorTubes),
						new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.0, boosterStage),
						new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.0, boosterStage),
						new FlightEvent(FlightEvent.Type.BURNOUT, 2.0, coreBody),
						new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.0, coreStage),						
						new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.0, coreStage),
						new FlightEvent(FlightEvent.Type.APOGEE, 2.12, rocket),
						new FlightEvent(FlightEvent.Type.TUMBLE, 2.202, null),
						new FlightEvent(FlightEvent.Type.GROUND_HIT, 14.12, null),
						new FlightEvent(FlightEvent.Type.SIMULATION_END, 14.12, null)
					};
                    break;
                // Core stage
                case 1:
					expectedEvents = new FlightEvent[] {
                        new FlightEvent(FlightEvent.Type.IGNITION, 0.0, coreBody),
                        new FlightEvent(FlightEvent.Type.BURNOUT, 2.0, coreBody),
                        new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.0, coreStage),
						new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.0, coreStage),
						new FlightEvent(FlightEvent.Type.APOGEE, 2.1, rocket),
						new FlightEvent(FlightEvent.Type.TUMBLE, 2.15, null),
						new FlightEvent(FlightEvent.Type.GROUND_HIT, 7.26, null),
						new FlightEvent(FlightEvent.Type.SIMULATION_END, 7.26, null)
					};
                    break;
                // Booster stage
                case 2:
					expectedEvents = new FlightEvent[] {
                        new FlightEvent(FlightEvent.Type.IGNITION, 0.0, boosterMotorTubes),
                        new FlightEvent(FlightEvent.Type.BURNOUT, 2.0, boosterMotorTubes),
                        new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, 2.0, boosterStage),
						new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 2.0, boosterStage),
						new FlightEvent(FlightEvent.Type.TUMBLE, 2.05, null),
						new FlightEvent(FlightEvent.Type.APOGEE, 2.06, rocket),
						new FlightEvent(FlightEvent.Type.GROUND_HIT, 12.0, null),
						new FlightEvent(FlightEvent.Type.SIMULATION_END, 12.0, null)
					};
                    break;
                default:
                    throw new IllegalStateException("Invalid branch number " + b);
            }

			checkEvents(expectedEvents, sim, b);
		}
	}

	private void checkEvents(FlightEvent[] expectedEvents, Simulation sim, int branchNo)	{

		FlightEvent[] actualEvents = sim.getSimulatedData().getBranch(branchNo).getEvents().toArray(new FlightEvent[0]);
		
		// Test event count
		assertEquals("Branch " + branchNo + " invalid number of events ", expectedEvents.length, actualEvents.length);

		// Test that all expected events are present, in the right order, at the right time, from the right sources
		for (int i = 0; i < actualEvents.length; i++) {
			final FlightEvent expected = expectedEvents[i];
			final FlightEvent actual = actualEvents[i];
			assertSame("Branch " + branchNo + " FlightEvent " + i + " type " + expected.getType() +  " not found; FlightEvent " + actual.getType() + " found instead",
					   expected.getType(), actual.getType());

			if (1200 != expected.getTime()) {
				// event times that are dependent on simulation step time shouldn't be held to tighter bounds than that
				double epsilon = (actual.getType() == FlightEvent.Type.TUMBLE) ||
					(actual.getType() == FlightEvent.Type.APOGEE) ||
					(actual.getType() == FlightEvent.Type.GROUND_HIT) ||
					(actual.getType() == FlightEvent.Type.SIMULATION_END) ? sim.getOptions().getTimeStep() : EPSILON;
				assertEquals("Branch " + branchNo + " FlightEvent " + i + " type " + expected.getType() + " has wrong time ",
							 expected.getTime(), actual.getTime(), epsilon);
			}

			// Test that the event sources are correct
			assertEquals("Branch " + branchNo + " FlightEvent " + i + " type " + expected.getType() + " has wrong source ",
						 expected.getSource(), actual.getSource());
		}
	}
}
