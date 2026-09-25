package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for the extended Barrowman drag calculations.
 */
public class BarrowmanDragCalculatorTest {
	private static final double EPSILON = 0.000001;

	/**
	 * Verify that body fineness is based on maximum diameter. A one-meter body
	 * with a 0.1-meter diameter has a fineness ratio of 10, giving a correction
	 * of 1 + 1 / (2 * 10) = 1.05.
	 */
	@Test
	public void testBodyFrictionCorrectionUsesDiameter() {
		double correction = BarrowmanDragCalculator.calculateBodyFrictionCorrection(1.0, 0.05);

		assertEquals(1.05, correction, EPSILON,
				"Body skin-friction correction should use length divided by diameter");
	}
}
