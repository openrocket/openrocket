package net.sf.openrocket.android.motor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.sf.openrocket.R;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.YValueMarker;

public class BurnPlotFragment extends Fragment {

	private ExtendedThrustCurveMotor motor;
	private long motorId;

	private XYPlot mySimpleXYPlot;
	private SimpleXYSeries mySeries;

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
		View v = inflater.inflate(R.layout.motor_burn, container, false);
		mySimpleXYPlot = (XYPlot) v.findViewById(R.id.xyplot);
		init(motor);
		return v;
	}

	private static List<Double> fromArray( double[] arry ) {
		List<Double> l = new ArrayList<Double>(arry.length);
		for( double d: arry ) {
			l.add(d);
		}
		return l;
	}

	private void init( ExtendedThrustCurveMotor motor ) {

		mySimpleXYPlot.setUserDomainOrigin(0);
		mySimpleXYPlot.setUserRangeOrigin(0);
		mySimpleXYPlot.setRangeLabel("impuse (n)");
		mySimpleXYPlot.setDomainLabel("time (s)");
		YValueMarker average = new YValueMarker(motor.getThrustCurveMotor().getAverageThrustEstimate(),"average" );
		average.getLinePaint().setColor(Color.BLACK);
		average.getTextPaint().setColor(Color.BLACK);
		mySimpleXYPlot.addMarker( average );
		mySimpleXYPlot.disableAllMarkup();

		try {
			mySeries = new SimpleXYSeries( 
					fromArray(motor.getThrustCurveMotor().getTimePoints()),
					fromArray(motor.getThrustCurveMotor().getThrustPoints()), 
					motor.getThrustCurveMotor().getManufacturer().getDisplayName() + " " + motor.getThrustCurveMotor().getDesignation()
					);
		} catch ( Exception ex ) {

			Vector<Double> data = new Vector<Double>();
			data.add(0.0);
			data.add(0.0);
			data.add(1.0);
			data.add(1.0);
			mySeries = new SimpleXYSeries(data, SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED,"no data");
		}

		LineAndPointFormatter formatter= new LineAndPointFormatter(Color.GREEN, Color.GREEN, Color.GREEN);

		formatter.getLinePaint().setShadowLayer(0, 0, 0, 0);
		formatter.getVertexPaint().setShadowLayer(0, 0, 0, 0);
		mySimpleXYPlot.addSeries(mySeries, LineAndPointRenderer.class,formatter);

		//Set of internal variables for keeping track of the boundaries
		mySimpleXYPlot.calculateMinMaxVals();

		mySimpleXYPlot.redraw();

	}

}

