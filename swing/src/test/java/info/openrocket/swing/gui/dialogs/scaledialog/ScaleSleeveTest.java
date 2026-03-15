package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.Sleeve;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the Sleeve component.
 * Sleeve is used to test linear scaling of inherited properties (Length, RadialPosition)
 * and verifies that its specific dimensions (InnerRadius, Thickness) are correctly
 * ignored by the generic dimensional scaler (SCALERS_NO_OFFSET).
 */
public class ScaleSleeveTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    // ==========================================
    // 1. INHERITED DIMENSION SCALING (Length)
    // ==========================================

    /**
     * Tests linear scaling up of Length (inherited from RingComponent).
     */
    @Test
    void testLengthScale_ScaleUp() throws Exception {
        // ARRANGE
        Sleeve sleeve = new Sleeve();
        sleeve.setLength(0.010); // L = 10 mm

        double multiplier = 2.5;
        double expectedLength = 0.025;

        // ACT: Invoke scaleMethod (handles Length scaling)
        scaleMethod.invoke(dialogInstance, sleeve, multiplier, false);

        // ASSERT
        assertEquals(expectedLength, sleeve.getLength(), DELTA,
                "Length must scale linearly.");
    }

    // ==========================================
    // 2. INHERITED POSITIONAL SCALING (RadialPosition)
    // ==========================================

    /**
     * Tests linear scaling of RadialPosition up (positional scaling).
     */
    @Test
    void testRadialPositionScale_ScaleUp() throws Exception {
        // ARRANGE
        Sleeve sleeve = new Sleeve();
        sleeve.setRadialPosition(0.005); // P = 5 mm

        double multiplier = 3.0;
        double expectedPosition = 0.015;

        // ACT: Invoke scaleOffsetMethod (handles positional scaling)
        scaleOffsetMethod.invoke(dialogInstance, sleeve, multiplier, false);

        // ASSERT
        assertEquals(expectedPosition, sleeve.getRadialPosition(), DELTA,
                "RadialPosition must scale linearly.");
    }

    // ==========================================
    // 3. SLEEVE-SPECIFIC NEGATIVE SCALING TEST
    // ==========================================

    /**
     * Tests that Sleeve's specific dimensions (InnerRadius, Thickness) are NOT
     * scaled by the generic scaleMethod, as Sleeve is not explicitly mapped for them.
     */
    @Test
    void testSleeveDimensions_AreNotScaled() throws Exception {
        // ARRANGE
        Sleeve sleeve = new Sleeve();
        sleeve.setInnerRadiusAutomatic(false); // Disable auto for deterministic value
        sleeve.setInnerRadius(0.020);
        sleeve.setThickness(0.005);
        sleeve.setLength(0.100); // Set length to verify it *does* scale

        final double originalInnerRadius = sleeve.getInnerRadius();
        final double originalThickness = sleeve.getThickness();
        final double multiplier = 2.0;

        // ACT: Invoke scaleMethod
        scaleMethod.invoke(dialogInstance, sleeve, multiplier, false);

        // ASSERT
        // InnerRadius and Thickness must remain unchanged
        assertEquals(originalInnerRadius, sleeve.getInnerRadius(), DELTA,
                "InnerRadius must NOT be scaled (no specific scaler defined).");
        assertEquals(originalThickness, sleeve.getThickness(), DELTA,
                "Thickness must NOT be scaled (no specific scaler defined).");

        // Sanity check: ensure Length *was* scaled
        assertEquals(0.200, sleeve.getLength(), DELTA, "Length must be scaled as it is inherited and mapped.");
    }
}