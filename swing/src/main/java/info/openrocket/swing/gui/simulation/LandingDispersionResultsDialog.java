package info.openrocket.swing.gui.simulation;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.montecarlo.DispersionStatistics;
import info.openrocket.core.simulation.montecarlo.DispersionStatistics.DispersionEllipse;
import info.openrocket.core.simulation.montecarlo.LandingBody;
import info.openrocket.core.simulation.montecarlo.LandingPoint;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloRunResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.LineStyle;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.widgets.HeaderToolTipTable;
import info.openrocket.swing.gui.widgets.SaveFileChooser;

import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Presents landing clouds, empirical containment radii, covariance ellipses,
 * per-run outcomes, and a lossless CSV export for a completed analysis.
 */
public final class LandingDispersionResultsDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	private static final Color LANDING_LIGHT_COLOR = new Color(0, 114, 178, 155);
	private static final Color LANDING_DARK_COLOR = new Color(86, 180, 233, 185);
	private static final Color NOMINAL_LIGHT_COLOR = new Color(230, 159, 0);
	private static final Color NOMINAL_DARK_COLOR = new Color(240, 228, 66);
	private static final Color MEAN_LIGHT_COLOR = new Color(213, 94, 0);
	private static final Color MEAN_DARK_COLOR = new Color(255, 112, 91);
	private static final Color PAD_LIGHT_COLOR = new Color(170, 40, 145);
	private static final Color PAD_DARK_COLOR = new Color(240, 130, 210);
	private static final Color ELLIPSE_LIGHT_COLOR = new Color(0, 125, 90);
	private static final Color ELLIPSE_DARK_COLOR = new Color(100, 215, 165);
	private static final float ELLIPSE_WIDTH = 1.8f;
	private static final double ELLIPSE_LEGEND_LINE_LENGTH = 24.0;

	/** Target width for a plot exported for use in reports. */
	private static final int EXPORT_TARGET_WIDTH = 2400;
	private static final int MIN_EXPORT_SCALE = 2;
	private static final int MAX_EXPORT_SCALE = 6;
	private static final int FALLBACK_EXPORT_WIDTH = 800;
	private static final int FALLBACK_EXPORT_HEIGHT = 600;

	private final String simulationName;
	private final MonteCarloResult result;
	private final Unit distanceUnit;
	private final JComboBox<LandingBody> bodyCombo;
	private final TextTitle chartSubtitle = new TextTitle("");
	private final JFreeChart chart;
	private final ChartPanel chartPanel;
	private final JTextArea summaryArea;
	private final RunTableModel runTableModel;
	private final JTabbedPane resultTabs;
	private final MonteCarloMetricsPanel metricsPanel;
	private final Runnable themeChangeListener;
	private Point panAnchor;
	private int highlightedSeries = -1;
	private boolean chartFitPending = true;
	private double lastPlotWidth;
	private double lastPlotHeight;

	public LandingDispersionResultsDialog(Window owner, String simulationName, MonteCarloResult result) {
		super(owner, String.format(trans.get("LandingDispersionResultsDlg.title"), simulationName),
				ModalityType.APPLICATION_MODAL);
		this.simulationName = simulationName;
		this.result = result;
		this.distanceUnit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
		this.bodyCombo = new JComboBox<>(result.getLandingBodies().toArray(new LandingBody[0]));
		this.chart = createChart();
		this.chartPanel = createChartPanel();
		this.summaryArea = createSummaryArea();
		this.runTableModel = new RunTableModel(result, distanceUnit);
		this.resultTabs = new JTabbedPane();
		this.metricsPanel = new MonteCarloMetricsPanel(simulationName, result);
		this.themeChangeListener = this::refreshChartThemes;

		buildDialog();
		updateSelectedBody();
		UITheme.Theme.addUIThemeChangeListener(themeChangeListener);
	}

	@Override
	public void dispose() {
		UITheme.Theme.removeUIThemeChangeListener(themeChangeListener);
		super.dispose();
	}

	private void buildDialog() {
		JPanel header = new JPanel(new MigLayout("ins 0", "[][grow][]"));
		header.add(new JLabel(trans.get("LandingDispersionResultsDlg.lbl.body")));
		bodyCombo.setEnabled(bodyCombo.getItemCount() > 0);
		header.add(bodyCombo, "growx, w 220::");
		header.add(new JLabel(String.format(trans.get("LandingDispersionResultsDlg.lbl.seed"),
				result.getSettings().getSeed())));

		JPanel summaryPanel = new JPanel(new BorderLayout());
		summaryPanel.setBorder(BorderFactory.createTitledBorder(
				trans.get("LandingDispersionResultsDlg.border.summary")));
		summaryPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
		summaryPanel.setMinimumSize(new Dimension(240, 200));

		JPanel chartContainer = new JPanel(new BorderLayout(0, 4));
		chartContainer.setMinimumSize(new Dimension(320, 220));
		chartContainer.add(chartPanel, BorderLayout.CENTER);
		chartContainer.add(createChartControls(), BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartContainer, summaryPanel);
		splitPane.setResizeWeight(0.7);
		splitPane.setContinuousLayout(true);

		JTable runTable = new HeaderToolTipTable(runTableModel, runTableModel::getColumnToolTip);
		runTable.setAutoCreateRowSorter(true);
		runTable.setFillsViewportHeight(true);
		runTable.getColumnModel().getColumn(0).setCellRenderer(new RunNumberRenderer());
		JScrollPane runScrollPane = new JScrollPane(runTable);
		runScrollPane.setBorder(BorderFactory.createTitledBorder(
				trans.get("LandingDispersionResultsDlg.border.runs")));
		runScrollPane.setPreferredSize(new Dimension(800, 180));
		runScrollPane.setMinimumSize(new Dimension(0, 120));

		JButton exportPlotButton = new JButton(trans.get("LandingDispersionResultsDlg.but.exportPlot"));
		exportPlotButton.addActionListener(event -> exportPlot());
		JButton exportRunsButton = new JButton(trans.get("LandingDispersionResultsDlg.but.export"));
		exportRunsButton.setToolTipText(trans.get("LandingDispersionResultsDlg.but.export.ttip"));
		exportRunsButton.addActionListener(event -> exportCsv());
		JButton closeButton = new JButton(trans.get("dlg.but.close"));
		closeButton.addActionListener(event -> dispose());
		JPanel buttons = new JPanel(new MigLayout("ins 0", "[grow][button][button][button]"));
		buttons.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		JLabel runtimeLabel = new JLabel(String.format(trans.get("LandingDispersionResultsDlg.lbl.runtime"),
				result.getElapsedMillis() / 1000.0));
		runtimeLabel.setToolTipText(String.format(trans.get("LandingDispersionResultsDlg.lbl.runtime.ttip"),
				result.getSettings().getRunCount()));
		buttons.add(runtimeLabel, "growx");
		buttons.add(exportPlotButton);
		buttons.add(exportRunsButton);
		buttons.add(closeButton, "tag close");

		JPanel landingPanel = new JPanel(new BorderLayout(0, 8));
		landingPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		landingPanel.add(header, BorderLayout.NORTH);
		landingPanel.add(splitPane, BorderLayout.CENTER);
		landingPanel.add(runScrollPane, BorderLayout.SOUTH);

		resultTabs.addTab(trans.get("LandingDispersionResultsDlg.tab.landing"), landingPanel);
		resultTabs.addTab(trans.get("LandingDispersionResultsDlg.tab.metrics"), metricsPanel);

		JPanel outer = new JPanel(new BorderLayout(0, 8));
		outer.add(resultTabs, BorderLayout.CENTER);
		outer.add(buttons, BorderLayout.SOUTH);
		outer.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		setContentPane(outer);

		bodyCombo.addActionListener(event -> updateSelectedBody());
		GUIUtil.setDisposableDialogOptions(this, closeButton);
		setSize(900, 680);
		setMinimumSize(new Dimension(700, 520));
		setLocationRelativeTo(getOwner());
	}

	private JFreeChart createChart() {
		JFreeChart dispersionChart = ChartFactory.createXYLineChart(
				chartTitle(),
				String.format(trans.get("LandingDispersionResultsDlg.chart.east"), distanceUnit.getUnit()),
				String.format(trans.get("LandingDispersionResultsDlg.chart.north"), distanceUnit.getUnit()),
				new XYSeriesCollection(), PlotOrientation.VERTICAL, true, true, false);

		Font titleFont = dispersionChart.getTitle().getFont();
		chartSubtitle.setFont(titleFont.deriveFont(Font.PLAIN, titleFont.getSize2D() - 3));
		// Insert above the legend, which JFreeChart also stores as a subtitle.
		dispersionChart.addSubtitle(0, chartSubtitle);

		applyChartTheme(dispersionChart);
		return dispersionChart;
	}

	private String chartTitle() {
		return chartTitle(simulationName);
	}

	/** Return a title that identifies the source simulation. */
	static String chartTitle(String simulationName) {
		return String.format(trans.get("LandingDispersionResultsDlg.chart.title"), simulationName);
	}

	/**
	 * Build a subtitle that identifies the displayed body and analysis settings.
	 *
	 * @param body selected landing body, or {@code null} when nothing landed
	 * @param landed number of dispersed runs that produced a landing for that body
	 * @param settings settings the analysis ran with
	 * @return the subtitle text, empty when there is no body to describe
	 */
	static String chartSubtitle(LandingBody body, int landed, MonteCarloSettings settings) {
		if (body == null) {
			return "";
		}
		return String.format(trans.get("LandingDispersionResultsDlg.chart.subtitle"), body.branchName(),
				landed, settings.getRunCount(), settings.getSeed());
	}

	private JTextArea createSummaryArea() {
		JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, area.getFont().getSize()));
		area.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		return area;
	}

	private ChartPanel createChartPanel() {
		ChartPanel panel = new ChartPanel(chart,
				/* properties */ false,
				/* save */ false,
				/* print */ false,
				/* zoom */ false,
				/* tooltips */ true);
		panel.setMouseZoomable(false);
		panel.setMouseWheelEnabled(false);
		panel.setPopupMenu(null);
		panel.setEnforceFileExtensions(true);
		panel.setMinimumDrawWidth(0);
		panel.setMaximumDrawWidth(Integer.MAX_VALUE);
		panel.setMinimumDrawHeight(0);
		panel.setMaximumDrawHeight(Integer.MAX_VALUE);
		panel.setBorder(BorderFactory.createLineBorder(UITheme.getColor(UITheme.Keys.BORDER)));
		panel.setPreferredSize(new Dimension(540, 420));
		panel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				SwingUtilities.invokeLater(LandingDispersionResultsDialog.this::handleChartResize);
			}
		});
		installChartMouseControls(panel);
		return panel;
	}

	private JPanel createChartControls() {
		JPanel controls = new JPanel(new MigLayout("ins 0", "[grow][]0[]0[]"));
		controls.add(new JLabel(trans.get("LandingDispersionResultsDlg.chart.controls")), "growx");
		controls.add(createChartButton(Icons.ZOOM_IN, "LandingDispersionResultsDlg.chart.zoomIn",
				() -> zoomChart(0.8)));
		controls.add(createChartButton(Icons.ZOOM_RESET, "LandingDispersionResultsDlg.chart.fit",
				this::fitChartToData));
		controls.add(createChartButton(Icons.ZOOM_OUT, "LandingDispersionResultsDlg.chart.zoomOut",
				() -> zoomChart(1.25)));
		return controls;
	}

	private static JButton createChartButton(Icon icon, String tooltipKey, Runnable action) {
		JButton button = new JButton(icon);
		button.setToolTipText(trans.get(tooltipKey));
		button.setFocusable(false);
		button.addActionListener(event -> action.run());
		return button;
	}

	/**
	 * Provide simple map-like chart interaction: drag to pan and use the wheel for
	 * uniform two-axis zoom. JFreeChart's rectangle zoom and popup zoom menu are
	 * intentionally disabled because they can distort a geographic scatter plot.
	 */
	private void installChartMouseControls(ChartPanel panel) {
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				Rectangle2D dataArea = panel.getScreenDataArea();
				if (event.getButton() == MouseEvent.BUTTON1 && dataArea.contains(event.getPoint())) {
					panAnchor = event.getPoint();
					panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				}
			}

			@Override
			public void mouseDragged(MouseEvent event) {
				if (panAnchor == null) {
					return;
				}
				Rectangle2D dataArea = panel.getScreenDataArea();
				panChart(event.getX() - panAnchor.x, event.getY() - panAnchor.y, dataArea);
				panAnchor = event.getPoint();
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				if (panAnchor != null) {
					panAnchor = null;
					panel.setCursor(Cursor.getDefaultCursor());
				}
			}

			@Override
			public void mouseMoved(MouseEvent event) {
				updateLegendHighlight(panel, event.getPoint());
			}

			@Override
			public void mouseExited(MouseEvent event) {
				setHighlightedSeries(-1);
			}
		};
		panel.addMouseListener(mouseAdapter);
		panel.addMouseMotionListener(mouseAdapter);
		panel.addMouseWheelListener(event -> {
			if (panel.getScreenDataArea().contains(event.getPoint())) {
				zoomChart(Math.pow(1.15, event.getPreciseWheelRotation()));
				event.consume();
			}
		});
	}

	private void updateLegendHighlight(ChartPanel panel, Point point) {
		EntityCollection entities = panel.getChartRenderingInfo().getEntityCollection();
		ChartEntity entity = entities == null ? null : entities.getEntity(point.x, point.y);
		int seriesIndex = -1;
		if (entity instanceof LegendItemEntity legendItem
				&& chart.getXYPlot().getDataset() instanceof XYSeriesCollection dataset) {
			Comparable<?> seriesKey = legendItem.getSeriesKey() != null
					? legendItem.getSeriesKey() : legendItem.getToolTipText();
			for (int series = 0; series < dataset.getSeriesCount(); series++) {
				if (seriesKey != null && seriesKey.equals(dataset.getSeriesKey(series))) {
					seriesIndex = series;
					break;
				}
			}
		}
		setHighlightedSeries(seriesIndex);
	}

	private void setHighlightedSeries(int seriesIndex) {
		if (highlightedSeries == seriesIndex) {
			return;
		}
		highlightedSeries = seriesIndex;
		if (chart.getXYPlot().getRenderer() instanceof XYLineAndShapeRenderer renderer) {
			applySeriesHighlight(renderer, seriesIndex);
			chartPanel.repaint();
		}
	}

	private static void applyChartTheme(JFreeChart dispersionChart) {
		boolean lightTheme = UITheme.isLightTheme(GUIUtil.getUITheme());
		Color chartBackground = UITheme.getColor(UITheme.Keys.BACKGROUND, Color.WHITE);
		Color text = UITheme.getColor(UITheme.Keys.TEXT, Color.BLACK);
		Color border = UITheme.getColor(UITheme.Keys.BORDER, Color.GRAY);
		Color plotBackground = lightTheme ? Color.WHITE : blend(chartBackground, Color.WHITE, 0.06);

		dispersionChart.setBackgroundPaint(chartBackground);
		dispersionChart.getTitle().setPaint(text);
		// Subtitles include the legend, which is themed separately below.
		for (Title subtitle : dispersionChart.getSubtitles()) {
			if (subtitle instanceof TextTitle textTitle) {
				textTitle.setPaint(blend(text, chartBackground, 0.25));
			}
		}
		if (dispersionChart.getLegend() != null) {
			dispersionChart.getLegend().setBackgroundPaint(chartBackground);
			dispersionChart.getLegend().setItemPaint(text);
			dispersionChart.getLegend().setFrame(new BlockBorder(border));
		}

		XYPlot plot = dispersionChart.getXYPlot();
		// Paint the landing cloud first and progressively more descriptive overlays
		// afterward, leaving covariance ellipses visible on top of every marker.
		plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
		plot.setBackgroundPaint(plotBackground);
		plot.setDomainGridlinePaint(border);
		plot.setRangeGridlinePaint(border);
		plot.setOutlinePaint(border);
		plot.setDomainCrosshairVisible(false);
		plot.setRangeCrosshairVisible(false);
		plot.setDomainZeroBaselineVisible(true);
		plot.setRangeZeroBaselineVisible(true);
		plot.setDomainZeroBaselinePaint(text);
		plot.setRangeZeroBaselinePaint(text);
		configureAxisTheme(plot.getDomainAxis(), text, border);
		configureAxisTheme(plot.getRangeAxis(), text, border);
	}

	private void refreshChartThemes() {
		SwingUtilities.invokeLater(() -> {
			if (!isDisplayable()) {
				return;
			}
			applyChartTheme(chart);
			if (chart.getXYPlot().getDataset() instanceof XYSeriesCollection dataset) {
				boolean hasNominal = dataset.getSeriesCount() > 1 && dataset.getItemCount(1) > 0;
				boolean hasStatistics = dataset.getSeriesCount() > 2 && dataset.getItemCount(2) > 0;
				highlightedSeries = -1;
				chart.getXYPlot().setRenderer(createRenderer(hasNominal, hasStatistics));
			}
			chartPanel.setBorder(BorderFactory.createLineBorder(UITheme.getColor(UITheme.Keys.BORDER)));
			chartPanel.repaint();
			metricsPanel.refreshTheme();
		});
	}

	private static void configureAxisTheme(ValueAxis axis, Color text, Color border) {
		axis.setLabelPaint(text);
		axis.setTickLabelPaint(text);
		axis.setAxisLinePaint(border);
		axis.setTickMarkPaint(border);
	}

	private static Color blend(Color first, Color second, double secondWeight) {
		double firstWeight = 1 - secondWeight;
		return new Color(
				(int) Math.round(first.getRed() * firstWeight + second.getRed() * secondWeight),
				(int) Math.round(first.getGreen() * firstWeight + second.getGreen() * secondWeight),
				(int) Math.round(first.getBlue() * firstWeight + second.getBlue() * secondWeight));
	}

	private void updateSelectedBody() {
		LandingBody body = (LandingBody) bodyCombo.getSelectedItem();
		runTableModel.setBody(body);
		if (body == null) {
			updateChart(null, List.of(), null, null);
			summaryArea.setText(trans.get("LandingDispersionResultsDlg.msg.noLandings"));
			return;
		}

		List<LandingPoint> points = result.getLandingPoints(body.bodyId());
		LandingPoint nominal = result.getNominalResult().getFailureMessage(body.bodyId()) == null
				? result.getNominalResult().getLandingPoint(body.bodyId()) : null;
		DispersionStatistics statistics = points.isEmpty() ? null : DispersionStatistics.from(points);
		updateChart(body, points, nominal, statistics);
		updateSummary(body, nominal, statistics);
	}

	private void updateChart(LandingBody body, List<LandingPoint> points, LandingPoint nominal,
			DispersionStatistics statistics) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries landings = new XYSeries(trans.get("LandingDispersionResultsDlg.series.landings"));
		for (LandingPoint point : points) {
			landings.add(toDisplayDistance(point.east()), toDisplayDistance(point.north()));
		}
		dataset.addSeries(landings);

		XYSeries nominalSeries = new XYSeries(trans.get("LandingDispersionResultsDlg.series.nominal"));
		if (nominal != null) {
			nominalSeries.add(toDisplayDistance(nominal.east()), toDisplayDistance(nominal.north()));
		}
		dataset.addSeries(nominalSeries);

		XYSeries meanSeries = new XYSeries(trans.get("LandingDispersionResultsDlg.series.mean"));
		if (statistics != null) {
			meanSeries.add(toDisplayDistance(statistics.getMeanEast()),
					toDisplayDistance(statistics.getMeanNorth()));
		}
		dataset.addSeries(meanSeries);

		XYSeries padSeries = new XYSeries(trans.get("LandingDispersionResultsDlg.series.pad"));
		padSeries.add(0, 0);
		dataset.addSeries(padSeries);

		addEllipseSeries(dataset, trans.get("LandingDispersionResultsDlg.series.oneSigma"), statistics, 1,
				distanceUnit);
		addEllipseSeries(dataset, trans.get("LandingDispersionResultsDlg.series.twoSigma"), statistics, 2,
				distanceUnit);
		addEllipseSeries(dataset, trans.get("LandingDispersionResultsDlg.series.threeSigma"), statistics, 3,
				distanceUnit);

		XYPlot plot = chart.getXYPlot();
		plot.setDataset(dataset);
		highlightedSeries = -1;
		plot.setRenderer(createRenderer(nominal != null, statistics != null));
		chart.setTitle(chartTitle());
		chartSubtitle.setText(chartSubtitle(body, points.size(), result.getSettings()));
		chartFitPending = true;
		SwingUtilities.invokeLater(this::fitChartToData);
	}

	static void addEllipseSeries(XYSeriesCollection dataset, String name,
			DispersionStatistics statistics, double sigma, Unit distanceUnit) {
		// Preserve angular perimeter order. Auto-sorting by X connects opposite sides
		// of the ellipse and renders a zig-zag instead of a closed curve.
		XYSeries series = new XYSeries(name, false, true);
		if (statistics != null) {
			DispersionEllipse ellipse = statistics.getEllipse(sigma);
			for (int i = 0; i <= 96; i++) {
				double parameter = 2 * Math.PI * i / 96;
				double major = ellipse.semiMajor() * Math.cos(parameter);
				double minor = ellipse.semiMinor() * Math.sin(parameter);
				double east = ellipse.centerEast() + major * Math.cos(ellipse.angle())
						- minor * Math.sin(ellipse.angle());
				double north = ellipse.centerNorth() + major * Math.sin(ellipse.angle())
						+ minor * Math.cos(ellipse.angle());
				series.add(distanceUnit.toUnit(east), distanceUnit.toUnit(north));
			}
		}
		dataset.addSeries(series);
	}

	private static XYLineAndShapeRenderer createRenderer(boolean hasNominal, boolean hasStatistics) {
		boolean lightTheme = UITheme.isLightTheme(GUIUtil.getUITheme());
		Color ellipseColor = themeColor(lightTheme, ELLIPSE_LIGHT_COLOR, ELLIPSE_DARK_COLOR);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		configureLineRendering(renderer);
		configurePointSeries(renderer, 0, themeColor(lightTheme, LANDING_LIGHT_COLOR, LANDING_DARK_COLOR),
				createPointShape(0, false), true);
		configurePointSeries(renderer, 1, themeColor(lightTheme, NOMINAL_LIGHT_COLOR, NOMINAL_DARK_COLOR),
				createPointShape(1, false), hasNominal);
		configurePointSeries(renderer, 2, themeColor(lightTheme, MEAN_LIGHT_COLOR, MEAN_DARK_COLOR),
				createPointShape(2, false), hasStatistics);
		configurePointSeries(renderer, 3, themeColor(lightTheme, PAD_LIGHT_COLOR, PAD_DARK_COLOR),
				createPointShape(3, false), true);
		configureEllipseSeries(renderer, 4, ellipseColor, hasStatistics);
		configureEllipseSeries(renderer, 5, ellipseColor, hasStatistics);
		configureEllipseSeries(renderer, 6, ellipseColor, hasStatistics);
		return renderer;
	}

	static void configureLineRendering(XYLineAndShapeRenderer renderer) {
		// Match the regular simulation plot: draw each curve as one path so the
		// dash phase continues across closely spaced ellipse samples.
		renderer.setDrawSeriesLineAsPath(true);
		// The JFreeChart default is too short to show a complete dash-dot cycle.
		renderer.setLegendLine(new Line2D.Double(-ELLIPSE_LEGEND_LINE_LENGTH / 2.0, 0.0,
				ELLIPSE_LEGEND_LINE_LENGTH / 2.0, 0.0));
	}

	/** Apply the same hover emphasis used by regular simulation-plot legends. */
	static void applySeriesHighlight(XYLineAndShapeRenderer renderer, int highlightedSeries) {
		for (int series = 0; series <= 3; series++) {
			renderer.setSeriesShape(series, createPointShape(series, highlightedSeries == series));
		}
		for (int series = 4; series <= 6; series++) {
			renderer.setSeriesStroke(series, createEllipseStroke(series, highlightedSeries == series));
		}
	}

	private static Shape createPointShape(int series, boolean highlighted) {
		double size;
		if (series == 0) {
			size = highlighted ? 8 : 5;
		} else if (series == 3) {
			size = highlighted ? 16 : 12;
		} else {
			size = highlighted ? 14 : 10;
		}
		double offset = -size / 2;
		return switch (series) {
			case 0, 2 -> new Ellipse2D.Double(offset, offset, size, size);
			case 1 -> new Rectangle2D.Double(offset, offset, size, size);
			case 3 -> createDiamond(size);
			default -> throw new IllegalArgumentException("Unsupported point series " + series);
		};
	}

	private static Shape createDiamond(double size) {
		double radius = size / 2;
		Path2D.Double diamond = new Path2D.Double();
		diamond.moveTo(0, -radius);
		diamond.lineTo(radius, 0);
		diamond.lineTo(0, radius);
		diamond.lineTo(-radius, 0);
		diamond.closePath();
		return diamond;
	}

	private static BasicStroke createEllipseStroke(int series, boolean highlighted) {
		float width = highlighted ? ELLIPSE_WIDTH * 2.5f : ELLIPSE_WIDTH;
		float[] dashes = switch (series) {
			case 4 -> null;
			case 5 -> LineStyle.DASHED.getDashes();
			case 6 -> LineStyle.DASHDOT.getDashes();
			default -> throw new IllegalArgumentException("Unsupported ellipse series " + series);
		};
		return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
				1.0f, dashes, 0.0f);
	}

	private static Color themeColor(boolean lightTheme, Color lightColor, Color darkColor) {
		return lightTheme ? lightColor : darkColor;
	}

	private static void configurePointSeries(XYLineAndShapeRenderer renderer, int series, Color color,
			Shape shape, boolean visibleInLegend) {
		renderer.setSeriesLinesVisible(series, false);
		renderer.setSeriesShapesVisible(series, true);
		renderer.setSeriesShapesFilled(series, true);
		renderer.setSeriesPaint(series, color);
		renderer.setSeriesFillPaint(series, color);
		renderer.setSeriesShape(series, shape);
		renderer.setSeriesVisibleInLegend(series, visibleInLegend);
	}

	static void configureEllipseSeries(XYLineAndShapeRenderer renderer, int series, Color color,
			boolean visibleInLegend) {
		renderer.setSeriesLinesVisible(series, true);
		renderer.setSeriesShapesVisible(series, false);
		renderer.setSeriesPaint(series, color);
		renderer.setSeriesStroke(series, createEllipseStroke(series, false));
		renderer.setSeriesVisibleInLegend(series, visibleInLegend);
	}

	private void fitChartToData() {
		Rectangle2D dataArea = chartPanel.getScreenDataArea();
		if (dataArea.getWidth() <= 0 || dataArea.getHeight() <= 0) {
			return;
		}
		if (chart.getXYPlot().getDataset() instanceof XYSeriesCollection dataset) {
			fitAxisRanges(chart.getXYPlot(), dataset, dataArea.getWidth() / dataArea.getHeight(),
					toDisplayDistance(10));
			chartFitPending = false;
			lastPlotWidth = dataArea.getWidth();
			lastPlotHeight = dataArea.getHeight();
		}
	}

	/** Keep one horizontal display pixel equal to one vertical display pixel after resizing. */
	private void handleChartResize() {
		if (chartFitPending) {
			fitChartToData();
			return;
		}
		Rectangle2D dataArea = chartPanel.getScreenDataArea();
		if (dataArea.getWidth() <= 0 || dataArea.getHeight() <= 0
				|| lastPlotWidth <= 0 || lastPlotHeight <= 0) {
			return;
		}
		XYPlot plot = chart.getXYPlot();
		double unitsPerPixel = Math.max(plot.getDomainAxis().getRange().getLength() / lastPlotWidth,
				plot.getRangeAxis().getRange().getLength() / lastPlotHeight);
		setCenteredRange(plot.getDomainAxis(), unitsPerPixel * dataArea.getWidth());
		setCenteredRange(plot.getRangeAxis(), unitsPerPixel * dataArea.getHeight());
		lastPlotWidth = dataArea.getWidth();
		lastPlotHeight = dataArea.getHeight();
	}

	private void zoomChart(double factor) {
		if (!Double.isFinite(factor) || factor <= 0) {
			return;
		}
		XYPlot plot = chart.getXYPlot();
		setCenteredRange(plot.getDomainAxis(), plot.getDomainAxis().getRange().getLength() * factor);
		setCenteredRange(plot.getRangeAxis(), plot.getRangeAxis().getRange().getLength() * factor);
		chartFitPending = false;
	}

	private void panChart(double horizontalPixels, double verticalPixels, Rectangle2D dataArea) {
		if (dataArea.getWidth() <= 0 || dataArea.getHeight() <= 0) {
			return;
		}
		XYPlot plot = chart.getXYPlot();
		ValueAxis eastAxis = plot.getDomainAxis();
		ValueAxis northAxis = plot.getRangeAxis();
		double eastShift = -horizontalPixels * eastAxis.getRange().getLength() / dataArea.getWidth();
		double northShift = verticalPixels * northAxis.getRange().getLength() / dataArea.getHeight();
		eastAxis.setRange(eastAxis.getLowerBound() + eastShift, eastAxis.getUpperBound() + eastShift);
		northAxis.setRange(northAxis.getLowerBound() + northShift, northAxis.getUpperBound() + northShift);
		chartFitPending = false;
	}

	private static void setCenteredRange(ValueAxis axis, double span) {
		double center = (axis.getLowerBound() + axis.getUpperBound()) / 2;
		axis.setRange(center - span / 2, center + span / 2);
	}

	/**
	 * Fit all plotted data while matching horizontal and vertical distance per
	 * screen pixel. This prevents a circular landing cloud from being stretched by
	 * a non-square chart panel.
	 */
	private static void fitAxisRanges(XYPlot plot, XYSeriesCollection dataset, double displayAspectRatio,
			double minimumSpan) {
		double minimumEast = 0;
		double maximumEast = 0;
		double minimumNorth = 0;
		double maximumNorth = 0;
		for (int series = 0; series < dataset.getSeriesCount(); series++) {
			for (int item = 0; item < dataset.getItemCount(series); item++) {
				double east = dataset.getXValue(series, item);
				double north = dataset.getYValue(series, item);
				minimumEast = Math.min(minimumEast, east);
				maximumEast = Math.max(maximumEast, east);
				minimumNorth = Math.min(minimumNorth, north);
				maximumNorth = Math.max(maximumNorth, north);
			}
		}
		double eastSpan = Math.max(minimumSpan, (maximumEast - minimumEast) * 1.15);
		double northSpan = Math.max(minimumSpan, (maximumNorth - minimumNorth) * 1.15);
		if (eastSpan / northSpan > displayAspectRatio) {
			northSpan = eastSpan / displayAspectRatio;
		} else {
			eastSpan = northSpan * displayAspectRatio;
		}
		double eastCenter = (minimumEast + maximumEast) / 2;
		double northCenter = (minimumNorth + maximumNorth) / 2;
		NumberAxis eastAxis = (NumberAxis) plot.getDomainAxis();
		NumberAxis northAxis = (NumberAxis) plot.getRangeAxis();
		eastAxis.setRange(eastCenter - eastSpan / 2, eastCenter + eastSpan / 2);
		northAxis.setRange(northCenter - northSpan / 2, northCenter + northSpan / 2);
	}

	private void updateSummary(LandingBody body, LandingPoint nominal, DispersionStatistics statistics) {
		StringBuilder text = new StringBuilder();
		text.append(body.branchName()).append('\n');
		text.append(repeat('-', Math.min(44, body.branchName().length()))).append("\n\n");
		text.append(String.format(trans.get("LandingDispersionResultsDlg.summary.landings"),
				statistics == null ? 0 : statistics.getSampleCount(), result.getSettings().getRunCount())).append('\n');
		text.append(String.format(trans.get("LandingDispersionResultsDlg.summary.failures"),
				result.getFailureCount(body.bodyId()))).append("\n\n");

		if (nominal == null) {
			text.append(trans.get("LandingDispersionResultsDlg.summary.noNominal")).append("\n\n");
		} else {
			text.append(trans.get("LandingDispersionResultsDlg.summary.nominal")).append('\n');
			appendPosition(text, nominal.east(), nominal.north());
			text.append("\n");
		}

		if (statistics == null) {
			text.append(trans.get("LandingDispersionResultsDlg.msg.noSuccessfulLandings"));
			summaryArea.setText(text.toString());
			summaryArea.setCaretPosition(0);
			return;
		}

		text.append(trans.get("LandingDispersionResultsDlg.summary.mean")).append('\n');
		appendPosition(text, statistics.getMeanEast(), statistics.getMeanNorth());
		text.append(String.format(trans.get("LandingDispersionResultsDlg.summary.bearing"),
				format(Math.toDegrees(statistics.getMeanBearing())))).append("\n\n");
		text.append(String.format(trans.get("LandingDispersionResultsDlg.summary.r50"),
				formatDistance(statistics.getContainmentRadius(0.50)), distanceUnit.getUnit())).append('\n');
		text.append(String.format(trans.get("LandingDispersionResultsDlg.summary.r90"),
				formatDistance(statistics.getContainmentRadius(0.90)), distanceUnit.getUnit())).append('\n');
		text.append(String.format(trans.get("LandingDispersionResultsDlg.summary.r95"),
				formatDistance(statistics.getContainmentRadius(0.95)), distanceUnit.getUnit())).append("\n\n");
		text.append(trans.get("LandingDispersionResultsDlg.summary.ellipses")).append('\n');
		for (int sigma = 1; sigma <= 3; sigma++) {
			DispersionEllipse ellipse = statistics.getEllipse(sigma);
			text.append(String.format(trans.get("LandingDispersionResultsDlg.summary.ellipse"), sigma,
					formatDistance(2 * ellipse.semiMajor()), formatDistance(2 * ellipse.semiMinor()),
					distanceUnit.getUnit(), format(Math.toDegrees(statistics.getMajorAxisBearing())))).append('\n');
		}
		text.append("\n").append(trans.get("LandingDispersionResultsDlg.summary.ellipseHelp"));
		summaryArea.setText(text.toString());
		summaryArea.setCaretPosition(0);
	}

	private void appendPosition(StringBuilder text, double east, double north) {
		text.append(String.format(trans.get("LandingDispersionResultsDlg.summary.eastNorth"),
				formatDistance(east), formatDistance(north), distanceUnit.getUnit())).append('\n');
		text.append(String.format(trans.get("LandingDispersionResultsDlg.summary.range"),
				formatDistance(Math.hypot(east, north)), distanceUnit.getUnit())).append('\n');
	}

	private double toDisplayDistance(double value) {
		return distanceUnit.toUnit(value);
	}

	private String formatDistance(double value) {
		return format(toDisplayDistance(value));
	}

	private static String format(double value) {
		return new DecimalFormat("0.##").format(value);
	}

	private static String repeat(char character, int count) {
		return String.valueOf(character).repeat(Math.max(1, count));
	}

	private void exportPlot() {
		JFileChooser chooser = new SaveFileChooser();
		chooser.setDialogTitle(trans.get("LandingDispersionResultsDlg.exportPlot.title"));
		chooser.setFileFilter(FileHelper.PNG_FILTER);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());
		boolean exportMetrics = resultTabs.getSelectedIndex() == 1;
		JFreeChart exportChart = exportMetrics ? metricsPanel.getChart() : chart;
		ChartPanel exportPanel = exportMetrics ? metricsPanel.getChartPanel() : chartPanel;
		LandingBody body = (LandingBody) bodyCombo.getSelectedItem();
		String plotName = exportMetrics ? metricsPanel.getExportName()
				: body == null ? simulationName + "-landing-dispersion"
						: simulationName + "-" + body.branchName() + "-landing-dispersion";
		chooser.setSelectedFile(new File(safeFileName(plotName) + ".png"));
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = FileHelper.forceExtension(chooser.getSelectedFile(), "png");
		if (!FileHelper.confirmWrite(file, this)) {
			return;
		}

		int width = exportPanel.getWidth() > 0 ? exportPanel.getWidth() : FALLBACK_EXPORT_WIDTH;
		int height = exportPanel.getHeight() > 0 ? exportPanel.getHeight() : FALLBACK_EXPORT_HEIGHT;
		int scale = exportScale(width);
		try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
			// Scale the displayed layout to preserve its equal east/north axis scale.
			ChartUtils.writeScaledChartAsPNG(stream, exportChart, width, height, scale, scale);
		} catch (IOException exception) {
			JOptionPane.showMessageDialog(this,
					String.format(trans.get("LandingDispersionResultsDlg.exportPlot.error"),
							exception.getLocalizedMessage()),
					trans.get("LandingDispersionResultsDlg.export.error.title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Return the supersampling factor for an exported plot.
	 *
	 * @param width on-screen chart width in pixels
	 * @return the factor to scale both axes by
	 */
	static int exportScale(int width) {
		if (width <= 0) {
			return MAX_EXPORT_SCALE;
		}
		int scale = (int) Math.ceil((double) EXPORT_TARGET_WIDTH / width);
		return Math.max(MIN_EXPORT_SCALE, Math.min(MAX_EXPORT_SCALE, scale));
	}

	private void exportCsv() {
		JFileChooser chooser = new SaveFileChooser();
		chooser.setDialogTitle(trans.get("LandingDispersionResultsDlg.export.title"));
		chooser.setFileFilter(FileHelper.CSV_FILTER);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());
		String safeName = safeFileName(simulationName);
		chooser.setSelectedFile(new File(safeName + "-monte-carlo.csv"));
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = FileHelper.forceExtension(chooser.getSelectedFile(), "csv");
		if (!FileHelper.confirmWrite(file, this)) {
			return;
		}

		try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
			LandingDispersionCsvExporter.write(writer, result);
		} catch (IOException exception) {
			JOptionPane.showMessageDialog(this,
					String.format(trans.get("LandingDispersionResultsDlg.export.error"),
							exception.getLocalizedMessage()),
					trans.get("LandingDispersionResultsDlg.export.error.title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/** Replace file-system-unfriendly characters in a proposed export name. */
	static String safeFileName(String name) {
		return name.replaceAll("[^a-zA-Z0-9._-]+", "-");
	}

	private static final class RunTableModel extends AbstractTableModel {
		private static final String[] COLUMN_KEYS = {
				"run", "status", "east", "north", "range", "maxAltitude", "flightTime"
		};
		private final List<MonteCarloRunResult> runs;
		private final Unit distanceUnit;
		private final String[] columns;
		private LandingBody body;

		private RunTableModel(MonteCarloResult result, Unit distanceUnit) {
			this.distanceUnit = distanceUnit;
			this.columns = new String[] {
					trans.get("LandingDispersionResultsDlg.col.run"),
					trans.get("LandingDispersionResultsDlg.col.status"),
					String.format(trans.get("LandingDispersionResultsDlg.col.east"), distanceUnit.getUnit()),
					String.format(trans.get("LandingDispersionResultsDlg.col.north"), distanceUnit.getUnit()),
					String.format(trans.get("LandingDispersionResultsDlg.col.range"), distanceUnit.getUnit()),
					String.format(trans.get("LandingDispersionResultsDlg.col.maxAltitude"), distanceUnit.getUnit()),
					trans.get("LandingDispersionResultsDlg.col.flightTime")
			};
			this.runs = new ArrayList<>(result.getRunResults().size() + 1);
			this.runs.add(result.getNominalResult());
			this.runs.addAll(result.getRunResults());
		}

		private void setBody(LandingBody body) {
			this.body = body;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return runs.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		private String getColumnToolTip(int column) {
			return trans.get("LandingDispersionResultsDlg.col." + COLUMN_KEYS[column] + ".ttip");
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnIndex == 0 ? Integer.class : String.class;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			MonteCarloRunResult run = runs.get(rowIndex);
			LandingPoint point = body == null ? null : run.getLandingPoint(body.bodyId());
			return switch (columnIndex) {
				case 0 -> run.sample().getRunNumber();
				case 1 -> status(run, body, point);
				case 2 -> point == null ? "" : format(distanceUnit.toUnit(point.east()));
				case 3 -> point == null ? "" : format(distanceUnit.toUnit(point.north()));
				case 4 -> point == null ? "" : format(distanceUnit.toUnit(point.rangeFromPad()));
				case 5 -> Double.isFinite(run.maximumAltitude())
						? format(distanceUnit.toUnit(run.maximumAltitude())) : "";
				case 6 -> Double.isFinite(run.flightTime()) ? format(run.flightTime()) : "";
				default -> throw new IndexOutOfBoundsException("Invalid table column " + columnIndex);
			};
		}

		private static String status(MonteCarloRunResult run, LandingBody body, LandingPoint point) {
			String failure = body == null ? run.failureMessage() : run.getFailureMessage(body.bodyId());
			if (failure != null) {
				return String.format(trans.get("LandingDispersionResultsDlg.status.failed"), failure);
			}
			if (point == null) {
				return trans.get("LandingDispersionResultsDlg.status.noLanding");
			}
			return trans.get("LandingDispersionResultsDlg.status.success");
		}
	}

	/** Display the reference trajectory by name while retaining numeric run sorting. */
	private static final class RunNumberRenderer extends DefaultTableCellRenderer {
		@Override
		protected void setValue(Object value) {
			if (value instanceof Integer runNumber && runNumber == 0) {
				super.setValue(trans.get("LandingDispersionResultsDlg.run.nominal"));
			} else {
				super.setValue(value);
			}
		}
	}
}
