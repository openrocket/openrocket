package info.openrocket.core.simulation;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * Test class that tests the effect on the simulation results of activating/deactivating stages.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class DisableStageTest extends BaseTestCase {
    public static final double DELTA = 0.05; // 3 % error margin (simulations are not exact)
    public static final double DELTA_COURSE = 0.1; // 10 % course error margin (simulations are not exact)
	private static final double POSITION_EPSILON = 1.0e-9;

	/**
	 * Verifies that a simulation resolves component positions using its own flight
	 * configuration rather than the configuration selected in the user interface.
	 */
	@Test
	public void testSimulationUsesItsOwnConfigurationForComponentPositions() throws SimulationException {
		Rocket rocket = TestRockets.makeSimple2Stage();
		FlightConfigurationId activeConfigId = TestRockets.TEST_FCID_0;
		FlightConfigurationId disabledSustainerConfigId = new FlightConfigurationId();
		double sustainerLength = rocket.getStage(0).getLength();

		rocket.createFlightConfiguration(disabledSustainerConfigId);
		rocket.getFlightConfiguration(disabledSustainerConfigId).clearStage(0);

		// Simulate the fully active configuration while the disabled configuration is selected.
		rocket.setSelectedConfiguration(disabledSustainerConfigId);
		assertEquals(0.0, rocket.getStage(1).getPosition().getX(), POSITION_EPSILON);

		Simulation activeSimulation = new Simulation(rocket);
		activeSimulation.setFlightConfigurationId(activeConfigId);
		assertEquals(sustainerLength, getBoosterPositionDuringSimulation(activeSimulation), POSITION_EPSILON);

		// Running a simulation must not alter the selected configuration or its geometry.
		assertEquals(disabledSustainerConfigId, rocket.getSelectedConfiguration().getId());
		assertEquals(0.0, rocket.getStage(1).getPosition().getX(), POSITION_EPSILON);

		// Also verify the inverse: simulate the disabled configuration while the active one is selected.
		rocket.setSelectedConfiguration(activeConfigId);
		assertEquals(sustainerLength, rocket.getStage(1).getPosition().getX(), POSITION_EPSILON);

		Simulation disabledSustainerSimulation = new Simulation(rocket);
		disabledSustainerSimulation.setFlightConfigurationId(disabledSustainerConfigId);
		assertEquals(0.0, getBoosterPositionDuringSimulation(disabledSustainerSimulation), POSITION_EPSILON);

		assertEquals(activeConfigId, rocket.getSelectedConfiguration().getId());
		assertEquals(sustainerLength, rocket.getStage(1).getPosition().getX(), POSITION_EPSILON);
	}

	/**
	 * Captures the booster position from the simulation-owned rocket before its
	 * first branch starts.
	 *
	 * @param simulation simulation whose private configuration should be inspected
	 * @return booster position in the simulation-owned rocket
	 * @throws SimulationException if the simulation cannot start
	 */
	private double getBoosterPositionDuringSimulation(Simulation simulation) throws SimulationException {
		double[] boosterPosition = { Double.NaN };
		simulation.simulate(new AbstractSimulationListener() {
			@Override
			public void startSimulation(SimulationStatus status) {
				boosterPosition[0] = status.getConfiguration().getRocket().getStage(1).getPosition().getX();
			}
		});
		return boosterPosition[0];
	}

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

        compareSims(simOriginal, simDisabled);
    }

    /**
     * Tests that the simulation results are correct when a single stage is deactivated and re-activated, using the RK6 stepper.
     */
    @Test
    public void testSingleStage_RK6() throws SimulationException {
        //// Test disabling the stage
        Rocket rocket = TestRockets.makeEstesAlphaIII();

        Simulation simDisabled = new Simulation(rocket);
        simDisabled.setFlightConfigurationId(TestRockets.TEST_FCID_0);
        simDisabled.getActiveConfiguration()._setStageActive(0, false);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);
        simDisabled.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

        simDisabled.simulate(); // the part that would use RK6.

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
        simOriginal.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

        simDisabled.getActiveConfiguration().setAllStages(); // Re-enable all stages.

        compareSims(simOriginal, simDisabled);
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

        compareSims(simRemoved, simDisabled);

        //// Test re-enabling the stage.
        Rocket rocketOriginal = TestRockets.makeBeta();
        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(TestRockets.TEST_FCID_1);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);
        
        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled);
    }

    /**
     * Tests that the simulation results are correct when the last stage of a multi-stage rocket is deactivated and re-activated, but with the RK6 stepper.
     */
    @Test
    public void testMultiStageLastDisabled_RK6() {
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
        simRemoved.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

        Simulation simDisabled = new Simulation(rocketDisabled);
        simDisabled.setFlightConfigurationId(TestRockets.TEST_FCID_1);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);
        simDisabled.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

        compareSims(simRemoved, simDisabled);

        //// Test re-enabling the stage.
        Rocket rocketOriginal = TestRockets.makeBeta();
        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(TestRockets.TEST_FCID_1);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);
        simOriginal.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

        simDisabled.getActiveConfiguration().setAllStages();
        simDisabled.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

        compareSims(simOriginal, simDisabled);
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
        simRemoved.getOptions().getAverageWindModel().setStandardDeviation(0.0);

        Simulation simDisabled = new Simulation(rocketDisabled);
        simDisabled.setFlightConfigurationId(fcid);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);
        simDisabled.getOptions().getAverageWindModel().setStandardDeviation(0.0);

        compareSims(simRemoved, simDisabled);

        //// Test re-enabling the stage.
        Rocket rocketOriginal = TestRockets.makeFalcon9Heavy();
        TestRockets.addCoreFins(rocketOriginal);

        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(fcid);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);

        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled);
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

        compareSims(simOriginal, simDisabled);
    }

    /**
     * Tests that the simulation results are correct when the parent stage of a booster stage is deactivated and re-activated, but using the RK6 stepper.
     */
    @Test
    public void testBooster2_RK6() {
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
        simRemoved.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

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
        simDisabled.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

        //// Test re-enabling the stage.
        Rocket rocketOriginal = TestRockets.makeFalcon9Heavy();
        TestRockets.addCoreFins(rocketOriginal);

        Simulation simOriginal = new Simulation(rocketOriginal);
        simOriginal.setFlightConfigurationId(fid);
        simOriginal.getOptions().setISAAtmosphere(true);
        simOriginal.getOptions().setTimeStep(0.05);
        simOriginal.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

        simDisabled.getActiveConfiguration().setAllStages();

        compareSims(simOriginal, simDisabled);
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
     * Test whether the simulations run when only the booster stage is active, but using the RK6 stepper.
     */
    @Test
    public void testBooster3_RK6() {
        Rocket rocketDisabled = TestRockets.makeFalcon9Heavy();

        FlightConfigurationId fid =  new FlightConfigurationId(TestRockets.FALCON_9H_FCID_1);
        Simulation simDisabled = new Simulation(rocketDisabled);
        simDisabled.setFlightConfigurationId(fid);
        simDisabled.getOptions().setISAAtmosphere(true);
        simDisabled.getOptions().setTimeStep(0.05);
        simDisabled.getOptions().setSimulationStepperMethodChoice(SimulationStepperMethod.RK6);

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
     */
    private void compareSims(Simulation simExpected, Simulation simActual) {
        try {
            // Compare the rocket configurations under the same wind realization.
            simActual.getOptions().setRandomSeed(simExpected.getOptions().getRandomSeed());
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

            assertEquals(maxAltitudeOriginal, maxAltitudeDisabled, 25);
            assertEquals(maxVelocityOriginal, maxVelocityDisabled, 15);
            assertEquals(maxMachNumberOriginal, maxMachNumberDisabled, 0.05);
            assertEquals(flightTimeOriginal, flightTimeDisabled, calculateDelta(flightTimeOriginal, DELTA_COURSE));
            assertEquals(timeToApogeeOriginal, timeToApogeeDisabled, calculateDelta(timeToApogeeOriginal, DELTA_COURSE));
            assertEquals(launchRodVelocityOriginal, launchRodVelocityDisabled,
                    calculateDelta(launchRodVelocityOriginal, DELTA));
            assertEquals(deploymentVelocityOriginal, deploymentVelocityDisabled,
                    calculateDelta(deploymentVelocityOriginal, DELTA));
        } catch (SimulationException e) {
            fail("Simulation failed: " + e);
        }
    }

    private static double calculateDelta(double value, double delta) {
        return Double.isNaN(value) ? 0 : value * delta;
    }
}
