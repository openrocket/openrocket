package net.sf.openrocket.android.motor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.sf.openrocket.R;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.YValueMarker;

public class BurnPlotFragment extends Fragment implements OnTouchListener {

	private ExtendedThrustCurveMotor motor;
	private long motorId;

	private XYPlot mySimpleXYPlot;
	private SimpleXYSeries mySeries;
	private PointF minXY;
	private PointF maxXY;

	private float absMinX;
	private float absMaxX;

	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;

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
		mySimpleXYPlot.setOnTouchListener(this);
		mScaleDetector = new ScaleGestureDetector(v.getContext(), new ScaleListener());
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

		LineAndPointFormatter formatter= new LineAndPointFormatter(Color.RED, Color.GREEN, Color.GREEN);

		formatter.getLinePaint().setShadowLayer(0, 0, 0, 0);
		formatter.getVertexPaint().setShadowLayer(0, 0, 0, 0);
		mySimpleXYPlot.addSeries(mySeries, LineAndPointRenderer.class,formatter);

		//Set of internal variables for keeping track of the boundaries
		mySimpleXYPlot.calculateMinMaxVals();

		mySimpleXYPlot.redraw();

		minXY=new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(),mySimpleXYPlot.getCalculatedMinY().floatValue());
		maxXY=new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(),mySimpleXYPlot.getCalculatedMaxY().floatValue());

		absMinX = minXY.x;
		absMaxX = maxXY.x;

	}

	private float mPosX;
	private float mPosY;

	private float mLastTouchX;
	private float mLastTouchY;

	private int mActivePointerId = -1;

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		mScaleDetector.onTouchEvent(event);

		final int action = event.getAction();
		switch ( action & MotionEvent.ACTION_MASK ) {
		case MotionEvent.ACTION_DOWN: {
			final float x = event.getX();
			final float y = event.getY();

			mLastTouchX = x;
			mLastTouchY = y;

			mActivePointerId = event.getPointerId(0);
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = event.findPointerIndex(mActivePointerId);
			final float x = event.getX(pointerIndex);
			final float y = event.getY(pointerIndex);

			if (!mScaleDetector.isInProgress()) {
				final float dx = x - mLastTouchX;
				final float dy = y - mLastTouchY;

				mPosX += dx;
				mPosY += dy;
				scroll(dx);
				// do scroll.

			}
			mLastTouchX = x;
			mLastTouchY = y;

			break;
		}

		case MotionEvent.ACTION_UP: {
			mActivePointerId = -1;
			break;
		}

		case MotionEvent.ACTION_CANCEL: {
			mActivePointerId = -1;
			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {
			final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			final int pointerId = event.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				// This was our active pointer going up.  choose a new active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex ==0 ? 1:0;
				mLastTouchX = event.getX(newPointerIndex);
				mLastTouchY = event.getY(newPointerIndex);
				mActivePointerId = event.getPointerId(newPointerIndex);
			}
			break;
		}
		}	
		return true;
	}

	private void zoom(float scale) {
		AndroidLogWrapper.d(BurnPlotFragment.class,"zoom by " + scale);
		float domainSpan = absMaxX	- absMinX;
		AndroidLogWrapper.d(BurnPlotFragment.class,"domainSpan = " + domainSpan);
		float domainMidPoint = absMaxX		- domainSpan / 2.0f;
		AndroidLogWrapper.d(BurnPlotFragment.class,"domainMidPoint = " + domainMidPoint);
		float offset = domainSpan / scale;
		AndroidLogWrapper.d(BurnPlotFragment.class,"offset " + offset);
		minXY.x=domainMidPoint- offset;
		AndroidLogWrapper.d(BurnPlotFragment.class,"min X " + minXY.x);
		maxXY.x=domainMidPoint+offset;
		AndroidLogWrapper.d(BurnPlotFragment.class,"max X " + maxXY.x);
		checkBoundaries();
		mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.AUTO);
		mySimpleXYPlot.redraw();
	}

	private void scroll(float pan) {
		float domainSpan = maxXY.x	- minXY.x;
		float step = domainSpan / mySimpleXYPlot.getWidth();
		float offset = pan * step;
		minXY.x+= offset;
		maxXY.x+= offset;
		checkBoundaries();
		mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.AUTO);
		mySimpleXYPlot.redraw();
	}

	private void checkBoundaries() {

		if ( minXY.x < absMinX) 
			minXY.x = absMinX;
		//		else if ( minXY.x > maxNoError )
		//			minXY.x = maxNoError;

		if ( maxXY.x > absMaxX)
			maxXY.x = absMaxX;
		//		else if ( maxXY.x < minNoError)
		//			maxXY.x = minNoError;
	}
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale( ScaleGestureDetector detector ) {
			mScaleFactor *= detector.getScaleFactor();

			mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 5.0f));
			zoom(mScaleFactor);
			return true;
		}
	}
}

