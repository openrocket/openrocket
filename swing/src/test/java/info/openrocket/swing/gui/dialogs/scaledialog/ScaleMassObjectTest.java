package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.MassComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the MassComponent.
 * Verifies the linear scaling of its dimensional (Length, Radius) and
 * positional (RadialPosition) properties.
 */
public class ScaleMassObjectTest extends ScaleDialogBaseTest {

    @Test
    public void testMassObject() throws Exception {
        MassComponent mass = new MassComponent();
        mass.setLength(10.0);
        mass.setRadius(2.0);
        mass.setRadiusAutomatic(false);
        mass.setRadialPosition(6.0);

        // --- STEP 1: Scale by 0.5 ---
        // Scale Dimensions (SCALERS_NO_OFFSET)
        scaleMethod.invoke(dialogInstance, mass, 0.5, false);
        // Scale Radial Position (SCALERS_OFFSET)
        scaleOffsetMethod.invoke(dialogInstance, mass, 0.5, false);

        assertEquals(5.0, mass.getLength(), 0.001);
        assertEquals(1.0, mass.getRadius(), 0.001);
        assertEquals(3.0, mass.getRadialPosition(), 0.001);

        // --- STEP 2: Scale by 1.5 ---
        scaleMethod.invoke(dialogInstance, mass, 1.5, false);
        scaleOffsetMethod.invoke(dialogInstance, mass, 1.5, false);

        assertEquals(7.5, mass.getLength(), 0.001);
        assertEquals(1.5, mass.getRadius(), 0.001);
        assertEquals(4.5, mass.getRadialPosition(), 0.001);
    }
}
