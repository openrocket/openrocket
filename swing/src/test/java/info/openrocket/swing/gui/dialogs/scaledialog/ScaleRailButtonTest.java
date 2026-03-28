package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.RailButton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the RailButton component.
 * Verifies the custom, order-dependent scaling of dimensions (RailButtonScaler)
 * and linear scaling of positional separation (InstanceSeparation).
 */
public class ScaleRailButtonTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    // ==========================================
    // 1. DIMENSIONAL SCALING (SCALE UP)
    // ==========================================

    /**
     * Tests scale-up (multiplier > 1.0) using the custom RailButtonScaler.
     * Scale up order is: OuterDiameter, InnerDiameter, TotalHeight, BaseHeight, FlangeHeight.
     */
    @Test
    void testDimensionsScale_ScaleUp() throws Exception {
        // ARRANGE
        RailButton button = new RailButton();
        button.setOuterDiameter(0.010);
        button.setInnerDiameter(0.005);
        button.setTotalHeight(0.020);
        button.setBaseHeight(0.008);
        button.setFlangeHeight(0.004);

        double multiplier = 2.0;

        // EXPECTED VALUES (All linear scaling * 2.0)
        double expectedOD = 0.020;
        double expectedID = 0.010;
        double expectedTH = 0.040;
        double expectedBH = 0.016;
        double expectedFH = 0.008;

        // ACT: RailButtonScaler is registered in SCALERS_NO_OFFSET
        scaleMethod.invoke(dialogInstance, button, multiplier, false);

        // ASSERT
        assertEquals(expectedOD, button.getOuterDiameter(), DELTA, "OuterDiameter must scale linearly.");
        assertEquals(expectedID, button.getInnerDiameter(), DELTA, "InnerDiameter must scale linearly.");
        assertEquals(expectedTH, button.getTotalHeight(), DELTA, "TotalHeight must scale linearly.");
        assertEquals(expectedBH, button.getBaseHeight(), DELTA, "BaseHeight must scale linearly.");
        assertEquals(expectedFH, button.getFlangeHeight(), DELTA, "FlangeHeight must scale linearly.");
    }

    // ==========================================
    // 2. DIMENSIONAL SCALING (SCALE DOWN)
    // ==========================================

    /**
     * Tests scale-down (multiplier < 1.0) using the custom RailButtonScaler.
     * Scale down order is: InnerDiameter, OuterDiameter, BaseHeight, FlangeHeight, TotalHeight.
     */
    @Test
    void testDimensionsScale_ScaleDown() throws Exception {
        // ARRANGE
        RailButton button = new RailButton();
        button.setOuterDiameter(0.020);
        button.setInnerDiameter(0.010);
        button.setTotalHeight(0.040);
        button.setBaseHeight(0.016);
        button.setFlangeHeight(0.008);

        double multiplier = 0.5;

        // EXPECTED VALUES (All linear scaling * 0.5)
        double expectedOD = 0.010;
        double expectedID = 0.005;
        double expectedTH = 0.020;
        double expectedBH = 0.008;
        double expectedFH = 0.004;

        // ACT
        scaleMethod.invoke(dialogInstance, button, multiplier, false);

        // ASSERT
        assertEquals(expectedOD, button.getOuterDiameter(), DELTA, "OuterDiameter must scale down linearly.");
        assertEquals(expectedID, button.getInnerDiameter(), DELTA, "InnerDiameter must scale down linearly.");
        assertEquals(expectedTH, button.getTotalHeight(), DELTA, "TotalHeight must scale down linearly.");
        assertEquals(expectedBH, button.getBaseHeight(), DELTA, "BaseHeight must scale down linearly.");
        assertEquals(expectedFH, button.getFlangeHeight(), DELTA, "FlangeHeight must scale down linearly.");
    }

    // ==========================================
    // 3. POSITIONAL SCALING
    // ==========================================

    /**
     * Tests linear scaling of InstanceSeparation (positional offset).
     */
    @Test
    void testPositionalScale_InstanceSeparation() throws Exception {
        // ARRANGE
        RailButton button = new RailButton();
        button.setInstanceSeparation(0.500);

        double multiplier = 1.5;
        double expectedSeparation = 0.750;

        // ACT: InstanceSeparation is registered in SCALERS_OFFSET
        scaleOffsetMethod.invoke(dialogInstance, button, multiplier, false);

        // ASSERT
        assertEquals(expectedSeparation, button.getInstanceSeparation(), DELTA,
                "InstanceSeparation must scale linearly.");
    }

    // ==========================================
    // 4. REVERSIBILITY TEST
    // ==========================================

    /**
     * Tests reversibility for key dimensional and positional properties.
     */
    @Test
    void testAllProperties_ScaleDownThenUp() throws Exception {
        // ARRANGE
        RailButton button = new RailButton();
        final double originalOD = 0.015;
        final double originalSep = 0.600;
        button.setOuterDiameter(originalOD);
        button.setInstanceSeparation(originalSep);

        // ACT 1: Scale down by 0.5 (must invoke both scaling methods)
        scaleMethod.invoke(dialogInstance, button, 0.5, false);
        scaleOffsetMethod.invoke(dialogInstance, button, 0.5, false);

        // ACT 2: Scale up by 2.0
        scaleMethod.invoke(dialogInstance, button, 2.0, false);
        scaleOffsetMethod.invoke(dialogInstance, button, 2.0, false);

        // ASSERT: Final values should be the originals (0.5 * 2.0 = 1.0)
        assertEquals(originalOD, button.getOuterDiameter(), DELTA,
                "OuterDiameter must revert to original.");
        assertEquals(originalSep, button.getInstanceSeparation(), DELTA,
                "InstanceSeparation must revert to original.");
    }
}