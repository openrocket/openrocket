package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the EllipticalFinSet component.
 * Verifies linear scaling of Length and Height using the GeneralScaler.
 */
public class ScaleEllipticalFinSetTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    /**
     * Tests linear scaling up of Length and Height (multiplier > 1.0).
     */
    @Test
    void testDimensionsScale_ScaleUp() throws Exception {
        // ARRANGE
        EllipticalFinSet fins = new EllipticalFinSet();
        fins.setLength(0.150);
        fins.setHeight(0.050);

        double multiplier = 2.0;

        // ACT
        scaleMethod.invoke(dialogInstance, fins, multiplier, false);

        // ASSERT
        assertEquals(0.300, fins.getLength(), DELTA,
                "Length must scale linearly.");
        assertEquals(0.100, fins.getHeight(), DELTA,
                "Height must scale linearly.");
    }

    /**
     * Tests linear scaling down of Length and Height (multiplier < 1.0).
     */
    @Test
    void testDimensionsScale_ScaleDown() throws Exception {
        // ARRANGE
        EllipticalFinSet fins = new EllipticalFinSet();
        fins.setLength(0.400);
        fins.setHeight(0.160);

        double multiplier = 0.25;

        // ACT
        scaleMethod.invoke(dialogInstance, fins, multiplier, false);

        // ASSERT
        assertEquals(0.100, fins.getLength(), DELTA,
                "Length must scale down linearly.");
        assertEquals(0.040, fins.getHeight(), DELTA,
                "Height must scale down linearly.");
    }

    /**
     * Tests scaling reversibility by scaling down (0.5) then scaling up (2.0).
     */
    @Test
    void testDimensionsScale_ScaleDownThenUp() throws Exception {
        // ARRANGE
        EllipticalFinSet fins = new EllipticalFinSet();
        final double originalLength = 0.220;
        final double originalHeight = 0.080;
        fins.setLength(originalLength);
        fins.setHeight(originalHeight);

        // ACT 1: Scale down by 0.5
        scaleMethod.invoke(dialogInstance, fins, 0.5, false);

        // ACT 2: Scale up by 2.0
        scaleMethod.invoke(dialogInstance, fins, 2.0, false);

        // ASSERT
        assertEquals(originalLength, fins.getLength(), DELTA,
                "Length must revert to original after 0.5 then 2.0 scaling.");
        assertEquals(originalHeight, fins.getHeight(), DELTA,
                "Height must revert to original after 0.5 then 2.0 scaling.");
    }

}