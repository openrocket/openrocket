package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the TrapezoidFinSet component.
 * Verifies the linear scaling of its primary dimensions: RootChord, TipChord,
 * Height, Sweep, and Thickness.
 */
public class ScaleTrapezoidFinSetTest extends ScaleDialogBaseTest {

    @Test
    public void testTrapezoidFinSet() throws Exception {
        TrapezoidFinSet fins = new TrapezoidFinSet();
        fins.setRootChord(20.0);
        fins.setTipChord(10.0);
        fins.setHeight(10.0);
        fins.setSweep(6.0);
        fins.setThickness(2.0);

        // --- STEP 1: Scale by 0.5 ---
        scaleMethod.invoke(dialogInstance, fins, 0.5, false);

        assertEquals(10.0, fins.getRootChord(), 0.001);
        assertEquals(5.0, fins.getTipChord(), 0.001);
        assertEquals(5.0, fins.getHeight(), 0.001);
        assertEquals(3.0, fins.getSweep(), 0.001);
        assertEquals(1.0, fins.getThickness(), 0.001);

        // --- STEP 2: Scale by 1.5 ---
        scaleMethod.invoke(dialogInstance, fins, 1.5, false);

        assertEquals(15.0, fins.getRootChord(), 0.001);
        assertEquals(7.5, fins.getTipChord(), 0.001);
        assertEquals(7.5, fins.getHeight(), 0.001);
        assertEquals(4.5, fins.getSweep(), 0.001);
        assertEquals(1.5, fins.getThickness(), 0.001);
    }
}
