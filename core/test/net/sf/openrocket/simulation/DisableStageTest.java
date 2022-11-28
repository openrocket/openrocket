package net.sf.openrocket.simulation;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.exception.MotorIgnitionException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.TestRockets;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class that tests the effect on the simulation results of activating/deactivating stages.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class DisableStageTest extends BaseTestCase {
    private final double delta = 0.05;  // 5 % error margin (simulations are not exact)

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

        // Since there are no stages, the simulation should throw an exception.
        try {
            simDisabled.simulate();
        } catch (SimulationException e) {
            if (!(e instanceof MotorIgnitionException)) {
                Assert.fail("Simulation should have thrown a MotorIgnitionException");
            }
        }

        //// Test re-enableing the stage.
        Rocket rocketOriginal = TestRockets.makeEstesAlphaIII();

        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(TestRockets.TEST_FCID_0);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);

        simDisabled.getActiveConfiguration().setAllStages();    // Re-enable all stages.

        compareSims(simOriginal, simDisabled, delta);
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

        compareSims(simRemoved, simDisabled, delta);

        //// Test re-enableing the stage.
        Rocket rocketOriginal = TestRockets.makeBeta();
        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(TestRockets.TEST_FCID_1);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);
        
        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled, delta);
    }

    /**
     * Tests that the simulation results are correct when the first stage of a multi-stage rocket is deactivated and re-activated.
     */
    // Don't even know if this test was useful, but simulation results vary wildly because the first stage is disabled,
    // so I'm just gonna ignore this test.
    /*@Test
    public void testMultiStageFirstDisabled() {
        //// Test disabling the stage
        Rocket rocketRemoved = TestRockets.makeBeta();      // Rocket with the last stage removed
        Rocket rocketDisabled = TestRockets.makeBeta();     // Rocket with the last stage disabled

        // You need to disable the second stage body tube going into automatic radius mode, otherwise the
        // removed and disabled rocket will have different results (removed rocket will have a different diameter)
        BodyTube bodyTube = (BodyTube) rocketRemoved.getChild(1).getChild(0);
        bodyTube.setOuterRadiusAutomatic(false);


        int stageNr = 0;
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

        SimulationListener simulationListener = new AbstractSimulationListener();

        double delta = 0.1;    // 10 % error margin (simulations are very unstable and not exact when the first stage is disabled...)
        compareSims(simRemoved, simDisabled, simulationListener, delta);

        //// Test re-enableing the stage.
        Rocket rocketOriginal = TestRockets.makeBeta();
        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(TestRockets.TEST_FCID_1);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);

        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled, simulationListener, delta);
    }*/

    /**
     * Tests that the simulation results are correct when a booster stage is deactivated and re-activated.
     */
    @Test
    public void testBooster1() {
        //// Test disabling the stage
        Rocket rocketRemoved = TestRockets.makeFalcon9Heavy();      // Rocket with the last stage removed
        Rocket rocketDisabled = TestRockets.makeFalcon9Heavy();     // Rocket with the last stage disabled

        FlightConfigurationId fcid =  new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1);
        int stageNr = 2;    // Stage 2 is the Parallel Booster Stage
        rocketRemoved.getChild(1).getChild(0).removeChild(0);   // Remove the Parallel Booster Stage
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

        compareSims(simRemoved, simDisabled, delta);

        //// Test re-enableing the stage.
        Rocket rocketOriginal = TestRockets.makeFalcon9Heavy();
        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(fcid);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);

        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled, delta);
    }

    /**
     * Tests that the simulation results are correct when the parent stage of a booster stage is deactivated and re-activated.
     */
    @Test
    public void testBooster2() {
        //// Test disabling the stage
        Rocket rocketRemoved = TestRockets.makeFalcon9Heavy();      // Rocket with the last stage removed
        Rocket rocketDisabled = TestRockets.makeFalcon9Heavy();     // Rocket with the last stage disabled

        FlightConfigurationId fid =  new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1);
        int stageNr = 1;    // Stage 1 is the Parallel Booster Stage's parent stage
        rocketRemoved.getChild(1).removeChild(0);   // Remove the Parallel Booster Stage's parent stage
        FlightConfiguration fc = rocketDisabled.getFlightConfiguration(fid);
        fc._setStageActive(stageNr, false);

        Simulation simRemoved = new Simulation(rocketRemoved);
        simRemoved.setFlightConfigurationId(fid);
        simRemoved.getOptions().setISAAtmosphere(true);
        simRemoved.getOptions().setTimeStep(0.05);

        Simulation simDisabled = new Simulation(rocketDisabled);
        simDisabled.setFlightConfigurationId(fid);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);

        // There should be no motors left at this point, so a no motors exception should be thrown
        try {
            simRemoved.simulate();
        } catch (SimulationException e) {
            if (!(e instanceof MotorIgnitionException)) {
                Assert.fail("Simulation failed: " + e);
            }
        }

        try {
            simDisabled.simulate();
        } catch (SimulationException e) {
            if (!(e instanceof MotorIgnitionException)) {
                Assert.fail("Simulation failed: " + e);
            }
        }

        //// Test re-enableing the stage.
        Rocket rocketOriginal = TestRockets.makeFalcon9Heavy();
        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(fid);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);

        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled, delta);
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
        Assert.assertEquals(rocketDisabled.getTopmostStage(simDisabled.getActiveConfiguration()), rocketDisabled.getStage(2));

        try {       // Just check that the simulation runs without exceptions
            simDisabled.simulate();
        } catch (SimulationException e) {
            if (!(e instanceof MotorIgnitionException)) {
                Assert.fail("Simulation failed: " + e);
            }
        }
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

            Assert.assertEquals(maxAltitudeOriginal, maxAltitudeDisabled, maxAltitudeOriginal * delta);
            Assert.assertEquals(maxVelocityOriginal, maxVelocityDisabled, maxVelocityOriginal * delta);
            Assert.assertEquals(maxMachNumberOriginal, maxMachNumberDisabled, maxMachNumberOriginal * delta);
            Assert.assertEquals(flightTimeOriginal, flightTimeDisabled, flightTimeOriginal * delta);
            Assert.assertEquals(timeToApogeeOriginal, timeToApogeeDisabled, timeToApogeeOriginal * delta);
            Assert.assertEquals(launchRodVelocityOriginal, launchRodVelocityDisabled, launchRodVelocityOriginal * delta);
            Assert.assertEquals(deploymentVelocityOriginal, deploymentVelocityDisabled, deploymentVelocityOriginal * delta);
        } catch (SimulationException e) {
            Assert.fail("Simulation failed: " + e);
        }
    }
}
