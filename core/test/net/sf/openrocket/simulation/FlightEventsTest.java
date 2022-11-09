package net.sf.openrocket.simulation;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
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
    /**
     * Tests for a single stage design.
     */
    @Test
    public void testSingleStage() throws SimulationException {
        Rocket rocket = TestRockets.makeEstesAlphaIII();
        Simulation sim = new Simulation(rocket);
        sim.getOptions().setISAAtmosphere(true);
        sim.getOptions().setTimeStep(0.05);
        sim.setFlightConfigurationId(TestRockets.TEST_FCID_0);

        sim.simulate();

        // Test branch count
        int branchCount = sim.getSimulatedData().getBranchCount();
        assertEquals(" Single stage simulation invalid branch count", 1, branchCount);

        FlightEvent.Type[] expectedEventTypes = {FlightEvent.Type.LAUNCH, FlightEvent.Type.IGNITION, FlightEvent.Type.LIFTOFF,
                FlightEvent.Type.LAUNCHROD, FlightEvent.Type.BURNOUT, FlightEvent.Type.EJECTION_CHARGE, FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
                FlightEvent.Type.APOGEE, FlightEvent.Type.GROUND_HIT, FlightEvent.Type.SIMULATION_END};

        // Test event count
        FlightDataBranch branch = sim.getSimulatedData().getBranch(0);
        List<FlightEvent> eventList = branch.getEvents();
        List<FlightEvent.Type> eventTypes = eventList.stream().map(FlightEvent::getType).collect(Collectors.toList());
        assertEquals(" Single stage simulation invalid number of events", expectedEventTypes.length, eventTypes.size());

        // Test that all expected events are present, and in the right order
        for (int i = 0; i < expectedEventTypes.length; i++) {
            assertSame(" Flight type " + expectedEventTypes[i] + " not found in single stage simulation",
                    eventTypes.get(i), expectedEventTypes[i]);
        }
    }

    /**
     * Tests for a multi-stage design.
     */
    @Test
    public void testMultiStage() throws SimulationException {
        Rocket rocket = TestRockets.makeFalcon9Heavy();
        Simulation sim = new Simulation(rocket);
        sim.getOptions().setISAAtmosphere(true);
        sim.getOptions().setTimeStep(0.05);
        rocket.getSelectedConfiguration().setAllStages();
        FlightConfigurationId fcid = rocket.getSelectedConfiguration().getFlightConfigurationID();
        sim.setFlightConfigurationId(fcid);

        sim.simulate();

        // Test branch count
        int branchCount = sim.getSimulatedData().getBranchCount();
        assertEquals(" Multi-stage simulation invalid branch count", 3, branchCount);

        for (int b = 0; b < 3; b++) {
            FlightEvent.Type[] expectedEventTypes;
            switch (b) {
                case 0:
                    expectedEventTypes = new FlightEvent.Type[]{FlightEvent.Type.LAUNCH, FlightEvent.Type.IGNITION, FlightEvent.Type.IGNITION,
                            FlightEvent.Type.LIFTOFF, FlightEvent.Type.LAUNCHROD, FlightEvent.Type.APOGEE,
                            FlightEvent.Type.BURNOUT, FlightEvent.Type.EJECTION_CHARGE, FlightEvent.Type.STAGE_SEPARATION,
                            FlightEvent.Type.BURNOUT, FlightEvent.Type.EJECTION_CHARGE, FlightEvent.Type.STAGE_SEPARATION,
                            FlightEvent.Type.TUMBLE, FlightEvent.Type.GROUND_HIT, FlightEvent.Type.SIMULATION_END};
                    break;
                case 1:
                case 2:
                    expectedEventTypes = new FlightEvent.Type[]{FlightEvent.Type.TUMBLE, FlightEvent.Type.GROUND_HIT,
                            FlightEvent.Type.SIMULATION_END};
                    break;
                default:
                    throw new IllegalStateException("Invalid branch number " + b);
            }

            // Test event count
            FlightDataBranch branch = sim.getSimulatedData().getBranch(b);
            List<FlightEvent> eventList = branch.getEvents();
            List<FlightEvent.Type> eventTypes = eventList.stream().map(FlightEvent::getType).collect(Collectors.toList());
            assertEquals(" Multi-stage simulation, branch " + b + " invalid number of events", expectedEventTypes.length, eventTypes.size());

            // Test that all expected events are present, and in the right order
            for (int i = 0; i < expectedEventTypes.length; i++) {
                assertSame(" Flight type " + expectedEventTypes[i] + ", branch " + b + " not found in multi-stage simulation",
                        eventTypes.get(i), expectedEventTypes[i]);
            }
        }
    }

}
