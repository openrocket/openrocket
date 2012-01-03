package net.sf.openrocket.android.simulation;

import java.util.List;
import java.util.Vector;

import net.sf.openrocket.R;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.androidplot.xy.ValueMarker;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.XYPlot;

public class SimulationPlotFragment extends Fragment implements OnTouchListener {

	private final static String TAG = "SimulationPlot";

	private XYPlot mySimpleXYPlot;
	private SimpleXYSeries mySeries;
	private PointF minXY;
	private PointF maxXY;
	
	private float absMinX;
	private float absMaxX;
	private float minNoError;
	private float maxNoError;

	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG,"onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG,"onCreateView");
		View v = inflater.inflate(R.layout.motor_burn, container, false);
		mySimpleXYPlot = (XYPlot) v.findViewById(R.id.xyplot);
		mySimpleXYPlot.setOnTouchListener(this);
		mScaleDetector = new ScaleGestureDetector(v.getContext(), new ScaleListener());
		//		Motor motor = getMotor();
		//		init(motor);
		return v;
	}

	void init( FlightDataBranch data, FlightDataType selectedSeries, List<FlightEvent> eventsToShow ) {

		mySimpleXYPlot.clear();
		
		if ( data == null || selectedSeries == null || eventsToShow == null ) {
			return;
		}
		
		mySimpleXYPlot.setUserDomainOrigin(0);
		mySimpleXYPlot.setUserRangeOrigin(0);
		mySimpleXYPlot.setRangeLabel("");
		mySimpleXYPlot.setDomainLabel(FlightDataType.TYPE_TIME.getUnitGroup().getDefaultUnit().toString());
		mySimpleXYPlot.setRangeLabel( selectedSeries.getUnitGroup().getDefaultUnit().toString() ); 
		mySimpleXYPlot.disableAllMarkup();

		for ( FlightEvent event : eventsToShow ) {
			XValueMarker xmarker = new XValueMarker( event.getTime(), event.getType().toString() );
			xmarker.setTextOrientation( ValueMarker.TextOrientation.VERTICAL );
			mySimpleXYPlot.addMarker( xmarker );
		}
		
		List<Double> yvals = null;
		List<Double> xvals = null;
		try {
			yvals = data.get(selectedSeries);
			xvals = data.get(FlightDataType.TYPE_TIME);
			Log.d("plot","data = " + yvals);
		} catch ( Exception ex ) {
			Log.d(TAG, "Exception: " + ex);
		}
		if ( yvals == null || yvals.size() == 0 ) {
			yvals = new Vector<Double>();
			yvals.add(0.0);
			yvals.add(0.0);
			yvals.add(1.0);
			yvals.add(1.0);
		}
		Log.d("plot","data = " + yvals.toString());

		mySeries = new SimpleXYSeries(xvals, yvals, FlightDataType.TYPE_ALTITUDE.toString());

		mySimpleXYPlot.addSeries(mySeries, LineAndPointRenderer.class,
				new LineAndPointFormatter(Color.rgb(0, 255, 0), Color.rgb(200, 0, 0), null));

		//Set of internal variables for keeping track of the boundaries
		mySimpleXYPlot.calculateMinMaxVals();
	
		mySimpleXYPlot.redraw();

		minXY=new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(),mySimpleXYPlot.getCalculatedMinY().floatValue());
		maxXY=new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(),mySimpleXYPlot.getCalculatedMaxY().floatValue());

		absMinX = minXY.x;
		absMaxX = maxXY.x;
		
		minNoError = Math.round(mySeries.getX(1).floatValue() +2);
		maxNoError = Math.round(mySeries.getX(mySeries.size() -1).floatValue()) - 2.0f;
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
		Log.d(TAG,"zoom by " + scale);
		float domainSpan = absMaxX	- absMinX;
		Log.d(TAG,"domainSpan = " + domainSpan);
		float domainMidPoint = absMaxX		- domainSpan / 2.0f;
		Log.d(TAG,"domainMidPoint = " + domainMidPoint);
		float offset = domainSpan / scale;
		Log.d(TAG,"offset " + offset);
		minXY.x=domainMidPoint- offset;
		Log.d(TAG,"min X " + minXY.x);
		maxXY.x=domainMidPoint+offset;
		Log.d(TAG,"max X " + maxXY.x);
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

