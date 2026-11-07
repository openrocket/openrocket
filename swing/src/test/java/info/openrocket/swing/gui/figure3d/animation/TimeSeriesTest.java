package info.openrocket.swing.gui.figure3d.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeSeriesTest {
	private static final double[] TIMES = { 0.0, 1.0, 1.0, 3.0 };
	private static final double[] VALUES = { 0.0, 10.0, 20.0, 40.0 };

	@Test
	void interpolatesBetweenSamplesAndHoldsTheEnds() {
		assertEquals(5.0, TimeSeries.interpolate(TIMES, VALUES, 0.5), 1e-12);
		assertEquals(30.0, TimeSeries.interpolate(TIMES, VALUES, 2.0), 1e-12);
		assertEquals(0.0, TimeSeries.interpolate(TIMES, VALUES, -4.0));
		assertEquals(40.0, TimeSeries.interpolate(TIMES, VALUES, 9.0));
	}

	@Test
	void aRepeatedTimeStepsToTheLaterSample() {
		// Data recorded twice at one instant (e.g. around an event) must not divide by zero.
		assertEquals(20.0, TimeSeries.interpolate(TIMES, VALUES, 1.0), 1e-12);
	}

	@Test
	void noSamplesReadAsZero() {
		assertEquals(0.0, TimeSeries.interpolate(new double[0], new double[0], 1.0));
	}
}
