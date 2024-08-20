package info.openrocket.swing.gui.plot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.util.LinearInterpolator;

import org.jfree.chart.annotations.XYImageAnnotation;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

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
		List<Double> eventTimes = new ArrayList<>();
		List<String> eventLabels = new ArrayList<>();
		List<Color> eventColors = new ArrayList<>();
		List<Image> eventImages = new ArrayList<>();

		// Plot the markers
		if (config.getDomainAxisType() == FlightDataType.TYPE_TIME && !preferences.getBoolean(ApplicationPreferences.MARKER_STYLE_ICON, false)) {
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
		Set<FlightEvent.Type> typeSet = new HashSet<>();
		double prevTime = -100;
		String text = null;
		Color color = null;
		Image image = null;
		int maxOrdinal = -1;
		for (EventDisplayInfo info : eventList) {
			if (branch >= 0 && branch != info.stage) {
				continue;
			}

			double t = info.time;
			FlightEvent.Type type = info.event.getType();
			if (Math.abs(t - prevTime) <= 0.05) {
				if (!typeSet.contains(type)) {
					text = text + ", " + type.toString();
					if (type.ordinal() > maxOrdinal) {
						color = EventGraphics.getEventColor(type);
						image = EventGraphics.getEventImage(type);
						maxOrdinal = type.ordinal();
					}
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
				maxOrdinal = type.ordinal();
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
				if (filledConfig.getAxis(index) != Util.PlotAxisSelection.LEFT.getValue()) {
					continue;
				}

				double ycoord = rangeInterpolator.getValue(t);
				if (!Double.isNaN(ycoord)) {
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
							.append(trans.get("simulationplot.abort.time")).append(": ").append(abortEvent.getTime()).append(" s; ")
							.append(trans.get("simulationplot.abort.cause")).append(": ").append(((SimulationAbort) abortEvent.getData()).getMessageDescription());
				}
			}

			if (!abortString.toString().isEmpty()) {
				TextTitle abortsTitle = new TextTitle(abortString.toString(),
													  new Font(Font.SANS_SERIF, Font.BOLD, 14), Color.RED,
													  RectangleEdge.TOP,
													  HorizontalAlignment.LEFT, VerticalAlignment.TOP,
													  new RectangleInsets(5, 5, 5, 5));
				abortsTitle.setBackgroundPaint(Color.WHITE);
				BlockBorder abortsBorder = new BlockBorder(Color.RED);
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

