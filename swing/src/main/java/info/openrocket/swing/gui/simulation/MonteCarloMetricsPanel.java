package info.openrocket.swing.gui.simulation;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.montecarlo.MetricStatistics;
import info.openrocket.core.simulation.montecarlo.MonteCarloBranchResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloFlightBranch;
import info.openrocket.core.simulation.montecarlo.MonteCarloMetric;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.StringUtils;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.HeaderToolTipTable;

import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.AbstractAnnotation;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.Size2D;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.Range;

/** Displays scalar Monte Carlo output distributions one metric at a time. */
final class MonteCarloMetricsPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();
	private static Color chartBackgroundColor;
	private static Color plotBackgroundColor;
	private static Color textColor;
	private static Color dimTextColor;
	private static Color borderColor;
	private static Color gridColor;
	private static Color histogramColor;
	private static Color intervalColor;
	private static Color boxFillColor;
	private static Color nominalColor;
	private static Color meanColor;
	private static Color medianColor;
	private static final BasicStroke MARKER_STROKE = new BasicStroke(2.0f);
	private static final Line2D MARKER_LEGEND_LINE = new Line2D.Double(-8.0, 0.0, 8.0, 0.0);
	private static final double LEGEND_RELATIVE_POSITION = 0.985;

	static {
		initColors();
	}

	private final String simulationName;
	private final MonteCarloResult result;
	private final JComboBox<MonteCarloFlightBranch> branchCombo;
	private final JComboBox<MetricPlotType> plotTypeCombo;
	private final MetricTableModel tableModel;
	private final JTable metricTable;
	private JFreeChart chart;
	private final ChartPanel chartPanel;

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(MonteCarloMetricsPanel::updateColors);
	}

	public static void updateColors() {
		chartBackgroundColor = UITheme.getColor(UITheme.Keys.BACKGROUND);
		plotBackgroundColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_PLOT_BACKGROUND,
				chartBackgroundColor);
		textColor = UITheme.getColor(UITheme.Keys.TEXT);
		dimTextColor = UITheme.getColor(UITheme.Keys.TEXT_DIM, textColor);
		borderColor = UITheme.getColor(UITheme.Keys.BORDER);
		gridColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_GRID, borderColor);
		histogramColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_HISTOGRAM,
				UITheme.getColor(UITheme.Keys.INFO));
		intervalColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_INTERVAL,
				UITheme.getColor(UITheme.Keys.INFO));
		boxFillColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_BOX_FILL, histogramColor);
		nominalColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_NOMINAL,
				UITheme.getColor(UITheme.Keys.WARNING));
		meanColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_MEAN,
				UITheme.getColor(UITheme.Keys.ERROR));
		medianColor = UITheme.getColor(UITheme.Keys.MONTE_CARLO_MEDIAN, textColor);
	}

	MonteCarloMetricsPanel(String simulationName, MonteCarloResult result) {
		super(new BorderLayout(0, 8));
		this.simulationName = simulationName;
		this.result = result;
		this.branchCombo = new JComboBox<>(result.getFlightBranches().toArray(new MonteCarloFlightBranch[0]));
		this.plotTypeCombo = new JComboBox<>(MetricPlotType.values());
		this.tableModel = new MetricTableModel(result);
		this.metricTable = new HeaderToolTipTable(tableModel, tableModel::getColumnToolTip);
		this.chart = createChart();
		this.chartPanel = createChartPanel();
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		buildPanel();
		updateBranch();
	}

	private void buildPanel() {
		JPanel header = new JPanel(new MigLayout("ins 0", "[][grow][][][]"));
		header.add(new JLabel(trans.get("LandingDispersionResultsDlg.metrics.branch")));
		branchCombo.setEnabled(branchCombo.getItemCount() > 0);
		header.add(branchCombo, "growx, w 220::");
		header.add(new JLabel(trans.get("LandingDispersionResultsDlg.metrics.plotType")));
		header.add(plotTypeCombo);
		header.add(new JLabel(String.format(trans.get("LandingDispersionResultsDlg.lbl.seed"),
				result.getSettings().getSeed())));
		add(header, BorderLayout.NORTH);

		metricTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		metricTable.setAutoCreateRowSorter(false);
		metricTable.setFillsViewportHeight(true);
		metricTable.getColumnModel().getColumn(0).setPreferredWidth(175);
		metricTable.getColumnModel().getColumn(7).setPreferredWidth(75);
		metricTable.getSelectionModel().addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				updateChart();
			}
		});
		JScrollPane tableScrollPane = new JScrollPane(metricTable);
		tableScrollPane.setBorder(BorderFactory.createTitledBorder(
				trans.get("LandingDispersionResultsDlg.metrics.summary")));
		tableScrollPane.setMinimumSize(new Dimension(0, 150));

		chartPanel.setMinimumSize(new Dimension(340, 260));
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, chartPanel);
		splitPane.setResizeWeight(0.34);
		splitPane.setContinuousLayout(true);
		add(splitPane, BorderLayout.CENTER);

		branchCombo.addActionListener(event -> updateBranch());
		plotTypeCombo.addActionListener(event -> updateChart());
	}

	private JFreeChart createChart() {
		JFreeChart metricChart = ChartFactory.createHistogram(
				trans.get("LandingDispersionResultsDlg.metrics.noMetric"), "", 
				trans.get("LandingDispersionResultsDlg.metrics.frequency"),
				new HistogramDataset(), PlotOrientation.VERTICAL, false, true, false);
		applyChartTheme(metricChart);
		return metricChart;
	}

	private ChartPanel createChartPanel() {
		ChartPanel panel = new ChartPanel(chart, false, false, false, false, true);
		configureChartInteraction(panel);
		panel.setEnforceFileExtensions(true);
		panel.setMinimumDrawWidth(0);
		panel.setMaximumDrawWidth(Integer.MAX_VALUE);
		panel.setMinimumDrawHeight(0);
		panel.setMaximumDrawHeight(Integer.MAX_VALUE);
		panel.setBorder(BorderFactory.createLineBorder(borderColor));
		return panel;
	}

	static void configureChartInteraction(ChartPanel panel) {
		panel.setMouseZoomable(false);
		panel.setMouseWheelEnabled(false);
		panel.setPopupMenu(null);
		panel.setInitialDelay(0);
		panel.setReshowDelay(0);
	}

	private void updateBranch() {
		MonteCarloFlightBranch branch = (MonteCarloFlightBranch) branchCombo.getSelectedItem();
		tableModel.setBranch(branch);
		if (tableModel.getRowCount() > 0) {
			metricTable.setRowSelectionInterval(0, 0);
		} else {
			updateChart();
		}
	}

	private void updateChart() {
		MetricRow row = tableModel.getRow(metricTable.getSelectedRow());
		if (row == null || row.statistics == null) {
			chart = createChart();
		} else if (plotTypeCombo.getSelectedItem() == MetricPlotType.BOX_PLOT) {
			chart = createBoxPlot(row);
		} else {
			chart = createHistogram(row);
		}
		chartPanel.setChart(chart);
	}

	private JFreeChart createHistogram(MetricRow row) {
		HistogramDataset dataset = createHistogramDataset(metricLabel(row.metric), row.values, row.unit);
		JFreeChart histogram = ChartFactory.createHistogram(metricChartTitle(row.metric, "histogram"),
				metricAxisLabel(row), trans.get("LandingDispersionResultsDlg.metrics.frequency"),
				dataset, PlotOrientation.VERTICAL, false, true, false);
		XYPlot plot = histogram.getXYPlot();

		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, histogramColor);
		renderer.setShadowVisible(false);
		renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter());
		renderer.setDefaultToolTipGenerator((tooltipDataset, series, item) ->
				formatHistogramTooltip(trans.get("LandingDispersionResultsDlg.metrics.histogram.ttip"),
						(IntervalXYDataset) tooltipDataset, item, row.unit));

		double p5 = row.unit.toUnit(row.statistics.getQuantile(0.05));
		double p95 = row.unit.toUnit(row.statistics.getQuantile(0.95));
		plot.addDomainMarker(new IntervalMarker(p5, p95, intervalColor));
		ValueMarker nominal = null;
		if (Double.isFinite(row.nominal)) {
			nominal = new ValueMarker(row.unit.toUnit(row.nominal), nominalColor, MARKER_STROKE);
			plot.addDomainMarker(nominal);
		}
		ValueMarker mean = new ValueMarker(row.unit.toUnit(row.statistics.getMean()), meanColor, MARKER_STROKE);
		plot.addDomainMarker(mean);
		addInsetMarkerLegend(plot, createMarkerLegendItems(nominal,
				trans.get("LandingDispersionResultsDlg.metrics.nominal"), mean,
				trans.get("LandingDispersionResultsDlg.metrics.mean")));
		finishChart(histogram, row);
		return histogram;
	}

	private JFreeChart createBoxPlot(MetricRow row) {
		MonteCarloFlightBranch branch = (MonteCarloFlightBranch) branchCombo.getSelectedItem();
		DefaultBoxAndWhiskerCategoryDataset dataset = createBoxDataset(row.values, row.unit,
				metricLabel(row.metric), branch.branchName());
		JFreeChart boxPlot = ChartFactory.createBoxAndWhiskerChart(metricChartTitle(row.metric, "boxPlot"), "",
				metricAxisLabel(row), dataset, false);
		CategoryPlot plot = boxPlot.getCategoryPlot();
		BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer) plot.getRenderer();
		configureBoxRenderer(renderer);
		renderer.setDefaultToolTipGenerator((tooltipDataset, series, item) ->
				formatBoxTooltip((BoxAndWhiskerCategoryDataset) tooltipDataset, series, item,
						metricLabel(row.metric), branch.branchName(), row.unit));
		configureBoxPlot(plot);
		ValueMarker nominal = null;
		if (Double.isFinite(row.nominal)) {
			nominal = new ValueMarker(row.unit.toUnit(row.nominal), nominalColor, MARKER_STROKE);
			plot.addRangeMarker(nominal);
		}
		ValueMarker mean = new ValueMarker(row.unit.toUnit(row.statistics.getMean()), meanColor, MARKER_STROKE);
		plot.addRangeMarker(mean);
		addInsetMarkerLegend(plot, createMarkerLegendItems(nominal,
				trans.get("LandingDispersionResultsDlg.metrics.nominal"), mean,
				trans.get("LandingDispersionResultsDlg.metrics.mean")));
		finishChart(boxPlot, row);
		return boxPlot;
	}

	static void configureBoxRenderer(BoxAndWhiskerRenderer renderer) {
		renderer.setSeriesPaint(0, boxFillColor);
		renderer.setSeriesOutlinePaint(0, histogramColor);
		renderer.setSeriesOutlineStroke(0, new BasicStroke(1.8f));
		renderer.setArtifactPaint(medianColor);
		renderer.setFillBox(true);
		renderer.setMeanVisible(false);
		renderer.setMedianVisible(true);
		renderer.setMaximumBarWidth(0.14);
		renderer.setWhiskerWidth(0.8);
		renderer.setUseOutlinePaintForWhiskers(true);
	}

	static void configureBoxPlot(CategoryPlot plot) {
		plot.setOrientation(PlotOrientation.HORIZONTAL);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		plot.getDomainAxis().setLowerMargin(0.4);
		plot.getDomainAxis().setUpperMargin(0.4);
	}

	static LegendItemCollection createMarkerLegendItems(ValueMarker nominal, String nominalLabel,
			ValueMarker mean, String meanLabel) {
		LegendItemCollection items = new LegendItemCollection();
		if (nominal != null) {
			items.add(createMarkerLegendItem(nominalLabel, nominal));
		}
		items.add(createMarkerLegendItem(meanLabel, mean));
		return items;
	}

	private static LegendItem createMarkerLegendItem(String label, ValueMarker marker) {
		return new LegendItem(label, null, null, null,
				MARKER_LEGEND_LINE, marker.getStroke(), marker.getPaint());
	}

	static void addInsetMarkerLegend(XYPlot plot, LegendItemCollection items) {
		plot.setFixedLegendItems(items);
		LegendTitle legend = createInsetLegend(plot);
		plot.addAnnotation(createTopRightLegendAnnotation(legend));
	}

	static XYTitleAnnotation createTopRightLegendAnnotation(LegendTitle legend) {
		XYTitleAnnotation annotation = new XYTitleAnnotation(LEGEND_RELATIVE_POSITION,
				LEGEND_RELATIVE_POSITION, legend, RectangleAnchor.TOP_RIGHT);
		annotation.setMaxWidth(0.45);
		annotation.setMaxHeight(0.35);
		return annotation;
	}

	static void addInsetMarkerLegend(CategoryPlot plot, LegendItemCollection items) {
		plot.setFixedLegendItems(items);
		plot.addAnnotation(new InsetLegendCategoryAnnotation(createInsetLegend(plot)));
	}

	private static LegendTitle createInsetLegend(org.jfree.chart.LegendItemSource source) {
		LegendTitle legend = new LegendTitle(source, new ColumnArrangement(), new ColumnArrangement());
		legend.setBackgroundPaint(plotBackgroundColor);
		legend.setItemPaint(textColor);
		legend.setFrame(new BlockBorder(borderColor));
		legend.setPadding(new RectangleInsets(3, 5, 3, 5));
		return legend;
	}

	private void finishChart(JFreeChart metricChart, MetricRow row) {
		MonteCarloFlightBranch branch = (MonteCarloFlightBranch) branchCombo.getSelectedItem();
		metricChart.addSubtitle(new TextTitle(String.format(
				trans.get("LandingDispersionResultsDlg.metrics.subtitle"), branch.branchName(),
				row.statistics.getSampleCount(), result.getSettings().getRunCount(),
				result.getSettings().getSeed())));
		applyChartTheme(metricChart);
	}

	private static String metricAxisLabel(MetricRow row) {
		return metricLabel(row.metric) + " (" + row.unit.getUnit() + ")";
	}

	JFreeChart getChart() {
		return chart;
	}

	ChartPanel getChartPanel() {
		return chartPanel;
	}

	void refreshTheme() {
		updateChart();
		chartPanel.setBorder(BorderFactory.createLineBorder(borderColor));
		chartPanel.repaint();
	}

	String getExportName() {
		MonteCarloFlightBranch branch = (MonteCarloFlightBranch) branchCombo.getSelectedItem();
		MetricRow row = tableModel.getRow(metricTable.getSelectedRow());
		String branchName = branch == null ? "flight" : branch.branchName();
		String metricName = row == null ? "metrics" : row.metric.name().toLowerCase(java.util.Locale.ROOT);
		return simulationName + "-" + branchName + "-" + metricName;
	}

	static int binsForSampleCount(int sampleCount) {
		return Math.max(5, Math.min(50, (int) Math.ceil(Math.sqrt(Math.max(1, sampleCount)))));
	}

	static HistogramDataset createHistogramDataset(String label, List<Double> values, Unit unit) {
		double[] displayValues = values.stream().mapToDouble(unit::toUnit).toArray();
		HistogramDataset dataset = new HistogramDataset();
		double minimum = java.util.Arrays.stream(displayValues).min().orElseThrow();
		double maximum = java.util.Arrays.stream(displayValues).max().orElseThrow();
		int bins = binsForSampleCount(displayValues.length);
		if (minimum == maximum) {
			double padding = Math.max(1.0e-9, Math.abs(minimum) * 0.01);
			dataset.addSeries(label, displayValues, bins, minimum - padding, maximum + padding);
		} else {
			dataset.addSeries(label, displayValues, bins);
		}
		return dataset;
	}

	static DefaultBoxAndWhiskerCategoryDataset createBoxDataset(List<Double> values, Unit unit,
			String rowKey, String columnKey) {
		List<Double> displayValues = values.stream().map(unit::toUnit).toList();
		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		dataset.add(displayValues, rowKey, columnKey);
		return dataset;
	}

	static String formatHistogramTooltip(String template, IntervalXYDataset dataset, int item, Unit unit) {
		DecimalFormat valueFormat = new DecimalFormat("0.###");
		DecimalFormat countFormat = new DecimalFormat("0");
		return String.format(template,
				valueFormat.format(dataset.getStartXValue(0, item)),
				valueFormat.format(dataset.getEndXValue(0, item)), unit.getUnit(),
				countFormat.format(dataset.getYValue(0, item)));
	}

	static String formatBoxTooltip(BoxAndWhiskerCategoryDataset dataset, int series, int item,
			String metricName, String branchName, Unit unit) {
		DecimalFormat format = new DecimalFormat("0.###");
		String suffix = unit.getUnit().isBlank() ? "" : " " + StringUtils.escapeHtml(unit.getUnit());
		StringBuilder tooltip = new StringBuilder("<html><b>")
				.append(StringUtils.escapeHtml(metricName))
				.append(" — ")
				.append(StringUtils.escapeHtml(branchName))
				.append("</b>");
		appendBoxTooltipLine(tooltip, trans.get("LandingDispersionResultsDlg.metrics.mean"),
				dataset.getMeanValue(series, item), format, suffix);
		appendBoxTooltipLine(tooltip, trans.get("LandingDispersionResultsDlg.metrics.col.median"),
				dataset.getMedianValue(series, item), format, suffix);
		appendBoxTooltipLine(tooltip, trans.get("LandingDispersionResultsDlg.metrics.q1"),
				dataset.getQ1Value(series, item), format, suffix);
		appendBoxTooltipLine(tooltip, trans.get("LandingDispersionResultsDlg.metrics.q3"),
				dataset.getQ3Value(series, item), format, suffix);
		appendBoxTooltipLine(tooltip, trans.get("LandingDispersionResultsDlg.metrics.lowerWhisker"),
				dataset.getMinRegularValue(series, item), format, suffix);
		appendBoxTooltipLine(tooltip, trans.get("LandingDispersionResultsDlg.metrics.upperWhisker"),
				dataset.getMaxRegularValue(series, item), format, suffix);
		return tooltip.append("</html>").toString();
	}

	private static void appendBoxTooltipLine(StringBuilder tooltip, String label, Number value,
			DecimalFormat format, String suffix) {
		if (value != null) {
			tooltip.append("<br>")
					.append(StringUtils.escapeHtml(label))
					.append(": ")
					.append(format.format(value))
					.append(suffix);
		}
	}

	static String metricLabel(MonteCarloMetric metric) {
		return trans.get(metricLabelKey(metric));
	}

	static String formatMetricChartTitle(String template, String metricName) {
		return String.format(template, metricName);
	}

	private static String metricChartTitle(MonteCarloMetric metric, String plotType) {
		return formatMetricChartTitle(
				trans.get("LandingDispersionResultsDlg.metrics.chartTitle." + plotType), metricLabel(metric));
	}

	static String metricLabelKey(MonteCarloMetric metric) {
		return switch (metric) {
			case APOGEE_ALTITUDE -> "simpanel.col.Apogee";
			case MAXIMUM_VELOCITY -> "MaximumVelocityParameter.name";
			case MAXIMUM_ACCELERATION -> "MaximumAccelerationParameter.name";
			case TIME_TO_APOGEE -> "simpanel.col.Timetoapogee";
			case FLIGHT_TIME -> "simpanel.col.Flighttime";
			case MAXIMUM_MACH, LANDING_VELOCITY -> "LandingDispersionResultsDlg.metric."
					+ metric.name().toLowerCase(java.util.Locale.ROOT);
		};
	}

	private static void applyChartTheme(JFreeChart metricChart) {
		metricChart.setBackgroundPaint(chartBackgroundColor);
		metricChart.getTitle().setPaint(textColor);
		for (org.jfree.chart.title.Title subtitle : metricChart.getSubtitles()) {
			if (subtitle instanceof TextTitle textTitle) {
				textTitle.setPaint(dimTextColor);
			}
		}
		if (metricChart.getLegend() != null) {
			metricChart.getLegend().setBackgroundPaint(chartBackgroundColor);
			metricChart.getLegend().setItemPaint(textColor);
			metricChart.getLegend().setFrame(new BlockBorder(borderColor));
		}
		if (metricChart.getPlot() instanceof XYPlot plot) {
			plot.setBackgroundPaint(plotBackgroundColor);
			plot.setDomainGridlinePaint(gridColor);
			plot.setRangeGridlinePaint(gridColor);
			plot.setOutlinePaint(borderColor);
			configureAxis(plot.getDomainAxis(), textColor, borderColor);
			configureAxis(plot.getRangeAxis(), textColor, borderColor);
		} else if (metricChart.getPlot() instanceof CategoryPlot plot) {
			plot.setBackgroundPaint(plotBackgroundColor);
			plot.setDomainGridlinePaint(gridColor);
			plot.setRangeGridlinePaint(gridColor);
			plot.setOutlinePaint(borderColor);
			configureAxis(plot.getDomainAxis(), textColor, borderColor);
			configureAxis(plot.getRangeAxis(), textColor, borderColor);
		}
	}

	private static void configureAxis(Axis axis, Color text, Color border) {
		axis.setLabelPaint(text);
		axis.setTickLabelPaint(text);
		axis.setAxisLinePaint(border);
		axis.setTickMarkPaint(border);
	}


	private static final class InsetLegendCategoryAnnotation extends AbstractAnnotation
			implements CategoryAnnotation {
		private static final long serialVersionUID = 1L;
		private final LegendTitle legend;

		private InsetLegendCategoryAnnotation(LegendTitle legend) {
			this.legend = legend;
		}

		@Override
		public void draw(Graphics2D graphics, CategoryPlot plot, Rectangle2D dataArea,
				CategoryAxis domainAxis, ValueAxis rangeAxis) {
			double maxWidth = dataArea.getWidth() * 0.45;
			double maxHeight = dataArea.getHeight() * 0.35;
			Size2D size = legend.arrange(graphics, new RectangleConstraint(
					new Range(0, maxWidth), new Range(0, maxHeight)));
			double horizontalInset = dataArea.getWidth() * (1 - LEGEND_RELATIVE_POSITION);
			double verticalInset = dataArea.getHeight() * (1 - LEGEND_RELATIVE_POSITION);
			Rectangle2D legendArea = new Rectangle2D.Double(
					dataArea.getMaxX() - horizontalInset - size.getWidth(),
					dataArea.getMinY() + verticalInset, size.getWidth(), size.getHeight());
			legend.draw(graphics, legendArea);
		}
	}

	private record MetricRow(MonteCarloMetric metric, Unit unit, double nominal,
			List<Double> values, MetricStatistics statistics, int missingCount) {
	}

	private enum MetricPlotType {
		HISTOGRAM("histogram"),
		BOX_PLOT("boxPlot");

		private final String key;

		MetricPlotType(String key) {
			this.key = key;
		}

		@Override
		public String toString() {
			return trans.get("LandingDispersionResultsDlg.metrics.plotType." + key);
		}
	}

	private static final class MetricTableModel extends AbstractTableModel {
		private static final String[] COLUMN_KEYS = { "metric", "nominal", "mean", "median",
				"standardDeviation", "p5", "p95", "valid" };
		private static final DecimalFormat FORMAT = new DecimalFormat("0.###");
		private final MonteCarloResult result;
		private final List<MetricRow> rows = new ArrayList<>();

		private MetricTableModel(MonteCarloResult result) {
			this.result = result;
		}

		private void setBranch(MonteCarloFlightBranch branch) {
			rows.clear();
			if (branch != null) {
				MonteCarloBranchResult nominalBranch = result.getNominalResult()
						.getBranchResult(branch.branchId());
				for (MonteCarloMetric metric : MonteCarloMetric.values()) {
					List<Double> values = result.getMetricValues(branch.branchId(), metric);
					double nominal = nominalBranch == null ? Double.NaN : nominalBranch.getMetric(metric);
					if (values.isEmpty() && !Double.isFinite(nominal)) {
						continue;
					}
					Unit unit = metric.getUnitGroup().getDefaultUnit();
					MetricStatistics statistics = values.isEmpty() ? null : MetricStatistics.from(values);
					rows.add(new MetricRow(metric, unit, nominal, values, statistics,
							result.getMetricMissingCount(branch.branchId(), metric)));
				}
			}
			fireTableDataChanged();
		}

		private MetricRow getRow(int row) {
			return row < 0 || row >= rows.size() ? null : rows.get(row);
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMN_KEYS.length;
		}

		@Override
		public String getColumnName(int column) {
			return trans.get("LandingDispersionResultsDlg.metrics.col." + COLUMN_KEYS[column]);
		}

		private String getColumnToolTip(int column) {
			return trans.get("LandingDispersionResultsDlg.metrics.col." + COLUMN_KEYS[column] + ".ttip");
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			MetricRow row = rows.get(rowIndex);
			return switch (columnIndex) {
				case 0 -> metricLabel(row.metric) + " (" + row.unit.getUnit() + ")";
				case 1 -> format(row.unit, row.nominal);
				case 2 -> statistic(row, MetricStatistics::getMean);
				case 3 -> statistic(row, MetricStatistics::getMedian);
				case 4 -> statistic(row, MetricStatistics::getStandardDeviation);
				case 5 -> statistic(row, value -> value.getQuantile(0.05));
				case 6 -> statistic(row, value -> value.getQuantile(0.95));
				case 7 -> row.statistics == null ? "0 / " + result.getSettings().getRunCount()
						: row.statistics.getSampleCount() + " / " + result.getSettings().getRunCount();
				default -> throw new IndexOutOfBoundsException(columnIndex);
			};
		}

		private static String statistic(MetricRow row,
				java.util.function.ToDoubleFunction<MetricStatistics> extractor) {
			return row.statistics == null ? "" : format(row.unit, extractor.applyAsDouble(row.statistics));
		}

		private static String format(Unit unit, double value) {
			return Double.isFinite(value) ? FORMAT.format(unit.toUnit(value)) : "";
		}
	}
}
