package info.openrocket.swing.gui.plot;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.simulation.DataBranch;
import info.openrocket.core.simulation.DataType;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.utils.DecimalFormatter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/*
 * TODO: It should be possible to simplify this code quite a bit by using a single Renderer instance for
 *  both datasets and the legend.  But for now, the renderers are queried for the line color information
 *  and this is held in the Legend.
 */
public abstract class Plot<T extends DataType, B extends DataBranch<T>, C extends PlotConfiguration<T, B>> {
	protected static final Translator trans = Application.getTranslator();
	protected static final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();

	protected static final float PLOT_STROKE_WIDTH = 1.5f;

	protected int branchCount;
	protected final List<B> allBranches;
	protected final List<ModifiedXYItemRenderer> renderers = new ArrayList<>();
	protected final LegendItems legendItems;
	protected final XYSeriesCollection[] data;
	protected final C filledConfig;		// Configuration after using 'fillAutoAxes' and 'fitAxes'

	protected final JFreeChart chart;

	protected Plot(String plotName, B mainBranch, C config, List<B> allBranches, boolean initialShowPoints) {
		this.branchCount = allBranches.size();
		this.allBranches = allBranches;

		this.chart = ChartFactory.createXYLineChart(
				//// Simulated flight
				/*title*/plotName,
				/*xAxisLabel*/null,
				/*yAxisLabel*/null,
				/*dataset*/null,
				/*orientation*/PlotOrientation.VERTICAL,
				/*legend*/false,
				/*tooltips*/true,
				/*urls*/false
		);
		this.chart.addSubtitle(new TextTitle(Util.formatHTMLString(config.getName())));
		this.chart.getTitle().setFont(new Font("Dialog", Font.BOLD, 23));
		this.chart.setBackgroundPaint(new Color(240, 240, 240));
		this.legendItems = new LegendItems();
		LegendTitle legend = new LegendTitle(this.legendItems);
		legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
		legend.setFrame(BlockBorder.NONE);
		legend.setBackgroundPaint(new Color(240, 240, 240));
		legend.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(legend);

		// Create the data series for both axes
		this.data = new XYSeriesCollection[2];
		this.data[Util.PlotAxisSelection.LEFT.getValue()] = new XYSeriesCollection();
		this.data[Util.PlotAxisSelection.RIGHT.getValue()] = new XYSeriesCollection();

		// Fill the auto-selections based on first branch selected.
		this.filledConfig = config.fillAutoAxes(mainBranch);

		// Get the domain axis type
		final T domainType = filledConfig.getDomainAxisType();
		final Unit domainUnit = filledConfig.getDomainAxisUnit();
		if (domainType == null) {
			throw new IllegalArgumentException("Domain axis type not specified.");
		}

		// Get plot length (ignore trailing NaN's)
		int dataCount = filledConfig.getDataCount();

		int seriesCount = 0;

		// Compute the axes based on the min and max value of all branches
		filledConfig.fitAxes(allBranches);
		List<Axis> minMaxAxes = filledConfig.getAllAxes();

		// Create the XYSeries objects from the flight data and store into the collections
		String[] axisLabel = new String[2];
		for (int i = 0; i < dataCount; i++) {
			// Get info
			T type = postProcessType(filledConfig.getType(i));
			Unit unit = filledConfig.getUnit(i);
			int axis = filledConfig.getAxis(i);
			String name = getLabel(type, unit);

			// Populate data for each branch.
			for (int branchIndex = 0; branchIndex < branchCount; branchIndex++) {
				B thisBranch = allBranches.get(branchIndex);

				// Ignore empty branches
				if (thisBranch.getLength() == 0) {
					continue;
				}

				String branchName = branchIndex == 0 ? null : thisBranch.getName();
				List<XYSeries> seriesList = createSeriesForType(i, seriesCount, type, unit, thisBranch, branchIndex,
						branchName, name);

				for (XYSeries series : seriesList) {
					data[axis].addSeries(series);
					seriesCount++;
				}
			}

			// Update axis label
			if (axisLabel[axis] == null)
				axisLabel[axis] = Util.formatHTMLString(type.getName());
			else
				axisLabel[axis] += "; " + Util.formatHTMLString(type.getName());
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

		int cumulativeSeriesCount = 0;

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
						MetadataXYSeries ser = (MetadataXYSeries) collection.getSeries(series);

						// Determine the appropriate name based on the time and series
						String name = getNameBasedOnIdxAndSeries(ser, item);

						int dataTypeIdx = ser.getDataIdx();
						DataType type = config.getType(dataTypeIdx);

						String nameT = FlightDataType.TYPE_TIME.getName();
						double dataT = Double.NaN;
						List<Double> time = allBranches.get(ser.getBranchIdx()).get((T)FlightDataType.TYPE_TIME);
						if (null != time) {
							dataT = time.get(item);
						}
						String unitT = FlightDataType.TYPE_TIME.getUnitGroup().getDefaultUnit().toString();

						String nameX = config.getDomainAxisType().getName();
						double dataX = dataset.getXValue(series, item);
						String unitX = domainUnit.getUnit();

						String nameY = type.toString();
						double dataY = dataset.getYValue(series, item);
						String unitY = ser.getUnit();
						
						return formatTooltip(name,
											 nameT, dataT, unitT,
											 nameX, dataX, unitX,
											 nameY, dataY, unitY,
											 null);
					}
				};

				// Add data and map to the axis
				plot.setDataset(axisno, data[axisno]);
				ModifiedXYItemRenderer r = new ModifiedXYItemRenderer(branchCount);
				renderers.add(r);
				r.setDefaultToolTipGenerator(tooltipGenerator);
				plot.setRenderer(axisno, r);
				r.setDefaultShapesVisible(initialShowPoints);
				r.setDefaultShapesFilled(true);

				// Set colors for all series of the current axis
				for (int seriesIndex = 0; seriesIndex < data[axisno].getSeriesCount(); seriesIndex++) {
					int colorIndex = cumulativeSeriesCount + seriesIndex;
					r.setSeriesPaint(seriesIndex, Util.getPlotColor(colorIndex));

					Stroke lineStroke = new BasicStroke(PLOT_STROKE_WIDTH);
					r.setSeriesStroke(seriesIndex, lineStroke);
				}

				// Update the cumulative count for the next axis
				cumulativeSeriesCount += data[axisno].getSeriesCount();

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
	}

	protected String getNameBasedOnIdxAndSeries(MetadataXYSeries ser, int dataIdx) {
		return ser.getDescription();
	}

	public JFreeChart getJFreeChart() {
		return chart;
	}

	private String getLabel(T type, Unit unit) {
		String name = Util.formatHTMLString(type.getName());
		if (unit != null && !UnitGroup.UNITS_NONE.contains(unit) &&
				!UnitGroup.UNITS_COEFFICIENT.contains(unit) && unit.getUnit().length() > 0)
			name += " (" + unit.getUnit() + ")";
		return name;
	}

	protected List<XYSeries> createSeriesForType(int dataIndex, int startIndex, T type, Unit unit, B branch,
												 int branchIdx, String branchName, String baseName) {
		// Default implementation for regular DataBranch
		MetadataXYSeries series = new MetadataXYSeries(startIndex, false, true, branchIdx, dataIndex, unit.getUnit(), branchName, baseName);

		List<Double> plotx = branch.get(filledConfig.getDomainAxisType());
		List<Double> ploty = branch.get(type);

		int pointCount = plotx.size();
		for (int j = 0; j < pointCount; j++) {
			double x = filledConfig.getDomainAxisUnit().toUnit(plotx.get(j));
			double y = unit.toUnit(ploty.get(j));
			series.add(x, y);
		}

		return Collections.singletonList(series);
	}

	protected T postProcessType(T type) {
		return type;
	}

	protected String formatTooltip(String dataName,
								   String nameT, double dataT, String unitT,
								   String nameX, double dataX, String unitX,
								   String nameY, double dataY, String unitY,
								   Set<FlightEvent> events) {

		final String strFormat = "%s: %s %s<br>";
		
		DecimalFormat df_x = DecimalFormatter.df(dataX, 2, false);

		StringBuilder sb = new StringBuilder();
		sb.append("<html>");

		sb.append(String.format("<b><i>%s</i></b><br>", dataName));

		// Any events?
		if ((null != events) && (events.size() != 0)) {
			// Pass through and collect any warnings
			for (FlightEvent event : events) {
				if (event.getType() == FlightEvent.Type.SIM_WARN) {
					sb.append("<b><i>Warning:  " + ((Warning) event.getData()).toString() + "</b></i><br>");
				}
			}

			// Now pass through and collect the other events
			String eventStr = "";
			for (FlightEvent event : events) {
				if (event.getType() != FlightEvent.Type.SIM_WARN) {
					if (eventStr != "") {
						eventStr = eventStr + ", ";
					}
					eventStr = eventStr + event.getType();
				}
			}
			if (eventStr != "") {
				sb.append(eventStr + "<br>");
			}
		}

		// Valid Y data?
		if (!Double.isNaN(dataY)) {
			DecimalFormat df_y = DecimalFormatter.df(dataY, 2, false);
			sb.append(String.format(strFormat, nameY, df_y.format(dataY), unitY));
		}

		// Assuming X data is valid
		sb.append(String.format(strFormat, nameX, df_x.format(dataX), unitX));

		// If I've got time data, and my domain isn't time, add time to tooltip
		if (!Double.isNaN(dataT) && !nameX.equals(nameT)) {
			DecimalFormat df_t = DecimalFormatter.df(dataT, 2, false);
			sb.append(String.format(strFormat, nameT, df_t.format(dataT), unitT));
		}
			
		sb.append("</html>");
		
		return sb.toString();
	}

	protected String formatTooltip(String dataName, String nameX, double dataX, String unitX) {
		return formatTooltip(dataName, "", Double.NaN, "", nameX, dataX, unitX, "", Double.NaN, "", null);
	}

	protected static class LegendItems implements LegendItemSource {
		protected final List<String> lineLabels = new ArrayList<>();
		protected final List<Paint> linePaints = new ArrayList<>();
		protected final List<Stroke> lineStrokes = new ArrayList<>();
		protected final List<Shape> pointShapes = new ArrayList<>();

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

	public void setShowPoints(boolean showPoints) {
		for (ModifiedXYItemRenderer r : renderers) {
			r.setDefaultShapesVisible(showPoints);
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
	protected static class ModifiedXYItemRenderer extends XYLineAndShapeRenderer {

		private final int branchCount;

		protected ModifiedXYItemRenderer(int branchCount) {
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
			Line2D line;
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

	protected static class PresetNumberAxis extends NumberAxis {
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

	protected static class MetadataXYSeries extends XYSeries {
		private final int branchIdx;
		private final int dataIdx;
		private final String unit;
		private final String branchName;
		private String baseName;

		public MetadataXYSeries(Comparable key, boolean autoSort, boolean allowDuplicateXValues, int branchIdx, int dataIdx, String unit,
								String branchName, String baseName) {
			super(key, autoSort, allowDuplicateXValues);
			this.branchIdx = branchIdx;
			this.dataIdx = dataIdx;
			this.unit = unit;
			this.branchName = branchName;
			this.baseName = baseName;
			updateDescription();
		}

		public MetadataXYSeries(Comparable key, boolean autoSort, boolean allowDuplicateXValues, int branchIdx, String unit,
								String branchName, String baseName) {
			this(key, autoSort, allowDuplicateXValues, branchIdx, -1, unit, branchName, baseName);
		}

		public String getUnit() {
			return unit;
		}

		public int getBranchIdx() {
			return branchIdx;
		}

		public int getDataIdx() {
			return dataIdx;
		}

		public String getBranchName() {
			return branchName;
		}

		public String getBaseName() {
			return baseName;
		}

		public void setBaseName(String baseName) {
			this.baseName = baseName;
		}

		public void updateDescription() {
			String description = branchName == null ? baseName : branchName + ": " + baseName;
			setDescription(description);
		}
	}
}
