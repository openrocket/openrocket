package info.openrocket.swing.gui.print;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
* PrintUnit Tester.
*
*/
public class PrintUnitTest {

    /**
     *
     * Method: toMillis(double length)
     *
     */
    @Test
    public void testToMillis() throws Exception {
        Assertions.assertEquals(25.400000, PrintUnit.INCHES.toMillis(1), 0.00001);
        Assertions.assertEquals(1, PrintUnit.MILLIMETERS.toInches(PrintUnit.INCHES.toMillis(1)), 0.000001);
    }

    /**
     *
     * Method: toCentis(double length)
     *
     */
    @Test
    public void testToCentis() throws Exception {
        Assertions.assertEquals(4, PrintUnit.CENTIMETERS.toMeters(PrintUnit.METERS.toCentis(4)), 0.000001);
        Assertions.assertEquals(4, PrintUnit.CENTIMETERS.toPoints(PrintUnit.POINTS.toCentis(4)), 0.000001);
    }

    /**
     *
     * Method: toPoints(double length)
     *
     */
    @Test
    public void testToPoints() throws Exception {
        Assertions.assertEquals(1, PrintUnit.POINTS.toInches(72), 0.00001);
        Assertions.assertEquals(25.4, PrintUnit.POINTS.toMillis(72), 0.00001);
        Assertions.assertEquals(1, PrintUnit.MILLIMETERS.toPoints(PrintUnit.POINTS.toMillis(1)), 0.000001);

        Assertions.assertEquals(28.3464567, PrintUnit.CENTIMETERS.toPoints(1), 0.000001d);
    }


}
