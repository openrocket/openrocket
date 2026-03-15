package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.EngineBlock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for scaling the EngineBlock component (a ThicknessRingComponent).
 * Focuses on the custom, order-dependent scaling and the isOuterRadiusAutomatic exclusion flag.
 */
public class ScaleEngineBlockTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    // Helper method to set the automatic flag since EngineBlock defines a public wrapper
    private void setAutomatic(EngineBlock block, boolean auto) {
        block.setOuterRadiusAutomatic(auto);
    }

    // ==========================================
    // 1. SCALING TESTS (Automatic OFF)
    // ==========================================

    /**
     * Tests scale-up (M > 1.0) when automatic is OFF.
     * Verifies Order: OR scales first, then T.
     */
    @Test
    void testScaleUp_AutomaticOff() throws Exception {
        // ARRANGE: Initial OR=0.020, T=0.005. Expected IR=0.015.
        EngineBlock block = new EngineBlock();
        setAutomatic(block, false);
        final double initialOR = 0.020;
        final double initialT = 0.005;
        block.setOuterRadius(initialOR);
        block.setThickness(initialT);

        double multiplier = 2.0;

        // EXPECTED: OR = 0.040, T = 0.010, IR = 0.030
        double expectedOR = 0.040;
        double expectedT = 0.010;
        double expectedIR = 0.030;

        // ACT
        scaleMethod.invoke(dialogInstance, block, multiplier, false);

        // ASSERT
        assertEquals(expectedOR, block.getOuterRadius(), DELTA, "OR must scale linearly.");
        assertEquals(expectedT, block.getThickness(), DELTA, "T must scale linearly.");
        assertEquals(expectedIR, block.getInnerRadius(), DELTA, "IR must match the difference.");
    }

    /**
     * Tests scale-down (M < 1.0) when automatic is OFF.
     * Verifies Order: T scales first, then OR. Uses T=OR to ensure IR=0.
     */
    @Test
    void testScaleDown_AutomaticOff() throws Exception {
        // ARRANGE: Initial OR=0.040, T=0.040. Initial IR=0.0 (Clamping-safe state).
        EngineBlock block = new EngineBlock();
        setAutomatic(block, false);
        final double initialOR = 0.040;
        final double initialT = 0.040;
        block.setOuterRadius(initialOR);
        block.setThickness(initialT);

        double multiplier = 0.5;

        // EXPECTED: OR = 0.020, T = 0.020, IR = 0.000
        double expectedOR = 0.020;
        double expectedT = 0.020;
        double expectedIR = 0.000;

        // ACT
        scaleMethod.invoke(dialogInstance, block, multiplier, false);

        // ASSERT
        assertEquals(expectedOR, block.getOuterRadius(), DELTA, "OR must scale linearly.");
        assertEquals(expectedT, block.getThickness(), DELTA, "T must scale linearly.");
        assertEquals(expectedIR, block.getInnerRadius(), DELTA, "IR must be 0.0.");
    }

    // ==========================================
    // 2. EXCLUSION CHECK TESTS (Automatic ON)
    // ==========================================

    /**
     * Tests the critical scenario: OuterRadiusAutomatic is TRUE.
     * OR scaling must be skipped, but Thickness scaling must proceed.
     */
    @Test
    void testExclusion_AutomaticOn() throws Exception {
        // ARRANGE
        EngineBlock block = new EngineBlock();
        final double initialOR = 0.020;
        final double initialT = 0.002;

        block.setOuterRadius(initialOR);
        block.setThickness(initialT);
        setAutomatic(block, true); // CRITICAL: Sets exclusion flag

        double multiplier = 3.0;

        // ACT
        scaleMethod.invoke(dialogInstance, block, multiplier, false);

        // ASSERT
        // 1. OuterRadius: Must NOT scale (GeneralScaler exclusion check)
        assertEquals(initialOR, block.getOuterRadius(), DELTA, "OR must NOT be scaled when Automatic is TRUE.");

        // 2. Thickness: Must scale (No exclusion for Thickness)
        double expectedT = initialT * multiplier; // 0.006
        assertEquals(expectedT, block.getThickness(), DELTA, "T must still scale (0.002 * 3.0).");

        // 3. InnerRadius: Must reflect the new, scaled thickness relative to the FIXED outer radius.
        double expectedIR = initialOR - expectedT; // 0.020 - 0.006 = 0.014
        assertEquals(expectedIR, block.getInnerRadius(), DELTA, "IR must reflect the new thickness.");

        // Sanity Check: Ensure the Automatic flag is still ON
        assertTrue(block.isOuterRadiusAutomatic(), "Automatic flag must remain TRUE.");
    }

    /**
     * Tests the boundary case where scaling is skipped (Automatic ON) and the multiplier is 1.0.
     */
    @Test
    void testBoundary_AutomaticOn_MultiplierOne() throws Exception {
        // ARRANGE
        EngineBlock block = new EngineBlock();
        final double initialOR = 0.035;
        final double initialT = 0.004;
        block.setOuterRadius(initialOR);
        block.setThickness(initialT);
        setAutomatic(block, true);

        double multiplier = 1.0;

        // ACT
        scaleMethod.invoke(dialogInstance, block, multiplier, false);

        // ASSERT
        // All values should be unchanged (due to M=1.0 and/or Automatic exclusion)
        assertEquals(initialOR, block.getOuterRadius(), DELTA, "OR must be unchanged.");
        assertEquals(initialT, block.getThickness(), DELTA, "T must be unchanged.");
        assertTrue(block.isOuterRadiusAutomatic(), "Automatic flag must remain TRUE.");
    }
}