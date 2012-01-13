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

import java.util.List;

import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;

import org.achartengine.ChartFactory;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.XYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.Log;

/**
 * Multiple temperature demo chart.
 */
public class SimulationChart {
	
	private final static String TAG = "SimulationChart";

	private FlightDataBranch flightDataBranch;
	private FlightDataType series1;
	private FlightDataType series2;
	private final FlightDataType time = FlightDataType.TYPE_TIME;
	private List<FlightEvent> flightEvents;
	private String simulationName;

	// Define 4 different colors and point styles to use for the series.
	// For now only 2 series are supported though.
	private final static int[] colors = new int[] { Color.BLUE, Color.YELLOW, Color.GREEN, Color.RED };
	private final static PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND,
		PointStyle.TRIANGLE, PointStyle.SQUARE };

	/**
	 * @param simulationName the simulationName to set
	 */
	public void setSimulationName(String simulationName) {
		this.simulationName = simulationName;
	}

	/**
	 * @param flightDataBranch the flightDataBranch to set
	 */
	public void setFlightDataBranch(FlightDataBranch flightDataBranch) {
		this.flightDataBranch = flightDataBranch;
	}

	/**
	 * @param series1 the series1 to set
	 */
	public void setSeries1(FlightDataType series1) {
		this.series1 = series1;
	}

	/**
	 * @param series2 the series2 to set
	 */
	public void setSeries2(FlightDataType series2) {
		this.series2 = series2;
	}

	/**
	 * @param flightEvents the flightEvents to set
	 */
	public void setFlightEvents(List<FlightEvent> flightEvents) {
		this.flightEvents = flightEvents;
	}

	private static String formatFlightDataTypeAxisLabel( FlightDataType fdt ) {
		return fdt.getName() + " (" + fdt.getUnitGroup().getDefaultUnit().toString() + ")";
	}

	/**
	 * Executes the chart demo.
	 * 
	 * @param context the context
	 * @return the built intent
	 */
	public Intent execute(Context context) {

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
		renderer.setChartTitle(simulationName);

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
		List<Double> series1values = flightDataBranch.get(series1);

		// compute the axis limits using timevalues and series1values.
		double xmin = 0;
		double ymin = 0;
		renderer.setXAxisMin(xmin);
		renderer.setYAxisMin(ymin);

		double ymax = computeMaxValueWithPadding( series1values );
		double xmax = Math.ceil( timevalues.get( timevalues.size()-1));
		
		Log.d(TAG,"ymax = " + ymax);
		renderer.setXAxisMax(xmax);
		renderer.setYAxisMax(ymax);

		// These configurations don't really work well just now.
		//renderer.setPanLimits(new double[] { xmin, xmax, ymin, ymax });
		//renderer.setZoomLimits(new double[] { xmin, xmax, ymin, ymax });

		// Add first series
		addXYSeries(dataset, series1.getName(), timevalues, series1values, 0);

		if ( seriesCount > 1 ) {
			// Add second series
			addXYSeries(dataset, series2.getName(), timevalues, flightDataBranch.get(series2), 1);
		}
		Intent intent = getLineChartIntent(context, dataset, renderer,"Simulation");
		return intent;
	}

	private static void addXYSeries(XYMultipleSeriesDataset dataset, String titles, List<Double> xValues, List<Double> yValues, int scale) {
		XYSeries series = new XYSeries(titles, scale);
		int datasize = xValues.size();
		for( int i = 0; i<datasize; i++ ) {
			series.add(xValues.get(i), yValues.get(i));
		}
		dataset.addSeries(series);

	}

	private static Intent getLineChartIntent(Context context, XYMultipleSeriesDataset dataset,
			XYMultipleSeriesRenderer renderer, String activityTitle) {
		//		    checkParameters(dataset, renderer);
		Intent intent = new Intent(context, GraphicalActivity.class);
		XYChart chart = new LineChart(dataset, renderer);
		intent.putExtra(ChartFactory.CHART, chart);
		intent.putExtra(ChartFactory.TITLE, activityTitle);
		return intent;
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
