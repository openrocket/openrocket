package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.TestRockets;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AxialStageTest extends BaseTestCase {
    /**
     * Test whether we can disable a stage, and that it is registered only in the target flight configuration and not
     * in the others.
     */
    @Test
    public void testDisableStage() {
        final Rocket rocket = TestRockets.makeFalcon9Heavy();
        final FlightConfiguration config = rocket.getSelectedConfiguration();
        final FlightConfigurationId fcid = rocket.createFlightConfiguration(new FlightConfigurationId()).getFlightConfigurationID();
        final FlightConfiguration config2 = rocket.getFlightConfiguration(fcid);

        // Disable the payload stage
        config._setStageActive(0, false);
        assertFalse(" Payload stage in selected configuration should be disabled", config.isStageActive(0));
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(1));
        assertTrue(" Booster stage in selected configuration should be enabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));

        // Enable the payload stage
        config._setStageActive(0, true);
        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(0));
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(1));
        assertTrue(" Booster stage in selected configuration should be enabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));

        // Toggle the payload stage to False
        config.toggleStage(0);
        assertFalse(" Payload stage in selected configuration should be disabled", config.isStageActive(0));
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(1));
        assertTrue(" Booster stage in selected configuration should be enabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));

        // Toggle the payload stage to True
        config.toggleStage(0);
        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(0));
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(1));
        assertTrue(" Booster stage in selected configuration should be enabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));

        // Set only stage
        config.setOnlyStage(1);
        assertFalse(" Payload stage in selected configuration should be disabled", config.isStageActive(0));
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(1));
        assertFalse(" Booster stage in selected configuration should be disabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));

        // Change stage activeness in other configuration
        config2.toggleStage(1);
        assertFalse(" Payload stage in selected configuration should be disabled", config.isStageActive(0));
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(1));
        assertFalse(" Booster stage in selected configuration should be disabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertFalse(" Core stage in other configuration should be disabled", config2.isStageActive(1));
        assertFalse(" Booster stage in other configuration should be disabled", config2.isStageActive(2));
        config.setAllStages();
        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(0));
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(1));
        assertTrue(" Booster stage in selected configuration should be enabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertFalse(" Core stage in other configuration should be disabled", config2.isStageActive(1));
        assertFalse(" Booster stage in other configuration should be disabled", config2.isStageActive(2));

        // Toggle stage with activateSubStages edited
        config.setAllStages();
        config2.setAllStages();
        config._setStageActive(1, false, true);
        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(0));
        assertFalse(" Core stage in selected configuration should be disabled", config.isStageActive(1));
        assertFalse(" Booster stage in selected configuration should be disabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));
        config._setStageActive(1, true, false);
        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(0));
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(1));
        assertFalse(" Booster stage in selected configuration should be disabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));
    }

    /**
     * Test disabling a stage and then moving the stage in the component tree.
     */
    @Test
    public void testDisableStageAndMove() {
        final Rocket rocket = TestRockets.makeFalcon9Heavy();
        final FlightConfiguration config = rocket.getSelectedConfiguration();
        final FlightConfigurationId fcid = rocket.createFlightConfiguration(new FlightConfigurationId()).getFlightConfigurationID();
        final FlightConfiguration config2 = rocket.getFlightConfiguration(fcid);

        // Disable the payload stage
        config.setAllStages();
        config._setStageActive(0, false);
        AxialStage payloadStage = rocket.getStage(0);

        // Move the payload stage to the back of the rocket
        try {
            rocket.freeze();
            rocket.removeChild(payloadStage);
            rocket.addChild(payloadStage);      // Moves to the back
        } finally {
            rocket.thaw();	// Unfreeze
        }

        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(0));
        assertTrue(" Booster stage in selected configuration should be enabled", config.isStageActive(1));
        assertFalse(" Payload stage in selected configuration should be disabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));

        // Re-enable the payload stage
        config._setStageActive(payloadStage.getStageNumber(), true);
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(0));
        assertTrue(" Booster stage in selected configuration should be enabled", config.isStageActive(1));
        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));

        // Disable the core stage (and booster stage)
        config._setStageActive(0, false);
        assertFalse(" Core stage in selected configuration should be disabled", config.isStageActive(0));
        assertFalse(" Booster stage in selected configuration should be disabled ", config.isStageActive(1));
        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));

        // Move the core stage to the back of the rocket
        AxialStage coreStage = rocket.getStage(0);
        try {
            rocket.freeze();
            rocket.removeChild(coreStage);
            rocket.addChild(coreStage);      // Moves to the back
        } finally {
            rocket.thaw();	// Unfreeze
        }

        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(0));
        assertFalse(" Core stage in selected configuration should be disabled", config.isStageActive(1));
        assertFalse(" Booster stage in selected configuration should be disabled ", config.isStageActive(2));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));
    }

    /**
     * Test disabling a stage and then copying that stage.
     */
    @Test
    public void testDisableStageAndCopy() {
        final Rocket rocket = TestRockets.makeFalcon9Heavy();
        final FlightConfiguration config = rocket.getSelectedConfiguration();
        final FlightConfigurationId fcid = rocket.createFlightConfiguration(new FlightConfigurationId()).getFlightConfigurationID();
        final FlightConfiguration config2 = rocket.getFlightConfiguration(fcid);

        // Disable the core stage
        config.setAllStages();
        config._setStageActive(1, false);
        AxialStage coreStage = rocket.getStage(1);

        // Copy the core stage to the back of the rocket
        AxialStage coreStageCopy = (AxialStage) coreStage.copy();
        rocket.addChild(coreStageCopy);

        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(0));
        assertFalse(" Core stage in selected configuration should be disabled", config.isStageActive(1));
        assertFalse(" Booster stage in selected configuration should be disabled", config.isStageActive(2));
        assertTrue(" Core copy stage in selected configuration should be enabled", config.isStageActive(3));
        assertTrue(" Booster copy stage in selected configuration should be enabled", config.isStageActive(4));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));
        assertTrue(" Core copy stage in selected configuration should be enabled", config2.isStageActive(3));
        assertTrue(" Booster copy stage in selected configuration should be enabled", config2.isStageActive(4));

        // Disable the copied core stage (not the booster copy stage)
        config._setStageActive(3, false, false);
        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(0));
        assertFalse(" Core stage in selected configuration should be disabled", config.isStageActive(1));
        assertFalse(" Booster stage in selected configuration should be disabled", config.isStageActive(2));
        assertFalse(" Core copy stage in selected configuration should be disabled", config.isStageActive(3));
        assertTrue(" Booster copy stage in selected configuration should be enabled", config.isStageActive(4));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));
        assertTrue(" Core copy stage in selected configuration should be enabled", config2.isStageActive(3));
        assertTrue(" Booster copy stage in selected configuration should be enabled", config2.isStageActive(4));

        // Toggle the original core stage back
        config.toggleStage(1);
        assertTrue(" Payload stage in selected configuration should be enabled", config.isStageActive(0));
        assertTrue(" Core stage in selected configuration should be enabled", config.isStageActive(1));
        assertTrue(" Booster stage in selected configuration should be enabled", config.isStageActive(2));
        assertFalse(" Core copy stage in selected configuration should be disabled", config.isStageActive(3));
        assertTrue(" Booster copy stage in selected configuration should be enabled", config.isStageActive(4));
        assertTrue(" Payload stage in other configuration should be enabled", config2.isStageActive(0));
        assertTrue(" Core stage in other configuration should be enabled", config2.isStageActive(1));
        assertTrue(" Booster stage in other configuration should be enabled", config2.isStageActive(2));
        assertTrue(" Core copy stage in selected configuration should be enabled", config2.isStageActive(3));
        assertTrue(" Booster copy stage in selected configuration should be enabled", config2.isStageActive(4));
    }
}
