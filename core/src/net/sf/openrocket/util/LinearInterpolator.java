package net.sf.openrocket.util;

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class LinearInterpolator implements Cloneable {

	private TreeMap<Double, Double> sortMap = new TreeMap<Double,Double>();

	/**
	 * Construct a <code>LinearInterpolator</code> with no points.  Some points must be
	 * added using {@link #addPoints(double[], double[])} before using the interpolator.
	 */
	public LinearInterpolator() {
	}

	/**
	 * Construct a <code>LinearInterpolator</code> with the given points.
	 * 
	 * @param x		the x-coordinates of the points.
	 * @param y		the y-coordinates of the points.
	 * @throws IllegalArgumentException		if the lengths of <code>x</code> and <code>y</code>
	 * 										are not equal.
	 * @see #addPoints(double[], double[])
	 */
	public LinearInterpolator(double[] x, double[] y) {
		addPoints(x,y);
	}

	public LinearInterpolator(List<Double> x, List<Double> y) {
		addPoints(x,y);
	}

	/**
	 * Add the point to the linear interpolation.
	 * 
	 * @param x		the x-coordinate of the point.
	 * @param y		the y-coordinate of the point.
	 */
	public void addPoint(double x, double y) {
		sortMap.put(x, y);
	}

	/**
	 * Add the points to the linear interpolation.
	 * 
	 * @param x		the x-coordinates of the points.
	 * @param y		the y-coordinates of the points.
	 * @throws IllegalArgumentException		if the lengths of <code>x</code> and <code>y</code>
	 * 										are not equal.
	 */
	public void addPoints(double[] x, double[] y) {
		if (x.length != y.length) {
			throw new IllegalArgumentException("Array lengths do not match, x="+x.length +
					" y="+y.length);
		}
		for (int i=0; i < x.length; i++) {
			sortMap.put(x[i],y[i]);
		}
	}

	public void addPoints(List<Double> x, List<Double> y){
		if (x.size() != y.size()) {
			throw new IllegalArgumentException("Array lengths do not match, x="+x.size() +
					" y="+y.size());
		}
		for (int i=0; i < x.size(); i++) {
			sortMap.put( (Double) x.toArray()[i], (Double) y.toArray()[i]);
		}
	}


	public double getValue(double x) {
		double x1, x2;
		Double y1, y2;
		// Froyo does not support floorEntry, firstEntry or higherEntry.  We instead have to
		// resort to using other more awkward methods.

		y1 = sortMap.get(x);

		if ( y1 != null ) {
			// Wow, x was a key in the map.  Such luck.
			return y1.doubleValue();
		}

		// we now know that x is not in the map, so we need to find the lower and higher keys.
		
		// let's just make certain that our map is not empty.
		if ( sortMap.isEmpty() ) {
			throw new IllegalStateException("No points added yet to the interpolator.");
		}
		
		// firstKey in the map - cannot be null since the map is not empty.
		Double firstKey = sortMap.firstKey();

		// x is smaller than the first entry in the map.
		if ( x < firstKey.doubleValue() ) {
			y1 = sortMap.get(firstKey);
			return y1.doubleValue();
		}
		
		// floor key is the largest key smaller than x - since we have at least one key,
		// and x>=firstKey, we know that floorKey != null.
		Double floorKey = sortMap.subMap(firstKey, x).lastKey();

		x1 = floorKey.doubleValue();
		y1 = sortMap.get(floorKey);

		// Now we need to find the key that is greater or equal to x
		SortedMap<Double,Double> tailMap = sortMap.tailMap(x);

		// Check if x is bigger than all the entries.
		if ( tailMap.isEmpty() ) {
			return y1.doubleValue();
		}
		Double ceilKey = tailMap.firstKey();
		
		// Check if x is bigger than all the entries.
		if ( ceilKey == null ) {
			return y1.doubleValue();
		}
		
		x2 = ceilKey.doubleValue();
		y2 = sortMap.get(ceilKey);

		return (x - x1)/(x2-x1) * (y2-y1) + y1;
	}


	public double[] getXPoints() {
		double[] x = new double[sortMap.size()];
		Iterator<Double> iter = sortMap.keySet().iterator();
		for (int i=0; iter.hasNext(); i++) {
			x[i] = iter.next();
		}
		return x;
	}


	@SuppressWarnings("unchecked")
	@Override
	public LinearInterpolator clone() {
		try {
			LinearInterpolator other = (LinearInterpolator)super.clone();
			other.sortMap = (TreeMap<Double,Double>)this.sortMap.clone();
			return other;
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException?!",e);
		}
	}

}
