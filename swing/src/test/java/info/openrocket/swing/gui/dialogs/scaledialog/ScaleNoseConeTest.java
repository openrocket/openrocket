package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.NoseCone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the NoseCone component.
 * Verifies the linear scaling of its primary dimensions: Length, BaseRadius,
 * ShoulderLength, and ShoulderRadius.
 */
public class ScaleNoseConeTest extends ScaleDialogBaseTest {

    @Test
    public void testNoseCone() throws Exception {
        NoseCone nose = new NoseCone();
        nose.setLength(40.0);
        nose.setBaseRadius(10.0);
        nose.setBaseRadiusAutomatic(false);
        nose.setShoulderLength(4.0);
        nose.setShoulderRadius(8.0);

        // --- STEP 1: Scale by 0.5 ---
        scaleMethod.invoke(dialogInstance, nose, 0.5, false);

        assertEquals(20.0, nose.getLength(), 0.001);
        assertEquals(5.0, nose.getBaseRadius(), 0.001);
        assertEquals(2.0, nose.getShoulderLength(), 0.001);
        assertEquals(4.0, nose.getShoulderRadius(), 0.001);

        // --- STEP 2: Scale by 1.5 ---
        scaleMethod.invoke(dialogInstance, nose, 1.5, false);

        assertEquals(30.0, nose.getLength(), 0.001);       // 20 * 1.5
        assertEquals(7.5, nose.getBaseRadius(), 0.001);    // 5 * 1.5
        assertEquals(3.0, nose.getShoulderLength(), 0.001); // 2 * 1.5
        assertEquals(6.0, nose.getShoulderRadius(), 0.001); // 4 * 1.5
    }
}
