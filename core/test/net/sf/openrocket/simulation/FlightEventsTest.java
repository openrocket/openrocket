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
        final Simulation sim = new Simulation(rocket);
        sim.getOptions().setISAAtmosphere(true);
        sim.getOptions().setTimeStep(0.05);
        sim.setFlightConfigurationId(TestRockets.TEST_FCID_0);

        sim.simulate();

        // Test branch count
        final int branchCount = sim.getSimulatedData().getBranchCount();
        assertEquals(" Single stage simulation invalid branch count", 1, branchCount);

        final FlightEvent.Type[] expectedEventTypes = {FlightEvent.Type.LAUNCH, FlightEvent.Type.IGNITION, FlightEvent.Type.LIFTOFF,
                FlightEvent.Type.LAUNCHROD, FlightEvent.Type.BURNOUT, FlightEvent.Type.EJECTION_CHARGE, FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
                FlightEvent.Type.APOGEE, FlightEvent.Type.GROUND_HIT, FlightEvent.Type.SIMULATION_END};
        final double[] expectedEventTimes = {0.0, 0.0, 0.1275, 0.13, 2.0, 2.0, 2.001, 2.48338};       // Ground hit time is too variable, so don't include it
        final AxialStage stage = rocket.getStage(0);
        final InnerTube motorMountTube = (InnerTube) stage.getChild(1).getChild(2);
        final Parachute parachute = (Parachute) stage.getChild(1).getChild(3);
        final RocketComponent[] expectedSources = {rocket, motorMountTube, null, null, motorMountTube,
                stage, parachute, rocket, null, null};

        // Test event count
        FlightDataBranch branch = sim.getSimulatedData().getBranch(0);
        List<FlightEvent> eventList = branch.getEvents();
        List<FlightEvent.Type> eventTypes = eventList.stream().map(FlightEvent::getType).collect(Collectors.toList());
        assertEquals(" Single stage simulation invalid number of events", expectedEventTypes.length, eventTypes.size());

        // Test that all expected events are present, and in the right order
        for (int i = 0; i < expectedEventTypes.length; i++) {
            assertSame(" Flight type " + expectedEventTypes[i] + " not found in single stage simulation",
                    expectedEventTypes[i], eventTypes.get(i));
        }

        // Test that the event times are correct
        for (int i = 0; i < expectedEventTimes.length; i++) {
            assertEquals(" Flight type " + expectedEventTypes[i] + " has wrong time",
                    expectedEventTimes[i], eventList.get(i).getTime(), EPSILON);

        }

        // Test that the event sources are correct
        for (int i = 0; i < expectedSources.length; i++) {
            assertEquals(" Flight type " + expectedEventTypes[i] + " has wrong source",
                    expectedSources[i], eventList.get(i).getSource());
        }
    }

    /**
     * Tests for a multi-stage design.
     */
    @Test
    public void testMultiStage() throws SimulationException {
        final Rocket rocket = TestRockets.makeFalcon9Heavy();
        final Simulation sim = new Simulation(rocket);
        sim.getOptions().setISAAtmosphere(true);
        sim.getOptions().setTimeStep(0.05);
        rocket.getSelectedConfiguration().setAllStages();
        FlightConfigurationId fcid = rocket.getSelectedConfiguration().getFlightConfigurationID();
        sim.setFlightConfigurationId(fcid);

        sim.simulate();

        // Test branch count
        final int branchCount = sim.getSimulatedData().getBranchCount();
        assertEquals(" Multi-stage simulation invalid branch count", 3, branchCount);

        for (int b = 0; b < 3; b++) {
            final FlightEvent.Type[] expectedEventTypes;
            final double[] expectedEventTimes;
            final RocketComponent[] expectedSources;
            switch (b) {
                case 0:
                    expectedEventTypes = new FlightEvent.Type[]{FlightEvent.Type.LAUNCH, FlightEvent.Type.IGNITION, FlightEvent.Type.IGNITION,
                            FlightEvent.Type.LIFTOFF, FlightEvent.Type.LAUNCHROD, FlightEvent.Type.APOGEE,
                            FlightEvent.Type.BURNOUT, FlightEvent.Type.EJECTION_CHARGE, FlightEvent.Type.STAGE_SEPARATION,
                            FlightEvent.Type.BURNOUT, FlightEvent.Type.EJECTION_CHARGE, FlightEvent.Type.STAGE_SEPARATION,
                            FlightEvent.Type.TUMBLE, FlightEvent.Type.GROUND_HIT, FlightEvent.Type.SIMULATION_END};
                    expectedEventTimes = new double[]{0.0, 0.0, 0.0, 0.1225, 0.125, 1.735, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0};       // Tumble and ground hit time are too variable, so don't include it
                    final AxialStage coreStage = rocket.getStage(1);
                    final ParallelStage boosterStage = (ParallelStage) rocket.getStage(2);
                    final InnerTube boosterMotorTubes = (InnerTube) boosterStage.getChild(1).getChild(0);
                    final BodyTube coreBody = (BodyTube) coreStage.getChild(0);
                    expectedSources = new RocketComponent[]{rocket, boosterMotorTubes, coreBody, null, null, rocket,
                            boosterMotorTubes, boosterStage, boosterStage, coreBody, coreStage, coreStage,
                            null, null, null};
                    break;
                case 1:
                case 2:
                    expectedEventTypes = new FlightEvent.Type[]{FlightEvent.Type.TUMBLE, FlightEvent.Type.GROUND_HIT,
                            FlightEvent.Type.SIMULATION_END};
                    expectedEventTimes = new double[]{};       // Tumble and ground hit time are too variable, so don't include it
                    expectedSources = new RocketComponent[]{null, null, null};
                    break;
                default:
                    throw new IllegalStateException("Invalid branch number " + b);
            }

            // Test event count
            final FlightDataBranch branch = sim.getSimulatedData().getBranch(b);
            final List<FlightEvent> eventList = branch.getEvents();
            final List<FlightEvent.Type> eventTypes = eventList.stream().map(FlightEvent::getType).collect(Collectors.toList());
            assertEquals(" Multi-stage simulation, branch " + b + " invalid number of events", expectedEventTypes.length, eventTypes.size());

            // Test that all expected events are present, and in the right order
            for (int i = 0; i < expectedEventTypes.length; i++) {
                assertSame(" Flight type " + expectedEventTypes[i] + ", branch " + b + " not found in multi-stage simulation",
                        expectedEventTypes[i], eventTypes.get(i));
            }

            // Test that the event times are correct
            for (int i = 0; i < expectedEventTimes.length; i++) {
                assertEquals(" Flight type " + expectedEventTypes[i] + " has wrong time",
                        expectedEventTimes[i], eventList.get(i).getTime(), EPSILON);
            }

            // Test that the event sources are correct
            for (int i = 0; i < expectedSources.length; i++) {
                assertEquals(" Flight type " + expectedEventTypes[i] + " has wrong source",
                        expectedSources[i], eventList.get(i).getSource());
            }
        }
    }

}
