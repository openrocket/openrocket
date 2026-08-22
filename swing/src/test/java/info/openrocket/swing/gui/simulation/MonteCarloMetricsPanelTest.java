package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.jfree.data.statistics.HistogramDataset;
import org.junit.jupiter.api.Test;

import info.openrocket.core.unit.UnitGroup;

public class MonteCarloMetricsPanelTest {
	@Test
	public void testHistogramBinCountIsUsefulAndBounded() {
		assertEquals(5, MonteCarloMetricsPanel.binsForSampleCount(2));
		assertEquals(10, MonteCarloMetricsPanel.binsForSampleCount(100));
		assertEquals(50, MonteCarloMetricsPanel.binsForSampleCount(100_000));
	}

	@Test
	public void testHistogramUsesTheSelectedDisplayUnit() {
		HistogramDataset dataset = MonteCarloMetricsPanel.createHistogramDataset("Apogee",
				List.of(100.0, 200.0), UnitGroup.UNITS_DISTANCE.getSIUnit());

		assertEquals(1, dataset.getSeriesCount());
		assertTrue(dataset.getXValue(0, 0) >= 100.0);
		assertTrue(dataset.getXValue(0, dataset.getItemCount(0) - 1) <= 200.0);
	}

	@Test
	public void testHistogramAcceptsAZeroSpreadMetric() {
		HistogramDataset dataset = MonteCarloMetricsPanel.createHistogramDataset("Flight time",
				List.of(10.0, 10.0, 10.0), UnitGroup.UNITS_LONG_TIME.getSIUnit());

		double total = 0;
		for (int item = 0; item < dataset.getItemCount(0); item++) {
			total += dataset.getYValue(0, item);
		}
		assertEquals(3.0, total);
	}
}
