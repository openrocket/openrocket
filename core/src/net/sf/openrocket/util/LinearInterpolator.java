package net.sf.openrocket.util;

import java.util.Iterator;
import java.util.Map;
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
	
	
	
	public double getValue(double x) {
		Map.Entry<Double,Double> e1, e2;
		double x1, x2;
		double y1, y2;
		
		e1 = sortMap.floorEntry(x);
		
		if (e1 == null) {
			// x smaller than any value in the set
			e1 = sortMap.firstEntry();
			if (e1 == null) {
				throw new IllegalStateException("No points added yet to the interpolator.");
			}
			return e1.getValue();
		}
		
		x1 = e1.getKey();
		e2 = sortMap.higherEntry(x1);

		if (e2 == null) {
			// x larger than any value in the set
			return e1.getValue();
		}
		
		x2 = e2.getKey();
		y1 = e1.getValue();
		y2 = e2.getValue();
		
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

	
	public static void main(String[] args) {
		LinearInterpolator interpolator = new LinearInterpolator(
				new double[] {1, 1.5, 2, 4, 5},
				new double[] {0, 1,   0, 2, 2}
		);
		
		for (double x=0; x < 6; x+=0.1) {
			System.out.printf("%.1f:  %.2f\n", x, interpolator.getValue(x));
		}
	}
	
}
