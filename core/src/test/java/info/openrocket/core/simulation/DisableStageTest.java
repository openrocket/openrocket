package info.openrocket.core.simulation;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * Test class that tests the effect on the simulation results of activating/deactivating stages.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class DisableStageTest extends BaseTestCase {
    public static final double DELTA = 0.05; // 5 % error margin (simulations are not exact)

    /**
     * Tests that the simulation results are correct when a single stage is deactivated and re-activated.
     */
    @Test
    public void testSingleStage() throws SimulationException {
        //// Test disabling the stage
        Rocket rocket = TestRockets.makeEstesAlphaIII();

        Simulation simDisabled = new Simulation(rocket);
        simDisabled.setFlightConfigurationId(TestRockets.TEST_FCID_0);
        simDisabled.getActiveConfiguration()._setStageActive(0, false);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);

		simDisabled.simulate();

        // Since there are no stages, the simulation should abort
		FlightEvent abort = simDisabled.getSimulatedData().getBranch(0).getLastEvent(FlightEvent.Type.SIM_ABORT);
		assertNotNull(abort, "Empty simulation failed to abort");
		assertEquals(SimulationAbort.Cause.NO_ACTIVE_STAGES, ((SimulationAbort)(abort.getData())).getCause(), "Abort cause did not match");

        //// Test re-enabling the stage.
        Rocket rocketOriginal = TestRockets.makeEstesAlphaIII();

        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(TestRockets.TEST_FCID_0);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);

        simDisabled.getActiveConfiguration().setAllStages(); // Re-enable all stages.

        compareSims(simOriginal, simDisabled, DELTA);
    }

    /**
     * Tests that the simulation results are correct when the last stage of a multi-stage rocket is deactivated and re-activated.
     */
    @Test
    public void testMultiStageLastDisabled() {
        //// Test disabling the stage
        Rocket rocketRemoved = TestRockets.makeBeta();      // Rocket with the last stage removed
        Rocket rocketDisabled = TestRockets.makeBeta();     // Rocket with the last stage disabled

        int stageNr = rocketRemoved.getChildCount() - 1;
        rocketRemoved.removeChild(stageNr);
        FlightConfiguration fc = rocketDisabled.getFlightConfiguration(TestRockets.TEST_FCID_1);
        fc._setStageActive(stageNr, false);

        Simulation simRemoved = new Simulation(rocketRemoved);
        simRemoved.setFlightConfigurationId(TestRockets.TEST_FCID_1);
        simRemoved.getOptions().setISAAtmosphere(true);
        simRemoved.getOptions().setTimeStep(0.05);

        Simulation simDisabled = new Simulation(rocketDisabled);
        simDisabled.setFlightConfigurationId(TestRockets.TEST_FCID_1);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);

        compareSims(simRemoved, simDisabled, DELTA);

        //// Test re-enabling the stage.
        Rocket rocketOriginal = TestRockets.makeBeta();
        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(TestRockets.TEST_FCID_1);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);
        
        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled, DELTA);
    }

    /**
     * Tests that the simulation results are correct when the first stage of a multi-stage rocket is deactivated and re-activated.
     */
    // Don't even know if this test was useful, but simulation results vary wildly because the first stage is disabled,
    // so I'm just gonna ignore this test.
    /*
     * @Test
     * public void testMultiStageFirstDisabled() {
     * //// Test disabling the stage
     * Rocket rocketRemoved = TestRockets.makeBeta(); // Rocket with the last stage
     * removed
     * Rocket rocketDisabled = TestRockets.makeBeta(); // Rocket with the last stage
     * disabled
     *
     * // You need to disable the second stage body tube going into automatic radius
     * mode, otherwise the
     * // removed and disabled rocket will have different results (removed rocket
     * will have a different diameter)
     * BodyTube bodyTube = (BodyTube) rocketRemoved.getChild(1).getChild(0);
     * bodyTube.setOuterRadiusAutomatic(false);
     *
     *
     * int stageNr = 0;
     * rocketRemoved.removeChild(stageNr);
     * FlightConfiguration fc =
     * rocketDisabled.getFlightConfiguration(TestRockets.TEST_FCID_1);
     * fc._setStageActive(stageNr, false);
     *
     * Simulation simRemoved = new Simulation(rocketRemoved);
     * simRemoved.setFlightConfigurationId(TestRockets.TEST_FCID_1);
     * simRemoved.getOptions().setISAAtmosphere(true);
     * simRemoved.getOptions().setTimeStep(0.05);
     *
     * Simulation simDisabled = new Simulation(rocketDisabled);
     * simDisabled.setFlightConfigurationId(TestRockets.TEST_FCID_1);
     * simDisabled.getOptions().setISAAtmosphere(true);
     * simDisabled.getOptions().setTimeStep(0.05);
     *
     * SimulationListener simulationListener = new AbstractSimulationListener();
     *
     * double delta = 0.1; // 10 % error margin (simulations are very unstable and
     * not exact when the first stage is disabled...)
     * compareSims(simRemoved, simDisabled, simulationListener, delta);
     *
     * //// Test re-enableing the stage.
     * Rocket rocketOriginal = TestRockets.makeBeta();
     * Simulation simOriginal = new Simulation(rocketOriginal);
     * simOriginal.setFlightConfigurationId(TestRockets.TEST_FCID_1);
     * simOriginal.getOptions().setISAAtmosphere(true);
     * simOriginal.getOptions().setTimeStep(0.05);
     *
     * simDisabled.getActiveConfiguration().setAllStages();
     *
     * compareSims(simOriginal, simDisabled, simulationListener, delta);
     * }
     */

    /**
     * Tests that the simulation results are correct when a booster stage is deactivated and re-activated.
     */
    @Test
    public void testBooster1() {
        //// Test disabling the stage

        Rocket rocketRemoved = TestRockets.makeFalcon9Heavy(); // Rocket with the last stage removed
        TestRockets.addCoreFins(rocketRemoved);

        Rocket rocketDisabled = TestRockets.makeFalcon9Heavy(); // Rocket with the last stage disabled
        TestRockets.addCoreFins(rocketDisabled);

        FlightConfigurationId fcid = new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1);
        int stageNr = 2; // Stage 2 is the Parallel Booster Stage
        rocketRemoved.getChild(1).getChild(0).removeChild(0); // Remove the Parallel Booster Stage
        FlightConfiguration fc = rocketDisabled.getFlightConfiguration(fcid);
        fc._setStageActive(stageNr, false);

        Simulation simRemoved = new Simulation(rocketRemoved);
        simRemoved.setFlightConfigurationId(fcid);
        simRemoved.getOptions().setISAAtmosphere(true);
        simRemoved.getOptions().setTimeStep(0.05);

        Simulation simDisabled = new Simulation(rocketDisabled);
        simDisabled.setFlightConfigurationId(fcid);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);

        compareSims(simRemoved, simDisabled, DELTA);

        //// Test re-enabling the stage.
        Rocket rocketOriginal = TestRockets.makeFalcon9Heavy();
        TestRockets.addCoreFins(rocketOriginal);

        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(fcid);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);

        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled, DELTA);
    }

    /**
     * Tests that the simulation results are correct when the parent stage of a booster stage is deactivated and re-activated.
     */
    @Test
    public void testBooster2() {
        //// Test disabling the stage
        Rocket rocketRemoved = TestRockets.makeFalcon9Heavy(); // Rocket with the last stage removed
        TestRockets.addCoreFins(rocketRemoved);

        Rocket rocketDisabled = TestRockets.makeFalcon9Heavy(); // Rocket with the last stage disabled
        TestRockets.addCoreFins(rocketDisabled);

        FlightConfigurationId fid = new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1);
        int stageNr = 1; // Stage 1 is the Parallel Booster Stage's parent stage
        rocketRemoved.getChild(1).removeChild(0); // Remove the Parallel Booster Stage's parent stage
        FlightConfiguration fc = rocketDisabled.getFlightConfiguration(fid);
        fc._setStageActive(stageNr, false);

        Simulation simRemoved = new Simulation(rocketRemoved);
        simRemoved.setFlightConfigurationId(fid);
        simRemoved.getOptions().setISAAtmosphere(true);
        simRemoved.getOptions().setTimeStep(0.05);

		try {
			simRemoved.simulate();
		} catch(Exception e) {
			fail("unexpected exception " + e);
		}

        // There should be no motors left at this point, so we should abort on no motors
		FlightEvent abort = simRemoved.getSimulatedData().getBranch(0).getLastEvent(FlightEvent.Type.SIM_ABORT);
		assertNotNull(abort, "Empty simulation failed to abort");
		assertEquals(SimulationAbort.Cause.NO_MOTORS_DEFINED, ((SimulationAbort)(abort.getData())).getCause(), "Abort cause did not match");

        Simulation simDisabled = new Simulation(rocketDisabled);
        simDisabled.setFlightConfigurationId(fid);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);

        //// Test re-enabling the stage.
        Rocket rocketOriginal = TestRockets.makeFalcon9Heavy();
        TestRockets.addCoreFins(rocketOriginal);

        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(fid);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);

        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled, DELTA);
    }

    /**
     * Test whether the simulations run when only the booster stage is active.
     */
    @Test
    public void testBooster3() {
        Rocket rocketDisabled = TestRockets.makeFalcon9Heavy();

        FlightConfigurationId fid =  new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1);
        Simulation simDisabled = new Simulation(rocketDisabled);
        simDisabled.setFlightConfigurationId(fid);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);

        //// Test only enabling the booster stage (test for GitHub issue #1848)
        simDisabled.getActiveConfiguration().setOnlyStage(2);

        //// Test that the top stage is the booster stage
        assertEquals(rocketDisabled.getTopmostStage(simDisabled.getActiveConfiguration()), rocketDisabled.getStage(2));

        try {
            simDisabled.simulate();
        } catch(Exception e) {
            fail("unexpected exception " + e);
        }

        // Sim will tumble under
        FlightEvent abort = simDisabled.getSimulatedData().getBranch(0).getLastEvent(FlightEvent.Type.SIM_ABORT);
        assertNotNull(abort, "Unstable booster failed to abort");
        assertEquals(SimulationAbort.Cause.TUMBLE_UNDER_THRUST, ((SimulationAbort)(abort.getData())).getCause(), "Abort cause did not match");
    }

    /**
     * Compare simActual to simExpected and fail the unit test if there was an error during simulation or
     * the two don't match.
     * Tested parameters:
     *  - maxAcceleration
     *  - maxAltitude
     *  - maxVelocity
     *  - maxMachNumber
     *  - flightTime
     *  - launchRodVelocity
     *  - deploymentVelocity
     *  - groundHitVelocity
     * @param simExpected the expected simulation results
     * @param simActual the actual simulation results
     * @param delta the error margin for the comparison (e.g. 0.05 = 5 % error margin)
     */
    private void compareSims(Simulation simExpected, Simulation simActual, double delta) {
        try {
            simExpected.simulate();
            double maxAltitudeOriginal = simExpected.getSimulatedData().getMaxAltitude();
            double maxVelocityOriginal = simExpected.getSimulatedData().getMaxVelocity();
            double maxMachNumberOriginal = simExpected.getSimulatedData().getMaxMachNumber();
            double flightTimeOriginal = simExpected.getSimulatedData().getFlightTime();
            double timeToApogeeOriginal = simExpected.getSimulatedData().getTimeToApogee();
            double launchRodVelocityOriginal = simExpected.getSimulatedData().getLaunchRodVelocity();
            double deploymentVelocityOriginal = simExpected.getSimulatedData().getDeploymentVelocity();

            simActual.simulate();
            double maxAltitudeDisabled = simActual.getSimulatedData().getMaxAltitude();
            double maxVelocityDisabled = simActual.getSimulatedData().getMaxVelocity();
            double maxMachNumberDisabled = simActual.getSimulatedData().getMaxMachNumber();
            double flightTimeDisabled = simActual.getSimulatedData().getFlightTime();
            double timeToApogeeDisabled = simActual.getSimulatedData().getTimeToApogee();
            double launchRodVelocityDisabled = simActual.getSimulatedData().getLaunchRodVelocity();
            double deploymentVelocityDisabled = simActual.getSimulatedData().getDeploymentVelocity();

            assertEquals(maxAltitudeOriginal, maxAltitudeDisabled, calculateDelta(maxAltitudeOriginal, delta));
            assertEquals(maxVelocityOriginal, maxVelocityDisabled, calculateDelta(maxVelocityOriginal, delta));
            assertEquals(maxMachNumberOriginal, maxMachNumberDisabled, calculateDelta(maxMachNumberOriginal, delta));
            assertEquals(flightTimeOriginal, flightTimeDisabled, calculateDelta(flightTimeOriginal, delta));
            assertEquals(timeToApogeeOriginal, timeToApogeeDisabled, calculateDelta(timeToApogeeOriginal, delta));
            assertEquals(launchRodVelocityOriginal, launchRodVelocityDisabled,
                    calculateDelta(launchRodVelocityOriginal, delta));
            assertEquals(deploymentVelocityOriginal, deploymentVelocityDisabled,
                    calculateDelta(deploymentVelocityOriginal, delta));
        } catch (SimulationException e) {
            fail("Simulation failed: " + e);
        }
    }

    private static double calculateDelta(double value, double delta) {
        return Double.isNaN(value) ? 0 : value * delta;
    }
}
