package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of ComponentAssembly descendants.
 * Focuses on PodSet's specific RadiusOffset scaling (SCALERS_OFFSET) and
 * verifying that organizational components (AxialStage) are not scaled incorrectly.
 */
public class ScaleComponentAssemblyTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    /**
     * Provides instances of the concrete ComponentAssembly types for testing.
     */
    static RocketComponent[] componentAssemblyComponents() {
        // PodSet requires RadiusOffset scaling. AxialStage acts as a negative control.
        return new RocketComponent[] { new PodSet(), new AxialStage() };
    }

    // ==========================================
    // POSITIONAL SCALING (PodSet - SCALERS_OFFSET)
    // ==========================================

    /**
     * Tests linear scaling of PodSet's RadiusOffset when scaling up.
     */
    @Test
    void testPodSet_RadiusOffset_ScaleUp() throws Exception {
        // ARRANGE
        PodSet podSet = new PodSet();
        podSet.setRadiusOffset(0.050);

        double multiplier = 2.0;
        double expectedOffset = 0.100;

        // ACT: Invoke scaleOffsetMethod (handles positional scaling)
        scaleOffsetMethod.invoke(dialogInstance, podSet, multiplier, false);

        // ASSERT
        assertEquals(expectedOffset, podSet.getRadiusOffset(), DELTA,
                "PodSet RadiusOffset must scale linearly.");
    }

    /**
     * Tests linear scaling of PodSet's RadiusOffset when scaling down.
     */
    @Test
    void testPodSet_RadiusOffset_ScaleDown() throws Exception {
        // ARRANGE
        PodSet podSet = new PodSet();
        podSet.setRadiusOffset(0.120);

        double multiplier = 0.5;
        double expectedOffset = 0.060;

        // ACT: Invoke scaleOffsetMethod (handles positional scaling)
        scaleOffsetMethod.invoke(dialogInstance, podSet, multiplier, false);

        // ASSERT
        assertEquals(expectedOffset, podSet.getRadiusOffset(), DELTA,
                "PodSet RadiusOffset must scale linearly.");
    }

    // ==========================================
    // NEGATIVE / INHERITANCE SCALING TESTS
    // ==========================================

    /**
     * Verifies that the length of ComponentAssembly descendants is NOT scaled
     * by the core dimension scaler, as their length is typically derived from children.
     */
    @ParameterizedTest
    @MethodSource("componentAssemblyComponents")
    void testAssembly_Length(RocketComponent comp) throws Exception {
        // ARRANGE
        // Length is read as its current (likely calculated) state.
        final double originalLength = comp.getLength();

        double multiplier = 2.0;

        // ACT: Invoke scaleMethod (SCALERS_NO_OFFSET: dimensions)
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT: Length must remain unchanged
        assertEquals(originalLength, comp.getLength(), DELTA,
                comp.getClass().getSimpleName() + ": Length must NOT be scaled by general dimension scaling.");
    }

    /**
     * Verifies that AxialStage's RadialPosition is NOT scaled, as it is not explicitly mapped for scaling.
     */
    @Test
    void testAxialStage_RadiusOffset() throws Exception {
        // ARRANGE
        AxialStage stage = new AxialStage();
        // RadialPosition/RadiusOffset should be 0.0 by default for an AxialStage
        final double originalOffset = stage.getRadiusOffset();

        double multiplier = 2.0;

        // ACT: Invoke scaleOffsetMethod (positional scaling)
        scaleOffsetMethod.invoke(dialogInstance, stage, multiplier, false);

        // ASSERT: RadiusOffset must remain unchanged.
        assertEquals(originalOffset, stage.getRadiusOffset(), DELTA,
                "AxialStage RadiusOffset must NOT be scaled.");
    }
}