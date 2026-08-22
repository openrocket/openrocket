package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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

	@Test
	public void testHistogramTooltipDescribesTheBinRangeAndCount() {
		HistogramDataset dataset = MonteCarloMetricsPanel.createHistogramDataset("Apogee",
				List.of(100.0, 101.0, 102.0), UnitGroup.UNITS_DISTANCE.getSIUnit());

		String template = ResourceBundle.getBundle("l10n.messages", Locale.ROOT)
				.getString("LandingDispersionResultsDlg.metrics.histogram.ttip");
		String tooltip = MonteCarloMetricsPanel.formatHistogramTooltip(template, dataset, 0,
				UnitGroup.UNITS_DISTANCE.getSIUnit());

		assertTrue(tooltip.contains("–"), tooltip);
		assertTrue(tooltip.contains("m"), tooltip);
		assertTrue(tooltip.endsWith("runs"), tooltip);
	}

	@Test
	public void testBoxPlotUsesDisplayValues() {
		var dataset = MonteCarloMetricsPanel.createBoxDataset(List.of(100.0, 110.0, 120.0),
				UnitGroup.UNITS_DISTANCE.getSIUnit(), "Apogee", "Rocket");

		assertEquals(110.0, dataset.getMedianValue("Apogee", "Rocket"));
		assertEquals(100.0, dataset.getMinRegularValue("Apogee", "Rocket"));
		assertEquals(120.0, dataset.getMaxRegularValue("Apogee", "Rocket"));
	}
}
