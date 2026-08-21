package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse; // Added for completeness, used in previous BodyTube logic

/**
 * Unit tests for the scaling logic of the BodyTube component.
 * Verifies linear scaling of dimensions (OuterRadius, MotorOverhang) and the
 * exclusion check for the isOuterRadiusAutomatic flag.
 */
public class ScaleBodyTubeTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    // ==========================================
    // CORE SCALING TESTS (Automatic OFF)
    // ==========================================

    /**
     * Tests linear scaling up of OuterRadius and MotorOverhang when automatic sizing is disabled.
     */
    @Test
    void testDimensionsScale_ScaleUp() throws Exception {
        // ARRANGE
        BodyTube tube = new BodyTube();
        tube.setOuterRadiusAutomatic(false);
        tube.setOuterRadius(0.020);
        tube.setMotorOverhang(0.015);

        double multiplier = 2.0;

        // ACT
        scaleMethod.invoke(dialogInstance, tube, multiplier, false);

        // ASSERT
        assertEquals(0.040, tube.getOuterRadius(), DELTA,
                "OuterRadius must scale linearly.");
        assertEquals(0.030, tube.getMotorOverhang(), DELTA,
                "MotorOverhang must scale linearly.");
        assertFalse(tube.isOuterRadiusAutomatic(),
                "Automatic flag should remain OFF.");
    }

    /**
     * Tests linear scaling down of OuterRadius and MotorOverhang when automatic sizing is disabled.
     */
    @Test
    void testDimensionsScale_ScaleDown() throws Exception {
        // ARRANGE
        BodyTube tube = new BodyTube();
        tube.setOuterRadiusAutomatic(false);
        tube.setOuterRadius(0.100);
        tube.setMotorOverhang(0.050);

        double multiplier = 0.5;

        // ACT
        scaleMethod.invoke(dialogInstance, tube, multiplier, false);

        // ASSERT
        assertEquals(0.050, tube.getOuterRadius(), DELTA,
                "OuterRadius must scale down linearly.");
        assertEquals(0.025, tube.getMotorOverhang(), DELTA,
                "MotorOverhang must scale down linearly.");
    }

    // ==========================================
    // INHERITED SCALING CHECK (Length)
    // ==========================================

    /**
     * Tests that the Length property (inherited from BodyComponent) scales linearly.
     */
    @Test
    void testLengthScale() throws Exception {
        // ARRANGE
        BodyTube tube = new BodyTube();
        tube.setLength(0.500);

        double multiplier = 1.5;

        // ACT
        scaleMethod.invoke(dialogInstance, tube, multiplier, false);

        // ASSERT
        assertEquals(0.750, tube.getLength(), DELTA,
                "Length must scale linearly.");
    }

    // ==========================================
    // EXCLUSION CHECK TEST (Automatic ON)
    // ==========================================

    /**
     * Tests that OuterRadius scaling is SKIPPED when OuterRadiusAutomatic is TRUE,
     * but MotorOverhang (which has no exclusion) still scales.
     * Sets up a simple rocket tree to provide a valid context for auto-sizing logic.
     */
    @Test
    void testOuterRadius_AutomaticOn() throws Exception {
        // ARRANGE - Create a necessary tree structure for getOuterRadius() to work
        Rocket rocket = new Rocket();
        AxialStage stage = new AxialStage();
        rocket.addChild(stage);

        // Source Component: Provides the fixed radius
        NoseCone noseCone = new NoseCone();
        noseCone.setAftRadius(0.050);
        noseCone.setAftRadiusAutomatic(false);
        stage.addChild(noseCone);

        // Subject Component: Reads the automatic radius
        BodyTube subject = new BodyTube();
        subject.setMotorOverhang(0.010);
        stage.addChild(subject);

        // CRITICAL: Set automatic AFTER the subject is in the tree
        subject.setOuterRadiusAutomatic(true);

        // Verify the automatic setup worked and stored the correct original radius
        final double originalRadius = subject.getOuterRadius();
        assertEquals(0.050, originalRadius, DELTA,
                "Setup verification: Subject must read the 0.050m radius from the NoseCone.");

        double multiplier = 3.0;

        // ACT
        // Note: The NoseCone/BodyTube context is required, but the test ensures the exclusion check is hit.
        scaleMethod.invoke(dialogInstance, subject, multiplier, false);

        // ASSERT
        // 1. OuterRadius: Must NOT scale (Exclusion check passed)
        assertEquals(originalRadius, subject.getOuterRadius(), DELTA,
                "OuterRadius must NOT be scaled when Automatic is TRUE.");

        // 2. MotorOverhang: Must scale linearly
        assertEquals(0.030, subject.getMotorOverhang(), DELTA,
                "MotorOverhang must be scaled linearly.");

        // 3. Flag State: Must remain TRUE (Scaling was skipped, setOuterRadius was not called)
        assertTrue(subject.isOuterRadiusAutomatic(),
                "The automatic flag must remain TRUE when scaling is correctly skipped.");
    }
}