package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.BodyTube;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the RocketComponent abstract class.
 * This test uses a concrete implementation (BodyTube) to verify that the
 * inherited override properties (CG and Mass) are scaled correctly.
 */
public class ScaleRocketComponentTest extends ScaleDialogBaseTest {

    @Test
    public void testRocketComponent_Overrides() throws Exception {
        // We use BodyTube as a concrete implementation of RocketComponent
        BodyTube comp = new BodyTube();

        // Setup Overrides
        comp.setCGOverridden(true);
        comp.setOverrideCGX(10.0);

        comp.setMassOverridden(true);
        comp.setOverrideMass(8.0); // Using 8.0 to make cubic math cleaner

        // --- STEP 1: Scale by 0.5 ---
        scaleOffsetMethod.invoke(dialogInstance, comp, 0.5, true);

        // Assert Step 1
        assertEquals(5.0, comp.getOverrideCGX(), 0.001, "Override CGX should halve");
        // Mass scales by cube: 0.5^3 = 0.125. Mass 8 * 0.125 = 1.0
        assertEquals(1.0, comp.getOverrideMass(), 0.001, "Override Mass should scale by cube (0.5)");

        // --- STEP 2: Scale by 1.5 ---
        scaleOffsetMethod.invoke(dialogInstance, comp, 1.5, true);

        // Assert Step 2
        // Previous CG (5.0) * 1.5 = 7.5
        assertEquals(7.5, comp.getOverrideCGX(), 0.001, "Override CGX should scale by 1.5");
        // Previous Mass (1.0) * 1.5^3 (3.375) = 3.375
        assertEquals(3.375, comp.getOverrideMass(), 0.001, "Override Mass should scale by cube (1.5)");
    }
}
