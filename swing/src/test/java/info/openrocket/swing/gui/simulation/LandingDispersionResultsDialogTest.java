package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.jupiter.api.Test;

import info.openrocket.core.simulation.montecarlo.DispersionStatistics;
import info.openrocket.core.simulation.montecarlo.LandingBody;
import info.openrocket.core.simulation.montecarlo.LandingPoint;
import info.openrocket.core.simulation.montecarlo.MonteCarloDistribution;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.unit.UnitGroup;

public class LandingDispersionResultsDialogTest {
	/**
	 * The plot used to be exported at its on-screen pixel size, which is far too coarse
	 * for a report. Scaling has to lift a typical panel well past the target width, and
	 * stay bounded so a maximised window does not produce an enormous file.
	 */
	@Test
	public void testPlotExportScalesUpToAUsableResolution() {
		assertEquals(5, LandingDispersionResultsDialog.exportScale(540));
		assertEquals(3, LandingDispersionResultsDialog.exportScale(900));
		assertTrue(540 * LandingDispersionResultsDialog.exportScale(540) >= 2400);
		assertTrue(900 * LandingDispersionResultsDialog.exportScale(900) >= 2400);

		// A very wide panel is already sharp enough, but never drops below supersampling.
		assertEquals(2, LandingDispersionResultsDialog.exportScale(3000));
		// A tiny or unrealised panel must not produce a degenerate scale.
		assertEquals(6, LandingDispersionResultsDialog.exportScale(0));
		assertEquals(6, LandingDispersionResultsDialog.exportScale(100));
	}

	/** With no body there is nothing to describe, so the subtitle stays empty. */
	@Test
	public void testChartSubtitleIsEmptyWithoutALandingBody() {
		MonteCarloSettings settings = MonteCarloSettings.builder()
				.runCount(500)
				.seed(24680)
				.uncertainty(MonteCarloParameter.AXIAL_DRAG, MonteCarloDistribution.NORMAL, 0.1)
				.build();

		assertEquals("", LandingDispersionResultsDialog.chartSubtitle(null, 0, settings));
	}

	/**
	 * An exported plot carries no context beyond the image, so its title has to name the
	 * simulation and its subtitle the body, the sample size and the seed. Tests run under
	 * a {@code DebugTranslator} that echoes keys, so assert against the real bundle.
	 */
	@Test
	public void testChartTitleAndSubtitleTemplatesIdentifyTheAnalysis() {
		ResourceBundle bundle = ResourceBundle.getBundle("l10n.messages", Locale.ROOT);

		String title = String.format(bundle.getString("LandingDispersionResultsDlg.chart.title"), "Alpha III");
		assertTrue(title.contains("Alpha III"), title);

		String subtitle = String.format(bundle.getString("LandingDispersionResultsDlg.chart.subtitle"),
				"Sustainer", 487, 500, 24680);
		assertTrue(subtitle.contains("Sustainer"), subtitle);
		assertTrue(subtitle.contains("487"), subtitle);
		assertTrue(subtitle.contains("500"), subtitle);
		assertTrue(subtitle.contains("24680"), subtitle);
	}

	@Test
	public void testLegendHighlightEmphasizesOnlySelectedSeries() {
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

		LandingDispersionResultsDialog.applySeriesHighlight(renderer, 0);

		assertEquals(8, renderer.getSeriesShape(0).getBounds2D().getWidth(), 0.0);
		assertEquals(10, renderer.getSeriesShape(1).getBounds2D().getWidth(), 0.0);

		LandingDispersionResultsDialog.applySeriesHighlight(renderer, 4);

		assertEquals(5, renderer.getSeriesShape(0).getBounds2D().getWidth(), 0.0);
		assertEquals(12, renderer.getSeriesShape(3).getBounds2D().getWidth(), 0.0);
		assertEquals(4.5f, ((BasicStroke) renderer.getSeriesStroke(4)).getLineWidth(), 0.0f);
		assertEquals(1.8f, ((BasicStroke) renderer.getSeriesStroke(5)).getLineWidth(), 0.0f);
	}

	@Test
	public void testEllipsesShareColorAndUseDistinctLineStyles() {
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		Color ellipseColor = Color.GREEN;
		LandingDispersionResultsDialog.configureEllipseSeries(renderer, 4, ellipseColor, true);
		LandingDispersionResultsDialog.configureEllipseSeries(renderer, 5, ellipseColor, true);
		LandingDispersionResultsDialog.configureEllipseSeries(renderer, 6, ellipseColor, true);
		BasicStroke oneSigma = (BasicStroke) renderer.getSeriesStroke(4);
		BasicStroke twoSigma = (BasicStroke) renderer.getSeriesStroke(5);
		BasicStroke threeSigma = (BasicStroke) renderer.getSeriesStroke(6);

		assertEquals(renderer.getSeriesPaint(4), renderer.getSeriesPaint(5));
		assertEquals(renderer.getSeriesPaint(5), renderer.getSeriesPaint(6));
		assertNull(oneSigma.getDashArray());
		assertArrayEquals(new float[] { 6, 4 }, twoSigma.getDashArray());
		assertNotNull(threeSigma.getDashArray());
		assertArrayEquals(new float[] { 8, 3, 2, 3 }, threeSigma.getDashArray());
	}

	@Test
	public void testRendererKeepsDashPatternContinuousAndVisibleInLegend() {
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		LandingDispersionResultsDialog.configureLineRendering(renderer);

		assertTrue(renderer.getDrawSeriesLineAsPath());
		assertEquals(24.0, renderer.getLegendLine().getBounds2D().getWidth(), 0.0);
	}

	@Test
	public void testPlotExportFileNameIsSafe() {
		assertEquals("Simulation-1-Main-body-dispersion",
				LandingDispersionResultsDialog.safeFileName("Simulation 1-Main/body-dispersion"));
	}

	@Test
	public void testEllipseSeriesPreservesPerimeterOrder() {
		DispersionStatistics statistics = DispersionStatistics.from(List.of(
				new LandingPoint(0, "Body", -4, 0),
				new LandingPoint(0, "Body", 4, 0),
				new LandingPoint(0, "Body", 0, -1),
				new LandingPoint(0, "Body", 0, 1)));
		XYSeriesCollection dataset = new XYSeriesCollection();

		LandingDispersionResultsDialog.addEllipseSeries(dataset, "Ellipse", statistics, 1,
				UnitGroup.UNITS_DISTANCE.getSIUnit());

		XYSeries series = dataset.getSeries(0);
		assertFalse(series.getAutoSort());
		assertEquals(97, series.getItemCount());
		assertEquals(series.getX(0).doubleValue(), series.getX(96).doubleValue(), 1.0e-12);
		assertEquals(series.getY(0).doubleValue(), series.getY(96).doubleValue(), 1.0e-12);
		assertTrue(series.getX(0).doubleValue() > series.getX(24).doubleValue());
		assertTrue(series.getX(24).doubleValue() > series.getX(48).doubleValue());
	}
}
