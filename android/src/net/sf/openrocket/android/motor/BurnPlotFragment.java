package net.sf.openrocket.android.motor;

import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.util.AndroidLogWrapper;

import org.achartengine.GraphicalView;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.XYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BurnPlotFragment extends Fragment {

	private ExtendedThrustCurveMotor motor;
	private long motorId;

	/** The encapsulated graphical view. */
	private GraphicalView mView;
	/** The chart to be drawn. */
	private XYChart mChart;

	public static BurnPlotFragment newInstance( long motorId ) {
		BurnPlotFragment frag = new BurnPlotFragment();
		Bundle bundle = new Bundle();
		bundle.putLong("motorId", motorId);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		AndroidLogWrapper.d(BurnPlotFragment.class,"onCreate");

		if ( savedInstanceState!= null) {
			motorId = savedInstanceState.getLong("motorId",-1);
		} else {
			Bundle b = getArguments();
			motorId = b.getLong("motorId");
		}

		DbAdapter mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();

		try {
			motor = mDbHelper.getMotorDao().fetchMotor(motorId);
		} catch ( Exception e ) {

		}

		mDbHelper.close();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("motorId", motorId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		AndroidLogWrapper.d(BurnPlotFragment.class,"onCreateView");
		
		init(motor);
		mView = new GraphicalView(container.getContext(), mChart);
		return mView;
	}

	private void init( ExtendedThrustCurveMotor motor ) {

		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(1);

		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setXLabels(10);
		renderer.setYLabels(10);
		renderer.setShowGrid(true);
		renderer.setZoomButtonsVisible(false);
		renderer.setZoomEnabled(false,false);
		renderer.setPanEnabled(false,false);
		renderer.setMargins(new int[] { 50, 40, 10, 20 });
		renderer.setShowLegend(false);
		renderer.setAxesColor(Color.LTGRAY);
		renderer.setLabelsColor(Color.LTGRAY);

		renderer.setChartTitle(motor.getManufacturer() + " " + motor.getDesignation());

		renderer.setXTitle("time (s)");
		renderer.setXLabelsAlign(Align.RIGHT);

		renderer.setYTitle("impuse (n)");
		renderer.setYLabelsAlign(Align.RIGHT,0);

		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.RED);
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillPoints(true);
		renderer.addSeriesRenderer(r);
		// setting the YAximMin to 0 locks the origins.
		renderer.setYAxisMin(0.0, 0);

		// TODO get markers working in achartengine
		//YValueMarker average = new YValueMarker(motor.getThrustCurveMotor().getAverageThrustEstimate(),"average" );
		//average.getLinePaint().setColor(Color.BLACK);
		//average.getTextPaint().setColor(Color.BLACK);
		//mySimpleXYPlot.addMarker( average );

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		XYSeries series = new XYSeries(motor.getDesignation(), 0);
		
		double[] timePoints = motor.getTimePoints();
		double[] thrustPoints = motor.getThrustPoints();

		// We are going to abuse this loop to also compute the Y axis max.
		int maxy = 0;
		int datasize = timePoints.length;
		for( int i = 0; i<datasize; i++ ) {
			series.add(timePoints[i], thrustPoints[i]);
			int ceil =(int) Math.ceil(thrustPoints[i]);
			if ( ceil > maxy ) {
				maxy = ceil;
			}
		}
		renderer.setYAxisMax(maxy);
		
		// Find the X axis max.  compute it as next larger integer if t_max > 2 else round up to next tenth.
		double maxx = timePoints[datasize-1];
		if ( maxx >= 2.0 ) {
			maxx = Math.ceil(maxx);
		} else {
			maxx = Math.ceil(maxx*10.0) /10.0;
		}
		renderer.setXAxisMax(maxx);
		
		dataset.addSeries(series);

		mChart = new LineChart(dataset, renderer);

	}

}

