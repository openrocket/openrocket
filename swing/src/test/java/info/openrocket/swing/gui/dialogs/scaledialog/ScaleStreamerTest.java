package info.openrocket.swing.gui.dialogs.scaledialog;

import info.openrocket.core.rocketcomponent.Streamer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the scaling logic of the Streamer component.
 * Verifies linear scaling (StripLength and StripWidth) using the GeneralScaler.
 */
public class ScaleStreamerTest extends ScaleDialogBaseTest {

    private static final double DELTA = 1e-6;

    /**
     * Tests linear dimensional scaling when the multiplier is greater than 1.0.
     */
    @Test
    void testDimensionsScale_ScaleUp() throws Exception {
        // ARRANGE
        Streamer streamer = new Streamer();
        streamer.setStripLength(0.75);
        streamer.setStripWidth(0.05);
        double multiplier = 3.0;

        double expectedLength = 2.25;
        double expectedWidth = 0.15;

        // ACT (scaleMass is false for dimensions)
        scaleMethod.invoke(dialogInstance, streamer, multiplier, false);

        // ASSERT
        assertEquals(expectedLength, streamer.getStripLength(), DELTA,
                "StripLength must scale linearly.");
        assertEquals(expectedWidth, streamer.getStripWidth(), DELTA,
                "StripWidth must scale linearly.");
    }

    /**
     * Tests linear dimensional scaling when the multiplier is less than 1.0.
     */
    @Test
    void testDimensionsScale_ScaleDown() throws Exception {
        // ARRANGE
        Streamer streamer = new Streamer();
        streamer.setStripLength(1.0);
        streamer.setStripWidth(0.2);
        double multiplier = 0.5;

        double expectedLength = 0.5;
        double expectedWidth = 0.1;

        // ACT
        scaleMethod.invoke(dialogInstance, streamer, multiplier, false);

        // ASSERT
        assertEquals(expectedLength, streamer.getStripLength(), DELTA,
                "StripLength must scale down linearly.");
        assertEquals(expectedWidth, streamer.getStripWidth(), DELTA,
                "StripWidth must scale down linearly.");
    }

    /**
     * Tests scaling with a multiplier of exactly 1.0 (boundary check).
     */
    @Test
    void testDimensionsScale_MultiplierOne() throws Exception {
        // ARRANGE
        Streamer streamer = new Streamer();
        streamer.setStripLength(0.55);
        streamer.setStripWidth(0.12);
        double multiplier = 1.0;

        // ACT
        scaleMethod.invoke(dialogInstance, streamer, multiplier, false);

        // ASSERT
        assertEquals(0.55, streamer.getStripLength(), DELTA,
                "StripLength must be unchanged when multiplier is 1.0.");
        assertEquals(0.12, streamer.getStripWidth(), DELTA,
                "StripWidth must be unchanged when multiplier is 1.0.");
    }

    /**
     * Tests scaling reversibility by scaling down (0.5) then scaling up (2.0).
     */
    @Test
    void testDimensionsScale_ScaleDownThenUp() throws Exception {
        // ARRANGE
        Streamer streamer = new Streamer();
        final double originalLength = 0.9;
        final double originalWidth = 0.3;
        streamer.setStripLength(originalLength);
        streamer.setStripWidth(originalWidth);

        // ACT 1: Scale down by 0.5
        scaleMethod.invoke(dialogInstance, streamer, 0.5, false);

        // ACT 2: Scale up by 2.0
        scaleMethod.invoke(dialogInstance, streamer, 2.0, false);

        // ASSERT: Final values should be the originals (0.5 * 2.0 = 1.0)
        assertEquals(originalLength, streamer.getStripLength(), DELTA,
                "StripLength must return to original state.");
        assertEquals(originalWidth, streamer.getStripWidth(), DELTA,
                "StripWidth must return to original state.");
    }

    /**
     * Tests that the scaleMass flag has NO effect on a component that does not implement cubic mass scaling.
     */
    @Test
    void testMassScaling_FlagTrue() throws Exception {
        // ARRANGE
        Streamer streamer = new Streamer();
        streamer.setStripLength(1.0);
        streamer.setStripWidth(0.1);
        double multiplier = 2.0;

        // ACT: Invoke with scaleMass = true (should be ignored by GeneralScaler)
        scaleMethod.invoke(dialogInstance, streamer, multiplier, true);

        // ASSERT: Scaling must still be linear (not cubic)
        assertEquals(2.0, streamer.getStripLength(), DELTA,
                "StripLength must be linear despite scaleMass=true.");
        assertEquals(0.2, streamer.getStripWidth(), DELTA,
                "StripWidth must be linear despite scaleMass=true.");
    }

}