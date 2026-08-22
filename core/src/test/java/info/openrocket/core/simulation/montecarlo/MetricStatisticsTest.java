package info.openrocket.core.simulation.montecarlo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class MetricStatisticsTest {
	@Test
	public void testDescriptiveStatisticsAndNearestRankQuantiles() {
		MetricStatistics statistics = MetricStatistics.from(List.of(5.0, 1.0, 4.0, 2.0, 3.0));

		assertEquals(5, statistics.getSampleCount());
		assertEquals(1.0, statistics.getMinimum());
		assertEquals(5.0, statistics.getMaximum());
		assertEquals(3.0, statistics.getMean());
		assertEquals(3.0, statistics.getMedian());
		assertEquals(Math.sqrt(2.5), statistics.getStandardDeviation(), 1.0e-12);
		assertEquals(1.0, statistics.getQuantile(0.05));
		assertEquals(5.0, statistics.getQuantile(0.95));
	}

	@Test
	public void testNonFiniteValuesAreIgnored() {
		MetricStatistics statistics = MetricStatistics.from(
				List.of(Double.NaN, 2.0, Double.POSITIVE_INFINITY, 4.0));

		assertEquals(2, statistics.getSampleCount());
		assertEquals(3.0, statistics.getMean());
		assertThrows(IllegalArgumentException.class,
				() -> MetricStatistics.from(List.of(Double.NaN)));
	}
}
