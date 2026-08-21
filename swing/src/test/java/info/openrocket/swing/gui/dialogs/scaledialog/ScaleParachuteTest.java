package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.Parachute;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the Parachute component.
 * Verifies the linear scaling of its primary dimensions: Diameter and LineLength.
 */
public class ScaleParachuteTest extends ScaleDialogBaseTest {

    @Test
    public void testParachute() throws Exception {
        Parachute chute = new Parachute();
        chute.setDiameter(60.0);
        chute.setLineLength(40.0);

        // --- STEP 1: Scale by 0.5 ---
        scaleMethod.invoke(dialogInstance, chute, 0.5, false);

        assertEquals(30.0, chute.getDiameter(), 0.001);
        assertEquals(20.0, chute.getLineLength(), 0.001);

        // --- STEP 2: Scale by 1.5 ---
        scaleMethod.invoke(dialogInstance, chute, 1.5, false);

        assertEquals(45.0, chute.getDiameter(), 0.001);
        assertEquals(30.0, chute.getLineLength(), 0.001);
    }
}
