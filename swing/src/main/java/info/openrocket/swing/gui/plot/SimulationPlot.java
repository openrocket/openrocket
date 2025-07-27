package info.openrocket.swing.gui.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.util.LinearInterpolator;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.utils.DecimalFormatter;

import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

@SuppressWarnings("serial")
public class SimulationPlot extends Plot<FlightDataType, FlightDataBranch, SimulationPlotConfiguration> {
	private final SimulationPlotConfiguration config;
	private final Simulation simulation;

	private final List<EventDisplayInfo> eventList;
	private ErrorAnnotationSet errorAnnotations = null;

	SimulationPlot(Simulation simulation, SimulationPlotConfiguration config, boolean initialShowPoints,
				   FlightDataBranch mainBranch, List<FlightDataBranch> allBranches) {
		super(simulation.getName(), mainBranch, config, allBranches, initialShowPoints);

		this.simulation = simulation;
		this.config = config;
		this.branchCount = simulation.getSimulatedData().getBranchCount();

		// Create list of events to show (combine event too close to each other)
		this.eventList = buildEventInfo();

		// Create the event markers
		drawDomainMarkers(-1);

		errorAnnotations = new ErrorAnnotationSet(branchCount);
	}

	public static SimulationPlot create(Simulation simulation, SimulationPlotConfiguration config, boolean initialShowPoints) {
		FlightDataBranch mainBranch = simulation.getSimulatedData().getBranch(0);

		return new SimulationPlot(simulation, config, initialShowPoints, mainBranch,
				simulation.getSimulatedData().getBranches());
	}

	@Override
	protected FlightDataType postProcessType(FlightDataType type) {
		if (Objects.equals(type.getName(), "Position upwind")) {
			type = FlightDataType.TYPE_POSITION_Y;
		}
		return type;
	}

	void setShowErrors(boolean showErrors) {
		errorAnnotations.setVisible(showErrors);
	}

	void setShowBranch(int branch) {
		XYPlot plot = (XYPlot) chart.getPlot();
		int datasetcount = plot.getDatasetCount();
		for (int i = 0; i < datasetcount; i++) {
			int seriescount = plot.getDataset(i).getSeriesCount();
			XYItemRenderer r = ((XYPlot) chart.getPlot()).getRenderer(i);
			for (int j = 0; j < seriescount; j++) {
				boolean show = (branch < 0) || (j % branchCount == branch);
				r.setSeriesVisible(j, show);
			}
		}

		drawDomainMarkers(branch);

		errorAnnotations.setCurrent(branch);
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
		List<Color> eventColors = new ArrayList<>();
		List<Image> eventImages = new ArrayList<>();
		List<Set<FlightEvent>> eventSets = new ArrayList<>();

		// Plot the markers
		if (config.getDomainAxisType() == FlightDataType.TYPE_TIME && !preferences.getBoolean(ApplicationPreferences.MARKER_STYLE_ICON, false)) {
			fillEventLists(branch, eventColors, eventImages, eventSets);
			plotVerticalLineMarkers(plot, eventColors, eventSets);

		} else {	// Other domains are plotted as image annotations
			if (branch == -1) {
				// For icon markers, we need to do the plotting separately, otherwise you can have icon markers from e.g.
				// branch 1 be plotted on branch 0.  Also, need to take the branches in reverse order so lower number
				// branches get their icons put on top of higher number and the tooltip headings are correct.
				for (int b = simulation.getSimulatedData().getBranchCount() - 1; b >= 0; b--) {
					fillEventLists(b, eventColors, eventImages, eventSets);
					dataBranch = simulation.getSimulatedData().getBranch(b);
					plotIconMarkers(plot, simulation, b, eventImages, eventSets);
					eventSets = new ArrayList<>();
					eventColors.clear();
					eventImages.clear();
				}
			} else {
				fillEventLists(branch, eventColors, eventImages, eventSets);
				plotIconMarkers(plot, simulation, branch, eventImages, eventSets);
			}
		}
	}

	@Override
	protected String getNameBasedOnIdxAndSeries(Plot.MetadataXYSeries ser, int dataIdx) {
		int branchIdx = ser.getBranchIdx();

		// Nothing special for single-stage rockets, or the first stage
		if (branchCount <= 1 || branchIdx == branchCount - 1) {
			return super.getNameBasedOnIdxAndSeries(ser, dataIdx);
		}

		// For multi-stage rockets, add the other attached stages to the series name
		String branchName = ser.getBranchName();
		branchName = branchName != null ? branchName : allBranches.get(branchIdx).getName();
		StringBuilder newBranchName = new StringBuilder(branchName);
		for (int i = branchIdx + 1; i < branchCount; i++) {
			// Get the separation time of the next stage
			double separationTime = allBranches.get(i).getSeparationTime();
			int separationIdx = allBranches.get(i).getDataIndexOfTime(separationTime);

			// If the separation time is at or after the current data index, the stage is still attached, so add the
			// stage name to the series name
			if (separationIdx != -1 && separationIdx >= dataIdx) {
				newBranchName.append(" + ").append(allBranches.get(i).getName());
			}
		}
		return newBranchName + ": " + ser.getBaseName();
	}

	private void fillEventLists(int branch,
								List<Color> eventColors, List<Image> eventImages, List<Set<FlightEvent>> eventSets) {
		Set<FlightEvent> eventSet = new HashSet<>();
		Set<FlightEvent.Type> typeSet = new HashSet<>();
		double prevTime = -100;
		Color color = null;
		Image image = null;
		int maxOrdinal = -1;
		
		for (EventDisplayInfo info : eventList) {
			if (branch >= 0 && branch != info.stage) {
				continue;
			}

			double t = info.time;
			FlightEvent event = info.event;
			FlightEvent.Type type = event.getType();
			
			if (Math.abs(t - prevTime) <= 0.05) {
				if (!typeSet.contains(type)) {
					if (type.ordinal() > maxOrdinal) {
						color = EventGraphics.getEventColor(event);
						image = EventGraphics.getEventImage(event);
						maxOrdinal = type.ordinal();
					}
					typeSet.add(type);
					eventSet.add(event);
				}

			} else {
				if (!eventSet.isEmpty()) {
					eventColors.add(color);
					eventImages.add(image);
					eventSets.add(eventSet);
				}
				prevTime = t;
				color = EventGraphics.getEventColor(event);
				image = EventGraphics.getEventImage(event);
				typeSet.clear();
				typeSet.add(type);
				eventSet = new HashSet<>();
				eventSet.add(event);
				maxOrdinal = type.ordinal();
			}
		}
		if (!eventSet.isEmpty()) {
			eventColors.add(color);
			eventImages.add(image);
			eventSets.add(eventSet);
		}
	}

	private static String constructEventLabels(Set<FlightEvent> events) {
		String text = "";

		for (FlightEvent event : events) {
			if (text != "") {
				text += ", ";
			}
			text += event.getType().toString();
		}

		return text;
	}

	private static void plotVerticalLineMarkers(XYPlot plot, List<Color> eventColors, List<Set<FlightEvent>> eventSets) {
		double markerWidth = 0.01 * plot.getDomainAxis().getUpperBound();

		// Domain time is plotted as vertical lines
		for (int i = 0; i < eventSets.size(); i++) {
			Set<FlightEvent> events = eventSets.get(i);
			double t = ((FlightEvent)events.toArray()[0]).getTime();
			String eventLabel = constructEventLabels(events);
			Color color = eventColors.get(i);

			ValueMarker m = new ValueMarker(t);
			m.setLabel(eventLabel);
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

	private void plotIconMarkers(XYPlot plot, Simulation simulation, int branch, List<Image> eventImages, List<Set<FlightEvent>> eventSets) {

		FlightDataBranch dataBranch = simulation.getSimulatedData().getBranch(branch);

		List<Double> time = dataBranch.get(FlightDataType.TYPE_TIME);
		String tName = FlightDataType.TYPE_TIME.getName();
		List<Double> domain = dataBranch.get(config.getDomainAxisType());
		LinearInterpolator domainInterpolator = new LinearInterpolator(time, domain);
		String xName = config.getDomainAxisType().getName();

		List<Axis> minMaxAxes = filledConfig.getAllAxes();

		for (int axisno = 0; axisno < data.length; axisno++) {
			// Image annotations are drawn using the data space defined by the left axis, so
			// the position of annotations on the right axis need to be mapped to the left axis
			// dataspace.
			
			double minLeft = minMaxAxes.get(0).getMinValue();
			double maxLeft = minMaxAxes.get(0).getMaxValue();
			
			double minThis = minMaxAxes.get(axisno).getMinValue();
			double maxThis = minMaxAxes.get(axisno).getMaxValue();
			
			double slope = (maxLeft - minLeft)/(maxThis - minThis);
			double intercept = (maxThis * minLeft - maxLeft * minThis)/(maxThis - minThis);
			
			XYSeriesCollection collection = data[axisno];
			for (MetadataXYSeries series : (List<MetadataXYSeries>)(collection.getSeries())) {

				if (series.getBranchIdx() != branch) {
					continue;
				}

				int dataTypeIdx = series.getDataIdx();
				FlightDataType type = config.getType(dataTypeIdx);
				String yName = type.toString();
				List<Double> range = dataBranch.get(type);
				LinearInterpolator rangeInterpolator = new LinearInterpolator(time, range);
				
				for (int i = 0; i < eventSets.size(); i++) {
					Set<FlightEvent> events = eventSets.get(i);
					double t = ((FlightEvent)events.toArray()[0]).getTime();
					Image image = eventImages.get(i);
					if (image == null) {
						continue;
					}

					double xcoord = domainInterpolator.getValue(t);
					double ycoord = rangeInterpolator.getValue(t);
				
					xcoord = config.getDomainAxisUnit().toUnit(xcoord);
					ycoord = config.getUnit(dataTypeIdx).toUnit(ycoord);
				
					if (!Double.isNaN(ycoord)) {
						// Get the sample index of the flight event. Because this can be an interpolation between two samples,
						// take the closest sample.
						final int sampleIdx;
						Optional<Double> closestSample = time.stream()
							.min(Comparator.comparingDouble(sample -> Math.abs(sample - t)));
						sampleIdx = closestSample.map(time::indexOf).orElse(-1);
						
						// Convert units
						String unitX = config.getDomainAxisUnit().getUnit();
						String unitY = series.getUnit();
						String unitT = FlightDataType.TYPE_TIME.getUnitGroup().getDefaultUnit().toString();
						String tooltipText = formatTooltip(getNameBasedOnIdxAndSeries(series, sampleIdx),
														   tName, t, unitT,
														   xName, xcoord, unitX,
														   yName, ycoord, unitY,
														   events);
						double yloc = slope * ycoord + intercept;
						
						if (!Double.isNaN(xcoord) && !Double.isNaN(ycoord)) {
							XYImageAnnotation annotation =
								new XYImageAnnotation(xcoord, yloc, image, RectangleAnchor.CENTER);
							annotation.setToolTipText(tooltipText);
							plot.addAnnotation(annotation);
						}
					}
				}
			}
		}
	}

	private List<EventDisplayInfo> buildEventInfo() {
		ArrayList<EventDisplayInfo> eventList = new ArrayList<>();

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

		eventList.sort(new Comparator<>() {

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


	private static class EventDisplayInfo {
		int stage;
		double time;
		FlightEvent event;
	}

	/**
	 * Is there really no way to set an annotation invisible?  This class provides a way
	 * to select at most one from a set of annotations and make it visible.
	 */
	private class ErrorAnnotationSet {
		private XYTitleAnnotation[] errorAnnotations;
		private XYTitleAnnotation currentAnnotation;
		private boolean visible = true;
		private int branchCount;

		protected ErrorAnnotationSet(int branches) {
			branchCount = branches;
			errorAnnotations = new XYTitleAnnotation[branchCount+1];

			for (int b = -1; b < branchCount; b++) {
				if (b < 0) {
					errorAnnotations[branchCount] = createAnnotation(b);
				} else {
					errorAnnotations[b] = createAnnotation(b);
				}
			}
			setCurrent(-1);
		}

		private XYTitleAnnotation createAnnotation(int branchNo) {

			StringBuilder abortString = new StringBuilder();

			for (int b = Math.max(0, branchNo);
				 b < ((branchNo < 0) ?
					  (simulation.getSimulatedData().getBranchCount()) :
					  (branchNo + 1)); b++) {
				FlightDataBranch branch = simulation.getSimulatedData().getBranch(b);
				FlightEvent abortEvent = branch.getFirstEvent(FlightEvent.Type.SIM_ABORT);
				if (abortEvent != null) {
					if (abortString.isEmpty()) {
						abortString = new StringBuilder(trans.get("simulationplot.abort.title"));
					}
					abortString.append("\n")
							.append(trans.get("simulationplot.abort.stage")).append(": ").append(branch.getName()).append("; ")
						.append(trans.get("simulationplot.abort.time")).append(": ").append(String.format("%.2f", abortEvent.getTime())).append(" s; ")
							.append(trans.get("simulationplot.abort.cause")).append(": ").append(((SimulationAbort) abortEvent.getData()).getMessageDescription());
				}
			}

			if (!abortString.toString().isEmpty()) {
				TextTitle abortsTitle = new TextTitle(abortString.toString(),
													  new Font(Font.SANS_SERIF, Font.BOLD, 14),
													  GUIUtil.getUITheme().getErrorColor(),
													  RectangleEdge.TOP,
													  HorizontalAlignment.LEFT, VerticalAlignment.TOP,
													  new RectangleInsets(5, 5, 5, 5));
				abortsTitle.setBackgroundPaint(Color.WHITE);
				BlockBorder abortsBorder = new BlockBorder(GUIUtil.getUITheme().getErrorColor());
				abortsTitle.setFrame(abortsBorder);

				return new XYTitleAnnotation(0.01, 0.01, abortsTitle, RectangleAnchor.BOTTOM_LEFT);
			} else {
				return null;
			}
		}

		protected void setCurrent(int branchNo) {
			XYPlot plot = chart.getXYPlot();
			// If we are currently displaying an annotation, and we want to
			// change to a new branch, stop displaying the old annotation

			XYTitleAnnotation newAnnotation = (branchNo < 0) ?
				errorAnnotations[branchCount] :
				errorAnnotations[branchNo];

			// if we're currently displaying an annotation, and it's different
			// from the new annotation, stop displaying it
			if ((currentAnnotation != null) &&
				(currentAnnotation != newAnnotation) &&
				visible) {
				plot.removeAnnotation(currentAnnotation);
			}

			// set our current annotation
			currentAnnotation = newAnnotation;

			// if visible, display it
			if ((currentAnnotation != null) && visible) {
				plot.addAnnotation(currentAnnotation);
			}
		}

		protected void setVisible(boolean v) {
			if (visible != v) {
				visible = v;
				if (currentAnnotation != null) {
					XYPlot plot = chart.getXYPlot();

					if (visible) {
						plot.addAnnotation(currentAnnotation);
					} else {
						plot.removeAnnotation(currentAnnotation);
					}
				}
			}
		}
	}
}

