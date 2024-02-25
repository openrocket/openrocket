package info.openrocket.core.rocketcomponent;

import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AxialStageTest extends BaseTestCase {
    /**
     * Test whether we can disable a stage, and that it is registered only in the
     * target flight configuration and not
     * in the others.
     */
    @Test
    public void testDisableStage() {
        final Rocket rocket = TestRockets.makeFalcon9Heavy();
        final FlightConfiguration config = rocket.getSelectedConfiguration();
        final FlightConfigurationId fcid = rocket.createFlightConfiguration(new FlightConfigurationId())
                .getFlightConfigurationID();
        final FlightConfiguration config2 = rocket.getFlightConfiguration(fcid);

        // Disable the payload stage
        config._setStageActive(0, false);
        assertFalse(config.isStageActive(0), " Payload stage in selected configuration should be disabled");
        assertTrue(config.isStageActive(1), " Core stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(2), " Booster stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");

        // Enable the payload stage
        config._setStageActive(0, true);
        assertTrue(config.isStageActive(0), " Payload stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(1), " Core stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(2), " Booster stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");

        // Toggle the payload stage to False
        config.toggleStage(0);
        assertFalse(config.isStageActive(0), " Payload stage in selected configuration should be disabled");
        assertTrue(config.isStageActive(1), " Core stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(2), " Booster stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");

        // Toggle the payload stage to True
        config.toggleStage(0);
        assertTrue(config.isStageActive(0), " Payload stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(1), " Core stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(2), " Booster stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");

        // Set only stage
        config.setOnlyStage(1);
        assertFalse(config.isStageActive(0), " Payload stage in selected configuration should be disabled");
        assertTrue(config.isStageActive(1), " Core stage in selected configuration should be enabled");
        assertFalse(config.isStageActive(2), " Booster stage in selected configuration should be disabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");

        // Change stage activeness in other configuration
        config2.toggleStage(1);
        assertFalse(config.isStageActive(0), " Payload stage in selected configuration should be disabled");
        assertTrue(config.isStageActive(1), " Core stage in selected configuration should be enabled");
        assertFalse(config.isStageActive(2), " Booster stage in selected configuration should be disabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertFalse(config2.isStageActive(1), " Core stage in other configuration should be disabled");
        assertFalse(config2.isStageActive(2), " Booster stage in other configuration should be disabled");
        config.setAllStages();
        assertTrue(config.isStageActive(0), " Payload stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(1), " Core stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(2), " Booster stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertFalse(config2.isStageActive(1), " Core stage in other configuration should be disabled");
        assertFalse(config2.isStageActive(2), " Booster stage in other configuration should be disabled");

        // Toggle stage with activateSubStages edited
        config.setAllStages();
        config2.setAllStages();
        config._setStageActive(1, false, true);
        assertTrue(config.isStageActive(0), " Payload stage in selected configuration should be enabled");
        assertFalse(config.isStageActive(1), " Core stage in selected configuration should be disabled");
        assertFalse(config.isStageActive(2), " Booster stage in selected configuration should be disabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");
        config._setStageActive(1, true, false);
        assertTrue(config.isStageActive(0), " Payload stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(1), " Core stage in selected configuration should be enabled");
        assertFalse(config.isStageActive(2), " Booster stage in selected configuration should be disabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");
    }

    /**
     * Test disabling a stage and then moving the stage in the component tree.
     */
    @Test
    public void testDisableStageAndMove() {
        final Rocket rocket = TestRockets.makeFalcon9Heavy();
        final FlightConfiguration config = rocket.getSelectedConfiguration();
        final FlightConfigurationId fcid = rocket.createFlightConfiguration(new FlightConfigurationId())
                .getFlightConfigurationID();
        final FlightConfiguration config2 = rocket.getFlightConfiguration(fcid);

        // Disable the payload stage
        config.setAllStages();
        config._setStageActive(0, false);
        AxialStage payloadStage = rocket.getStage(0);

        // Move the payload stage to the back of the rocket
        try {
            rocket.freeze();
            rocket.removeChild(payloadStage);
            rocket.addChild(payloadStage); // Moves to the back
        } finally {
            rocket.thaw(); // Unfreeze
        }

        assertTrue(config.isStageActive(0), " Core stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(1), " Booster stage in selected configuration should be enabled");
        assertFalse(config.isStageActive(2), " Payload stage in selected configuration should be disabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");

        // Re-enable the payload stage
        config._setStageActive(payloadStage.getStageNumber(), true);
        assertTrue(config.isStageActive(0), " Core stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(1), " Booster stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(2), " Payload stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");

        // Disable the core stage (and booster stage)
        config._setStageActive(0, false);
        assertFalse(config.isStageActive(0), " Core stage in selected configuration should be disabled");
        assertFalse(config.isStageActive(1), " Booster stage in selected configuration should be disabled ");
        assertTrue(config.isStageActive(2), " Payload stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");

        // Move the core stage to the back of the rocket
        AxialStage coreStage = rocket.getStage(0);
        try {
            rocket.freeze();
            rocket.removeChild(coreStage);
            rocket.addChild(coreStage); // Moves to the back
        } finally {
            rocket.thaw(); // Unfreeze
        }

        assertTrue(config.isStageActive(0), " Payload stage in selected configuration should be enabled");
        assertFalse(config.isStageActive(1), " Core stage in selected configuration should be disabled");
        assertFalse(config.isStageActive(2), " Booster stage in selected configuration should be disabled ");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");
    }

    /**
     * Test disabling a stage and then copying that stage.
     */
    @Test
    public void testDisableStageAndCopy() {
        final Rocket rocket = TestRockets.makeFalcon9Heavy();
        final FlightConfiguration config = rocket.getSelectedConfiguration();
        final FlightConfigurationId fcid = rocket.createFlightConfiguration(new FlightConfigurationId())
                .getFlightConfigurationID();
        final FlightConfiguration config2 = rocket.getFlightConfiguration(fcid);

        // Disable the core stage
        config.setAllStages();
        config._setStageActive(1, false);
        AxialStage coreStage = rocket.getStage(1);

        // Copy the core stage to the back of the rocket
        AxialStage coreStageCopy = (AxialStage) coreStage.copy();
        rocket.addChild(coreStageCopy);

        assertTrue(config.isStageActive(0), " Payload stage in selected configuration should be enabled");
        assertFalse(config.isStageActive(1), " Core stage in selected configuration should be disabled");
        assertFalse(config.isStageActive(2), " Booster stage in selected configuration should be disabled");
        assertTrue(config.isStageActive(3), " Core copy stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(4), " Booster copy stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(3), " Core copy stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(4), " Booster copy stage in selected configuration should be enabled");

        // Disable the copied core stage (not the booster copy stage)
        config._setStageActive(3, false, false);
        assertTrue(config.isStageActive(0), " Payload stage in selected configuration should be enabled");
        assertFalse(config.isStageActive(1), " Core stage in selected configuration should be disabled");
        assertFalse(config.isStageActive(2), " Booster stage in selected configuration should be disabled");
        assertFalse(config.isStageActive(3), " Core copy stage in selected configuration should be disabled");
        assertTrue(config.isStageActive(4), " Booster copy stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(3), " Core copy stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(4), " Booster copy stage in selected configuration should be enabled");

        // Toggle the original core stage back
        config.toggleStage(1);
        assertTrue(config.isStageActive(0), " Payload stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(1), " Core stage in selected configuration should be enabled");
        assertTrue(config.isStageActive(2), " Booster stage in selected configuration should be enabled");
        assertFalse(config.isStageActive(3), " Core copy stage in selected configuration should be disabled");
        assertTrue(config.isStageActive(4), " Booster copy stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(0), " Payload stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(1), " Core stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(2), " Booster stage in other configuration should be enabled");
        assertTrue(config2.isStageActive(3), " Core copy stage in selected configuration should be enabled");
        assertTrue(config2.isStageActive(4), " Booster copy stage in selected configuration should be enabled");
    }
}
