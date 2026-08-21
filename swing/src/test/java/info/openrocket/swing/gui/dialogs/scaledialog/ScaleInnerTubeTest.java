package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.InnerTube;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the InnerTube component.
 * Verifies the linear scaling of its dimensional (MotorOverhang) and
 * positional (AxialOffset) properties.
 */
public class ScaleInnerTubeTest extends ScaleDialogBaseTest {

    @Test
    public void testInnerTube() throws Exception {
        InnerTube tube = new InnerTube();

        tube.setMotorOverhang(6.0);
        tube.setAxialOffset(12.0);

        // --- STEP 1: Scale by 0.5 ---
        scaleMethod.invoke(dialogInstance, tube, 0.5, false);
        scaleOffsetMethod.invoke(dialogInstance, tube, 0.5, false);

        assertEquals(3.0, tube.getMotorOverhang(), 0.001);
        assertEquals(6.0, tube.getAxialOffset(), 0.001);

        // --- STEP 2: Scale by 1.5 ---
        scaleMethod.invoke(dialogInstance, tube, 1.5, false);
        scaleOffsetMethod.invoke(dialogInstance, tube, 1.5, false);

        assertEquals(4.5, tube.getMotorOverhang(), 0.001);
        assertEquals(9.0, tube.getAxialOffset(), 0.001);
    }
}
