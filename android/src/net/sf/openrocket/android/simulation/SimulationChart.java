/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.openrocket.android.simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.unit.Unit;

import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.XYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * This is really a flyweight object so we can serialize the
 * values behind a simulation chart.  Since OpenRocketDocument, FlightDataBranch,
 * FlightDataType, Unit and all the other underlying types are not serializable,
 * we have to resort to persisting just the bare minimum of information.
 * 
 * This also means without further changes to FlightDataType, we cannot actually
 * restore the displayed series.
 * 
 * TODO make FlightDataBranch serializable or at least reconstructable from
 * from some the name.
 * 
 */
public class SimulationChart implements Serializable {

	private final int simulationIndex;
	private transient FlightDataType series1;
	private transient FlightDataType series2;
	private transient List<FlightEvent> events;

	// Define 4 different colors and point styles to use for the series.
	// For now only 2 series are supported though.
	private final static int[] colors = new int[] { Color.BLUE, Color.YELLOW, Color.GREEN, Color.RED };
	private final static PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND,
		PointStyle.TRIANGLE, PointStyle.SQUARE };

	public SimulationChart(int simulationIndex) {
		super();
		this.simulationIndex = simulationIndex;
	}

	private static String formatFlightDataTypeAxisLabel( FlightDataType fdt ) {
		return fdt.getName() + " (" + fdt.getUnitGroup().getDefaultUnit().toString() + ")";
	}

	public void setSeries1(FlightDataType series1) {
		this.series1 = series1;
	}

	public FlightDataType getSeries1() {
		return series1;
	}

	public void setSeries2(FlightDataType series2) {
		this.series2 = series2;
	}

	public FlightDataType getSeries2() {
		return series2;
	}

	public void setEvents( List<FlightEvent> events ) {
		this.events = events;
	}

	public List<FlightEvent> getEvents() {
		return events;
	}

	public FlightDataBranch getFlightDataBranch( OpenRocketDocument rocketDocument ) {
		Simulation sim = rocketDocument.getSimulation(simulationIndex);
		FlightDataBranch flightDataBranch = sim.getSimulatedData().getBranch(0);
		return flightDataBranch;
	}
	/**
	 * Executes the chart demo.
	 * 
	 * @param context the context
	 * @return the built intent
	 */
	public XYChart buildChart(OpenRocketDocument rocketDocument) {

		Simulation sim = rocketDocument.getSimulation(simulationIndex);
		FlightDataBranch flightDataBranch = sim.getSimulatedData().getBranch(0);
		FlightDataType time = FlightDataType.TYPE_TIME;
		if (series1== null) {
			series1 = flightDataBranch.getTypes()[1];
		}
		if (series2== null) {
			series2 = flightDataBranch.getTypes()[2];
		}

		if ( events == null ) {
			events = new ArrayList<FlightEvent>();
			for ( FlightEvent event : flightDataBranch.getEvents() ) {
				switch( event.getType()) {
				case LAUNCHROD:
				case APOGEE:
				case BURNOUT:
				case EJECTION_CHARGE:
					events.add(event);
				default:
					break;
				}
			}
		}

		/*
		 * TODO -
		 * Figure out why you can pan all over the place even where there are no visible points.
		 */
		int seriesCount = 2;
		// if the same series is selected twice, only plot it once.
		if ( series1 == series2 ) {
			seriesCount = 1;
		}

		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(seriesCount);

		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setShowGrid(true);
		renderer.setZoomButtonsVisible(true);
		renderer.setChartTitle(sim.getName());
		renderer.setShowCustomTextGrid(true);
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setXLabelsAngle(90);  // rotate right
		for( FlightEvent event : events ) {
			renderer.addXTextLabel(event.getTime(), event.getType().toString());
		}

		renderer.setMargins(new int[] { 50, 30, 0, 20 });
		{
			for (int i = 0; i < seriesCount; i++) {
				XYSeriesRenderer r = new XYSeriesRenderer();
				r.setColor(colors[i]);
				r.setPointStyle(styles[i]);
				r.setFillPoints(true);
				renderer.addSeriesRenderer(r);
				// setting the YAximMin to 0 locks the origins.
				renderer.setYAxisMin(0.0, i);
			}
		}

		renderer.setXTitle(formatFlightDataTypeAxisLabel(time));
		renderer.setXLabelsAlign(Align.RIGHT);

		renderer.setYTitle(formatFlightDataTypeAxisLabel(series1),0);
		renderer.setYLabelsAlign(Align.RIGHT,0);

		if ( seriesCount > 1 ) {
			renderer.setYTitle(formatFlightDataTypeAxisLabel(series2), 1);
			renderer.setYAxisAlign(Align.RIGHT, 1);
			renderer.setYLabelsAlign(Align.LEFT, 1);
		}

		renderer.setAxesColor(Color.LTGRAY);
		renderer.setLabelsColor(Color.LTGRAY);

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		List<Double> timevalues = flightDataBranch.get(time);
		List<Double> series1values = new ArrayList<Double>( flightDataBranch.get(series1).size() );
		{
			Unit u = series1.getUnitGroup().getDefaultUnit();
			for( Double d: flightDataBranch.get(series1) ) {
				series1values.add( u.toUnit(d));
			}
		}

		// compute the axis limits using timevalues and series1values.
		double xmin = 0;
		double ymin = 0;
		renderer.setXAxisMin(xmin);
		renderer.setYAxisMin(ymin);

		double ymax = computeMaxValueWithPadding( series1values );
		double xmax = Math.ceil( timevalues.get( timevalues.size()-1));

		AndroidLogWrapper.d(SimulationChart.class,"ymax = " + ymax);
		renderer.setXAxisMax(xmax);
		renderer.setYAxisMax(ymax);

		// These configurations don't really work well just now.
		//renderer.setPanLimits(new double[] { xmin, xmax, ymin, ymax });
		//renderer.setZoomLimits(new double[] { xmin, xmax, ymin, ymax });

		// Add first series
		addXYSeries(dataset, series1.getName(), timevalues, series1values, 0);

		if ( seriesCount > 1 ) {
			// Add second series
			List<Double> series2values = new ArrayList<Double>( flightDataBranch.get(series2).size() );
			{
				Unit u = series2.getUnitGroup().getDefaultUnit();
				for( Double d: flightDataBranch.get(series2) ) {
					series2values.add( u.toUnit(d));
				}
			}

			addXYSeries(dataset, series2.getName(), timevalues, series2values, 1);
		}
		XYChart chart = new LineChart(dataset, renderer);

		return chart;
	}

	private static void addXYSeries(XYMultipleSeriesDataset dataset, String titles, List<Double> xValues, List<Double> yValues, int scale) {
		XYSeries series = new XYSeries(titles, scale);
		int datasize = xValues.size();
		for( int i = 0; i<datasize; i++ ) {
			series.add(xValues.get(i), yValues.get(i));
		}
		dataset.addSeries(series);

	}

	private static double computeMaxValueWithPadding( List<Double> list ) {
		double max = list.get(0);
		for( double v : list ) {
			if ( v > max ) {
				max = v;
			}
		}
		if ( max <= 0 ) return 1.0;

		// Do something stupid.
		// return:
		//  10 if max <= 10
		//  next 10 if 10 < max < 1000
		//  next 100 if 1000 < max < 10,000
		//  next 1000 if max >= 10,000
		double numdigits = Math.floor(Math.log10(max));

		if ( numdigits <= 1.0 ) {
			return 10.0;
		} else if ( numdigits <= 3.0 ) {
			return 10.0 * ( Math.ceil( max/10.0));
		} else if ( numdigits <= 4.0 ) {
			return 100.0 * ( Math.ceil( max/ 100.0) );
		} else {
			return 1000.0 * ( Math.ceil( max / 1000.0 ));
		}

	}

}
