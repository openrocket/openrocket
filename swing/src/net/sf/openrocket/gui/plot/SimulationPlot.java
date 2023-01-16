package net.sf.openrocket.gui.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.simulation.SimulationPlotPanel;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.LinearInterpolator;

import net.sf.openrocket.utils.DecimalFormatter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextUtilities;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

/*
 * TODO: It should be possible to simplify this code quite a bit by using a single Renderer instance for
 * both datasets and the legend.  But for now, the renderers are queried for the line color information
 * and this is held in the Legend.
 */
@SuppressWarnings("serial")
public class SimulationPlot {
	private static final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();


	private static final float PLOT_STROKE_WIDTH = 1.5f;

	private final JFreeChart chart;

	private final PlotConfiguration config;
	private final Simulation simulation;
	private final PlotConfiguration filled;

	private final List<EventDisplayInfo> eventList;
	private final List<ModifiedXYItemRenderer> renderers = new ArrayList<ModifiedXYItemRenderer>();

	private final LegendItems legendItems;

	private int branchCount;

	void setShowPoints(boolean showPoints) {
		for (ModifiedXYItemRenderer r : renderers) {
			r.setBaseShapesVisible(showPoints);
		}
	}

	void setShowBranch(int branch) {
		XYPlot plot = (XYPlot) chart.getPlot();
		int datasetcount = plot.getDatasetCount();
		for (int i = 0; i < datasetcount; i++) {
			int seriescount = ((XYSeriesCollection) plot.getDataset(i)).getSeriesCount();
			XYItemRenderer r = ((XYPlot) chart.getPlot()).getRenderer(i);
			for (int j = 0; j < seriescount; j++) {
				boolean show = (branch < 0) || (j % branchCount == branch);
				r.setSeriesVisible(j, show);
			}
		}
		drawDomainMarkers(branch);
	}

	SimulationPlot(Simulation simulation, PlotConfiguration config, boolean initialShowPoints) {
		this.simulation = simulation;
		this.config = config;
		this.branchCount = simulation.getSimulatedData().getBranchCount();

		this.chart = ChartFactory.createXYLineChart(
				//// Simulated flight
				/*title*/simulation.getName(),
				/*xAxisLabel*/null,
				/*yAxisLabel*/null,
				/*dataset*/null,
				/*orientation*/PlotOrientation.VERTICAL,
				/*legend*/false,
				/*tooltips*/true,
				/*urls*/false
		);

		chart.getTitle().setFont(new Font("Dialog", Font.BOLD, 23));
		chart.setBackgroundPaint(new Color(240, 240, 240));
		this.legendItems = new LegendItems();
		LegendTitle legend = new LegendTitle(legendItems);
		legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		legend.setFrame(BlockBorder.NONE);
		legend.setBackgroundPaint(new Color(240, 240, 240));
		legend.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(legend);

		chart.addSubtitle(new TextTitle(config.getName()));

		// Fill the auto-selections based on first branch selected.
		FlightDataBranch mainBranch = simulation.getSimulatedData().getBranch(0);
		this.filled = config.fillAutoAxes(mainBranch);

		// Compute the axes based on the min and max value of all branches
		PlotConfiguration plotConfig = filled.clone();
		plotConfig.fitAxes(simulation.getSimulatedData().getBranches());
		List<Axis> minMaxAxes = plotConfig.getAllAxes();

		// Create the data series for both axes
		XYSeriesCollection[] data = new XYSeriesCollection[2];
		data[0] = new XYSeriesCollection();
		data[1] = new XYSeriesCollection();

		// Get the domain axis type
		final FlightDataType domainType = filled.getDomainAxisType();
		final Unit domainUnit = filled.getDomainAxisUnit();
		if (domainType == null) {
			throw new IllegalArgumentException("Domain axis type not specified.");
		}

		// Get plot length (ignore trailing NaN's)
		int typeCount = filled.getTypeCount();

		int seriesCount = 0;

		// Create the XYSeries objects from the flight data and store into the collections
		String[] axisLabel = new String[2];
		for (int i = 0; i < typeCount; i++) {
			// Get info
			FlightDataType type = filled.getType(i);
			if (Objects.equals(type.getName(), "Position upwind")) {
				type = FlightDataType.TYPE_POSITION_Y;
			}
			Unit unit = filled.getUnit(i);
			int axis = filled.getAxis(i);
			String name = getLabel(type, unit);

			List<String> seriesNames = Util.generateSeriesLabels(simulation);

			// Populate data for each branch.

			// The primary branch (branchIndex = 0) is easy since all the data is copied
			{
				int branchIndex = 0;
				FlightDataBranch thisBranch = simulation.getSimulatedData().getBranch(branchIndex);
				// Store data in provided units
				List<Double> plotx = thisBranch.get(domainType);
				List<Double> ploty = thisBranch.get(type);
				XYSeries series = new XYSeries(seriesCount++, false, true);
				series.setDescription(name);
				int pointCount = plotx.size();
				for (int j = 0; j < pointCount; j++) {
					series.add(domainUnit.toUnit(plotx.get(j)), unit.toUnit(ploty.get(j)));
				}
				data[axis].addSeries(series);
			}
			// For each of the secondary branches, we use data from branch 0 for the earlier times
			for (int branchIndex = 1; branchIndex < branchCount; branchIndex++) {
				FlightDataBranch primaryBranch = simulation.getSimulatedData().getBranch(0);
				FlightDataBranch thisBranch = simulation.getSimulatedData().getBranch(branchIndex);

				// Ignore empty branches
				if (thisBranch.getLength() == 0) {
					// Add an empty series to keep the series count consistent
					XYSeries series = new XYSeries(seriesCount++, false, true);
					series.setDescription(thisBranch.getBranchName() + ": " + name);
					data[axis].addSeries(series);
					continue;
				}

				// Get first time index used in secondary branch;
				double firstSampleTime = thisBranch.get(FlightDataType.TYPE_TIME).get(0);

				XYSeries series = new XYSeries(seriesCount++, false, true);
				series.setDescription(thisBranch.getBranchName() + ": " + name);

				// Copy the first points from the primaryBranch.
				List<Double> primaryT = primaryBranch.get(FlightDataType.TYPE_TIME);
				List<Double> primaryx = primaryBranch.get(domainType);
				List<Double> primaryy = primaryBranch.get(type);

				for (int j = 0; j < primaryT.size(); j++) {
					if (primaryT.get(j) >= firstSampleTime) {
						break;
					}
					series.add(domainUnit.toUnit(primaryx.get(j)), unit.toUnit(primaryy.get(j)));
				}

				// Now copy all the data from the secondary branch
				List<Double> plotx = thisBranch.get(domainType);
				List<Double> ploty = thisBranch.get(type);

				int pointCount = plotx.size();
				for (int j = 0; j < pointCount; j++) {
					series.add(domainUnit.toUnit(plotx.get(j)), unit.toUnit(ploty.get(j)));
				}
				data[axis].addSeries(series);
			}

			// Update axis label
			if (axisLabel[axis] == null)
				axisLabel[axis] = type.getName();
			else
				axisLabel[axis] += "; " + type.getName();
		}

		// Add the data and formatting to the plot
		XYPlot plot = chart.getXYPlot();
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		// Plot appearance
		plot.setBackgroundPaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.lightGray);

		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.lightGray);

		Color[] colors = {new Color(0,114,189),		// Colors for data lines
				new Color(217,83,25),
				new Color(237,177,32),
				new Color(126,49,142),
				new Color(119,172,48),
				new Color(77,190,238),
				new Color(162,20,47)};
		for (int axisno = 0; axisno < 2; axisno++) {
			// Check whether axis has any data
			if (data[axisno].getSeriesCount() > 0) {
				// Create and set axis
				double min = minMaxAxes.get(axisno).getMinValue();
				double max = minMaxAxes.get(axisno).getMaxValue();

				NumberAxis axis = new PresetNumberAxis(min, max);
				axis.setLabel(axisLabel[axisno]);
				plot.setRangeAxis(axisno, axis);
				axis.setLabelFont(new Font("Dialog", Font.BOLD, 14));

				double domainMin = data[axisno].getDomainLowerBound(true);
				double domainMax = data[axisno].getDomainUpperBound(true);

				plot.setDomainAxis(new PresetNumberAxis(domainMin, domainMax));

				// Custom tooltip generator
				int finalAxisno = axisno;
				StandardXYToolTipGenerator tooltipGenerator = new StandardXYToolTipGenerator() {
					@Override
					public String generateToolTip(XYDataset dataset, int series, int item) {
						XYSeriesCollection collection = data[finalAxisno];
						if (collection.getSeriesCount() == 0) {
							return null;
						}
						XYSeries ser = collection.getSeries(series);
						String name = ser.getDescription();

						// Extract the unit from the last part of the series description, between parenthesis
						Matcher m = Pattern.compile(".*\\((.*?)\\)").matcher(name);
						String unitY = "";
						if (m.find()) {
							unitY = m.group(1);
						}
						String unitX = domainUnit.getUnit();

						double dataY = dataset.getYValue(series, item);
						double dataX = dataset.getXValue(series, item);

						return formatSampleTooltip(name, dataX, unitX, dataY, unitY, item);
					}
				};

				// Add data and map to the axis
				plot.setDataset(axisno, data[axisno]);
				ModifiedXYItemRenderer r = new ModifiedXYItemRenderer(branchCount);
				renderers.add(r);
				r.setBaseToolTipGenerator(tooltipGenerator);
				plot.setRenderer(axisno, r);
				r.setBaseShapesVisible(initialShowPoints);
				r.setBaseShapesFilled(true);
				r.setSeriesPaint(0, colors[axisno]);
				r.setSeriesPaint(1, colors[axisno+2]);
				r.setSeriesPaint(2, colors[axisno+4]);
				for (int j = 0; j < data[axisno].getSeriesCount(); j++) {
					Stroke lineStroke = new BasicStroke(PLOT_STROKE_WIDTH);
					r.setSeriesStroke(j, lineStroke);
				}
				// Now we pull the colors for the legend.
				for (int j = 0; j < data[axisno].getSeriesCount(); j += branchCount) {
					String name = data[axisno].getSeries(j).getDescription();
					this.legendItems.lineLabels.add(name);
					Paint linePaint = r.lookupSeriesPaint(j);
					this.legendItems.linePaints.add(linePaint);
					Shape itemShape = r.lookupSeriesShape(j);
					this.legendItems.pointShapes.add(itemShape);
					Stroke lineStroke = r.getSeriesStroke(j);
					this.legendItems.lineStrokes.add(lineStroke);
				}

				plot.mapDatasetToRangeAxis(axisno, axisno);
			}
		}

		plot.getDomainAxis().setLabel(getLabel(domainType, domainUnit));
		plot.addDomainMarker(new ValueMarker(0));
		plot.addRangeMarker(new ValueMarker(0));

		plot.getDomainAxis().setLabelFont(new Font("Dialog", Font.BOLD, 14));

		// Create list of events to show (combine event too close to each other)
		this.eventList = buildEventInfo();

		// Create the event markers
		drawDomainMarkers(-1);

	}

	JFreeChart getJFreeChart() {
		return chart;
	}

	private String formatSampleTooltip(String dataName, double dataX, String unitX, double dataY, String unitY, int sampleIdx, boolean addYValue) {
		String ord_end = "th";		// Ordinal number ending (1'st', 2'nd'...)
		if (sampleIdx % 10 == 1) {
			ord_end = "st";
		} else if (sampleIdx % 10 == 2) {
			ord_end = "nd";
		} else if (sampleIdx % 10 == 3) {
			ord_end = "rd";
		}

		DecimalFormat df_y = DecimalFormatter.df(dataY, 2, false);
		DecimalFormat df_x = DecimalFormatter.df(dataX, 2, false);

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<html>" +
						"<b><i>%s</i></b><br>", dataName));

		if (addYValue) {
			sb.append(String.format("Y: %s %s<br>", df_y.format(dataY), unitY));
		}

		sb.append(String.format("X: %s %s<br>" +
						"%d<sup>%s</sup> sample" +
						"</html>", df_x.format(dataX), unitX, sampleIdx, ord_end));

		return sb.toString();
	}

	private String formatSampleTooltip(String dataName, double dataX, String unitX, double dataY, String unitY, int sampleIdx) {
		return formatSampleTooltip(dataName, dataX, unitX, dataY, unitY, sampleIdx, true);
	}

	private String formatSampleTooltip(String dataName, double dataX, String unitX, int sampleIdx) {
		return formatSampleTooltip(dataName, dataX, unitX, 0, "", sampleIdx, false);
	}

	private String getLabel(FlightDataType type, Unit unit) {
		String name = type.getName();
		if (unit != null && !UnitGroup.UNITS_NONE.contains(unit) &&
				!UnitGroup.UNITS_COEFFICIENT.contains(unit) && unit.getUnit().length() > 0)
			name += " (" + unit.getUnit() + ")";
		return name;
	}

	/**
	 * Draw the domain markers for a certain branch. Draws all the markers if the branch is -1.
	 * @param branch branch to draw, or -1 to draw all
	 */
	private void drawDomainMarkers(int branch) {
		XYPlot plot = chart.getXYPlot();
		FlightDataBranch dataBranch = simulation.getSimulatedData().getBranch(Math.max(branch, 0));

		// Clear existing domain markers and annotations
		plot.clearDomainMarkers();
		plot.clearAnnotations();

		// Store flight event information
		List<Double> eventTimes = new ArrayList<>();
		List<String> eventLabels = new ArrayList<>();
		List<Color> eventColors = new ArrayList<>();
		List<Image> eventImages = new ArrayList<>();

		// Plot the markers
		if (config.getDomainAxisType() == FlightDataType.TYPE_TIME && !preferences.getBoolean(Preferences.MARKER_STYLE_ICON, false)) {
			fillEventLists(branch, eventTimes, eventLabels, eventColors, eventImages);
			plotVerticalLineMarkers(plot, eventTimes, eventLabels, eventColors);

		} else {	// Other domains are plotted as image annotations
			if (branch == -1) {
				// For icon markers, we need to do the plotting separately, otherwise you can have icon markers from e.g.
				// branch 1 be plotted on branch 0
				for (int b = 0; b < simulation.getSimulatedData().getBranchCount(); b++) {
					fillEventLists(b, eventTimes, eventLabels, eventColors, eventImages);
					dataBranch = simulation.getSimulatedData().getBranch(b);
					plotIconMarkers(plot, dataBranch, eventTimes, eventLabels, eventImages);
					eventTimes.clear();
					eventLabels.clear();
					eventColors.clear();
					eventImages.clear();
				}
			} else {
				fillEventLists(branch, eventTimes, eventLabels, eventColors, eventImages);
				plotIconMarkers(plot, dataBranch, eventTimes, eventLabels, eventImages);
			}
		}
	}

	private void fillEventLists(int branch, List<Double> eventTimes, List<String> eventLabels,
								List<Color> eventColors, List<Image> eventImages) {
		HashSet<FlightEvent.Type> typeSet = new HashSet<>();
		double prevTime = -100;
		String text = null;
		Color color = null;
		Image image = null;
		for (EventDisplayInfo info : eventList) {
			if (branch >= 0 && branch != info.stage) {
				continue;
			}

			double t = info.time;
			FlightEvent.Type type = info.event.getType();

			if (Math.abs(t - prevTime) <= 0.05) {
				if (!typeSet.contains(type)) {
					text = text + ", " + type.toString();
					color = EventGraphics.getEventColor(type);
					image = EventGraphics.getEventImage(type);
					typeSet.add(type);
				}

			} else {
				if (text != null) {
					eventTimes.add(prevTime);
					eventLabels.add(text);
					eventColors.add(color);
					eventImages.add(image);
				}
				prevTime = t;
				text = type.toString();
				color = EventGraphics.getEventColor(type);
				image = EventGraphics.getEventImage(type);
				typeSet.clear();
				typeSet.add(type);
			}

		}
		if (text != null) {
			eventTimes.add(prevTime);
			eventLabels.add(text);
			eventColors.add(color);
			eventImages.add(image);
		}
	}

	private static void plotVerticalLineMarkers(XYPlot plot, List<Double> eventTimes, List<String> eventLabels, List<Color> eventColors) {
		double markerWidth = 0.01 * plot.getDomainAxis().getUpperBound();

		// Domain time is plotted as vertical lines
		for (int i = 0; i < eventTimes.size(); i++) {
			double t = eventTimes.get(i);
			String event = eventLabels.get(i);
			Color color = eventColors.get(i);

			ValueMarker m = new ValueMarker(t);
			m.setLabel(event);
			m.setPaint(color);
			m.setLabelPaint(color);
			m.setAlpha(0.7f);
			m.setLabelFont(new Font("Dialog", Font.PLAIN, 13));
			plot.addDomainMarker(m);

			if (t > plot.getDomainAxis().getUpperBound() - markerWidth) {
				plot.setDomainAxis(new PresetNumberAxis(plot.getDomainAxis().getLowerBound(), t + markerWidth));
			}
		}
	}

	private void plotIconMarkers(XYPlot plot, FlightDataBranch dataBranch, List<Double> eventTimes,
								List<String> eventLabels, List<Image> eventImages) {
		List<Double> time = dataBranch.get(FlightDataType.TYPE_TIME);
		List<Double> domain = dataBranch.get(config.getDomainAxisType());

		LinearInterpolator domainInterpolator = new LinearInterpolator(time, domain);

		for (int i = 0; i < eventTimes.size(); i++) {
			double t = eventTimes.get(i);
			Image image = eventImages.get(i);

			if (image == null) {
				continue;
			}

			double xcoord = domainInterpolator.getValue(t);

			for (int index = 0; index < config.getTypeCount(); index++) {
				FlightDataType type = config.getType(index);
				List<Double> range = dataBranch.get(type);

				LinearInterpolator rangeInterpolator = new LinearInterpolator(time, range);
				// Image annotations are not supported on the right-side axis
				// TODO: LOW: Can this be achieved by JFreeChart?
				if (filled.getAxis(index) != SimulationPlotPanel.LEFT) {
					continue;
				}

				double ycoord = rangeInterpolator.getValue(t);

				// Convert units
				xcoord = config.getDomainAxisUnit().toUnit(xcoord);
				ycoord = config.getUnit(index).toUnit(ycoord);

				// Get the sample index of the flight event. Because this can be an interpolation between two samples,
				// take the closest sample.
				final int sampleIdx;
				Optional<Double> closestSample = time.stream()
						.min(Comparator.comparingDouble(sample -> Math.abs(sample - t)));
				sampleIdx = closestSample.map(time::indexOf).orElse(-1);

				String tooltipText = formatSampleTooltip(eventLabels.get(i), xcoord, config.getDomainAxisUnit().getUnit(), sampleIdx) ;

				XYImageAnnotation annotation =
						new XYImageAnnotation(xcoord, ycoord, image, RectangleAnchor.CENTER);
				annotation.setToolTipText(tooltipText);
				plot.addAnnotation(annotation);
			}
		}
	}

	private List<EventDisplayInfo> buildEventInfo() {
		ArrayList<EventDisplayInfo> eventList = new ArrayList<EventDisplayInfo>();

		for (int branch = 0; branch < branchCount; branch++) {
			List<FlightEvent> events = simulation.getSimulatedData().getBranch(branch).getEvents();
			for (FlightEvent event : events) {
				FlightEvent.Type type = event.getType();
				if (type != FlightEvent.Type.ALTITUDE && config.isEventActive(type)) {
					EventDisplayInfo info = new EventDisplayInfo();
					info.stage = branch;
					info.time = event.getTime();
					info.event = event;
					eventList.add(info);
				}
			}
		}

		Collections.sort(eventList, new Comparator<EventDisplayInfo>() {

			@Override
			public int compare(EventDisplayInfo o1, EventDisplayInfo o2) {
				if (o1.time < o2.time)
					return -1;
				if (o1.time == o2.time)
					return 0;
				return 1;
			}

		});

		return eventList;

	}

	private static class LegendItems implements LegendItemSource {

		private final List<String> lineLabels = new ArrayList<String>();
		private final List<Paint> linePaints = new ArrayList<Paint>();
		private final List<Stroke> lineStrokes = new ArrayList<Stroke>();
		private final List<Shape> pointShapes = new ArrayList<Shape>();

		@Override
		public LegendItemCollection getLegendItems() {
			LegendItemCollection c = new LegendItemCollection();
			int i = 0;
			for (String s : lineLabels) {
				String label = s;
				String description = s;
				String toolTipText = null;
				String urlText = null;
				boolean shapeIsVisible = false;
				Shape shape = pointShapes.get(i);
				boolean shapeIsFilled = false;
				Paint fillPaint = linePaints.get(i);
				boolean shapeOutlineVisible = false;
				Paint outlinePaint = linePaints.get(i);
				Stroke outlineStroke = lineStrokes.get(i);
				boolean lineVisible = true;
				Stroke lineStroke = lineStrokes.get(i);
				Paint linePaint = linePaints.get(i);

				Shape legendLine = new Line2D.Double(-7.0, 0.0, 7.0, 0.0);

				LegendItem result = new LegendItem(label, description, toolTipText,
						urlText, shapeIsVisible, shape, shapeIsFilled, fillPaint,
						shapeOutlineVisible, outlinePaint, outlineStroke, lineVisible,
						legendLine, lineStroke, linePaint);

				c.add(result);
				i++;
			}
			return c;
		}
	}

	/**
	 * A modification to the standard renderer that renders the domain marker
	 * labels vertically instead of horizontally.
	 *
	 * This class is special in that it assumes the data series are added to it
	 * in a specific order.  In particular they must be "by parameter by stage".
	 * Assuming that three series are chosen (a, b, c) and the rocket has 2 stages, the
	 * data series are added in this order:
	 *
	 * series a stage 0
	 * series a stage 1
	 * series b stage 0
	 * series b stage 1
	 * series c stage 0
	 * series c stage 1
	 */
	private static class ModifiedXYItemRenderer extends XYLineAndShapeRenderer {

		private final int branchCount;

		private ModifiedXYItemRenderer(int branchCount) {
			this.branchCount = branchCount;
		}

		@Override
		public Paint lookupSeriesPaint(int series) {
			return super.lookupSeriesPaint(series / branchCount);
		}

		@Override
		public Paint lookupSeriesFillPaint(int series) {
			return super.lookupSeriesFillPaint(series / branchCount);
		}

		@Override
		public Paint lookupSeriesOutlinePaint(int series) {
			return super.lookupSeriesOutlinePaint(series / branchCount);
		}

		@Override
		public Stroke lookupSeriesStroke(int series) {
			return super.lookupSeriesStroke(series / branchCount);
		}

		@Override
		public Stroke lookupSeriesOutlineStroke(int series) {
			return super.lookupSeriesOutlineStroke(series / branchCount);
		}

		@Override
		public Shape lookupSeriesShape(int series) {
			return DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[series % branchCount % DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE.length];
		}

		@Override
		public Shape lookupLegendShape(int series) {
			return DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[series % branchCount % DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE.length];
		}

		@Override
		public Font lookupLegendTextFont(int series) {
			return super.lookupLegendTextFont(series / branchCount);
		}

		@Override
		public Paint lookupLegendTextPaint(int series) {
			return super.lookupLegendTextPaint(series / branchCount);
		}

		@Override
		public void drawDomainMarker(Graphics2D g2, XYPlot plot, ValueAxis domainAxis,
				Marker marker, Rectangle2D dataArea) {

			if (!(marker instanceof ValueMarker)) {
				// Use parent for all others
				super.drawDomainMarker(g2, plot, domainAxis, marker, dataArea);
				return;
			}

			/*
			 * Draw the normal marker, but with rotated text.
			 * Copied from the overridden method.
			 */
			ValueMarker vm = (ValueMarker) marker;
			double value = vm.getValue();
			Range range = domainAxis.getRange();
			if (!range.contains(value)) {
				return;
			}

			double v = domainAxis.valueToJava2D(value, dataArea, plot.getDomainAxisEdge());

			PlotOrientation orientation = plot.getOrientation();
			Line2D line = null;
			if (orientation == PlotOrientation.HORIZONTAL) {
				line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
			} else {
				line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
			}

			final Composite originalComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, marker
					.getAlpha()));
			g2.setPaint(marker.getPaint());
			g2.setStroke(marker.getStroke());
			g2.draw(line);

			String label = marker.getLabel();
			RectangleAnchor anchor = marker.getLabelAnchor();
			if (label != null) {
				Font labelFont = marker.getLabelFont();
				g2.setFont(labelFont);
				g2.setPaint(marker.getLabelPaint());
				Point2D coordinates = calculateDomainMarkerTextAnchorPoint(g2,
						orientation, dataArea, line.getBounds2D(), marker
								.getLabelOffset(), LengthAdjustmentType.EXPAND, anchor);

				// Changed:
				TextAnchor textAnchor = TextAnchor.TOP_RIGHT;
				TextUtilities.drawRotatedString(label, g2, (float) coordinates.getX() + 2,
						(float) coordinates.getY(), textAnchor,
						-Math.PI / 2, textAnchor);
			}
			g2.setComposite(originalComposite);
		}

	}

	private static class PresetNumberAxis extends NumberAxis {
		private final double min;
		private final double max;

		public PresetNumberAxis(double min, double max) {
			this.min = min;
			this.max = max;
			autoAdjustRange();
		}

		@Override
		protected void autoAdjustRange() {
			this.setRange(min, max);
		}

		@Override
		public void setRange(Range range) {
			double lowerValue = range.getLowerBound();
			double upperValue = range.getUpperBound();
			if (lowerValue < min || upperValue > max) {
				// Don't blow past the min & max of the range this is important to keep
				// panning constrained within the current bounds.
				return;
			}
			super.setRange(new Range(lowerValue, upperValue));
		}

	}

	private static class EventDisplayInfo {
		int stage;
		double time;
		FlightEvent event;
	}

}

