package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.BodyTube;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the BodyComponent abstract class.
 * This test uses a concrete implementation (BodyTube) to verify that the
 * inherited 'Length' property is scaled correctly.
 */
public class ScaleBodyComponentTest extends ScaleDialogBaseTest {

    @Test
    public void testBodyComponent() throws Exception {
        BodyTube tube = new BodyTube();
        tube.setLength(100.0);

        // --- STEP 1: Scale by 0.5 ---
        scaleMethod.invoke(dialogInstance, tube, 0.5, false);
        assertEquals(50.0, tube.getLength(), 0.001, "Body length should be halved");

        // --- STEP 2: Scale by 1.5 ---
        scaleMethod.invoke(dialogInstance, tube, 1.5, false);
        assertEquals(75.0, tube.getLength(), 0.001, "Body length should be 50 * 1.5");
    }
}
