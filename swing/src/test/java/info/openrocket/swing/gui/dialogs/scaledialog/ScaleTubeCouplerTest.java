package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.TubeCoupler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for scaling the TubeCoupler component (a ThicknessRingComponent).
 * Focuses on custom, order-dependent scaling (OuterRadius/Thickness) and the
 * exclusion check for the OuterRadiusAutomatic flag.
 */
public class ScaleTubeCouplerTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    // Helper method to set the automatic flag on TubeCoupler
    private void setAutomatic(TubeCoupler coupler, boolean auto) {
        coupler.setOuterRadiusAutomatic(auto);
    }

    // ==========================================
    // 1. CUSTOM SCALING - SCALE UP (M >= 1)
    // ==========================================

    /**
     * Tests scale-up (M > 1.0). Scales OR first, then T.
     */
    @Test
    void testDimensionsScale_ScaleUp() throws Exception {
        // ARRANGE: Initial OR=0.020, T=0.005. Expected IR=0.015.
        TubeCoupler comp = new TubeCoupler();
        setAutomatic(comp, false); // Must be OFF for scaling to occur
        final double initialOR = 0.020;
        final double initialT = 0.005;
        comp.setOuterRadius(initialOR);
        comp.setThickness(initialT);

        double multiplier = 2.0;

        // EXPECTED: OR = 0.040, T = 0.010, IR = 0.030
        double expectedOR = 0.040;
        double expectedT = 0.010;
        double expectedIR = 0.030;

        // ACT
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT
        assertEquals(expectedOR, comp.getOuterRadius(), DELTA, "OR must scale linearly.");
        assertEquals(expectedT, comp.getThickness(), DELTA, "T must scale linearly.");
        assertEquals(expectedIR, comp.getInnerRadius(), DELTA, "IR must match the difference.");
    }

    // ==========================================
    // 2. CUSTOM SCALING - SCALE DOWN (M < 1) - CLAMPING SAFE
    // ==========================================

    /**
     * Tests scale-down (M < 1.0). Scales T first, then OR.
     * Uses T = OR (IR = 0.0) to safely bypass the negative thickness clamp.
     */
    @Test
    void testDimensionsScale_ScaleDown() throws Exception {
        // ARRANGE: Initial OR=0.040, T=0.040. Initial IR=0.0.
        TubeCoupler comp = new TubeCoupler();
        setAutomatic(comp, false);
        final double initialOR = 0.040;
        final double initialT = 0.040;
        comp.setOuterRadius(initialOR);
        comp.setThickness(initialT);

        double multiplier = 0.5; // Scale Down

        // EXPECTED: OR = 0.020, T = 0.020, IR = 0.000 (Correct linear scaled result)
        double expectedOR = 0.020;
        double expectedT = 0.020;
        double expectedIR = 0.000;

        // ACT
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT
        assertEquals(expectedOR, comp.getOuterRadius(), DELTA, "OR must scale linearly.");
        assertEquals(expectedT, comp.getThickness(), DELTA, "T must scale linearly.");
        assertEquals(expectedIR, comp.getInnerRadius(), DELTA, "IR must be 0.0.");
    }

    // ==========================================
    // 3. EXCLUSION CHECK (Automatic Flag)
    // ==========================================

    /**
     * Tests that OuterRadius scaling is SKIPPED when OuterRadiusAutomatic is TRUE,
     * but Thickness (which has no exclusion) still scales.
     */
    @Test
    void testExclusion_OuterRadiusAutomatic() throws Exception {
        // ARRANGE
        TubeCoupler comp = new TubeCoupler();
        final double initialOR = 0.020;
        final double initialT = 0.002;

        comp.setOuterRadius(initialOR);
        comp.setThickness(initialT);
        setAutomatic(comp, true); // CRITICAL: Sets exclusion flag to TRUE

        double multiplier = 3.0;

        // ACT
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT
        // 1. OuterRadius: Must NOT scale (GeneralScaler exclusion check)
        assertEquals(initialOR, comp.getOuterRadius(), DELTA, "OR must NOT be scaled when Automatic is TRUE.");

        // 2. Thickness: Must scale (No exclusion for Thickness)
        double expectedT = initialT * multiplier; // 0.006
        assertEquals(expectedT, comp.getThickness(), DELTA, "T must still scale (0.002 * 3.0).");

        // 3. InnerRadius: Must reflect the new, smaller thickness relative to the FIXED outer radius.
        double expectedIR = initialOR - expectedT; // 0.020 - 0.006 = 0.014
        assertEquals(expectedIR, comp.getInnerRadius(), DELTA,
                "IR must reflect the new thickness (0.020 - 0.006 = 0.014).");
    }

    // ==========================================
    // 4. INHERITED SCALING CHECK (Length/Position)
    // ==========================================

    /**
     * Tests that inherited properties (Length, RadialPosition) scale linearly.
     */
    @Test
    void testInheritedLengthAndPosition() throws Exception {
        // ARRANGE
        TubeCoupler comp = new TubeCoupler();
        comp.setLength(0.010);
        comp.setRadialPosition(0.005);

        double multiplier = 2.0;

        // ACT
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);
        scaleOffsetMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT
        assertEquals(0.020, comp.getLength(), DELTA, "Length must scale linearly.");
        assertEquals(0.010, comp.getRadialPosition(), DELTA, "RadialPosition must scale linearly.");
    }
}