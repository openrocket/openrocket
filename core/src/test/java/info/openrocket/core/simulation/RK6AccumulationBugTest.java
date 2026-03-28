package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.*;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MutableCoordinate;
import info.openrocket.core.util.BaseTestCase;

import org.junit.jupiter.api.Test;

/**
 * Tests for RK6 weighted k-value accumulation.
 *
 * The RK6 algorithm requires combining multiple weighted k-values when computing
 * intermediate positions (k4, k5, k6, k7). For example:
 *   k4 = f(t + h/3, y + (1/12)*k1 + (1/3)*k2 + (-1/12)*k3)
 *
 * All terms must be accumulated together, not computed separately.
 */
public class RK6AccumulationBugTest extends BaseTestCase {

    /**
     * Verifies that chaining addScaled calls properly accumulates all terms.
     */
    @Test
    public void testAccumulationPattern() {
        Coordinate base = new Coordinate(0, 0, 0);
        Coordinate k1 = new Coordinate(1, 0, 0);
        Coordinate k2 = new Coordinate(2, 0, 0);
        Coordinate k3 = new Coordinate(3, 0, 0);

        double w1 = 0.5;
        double w2 = 0.3;
        double w3 = 0.2;

        // Expected: base + k1*w1 + k2*w2 + k3*w3 = 0 + 0.5 + 0.6 + 0.6 = 1.7
        double expectedX = base.getX() + k1.getX()*w1 + k2.getX()*w2 + k3.getX()*w3;

        // Chained addScaled calls (correct pattern used in RK6SimulationStepper)
        MutableCoordinate mutableCoord = new MutableCoordinate();
        Coordinate result = mutableCoord.set(base)
            .addScaled(k1, w1)
            .addScaled(k2, w2)
            .addScaled(k3, w3)
            .toImmutable();

        assertEquals(expectedX, result.getX(), 0.001,
            "Chained addScaled should accumulate all weighted k-values");
    }
}
