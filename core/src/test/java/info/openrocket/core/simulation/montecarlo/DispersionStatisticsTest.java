package info.openrocket.core.simulation.montecarlo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import info.openrocket.core.simulation.montecarlo.DispersionStatistics.DispersionEllipse;

public class DispersionStatisticsTest {
	private static final double EPSILON = 1.0e-12;

	@Test
	public void testContainmentAndPrincipalAxes() {
		List<LandingPoint> points = List.of(
				new LandingPoint(0, "Rocket", 1, 0),
				new LandingPoint(0, "Rocket", -1, 0),
				new LandingPoint(0, "Rocket", 0, 2),
				new LandingPoint(0, "Rocket", 0, -2));

		DispersionStatistics statistics = DispersionStatistics.from(points);
		assertEquals(4, statistics.getSampleCount());
		assertEquals(0, statistics.getMeanEast(), EPSILON);
		assertEquals(0, statistics.getMeanNorth(), EPSILON);
		assertEquals(1, statistics.getContainmentRadius(0.50), EPSILON);
		assertEquals(2, statistics.getContainmentRadius(0.90), EPSILON);

		DispersionEllipse ellipse = statistics.getEllipse(1);
		assertEquals(Math.sqrt(8.0 / 3.0), ellipse.semiMajor(), EPSILON);
		assertEquals(Math.sqrt(2.0 / 3.0), ellipse.semiMinor(), EPSILON);
		assertEquals(0, statistics.getMajorAxisBearing(), EPSILON);
	}

	@Test
	public void testNearestRankContainmentActuallyContainsRequestedFraction() {
		List<LandingPoint> points = List.of(
				new LandingPoint(0, "Rocket", 0, 0),
				new LandingPoint(0, "Rocket", 1, 0),
				new LandingPoint(0, "Rocket", 2, 0),
				new LandingPoint(0, "Rocket", 3, 0),
				new LandingPoint(0, "Rocket", 4, 0));
		DispersionStatistics statistics = DispersionStatistics.from(points);

		assertEquals(2, statistics.getContainmentRadius(0.80), EPSILON);
		assertThrows(IllegalArgumentException.class, () -> statistics.getContainmentRadius(0));
	}
}
