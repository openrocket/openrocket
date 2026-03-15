package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.MassComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the MassComponent.
 * Verifies linear scaling of dimensions and cubic scaling of mass (M' = M * multiplier^3).
 */
public class ScaleMassComponentTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    // ==========================================
    // CUBIC MASS SCALING TESTS
    // ==========================================

    /**
     * Tests scaling the component mass up cubically (multiplier > 1.0) when the mass scaling flag is true.
     */
    @Test
    void testMassScale_ScaleUp() throws Exception {
        // ARRANGE
        MassComponent massComp = new MassComponent();
        massComp.setComponentMass(1.0);
        double multiplier = 2.0;

        // Expected mass: 1.0 * 2.0^3 = 8.0
        double expectedMass = 8.0;

        // ACT: Invoke scaleMethod with scaleMass = true
        scaleMethod.invoke(dialogInstance, massComp, multiplier, true);

        // ASSERT
        assertEquals(expectedMass, massComp.getComponentMass(), DELTA,
                "Mass must scale cubically.");
    }

    /**
     * Tests scaling the component mass down cubically (multiplier < 1.0) when the mass scaling flag is true.
     */
    @Test
    void testMassScale_ScaleDown() throws Exception {
        // ARRANGE
        MassComponent massComp = new MassComponent();
        massComp.setComponentMass(16.0);
        double multiplier = 0.5;

        // Expected mass: 16.0 * 0.5^3 = 2.0
        double expectedMass = 2.0;

        // ACT: Invoke scaleMethod with scaleMass = true
        scaleMethod.invoke(dialogInstance, massComp, multiplier, true);

        // ASSERT
        assertEquals(expectedMass, massComp.getComponentMass(), DELTA,
                "Mass must scale cubically.");
    }

    /**
     * Tests that mass is NOT scaled when the mass scaling flag is false.
     */
    @Test
    void testMassScale_FlagFalse_Unchanged() throws Exception {
        // ARRANGE
        MassComponent massComp = new MassComponent();
        massComp.setComponentMass(10.0);
        double multiplier = 2.0;

        // ACT: Invoke scaleMethod with scaleMass = false
        scaleMethod.invoke(dialogInstance, massComp, multiplier, false);

        // ASSERT
        assertEquals(10.0, massComp.getComponentMass(), DELTA,
                "Mass must remain unchanged when scaleMass is false.");
    }

    // ==========================================
    // LINEAR DIMENSION/POSITION SCALING TESTS
    // ==========================================

    /**
     * Tests linear scaling of physical dimensions (Length and Radius).
     */
    @Test
    void testDimensionsScale() throws Exception {
        // ARRANGE
        MassComponent massComp = new MassComponent();
        massComp.setLength(10.0);
        massComp.setRadius(2.0);
        // Disable auto-radius to ensure manual radius is scaled
        massComp.setRadiusAutomatic(false);
        double multiplier = 1.5;

        // ACT: Invoke scaleMethod (handles dimensional scaling)
        scaleMethod.invoke(dialogInstance, massComp, multiplier, false);

        // ASSERT
        assertEquals(15.0, massComp.getLength(), DELTA,
                "Length must scale linearly.");
        assertEquals(3.0, massComp.getRadius(), DELTA,
                "Radius must scale linearly.");
    }

    /**
     * Tests linear scaling of RadialPosition (positional offset).
     */
    @Test
    void testRadialPositionScale_Linear() throws Exception {
        // ARRANGE
        MassComponent massComp = new MassComponent();
        massComp.setRadialPosition(5.0);
        double multiplier = 0.5;

        // ACT: Invoke scaleOffsetMethod (handles positional scaling)
        scaleOffsetMethod.invoke(dialogInstance, massComp, multiplier, false);

        // ASSERT
        assertEquals(2.5, massComp.getRadialPosition(), DELTA,
                "RadialPosition must scale linearly.");
    }

    /**
     * Tests reversibility for both linear dimensions and cubic mass.
     */
    @Test
    void testAllProperties_ScaleDownThenUp() throws Exception {
        // ARRANGE
        MassComponent massComp = new MassComponent();
        final double originalLength = 10.0;
        final double originalMass = 8.0;
        massComp.setLength(originalLength);
        massComp.setComponentMass(originalMass);

        // ACT 1: Scale down by 0.5 (linear for length, cubic for mass)
        scaleMethod.invoke(dialogInstance, massComp, 0.5, true);

        // ACT 2: Scale up by 2.0 
        scaleMethod.invoke(dialogInstance, massComp, 2.0, true);

        // ASSERT: Final values should revert to the originals (0.5 * 2.0 = 1.0)
        assertEquals(originalLength, massComp.getLength(), DELTA,
                "Length must revert to original.");
        assertEquals(originalMass, massComp.getComponentMass(), DELTA,
                "Mass must revert to original.");
    }
}