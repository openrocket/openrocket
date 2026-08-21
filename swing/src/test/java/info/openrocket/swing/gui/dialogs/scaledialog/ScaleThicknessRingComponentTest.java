package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.ThicknessRingComponent;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for scaling the ThicknessRingComponent, focusing on the custom,
 * order-dependent scaling logic of ThicknessRingComponentScaler.
 * Uses the clamping-safe state (Inner Radius = 0) for scale-down testing.
 */
public class ScaleThicknessRingComponentTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    // --- SETUP: Provide Components ---

    /**
     * Provides instances of concrete ThicknessRingComponent types for parameterized tests.
     */
    static ThicknessRingComponent[] thicknessRingComponents() {
        // Ensure OuterRadiusAutomatic is FALSE to enable scaling in core tests.
        EngineBlock block = new EngineBlock();
        block.setOuterRadiusAutomatic(false);
        TubeCoupler coupler = new TubeCoupler();
        coupler.setOuterRadiusAutomatic(false);

        return new ThicknessRingComponent[] { block, coupler };
    }

    // ==========================================
    // 1. CUSTOM SCALING - SCALE UP (M >= 1)
    // ==========================================

    /**
     * Tests scale-up (M > 1.0). Scales OR first, then T.
     */
    @ParameterizedTest
    @MethodSource("thicknessRingComponents")
    void testDimensionsScale_ScaleUp(ThicknessRingComponent comp) throws Exception {
        // ARRANGE: Initial OR=0.020, T=0.005.
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
        assertEquals(expectedOR, comp.getOuterRadius(), DELTA, comp.getClass().getSimpleName() + ": OR must scale linearly.");
        assertEquals(expectedT, comp.getThickness(), DELTA, comp.getClass().getSimpleName() + ": T must scale linearly.");
        assertEquals(expectedIR, comp.getInnerRadius(), DELTA, comp.getClass().getSimpleName() + ": IR must match the difference.");
    }

    // ==========================================
    // 2. CUSTOM SCALING - SCALE DOWN (M < 1) - CLAMPING SAFE
    // ==========================================

    /**
     * Tests scale-down (M < 1.0). Scales T first, then OR.
     * Uses T = OR (IR = 0.0) to safely bypass the negative thickness clamp,
     * ensuring the expected linear scaling occurs.
     */
    @ParameterizedTest
    @MethodSource("thicknessRingComponents")
    void testDimensionsScale_ScaleDown(ThicknessRingComponent comp) throws Exception {
        // ARRANGE: Initial OR=0.040, T=0.040. Initial IR=0.0.
        final double initialOR = 0.040;
        final double initialT = 0.040;
        comp.setOuterRadius(initialOR);
        comp.setThickness(initialT);

        double multiplier = 0.5; // Scale Down

        // EXPECTED: OR = 0.020, T = 0.020, IR = 0.000 (The correct linear scaled result)
        double expectedOR = 0.020;
        double expectedT = 0.020;
        double expectedIR = 0.000;

        // ACT
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT
        assertEquals(expectedOR, comp.getOuterRadius(), DELTA, comp.getClass().getSimpleName() + ": OR must scale linearly.");
        assertEquals(expectedT, comp.getThickness(), DELTA, comp.getClass().getSimpleName() + ": T must scale linearly.");
        assertEquals(expectedIR, comp.getInnerRadius(), DELTA, comp.getClass().getSimpleName() + ": IR must be 0.0.");
    }

    // ==========================================
    // 3. EXCLUSION CHECK (Automatic Flag)
    // ==========================================

    /**
     * Tests that OuterRadius scaling is SKIPPED when OuterRadiusAutomatic is TRUE,
     * but Thickness (which has no exclusion) still scales.
     */
    @ParameterizedTest
    @MethodSource("thicknessRingComponents")
    void testOuterRadius_AutomaticOn(ThicknessRingComponent comp) throws Exception {
        // ARRANGE
        final double initialOR = 0.020;
        final double initialT = 0.002;

        comp.setOuterRadius(initialOR);
        comp.setThickness(initialT);

        // Set flag to TRUE (Must use the public wrapper on the concrete component)
        if (comp instanceof EngineBlock) {
            ((EngineBlock) comp).setOuterRadiusAutomatic(true);
        } else if (comp instanceof TubeCoupler) {
            ((TubeCoupler) comp).setOuterRadiusAutomatic(true);
        }

        double multiplier = 3.0;

        // ACT
        scaleMethod.invoke(dialogInstance, comp, multiplier, false);

        // ASSERT
        // 1. OuterRadius: Must NOT scale (GeneralScaler exclusion check)
        assertEquals(initialOR, comp.getOuterRadius(), DELTA, comp.getClass().getSimpleName() + ": OR must NOT be scaled when Automatic is TRUE.");

        // 2. Thickness: Must scale (No exclusion for Thickness)
        assertEquals(initialT * multiplier, comp.getThickness(), DELTA, comp.getClass().getSimpleName() + ": T must still scale (0.002 * 3.0).");

        // 3. InnerRadius: Must reflect the new, smaller thickness relative to the FIXED outer radius.
        assertEquals(initialOR - (initialT * multiplier), comp.getInnerRadius(), DELTA,
                comp.getClass().getSimpleName() + ": IR must reflect the new thickness (0.020 - 0.006 = 0.014).");
    }
}