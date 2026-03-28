package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the FreeformFinSet component.
 * Verifies that the coordinate points of the fin shape are scaled linearly.
 */
public class ScaleFreeformFinSetTest extends ScaleDialogBaseTest {

    @Test
    public void testFreeformFinSet() throws Exception {
        FreeformFinSet fins = new FreeformFinSet();

        // FreeformFinSet has a hard limit of 2.5m (SNAP_LARGER_THAN).
        Coordinate[] originalPoints = new Coordinate[] {
                new Coordinate(0,0),
                new Coordinate(1.0, 0),
                new Coordinate(0, 1.0)
        };
        fins.setPoints(originalPoints);

        // --- STEP 1: Scale by 0.5 ---
        scaleMethod.invoke(dialogInstance, fins, 0.5, false);
        CoordinateIF[] pointsStep1 = fins.getFinPoints();

        assertEquals(0.5, pointsStep1[1].getX(), 0.001);
        assertEquals(0.0, pointsStep1[1].getY(), 0.001);
        assertEquals(0.0, pointsStep1[2].getX(), 0.001);
        assertEquals(0.5, pointsStep1[2].getY(), 0.001);

        // --- STEP 2: Scale by 1.5 ---
        scaleMethod.invoke(dialogInstance, fins, 1.5, false);
        CoordinateIF[] pointsStep2 = fins.getFinPoints();

        // 0.5 * 1.5 = 0.75
        assertEquals(0.75, pointsStep2[1].getX(), 0.001);
        assertEquals(0.0, pointsStep1[1].getY(), 0.001);
        assertEquals(0.0, pointsStep2[2].getX(), 0.001);
        assertEquals(0.75, pointsStep2[2].getY(), 0.001);
    }
}
