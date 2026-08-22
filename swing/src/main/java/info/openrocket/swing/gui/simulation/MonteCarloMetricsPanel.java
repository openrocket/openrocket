package info.openrocket.swing.gui.simulation;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;

import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

/** Displays scalar Monte Carlo output distributions one metric at a time. */
final class MonteCarloMetricsPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();
	private static final Color HISTOGRAM_LIGHT_COLOR = new Color(0, 114, 178);
	private static final Color HISTOGRAM_DARK_COLOR = new Color(86, 180, 233);
	private static final Color INTERVAL_LIGHT_COLOR = new Color(0, 125, 90, 55);
	private static final Color INTERVAL_DARK_COLOR = new Color(100, 215, 165, 65);
	private static final Color NOMINAL_LIGHT_COLOR = new Color(230, 159, 0);
	private static final Color NOMINAL_DARK_COLOR = new Color(240, 228, 66);
	private static final Color MEAN_LIGHT_COLOR = new Color(213, 94, 0);
	private static final Color MEAN_DARK_COLOR = new Color(255, 112, 91);

	private final String simulationName;
	private final MonteCarloResult result;
	private final JComboBox<MonteCarloFlightBranch> branchCombo;
	private final JComboBox<MetricPlotType> plotTypeCombo;
	private final MetricTableModel tableModel;
	private final JTable metricTable;
	private JFreeChart chart;
	private final ChartPanel chartPanel;

	MonteCarloMetricsPanel(String simulationName, MonteCarloResult result) {
		super(new BorderLayout(0, 8));
		this.simulationName = simulationName;
		this.result = result;
		this.branchCombo = new JComboBox<>(result.getFlightBranches().toArray(new MonteCarloFlightBranch[0]));
		this.plotTypeCombo = new JComboBox<>(MetricPlotType.values());
		this.tableModel = new MetricTableModel(result);
		this.metricTable = new JTable(tableModel);
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
		panel.setBorder(BorderFactory.createLineBorder(UITheme.getColor(UITheme.Keys.BORDER)));
		return panel;
	}

	static void configureChartInteraction(ChartPanel panel) {
		panel.setMouseZoomable(false);
		panel.setMouseWheelEnabled(false);
		panel.setPopupMenu(null);
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
		JFreeChart histogram = ChartFactory.createHistogram(metricLabel(row.metric),
				metricAxisLabel(row), trans.get("LandingDispersionResultsDlg.metrics.frequency"),
				dataset, PlotOrientation.VERTICAL, false, true, false);
		XYPlot plot = histogram.getXYPlot();

		boolean lightTheme = UITheme.isLightTheme(GUIUtil.getUITheme());
		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, lightTheme ? HISTOGRAM_LIGHT_COLOR : HISTOGRAM_DARK_COLOR);
		renderer.setShadowVisible(false);
		renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter());
		renderer.setDefaultToolTipGenerator((tooltipDataset, series, item) ->
				formatHistogramTooltip(trans.get("LandingDispersionResultsDlg.metrics.histogram.ttip"),
						(IntervalXYDataset) tooltipDataset, item, row.unit));

		double p5 = row.unit.toUnit(row.statistics.getQuantile(0.05));
		double p95 = row.unit.toUnit(row.statistics.getQuantile(0.95));
		plot.addDomainMarker(new IntervalMarker(p5, p95,
				lightTheme ? INTERVAL_LIGHT_COLOR : INTERVAL_DARK_COLOR));
		if (Double.isFinite(row.nominal)) {
			ValueMarker nominal = new ValueMarker(row.unit.toUnit(row.nominal),
					lightTheme ? NOMINAL_LIGHT_COLOR : NOMINAL_DARK_COLOR,
					new BasicStroke(2.0f));
			nominal.setLabel(trans.get("LandingDispersionResultsDlg.metrics.nominal"));
			nominal.setLabelPaint(lightTheme ? NOMINAL_LIGHT_COLOR : NOMINAL_DARK_COLOR);
			plot.addDomainMarker(nominal);
		}
		ValueMarker mean = new ValueMarker(row.unit.toUnit(row.statistics.getMean()),
				lightTheme ? MEAN_LIGHT_COLOR : MEAN_DARK_COLOR, new BasicStroke(2.0f));
		mean.setLabel(trans.get("LandingDispersionResultsDlg.metrics.mean"));
		mean.setLabelPaint(lightTheme ? MEAN_LIGHT_COLOR : MEAN_DARK_COLOR);
		plot.addDomainMarker(mean);
		finishChart(histogram, row);
		return histogram;
	}

	private JFreeChart createBoxPlot(MetricRow row) {
		MonteCarloFlightBranch branch = (MonteCarloFlightBranch) branchCombo.getSelectedItem();
		DefaultBoxAndWhiskerCategoryDataset dataset = createBoxDataset(row.values, row.unit,
				metricLabel(row.metric), branch.branchName());
		JFreeChart boxPlot = ChartFactory.createBoxAndWhiskerChart(metricLabel(row.metric), "",
				metricAxisLabel(row), dataset, false);
		CategoryPlot plot = boxPlot.getCategoryPlot();
		boolean lightTheme = UITheme.isLightTheme(GUIUtil.getUITheme());
		BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer) plot.getRenderer();
		configureBoxRenderer(renderer, lightTheme);
		renderer.setDefaultToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
		plot.getDomainAxis().setLowerMargin(0.4);
		plot.getDomainAxis().setUpperMargin(0.4);
		if (Double.isFinite(row.nominal)) {
			ValueMarker nominal = new ValueMarker(row.unit.toUnit(row.nominal),
					lightTheme ? NOMINAL_LIGHT_COLOR : NOMINAL_DARK_COLOR,
					new BasicStroke(2.0f));
			nominal.setLabel(trans.get("LandingDispersionResultsDlg.metrics.nominal"));
			nominal.setLabelPaint(lightTheme ? NOMINAL_LIGHT_COLOR : NOMINAL_DARK_COLOR);
			plot.addRangeMarker(nominal);
		}
		finishChart(boxPlot, row);
		return boxPlot;
	}

	static void configureBoxRenderer(BoxAndWhiskerRenderer renderer, boolean lightTheme) {
		Color boxColor = lightTheme ? HISTOGRAM_LIGHT_COLOR : HISTOGRAM_DARK_COLOR;
		Color artifactColor = lightTheme ? MEAN_LIGHT_COLOR : MEAN_DARK_COLOR;
		renderer.setSeriesPaint(0, withAlpha(boxColor, lightTheme ? 85 : 110));
		renderer.setSeriesOutlinePaint(0, boxColor);
		renderer.setSeriesOutlineStroke(0, new BasicStroke(1.8f));
		renderer.setArtifactPaint(artifactColor);
		renderer.setFillBox(true);
		renderer.setMeanVisible(true);
		renderer.setMedianVisible(true);
		renderer.setMaximumBarWidth(0.14);
		renderer.setWhiskerWidth(0.8);
		renderer.setUseOutlinePaintForWhiskers(true);
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

	static String metricLabel(MonteCarloMetric metric) {
		return trans.get("LandingDispersionResultsDlg.metric."
				+ metric.name().toLowerCase(java.util.Locale.ROOT));
	}

	private static void applyChartTheme(JFreeChart metricChart) {
		boolean lightTheme = UITheme.isLightTheme(GUIUtil.getUITheme());
		Color background = UITheme.getColor(UITheme.Keys.BACKGROUND, Color.WHITE);
		Color text = UITheme.getColor(UITheme.Keys.TEXT, Color.BLACK);
		Color border = UITheme.getColor(UITheme.Keys.BORDER, Color.GRAY);
		Color plotBackground = lightTheme ? Color.WHITE : blend(background, Color.WHITE, 0.06);
		metricChart.setBackgroundPaint(background);
		metricChart.getTitle().setPaint(text);
		for (org.jfree.chart.title.Title subtitle : metricChart.getSubtitles()) {
			if (subtitle instanceof TextTitle textTitle) {
				textTitle.setPaint(blend(text, background, 0.25));
			}
		}
		if (metricChart.getLegend() != null) {
			metricChart.getLegend().setBackgroundPaint(background);
			metricChart.getLegend().setItemPaint(text);
			metricChart.getLegend().setFrame(new BlockBorder(border));
		}
		if (metricChart.getPlot() instanceof XYPlot plot) {
			plot.setBackgroundPaint(plotBackground);
			plot.setDomainGridlinePaint(border);
			plot.setRangeGridlinePaint(border);
			plot.setOutlinePaint(border);
			configureAxis(plot.getDomainAxis(), text, border);
			configureAxis(plot.getRangeAxis(), text, border);
		} else if (metricChart.getPlot() instanceof CategoryPlot plot) {
			plot.setBackgroundPaint(plotBackground);
			plot.setDomainGridlinePaint(border);
			plot.setRangeGridlinePaint(border);
			plot.setOutlinePaint(border);
			configureAxis(plot.getDomainAxis(), text, border);
			configureAxis(plot.getRangeAxis(), text, border);
		}
	}

	private static void configureAxis(Axis axis, Color text, Color border) {
		axis.setLabelPaint(text);
		axis.setTickLabelPaint(text);
		axis.setAxisLinePaint(border);
		axis.setTickMarkPaint(border);
	}

	private static Color blend(Color first, Color second, double secondWeight) {
		double firstWeight = 1 - secondWeight;
		return new Color((int) Math.round(first.getRed() * firstWeight + second.getRed() * secondWeight),
				(int) Math.round(first.getGreen() * firstWeight + second.getGreen() * secondWeight),
				(int) Math.round(first.getBlue() * firstWeight + second.getBlue() * secondWeight));
	}

	private static Color withAlpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
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
