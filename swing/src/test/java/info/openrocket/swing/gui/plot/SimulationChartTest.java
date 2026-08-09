package info.openrocket.swing.gui.plot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.BasicStroke;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.jupiter.api.Test;

import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.componentanalysis.CADomainDataType;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.swing.gui.dialogs.componentanalysis.CAPlot;
import info.openrocket.swing.gui.dialogs.componentanalysis.CAPlotConfiguration;
import info.openrocket.swing.util.BaseTestCase;

/**
 * Tests interactions with data series in simulation and component-analysis charts.
 */
public class SimulationChartTest extends BaseTestCase {

	/**
	 * Highlighting one component-analysis legend item must only widen that component's series.
	 */
	@Test
	public void componentAnalysisLegendHighlightsOnlyMatchingSeries() {
		BodyTube firstComponent = new BodyTube();
		firstComponent.setName("Body Tube");
		BodyTube secondComponent = new BodyTube();
		secondComponent.setName("Body Tube");

		CADataBranch branch = new CADataBranch("Analysis", CADomainDataType.MACH, CADataType.TOTAL_CD);
		addAnalysisPoint(branch, firstComponent, secondComponent, 0.1, 0.2, 0.3);
		addAnalysisPoint(branch, firstComponent, secondComponent, 0.2, 0.25, 0.35);

		CAPlotConfiguration configuration = new CAPlotConfiguration("Test", CADomainDataType.MACH);
		configuration.addPlotDataType(CADataType.TOTAL_CD, 0);
		configuration.setPlotDataComponents(0, List.of(firstComponent, secondComponent));
		CAPlot componentAnalysisPlot = new CAPlot("Test", branch, configuration,
				Collections.singletonList(branch), false);
		SimulationChart chart = new SimulationChart(componentAnalysisPlot.getJFreeChart());

		XYPlot plot = chart.getChart().getXYPlot();
		XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset(0);
		XYItemRenderer renderer = plot.getRenderer(0);
		float firstBaseWidth = ((BasicStroke) renderer.getSeriesStroke(0)).getLineWidth();
		float secondBaseWidth = ((BasicStroke) renderer.getSeriesStroke(1)).getLineWidth();

		LegendTitle legend = chart.getChart().getLegend();
		LegendItemCollection legendItems = legend.getSources()[0].getLegendItems();
		Comparable<?> firstLegendKey = legendItems.get(0).getSeriesKey();
		Comparable<?> secondLegendKey = legendItems.get(1).getSeriesKey();
		assertEquals(legendItems.get(0).getLabel(), legendItems.get(1).getLabel());
		assertNotEquals(firstLegendKey, secondLegendKey);

		chart.applyLegendHighlight(Set.of(firstLegendKey));

		assertEquals(firstBaseWidth * 3.0f,
				((BasicStroke) renderer.getSeriesStroke(0)).getLineWidth());
		assertEquals(secondBaseWidth,
				((BasicStroke) renderer.getSeriesStroke(1)).getLineWidth());
	}

	/**
	 * Simulation branches for different stages share one legend item and must continue to highlight together.
	 */
	@Test
	public void simulationLegendHighlightsAllBranchesOfMatchingDataSeries() {
		FlightDataBranch firstBranch = createFlightDataBranch("Sustainer", 100);
		FlightDataBranch secondBranch = createFlightDataBranch("Booster", 50);
		SimulationPlotConfiguration configuration = new SimulationPlotConfiguration("Test", FlightDataType.TYPE_TIME);
		configuration.addPlotDataType(FlightDataType.TYPE_ALTITUDE, 0);
		TestPlot simulationPlot = new TestPlot(firstBranch, configuration,
				List.of(firstBranch, secondBranch));
		SimulationChart chart = new SimulationChart(simulationPlot.getJFreeChart());

		XYPlot plot = chart.getChart().getXYPlot();
		XYItemRenderer renderer = plot.getRenderer(0);
		float firstBaseWidth = ((BasicStroke) renderer.getSeriesStroke(0)).getLineWidth();
		float secondBaseWidth = ((BasicStroke) renderer.getSeriesStroke(1)).getLineWidth();
		Comparable<?> legendKey = chart.getChart().getLegend().getSources()[0].getLegendItems()
				.get(0).getSeriesKey();

		chart.applyLegendHighlight(Set.of(legendKey));

		assertEquals(firstBaseWidth * 3.0f,
				((BasicStroke) renderer.getSeriesStroke(0)).getLineWidth());
		assertEquals(secondBaseWidth * 3.0f,
				((BasicStroke) renderer.getSeriesStroke(1)).getLineWidth());
	}

	private static void addAnalysisPoint(CADataBranch branch, BodyTube firstComponent,
			BodyTube secondComponent, double mach, double firstValue, double secondValue) {
		branch.addPoint();
		branch.setDomainValue(CADomainDataType.MACH, mach);
		branch.setValue(CADataType.TOTAL_CD, firstComponent, firstValue);
		branch.setValue(CADataType.TOTAL_CD, secondComponent, secondValue);
	}

	private static FlightDataBranch createFlightDataBranch(String name, double altitude) {
		FlightDataBranch branch = new FlightDataBranch(name, FlightDataType.TYPE_TIME,
				FlightDataType.TYPE_ALTITUDE);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, altitude);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 1);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, altitude + 10);
		return branch;
	}

	/**
	 * Minimal concrete plot used to exercise the shared simulation-series behavior.
	 */
	private static class TestPlot extends Plot<FlightDataType, FlightDataBranch, SimulationPlotConfiguration> {
		TestPlot(FlightDataBranch mainBranch, SimulationPlotConfiguration configuration,
				List<FlightDataBranch> allBranches) {
			super("Test", mainBranch, configuration, allBranches, false);
		}
	}
}
