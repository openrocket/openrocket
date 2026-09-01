package info.openrocket.swing.gui.print;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.montecarlo.DispersionStatistics;
import info.openrocket.core.simulation.montecarlo.DispersionStatistics.DispersionEllipse;
import info.openrocket.core.simulation.montecarlo.LandingBody;
import info.openrocket.core.simulation.montecarlo.LandingPoint;
import info.openrocket.core.simulation.montecarlo.MetricStatistics;
import info.openrocket.core.simulation.montecarlo.MonteCarloBranchResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloFlightBranch;
import info.openrocket.core.simulation.montecarlo.MonteCarloMetric;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.LineStyle;
import info.openrocket.swing.gui.simulation.MonteCarloLabels;
import info.openrocket.swing.gui.theme.UITheme;

/** Creates charts with a stable print palette independent of the active UI theme. */
final class MonteCarloReportCharts {
	private static final Translator trans = Application.getTranslator();
	private static final BasicStroke MARKER_STROKE = new BasicStroke(2.0f);
	private static final Line2D LEGEND_LINE = new Line2D.Double(-7, 0, 7, 0);
	private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 32);
	private static final Font SUBTITLE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
	private static final Font AXIS_LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 26);
	private static final Font TICK_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 22);
	private static final Font LEGEND_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 22);

	private static Color backgroundColor;
	private static Color textColor;
	private static Color gridColor;
	private static Color landingColor;
	private static Color histogramColor;
	private static Color boxFillColor;
	private static Color medianColor;
	private static Color intervalColor;
	private static Color nominalColor;
	private static Color meanColor;
	private static Color launchPadColor;
	private static Color ellipseColor;

	static {
		initColors();
	}

	private MonteCarloReportCharts() {
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(MonteCarloReportCharts::updateColors);
	}

	public static void updateColors() {
		backgroundColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_BACKGROUND,
				UITheme.getColor(UITheme.Keys.BACKGROUND));
		textColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_TEXT,
				UITheme.getColor(UITheme.Keys.TEXT));
		gridColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_GRID,
				UITheme.getColor(UITheme.Keys.BORDER));
		landingColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_LANDING,
				UITheme.getColor(UITheme.Keys.INFO));
		histogramColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_HISTOGRAM,
				UITheme.getColor(UITheme.Keys.INFO));
		boxFillColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_BOX_FILL,
				UITheme.getColor(UITheme.Keys.INFO));
		medianColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_MEDIAN,
				UITheme.getColor(UITheme.Keys.TEXT));
		intervalColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_INTERVAL,
				UITheme.getColor(UITheme.Keys.INFO));
		nominalColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_NOMINAL,
				UITheme.getColor(UITheme.Keys.WARNING));
		meanColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_MEAN,
				UITheme.getColor(UITheme.Keys.ERROR));
		launchPadColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_LAUNCH_PAD,
				UITheme.getColor(UITheme.Keys.INFO));
		ellipseColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_REPORT_ELLIPSE,
				UITheme.getColor(UITheme.Keys.INFO));
	}

	static JFreeChart landingChart(String simulationName, MonteCarloResult result, LandingBody body) {
		Unit unit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
		List<LandingPoint> points = result.getLandingPoints(body.bodyId());
		DispersionStatistics statistics = points.isEmpty() ? null : DispersionStatistics.from(points);
		XYSeriesCollection dataset = new XYSeriesCollection();

		XYSeries landings = new XYSeries(trans.get("LandingDispersionResultsDlg.series.landings"));
		for (LandingPoint point : points) {
			landings.add(unit.toUnit(point.east()), unit.toUnit(point.north()));
		}
		dataset.addSeries(landings);

		LandingPoint nominal = result.getNominalResult().getFailureMessage(body.bodyId()) == null
				? result.getNominalResult().getLandingPoint(body.bodyId()) : null;
		XYSeries nominalSeries = new XYSeries(trans.get("LandingDispersionResultsDlg.series.nominal"));
		if (nominal != null) {
			nominalSeries.add(unit.toUnit(nominal.east()), unit.toUnit(nominal.north()));
		}
		dataset.addSeries(nominalSeries);

		XYSeries meanSeries = new XYSeries(trans.get("LandingDispersionResultsDlg.series.mean"));
		if (statistics != null) {
			meanSeries.add(unit.toUnit(statistics.getMeanEast()), unit.toUnit(statistics.getMeanNorth()));
		}
		dataset.addSeries(meanSeries);

		XYSeries padSeries = new XYSeries(trans.get("LandingDispersionResultsDlg.series.pad"));
		padSeries.add(0, 0);
		dataset.addSeries(padSeries);
		addEllipseSeries(dataset, trans.get("LandingDispersionResultsDlg.series.oneSigma"), statistics, 1, unit);
		addEllipseSeries(dataset, trans.get("LandingDispersionResultsDlg.series.twoSigma"), statistics, 2, unit);
		addEllipseSeries(dataset, trans.get("LandingDispersionResultsDlg.series.threeSigma"), statistics, 3, unit);

		JFreeChart chart = ChartFactory.createXYLineChart(
				String.format(trans.get("monteCarloReport.landing.chartTitle"), simulationName),
				String.format(trans.get("LandingDispersionResultsDlg.chart.east"), unit.getUnit()),
				String.format(trans.get("LandingDispersionResultsDlg.chart.north"), unit.getUnit()),
				dataset, PlotOrientation.VERTICAL, true, false, false);
		chart.addSubtitle(new TextTitle(String.format(trans.get("LandingDispersionResultsDlg.chart.subtitle"),
				body.branchName(), points.size(), result.getSettings().getRunCount(), result.getSettings().getSeed())));
		configureLandingRenderer((XYLineAndShapeRenderer) chart.getXYPlot().getRenderer(),
				nominal != null, statistics != null);
		applyTheme(chart);
		return chart;
	}

	static JFreeChart histogramChart(MonteCarloResult result, MonteCarloFlightBranch branch,
			MonteCarloMetric metric) {
		Unit unit = metric.getUnitGroup().getDefaultUnit();
		List<Double> values = result.getMetricValues(branch.branchId(), metric);
		double[] displayValues = values.stream().mapToDouble(unit::toUnit).toArray();
		HistogramDataset dataset = new HistogramDataset();
		if (displayValues.length > 0) {
			double minimum = java.util.Arrays.stream(displayValues).min().orElseThrow();
			double maximum = java.util.Arrays.stream(displayValues).max().orElseThrow();
			int bins = Math.max(5, Math.min(50, (int) Math.ceil(Math.sqrt(displayValues.length))));
			if (minimum == maximum) {
				double padding = Math.max(1.0e-9, Math.abs(minimum) * 0.01);
				dataset.addSeries(MonteCarloLabels.metric(metric), displayValues, bins,
						minimum - padding, maximum + padding);
			} else {
				dataset.addSeries(MonteCarloLabels.metric(metric), displayValues, bins);
			}
		}

		String title = String.format(trans.get("monteCarloReport.metrics.chartTitle"),
				MonteCarloLabels.metric(metric));
		JFreeChart chart = ChartFactory.createHistogram(title,
				MonteCarloLabels.metric(metric) + " (" + unit.getUnit() + ")",
				trans.get("LandingDispersionResultsDlg.metrics.frequency"), dataset,
				PlotOrientation.VERTICAL, false, false, false);
		chart.addSubtitle(new TextTitle(String.format(trans.get("LandingDispersionResultsDlg.metrics.subtitle"),
				branch.branchName(), values.size(), result.getSettings().getRunCount(), result.getSettings().getSeed())));

		XYPlot plot = chart.getXYPlot();
		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, histogramColor);
		renderer.setShadowVisible(false);
		renderer.setBarPainter(new StandardXYBarPainter());
		if (!values.isEmpty()) {
			addMetricMarkers(plot, result, branch, metric, unit, values);
		}
		applyTheme(chart);
		return chart;
	}

	static JFreeChart boxPlotChart(MonteCarloResult result, MonteCarloFlightBranch branch,
			MonteCarloMetric metric) {
		Unit unit = metric.getUnitGroup().getDefaultUnit();
		List<Double> values = result.getMetricValues(branch.branchId(), metric);
		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		dataset.add(values.stream().map(unit::toUnit).toList(), MonteCarloLabels.metric(metric),
				branch.branchName());

		String title = String.format(trans.get("monteCarloReport.metrics.boxPlotTitle"),
				MonteCarloLabels.metric(metric));
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(title, "",
				MonteCarloLabels.metric(metric) + " (" + unit.getUnit() + ")", dataset, false);
		chart.addSubtitle(new TextTitle(String.format(trans.get("LandingDispersionResultsDlg.metrics.subtitle"),
				branch.branchName(), values.size(), result.getSettings().getRunCount(), result.getSettings().getSeed())));

		CategoryPlot plot = chart.getCategoryPlot();
		plot.setOrientation(PlotOrientation.HORIZONTAL);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		plot.getDomainAxis().setLowerMargin(0.4);
		plot.getDomainAxis().setUpperMargin(0.4);
		BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, boxFillColor);
		renderer.setSeriesOutlinePaint(0, histogramColor);
		renderer.setSeriesOutlineStroke(0, new BasicStroke(2.4f));
		renderer.setArtifactPaint(medianColor);
		renderer.setFillBox(true);
		renderer.setMeanVisible(false);
		renderer.setMedianVisible(true);
		renderer.setMaximumBarWidth(0.14);
		renderer.setWhiskerWidth(0.8);
		renderer.setUseOutlinePaintForWhiskers(true);
		addBoxMetricMarkers(plot, result, branch, metric, unit, values);
		applyTheme(chart);
		return chart;
	}

	private static void addMetricMarkers(XYPlot plot, MonteCarloResult result, MonteCarloFlightBranch branch,
			MonteCarloMetric metric, Unit unit, List<Double> values) {
		MetricStatistics statistics = MetricStatistics.from(values);
		plot.addDomainMarker(new IntervalMarker(unit.toUnit(statistics.getQuantile(0.05)),
				unit.toUnit(statistics.getQuantile(0.95)), intervalColor));
		LegendItemCollection legendItems = new LegendItemCollection();
		MonteCarloBranchResult nominalBranch = result.getNominalResult().getBranchResult(branch.branchId());
		double nominal = nominalBranch == null ? Double.NaN : nominalBranch.getMetric(metric);
		if (Double.isFinite(nominal)) {
			ValueMarker marker = new ValueMarker(unit.toUnit(nominal), nominalColor, MARKER_STROKE);
			plot.addDomainMarker(marker);
			legendItems.add(markerLegend(trans.get("LandingDispersionResultsDlg.metrics.nominal"), marker));
		}
		ValueMarker mean = new ValueMarker(unit.toUnit(statistics.getMean()), meanColor, MARKER_STROKE);
		plot.addDomainMarker(mean);
		legendItems.add(markerLegend(trans.get("LandingDispersionResultsDlg.metrics.mean"), mean));
		plot.setFixedLegendItems(legendItems);
		LegendTitle legend = new LegendTitle(plot);
		legend.setPosition(RectangleEdge.BOTTOM);
		legend.setBackgroundPaint(backgroundColor);
		legend.setItemPaint(textColor);
		legend.setFrame(BlockBorder.NONE);
		plot.getChart().addLegend(legend);
	}

	private static void addBoxMetricMarkers(CategoryPlot plot, MonteCarloResult result,
			MonteCarloFlightBranch branch, MonteCarloMetric metric, Unit unit, List<Double> values) {
		MetricStatistics statistics = MetricStatistics.from(values);
		LegendItemCollection legendItems = new LegendItemCollection();
		MonteCarloBranchResult nominalBranch = result.getNominalResult().getBranchResult(branch.branchId());
		double nominal = nominalBranch == null ? Double.NaN : nominalBranch.getMetric(metric);
		if (Double.isFinite(nominal)) {
			ValueMarker marker = new ValueMarker(unit.toUnit(nominal), nominalColor, MARKER_STROKE);
			plot.addRangeMarker(marker);
			legendItems.add(markerLegend(trans.get("LandingDispersionResultsDlg.metrics.nominal"), marker));
		}
		ValueMarker mean = new ValueMarker(unit.toUnit(statistics.getMean()), meanColor, MARKER_STROKE);
		plot.addRangeMarker(mean);
		legendItems.add(markerLegend(trans.get("LandingDispersionResultsDlg.metrics.mean"), mean));
		plot.setFixedLegendItems(legendItems);
		LegendTitle legend = new LegendTitle(plot);
		legend.setPosition(RectangleEdge.BOTTOM);
		legend.setBackgroundPaint(backgroundColor);
		legend.setItemPaint(textColor);
		legend.setFrame(BlockBorder.NONE);
		plot.getChart().addLegend(legend);
	}

	private static LegendItem markerLegend(String label, ValueMarker marker) {
		return new LegendItem(label, null, null, null, LEGEND_LINE, marker.getStroke(), marker.getPaint());
	}

	private static void configureLandingRenderer(XYLineAndShapeRenderer renderer, boolean hasNominal,
			boolean hasStatistics) {
		configurePoint(renderer, 0, landingColor, new Ellipse2D.Double(-2.5, -2.5, 5, 5), true);
		configurePoint(renderer, 1, nominalColor, new Rectangle2D.Double(-5, -5, 10, 10), hasNominal);
		configurePoint(renderer, 2, meanColor, new Ellipse2D.Double(-5, -5, 10, 10), hasStatistics);
		configurePoint(renderer, 3, launchPadColor, diamond(12), true);
		for (int series = 4; series <= 6; series++) {
			renderer.setSeriesLinesVisible(series, true);
			renderer.setSeriesShapesVisible(series, false);
			renderer.setSeriesPaint(series, ellipseColor);
			renderer.setSeriesStroke(series, ellipseStroke(series));
			renderer.setSeriesVisibleInLegend(series, hasStatistics);
		}
		renderer.setDrawSeriesLineAsPath(true);
		renderer.setLegendLine(new Line2D.Double(-12, 0, 12, 0));
	}

	private static void configurePoint(XYLineAndShapeRenderer renderer, int series, Color color, Shape shape,
			boolean visibleInLegend) {
		renderer.setSeriesLinesVisible(series, false);
		renderer.setSeriesShapesVisible(series, true);
		renderer.setSeriesShapesFilled(series, true);
		renderer.setSeriesPaint(series, color);
		renderer.setSeriesFillPaint(series, color);
		renderer.setSeriesShape(series, shape);
		renderer.setSeriesVisibleInLegend(series, visibleInLegend);
	}

	private static Shape diamond(double size) {
		double radius = size / 2;
		Path2D.Double diamond = new Path2D.Double();
		diamond.moveTo(0, -radius);
		diamond.lineTo(radius, 0);
		diamond.lineTo(0, radius);
		diamond.lineTo(-radius, 0);
		diamond.closePath();
		return diamond;
	}

	private static BasicStroke ellipseStroke(int series) {
		float[] dashes = switch (series) {
			case 4 -> null;
			case 5 -> LineStyle.DASHED.getDashes();
			case 6 -> LineStyle.DASHDOT.getDashes();
			default -> throw new IllegalArgumentException("Unsupported ellipse series " + series);
		};
		return new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, dashes, 0);
	}

	private static void addEllipseSeries(XYSeriesCollection dataset, String name,
			DispersionStatistics statistics, double sigma, Unit unit) {
		XYSeries series = new XYSeries(name, false, true);
		if (statistics != null) {
			DispersionEllipse ellipse = statistics.getEllipse(sigma);
			for (int index = 0; index <= 96; index++) {
				double parameter = 2 * Math.PI * index / 96;
				double major = ellipse.semiMajor() * Math.cos(parameter);
				double minor = ellipse.semiMinor() * Math.sin(parameter);
				double east = ellipse.centerEast() + major * Math.cos(ellipse.angle())
						- minor * Math.sin(ellipse.angle());
				double north = ellipse.centerNorth() + major * Math.sin(ellipse.angle())
						+ minor * Math.cos(ellipse.angle());
				series.add(unit.toUnit(east), unit.toUnit(north));
			}
		}
		dataset.addSeries(series);
	}

	private static void applyTheme(JFreeChart chart) {
		chart.setBackgroundPaint(backgroundColor);
		chart.setPadding(new RectangleInsets(24, 16, 16, 16));
		chart.getTitle().setPaint(textColor);
		chart.getTitle().setFont(TITLE_FONT);
		for (org.jfree.chart.title.Title subtitle : chart.getSubtitles()) {
			if (subtitle instanceof TextTitle textTitle) {
				textTitle.setPaint(textColor);
				textTitle.setFont(SUBTITLE_FONT);
			}
		}
		if (chart.getLegend() != null) {
			chart.getLegend().setBackgroundPaint(backgroundColor);
			chart.getLegend().setItemPaint(textColor);
			chart.getLegend().setItemFont(LEGEND_FONT);
			chart.getLegend().setFrame(BlockBorder.NONE);
		}
		if (chart.getPlot() instanceof XYPlot plot) {
			plot.setBackgroundPaint(backgroundColor);
			plot.setDomainGridlinePaint(gridColor);
			plot.setRangeGridlinePaint(gridColor);
			plot.setOutlinePaint(gridColor);
			configureAxis(plot.getDomainAxis());
			configureAxis(plot.getRangeAxis());
		} else if (chart.getPlot() instanceof CategoryPlot plot) {
			plot.setBackgroundPaint(backgroundColor);
			plot.setDomainGridlinePaint(gridColor);
			plot.setRangeGridlinePaint(gridColor);
			plot.setOutlinePaint(gridColor);
			configureAxis(plot.getDomainAxis());
			configureAxis(plot.getRangeAxis());
		}
	}

	private static void configureAxis(Axis axis) {
		axis.setLabelPaint(textColor);
		axis.setLabelFont(AXIS_LABEL_FONT);
		axis.setTickLabelPaint(textColor);
		axis.setTickLabelFont(TICK_FONT);
		axis.setAxisLinePaint(textColor);
		axis.setTickMarkPaint(textColor);
	}
}
