package info.openrocket.core.aerodynamics.barrowman;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FinBodyInterferenceTest {
	private static final double EPSILON = 1.0e-12;
	private static final double TAU = 0.25;

	/**
	 * The subsonic multiplier includes both the fin-in-body and body-in-fin terms.
	 */
	@Test
	public void includesBodyContributionAtSubsonicSpeeds() {
		assertEquals(1.5625, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 0.5), EPSILON);
		assertEquals(1.5625, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 0.9), EPSILON);
	}

	/**
	 * The unmodeled body term is smoothly removed through the transonic interval.
	 */
	@Test
	public void blendsBodyContributionThroughTransonicSpeeds() {
		assertEquals(1.40625, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 1.2), EPSILON);
	}

	/**
	 * The established fin-in-body correction remains unchanged at supersonic speeds.
	 */
	@Test
	public void retainsClassicalCorrectionAtSupersonicSpeeds() {
		assertEquals(1.25, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 1.5), EPSILON);
		assertEquals(1.25, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 3.0), EPSILON);
	}
}
