package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.LaunchLug;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the LaunchLug component.
 * Verifies the linear scaling of its primary dimensions: Length, OuterRadius,
 * and Thickness.
 */
public class ScaleLaunchLugTest extends ScaleDialogBaseTest {

    @Test
    public void testLaunchLug() throws Exception {
        LaunchLug lug = new LaunchLug();
        lug.setLength(40.0);
        lug.setOuterRadius(4.0);
        lug.setThickness(0.5);

        // --- STEP 1: Scale by 0.5 ---
        scaleMethod.invoke(dialogInstance, lug, 0.5, false);

        assertEquals(20.0, lug.getLength(), 0.001);
        assertEquals(2.0, lug.getOuterRadius(), 0.001);
        assertEquals(0.25, lug.getThickness(), 0.001);

        // --- STEP 2: Scale by 1.5 ---
        scaleMethod.invoke(dialogInstance, lug, 1.5, false);

        assertEquals(30.0, lug.getLength(), 0.001);
        assertEquals(3.0, lug.getOuterRadius(), 0.001);
        assertEquals(0.375, lug.getThickness(), 0.001);
    }
}
