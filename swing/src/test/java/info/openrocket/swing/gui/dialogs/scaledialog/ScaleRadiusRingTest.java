package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.RadiusRingComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of RadiusRingComponent descendants.
 * This verifies the order-dependent scaling of OuterRadius (OR) and InnerRadius (IR),
 * and the specific exclusion logic for automatic radii.
 */
public class ScaleRadiusRingTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    // --- SETUP: Provide Components ---

    /**
     * Provides instances of CenteringRing (set to manual for core scaling tests).
     */
    static CenteringRing[] centeringRingComponentsManual() {
        // Create instance where both radii are manually set for deterministic core scaling
        CenteringRing ring = new CenteringRing();
        ring.setOuterRadiusAutomatic(false);
        ring.setInnerRadiusAutomatic(false);
        return new CenteringRing[] { ring };
    }

    // ==========================================
    // 1. CORE SCALING TESTS (Manual Radii)
    // ==========================================

    /**
     * Tests scale-up (M >= 1.0). The custom scaler must scale OuterRadius first,
     * then InnerRadius, to prevent geometric clipping.
     */
    @ParameterizedTest
    @MethodSource("centeringRingComponentsManual")
    void testDimensionsScale_ScaleUp(RadiusRingComponent comp) throws Exception {
        // ARRANGE: OR=0.040, IR=0.025.
        comp.setOuterRadius(0.040);
        comp.setInnerRadius(0.025);

        double multiplier = 2.0; // Scale Up

        // EXPECTED: OR = 0.080, IR = 0.050.
        double expectedOR = 0.080;
        double expectedIR = 0.050;

        // ACT: RadiusRingComponentScaler is triggered
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT
        assertEquals(expectedOR, comp.getOuterRadius(), DELTA, "OuterRadius must scale linearly.");
        assertEquals(expectedIR, comp.getInnerRadius(), DELTA, "InnerRadius must scale linearly.");
        assertEquals(expectedOR - expectedIR, comp.getThickness(), DELTA, "Thickness must match the difference.");
    }

    /**
     * Tests scale-down (M < 1.0). The custom scaler must scale InnerRadius first,
     * then OuterRadius, to prevent geometric clipping.
     */
    @ParameterizedTest
    @MethodSource("centeringRingComponentsManual")
    void testDimensionsScale_ScaleDown(RadiusRingComponent comp) throws Exception {
        // ARRANGE: OR=0.080, IR=0.050.
        comp.setOuterRadius(0.080);
        comp.setInnerRadius(0.050);

        double multiplier = 0.5; // Scale Down

        // EXPECTED: OR = 0.040, IR = 0.025.
        double expectedOR = 0.040;
        double expectedIR = 0.025;

        // ACT
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT
        assertEquals(expectedOR, comp.getOuterRadius(), DELTA, "OuterRadius must scale down linearly.");
        assertEquals(expectedIR, comp.getInnerRadius(), DELTA, "InnerRadius must scale down linearly.");
        assertEquals(expectedOR - expectedIR, comp.getThickness(), DELTA, "Thickness must match the difference.");
    }

    // ==========================================
    // 2. AUTOMATIC RADIUS EXCLUSION CHECKS
    // ==========================================

    /**
     * Tests that InnerRadius scaling is SKIPPED when InnerRadiusAutomatic is TRUE,
     * but OuterRadius still scales. This verifies the GeneralScaler exclusion check.
     */
    @Test
    void testInnerRadius_AutomaticOn() throws Exception {
        // ARRANGE
        CenteringRing comp = new CenteringRing();
        double multiplier = 2.0;

        // 1. Set OR state: Manual (scales)
        comp.setOuterRadiusAutomatic(false);
        comp.setOuterRadius(0.050);

        // 2. Set IR value (resets flag), then set flag TRUE LAST (critical order)
        comp.setInnerRadius(0.020);
        comp.setInnerRadiusAutomatic(true); // Flag TRUE for scaler check

        final double originalIR = comp.getInnerRadius(); // 0.020

        // ACT
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT
        // 1. OR scales (0.050 * 2.0 = 0.100)
        assertEquals(0.100, comp.getOuterRadius(), DELTA, "OuterRadius must scale.");

        // 2. IR is skipped (must remain 0.020)
        assertEquals(originalIR, comp.getInnerRadius(), DELTA,
                "InnerRadius must NOT scale when Automatic is TRUE.");
    }

    // ==========================================
    // 3. BULKHEAD SPECIFIC TEST
    // ==========================================

    /**
     * Tests Bulkhead scaling. Verifies OuterRadius scaling while asserting
     * InnerRadius remains 0.0 due to component-specific logic.
     */
    @Test
    void testBulkhead_InnerRadiusIsFixedZero() throws Exception {
        // ARRANGE
        Bulkhead bulkhead = new Bulkhead();
        bulkhead.setOuterRadiusAutomatic(false); // OR scales
        bulkhead.setOuterRadius(0.030);

        double multiplier = 2.0;

        // ACT
        scaleMethod.invoke(dialogInstance, bulkhead, multiplier, false);

        // ASSERT
        assertEquals(0.060, bulkhead.getOuterRadius(), DELTA, "Bulkhead OuterRadius must scale.");

        // IR is hardcoded to 0.0 for Bulkhead
        assertEquals(0.0, bulkhead.getInnerRadius(), DELTA,
                "Bulkhead InnerRadius must remain 0.0.");
    }

    // ==========================================
    // 4. INHERITED SCALING CHECK (Length/Position)
    // ==========================================

    /**
     * Tests inherited Length and RadialPosition scaling using CenteringRing.
     */
    @Test
    void testInheritedProperties_ScaleLinearly() throws Exception {
        // ARRANGE
        CenteringRing comp = new CenteringRing();
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