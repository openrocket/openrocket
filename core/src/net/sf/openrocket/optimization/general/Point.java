package net.sf.openrocket.optimization.general;

import java.util.Arrays;

import net.sf.openrocket.util.MathUtil;

/**
 * An immutable n-dimensional coordinate point.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class Point {
	
	private final double[] point;
	private double length = -1;
	private double length2 = -1;
	
	
	/**
	 * Create a new point with all values zero.
	 * 
	 * @param dim	the dimensionality of the point
	 */
	public Point(int dim) {
		if (dim <= 0) {
			throw new IllegalArgumentException("Invalid dimensionality " + dim);
		}
		point = new double[dim];
	}
	
	
	/**
	 * Create a new point filled with a specific value.
	 * 
	 * @param dim		the dimensionality of the point
	 * @param value		the value for all dimensions
	 */
	public Point(int dim, double value) {
		this(dim);
		Arrays.fill(point, value);
	}
	
	
	/**
	 * Create a new point with specific values.
	 * 
	 * @param value		the values of the dimensions.
	 */
	public Point(double... value) {
		if (value.length == 0) {
			throw new IllegalArgumentException("Zero-dimensional point not allowed");
		}
		point = value.clone();
	}
	
	
	/**
	 * Create a copy of a point.  Used locally to create copies.
	 * 
	 * @param p		the point to copy.
	 */
	private Point(Point p) {
		point = p.point.clone();
	}
	
	

	/**
	 * Return the point dimensionality.
	 * 
	 * @return	the point dimensionality
	 */
	public int dim() {
		return point.length;
	}
	
	

	public double get(int i) {
		return point[i];
	}
	
	public Point set(int i, double v) {
		Point p = new Point(this);
		p.point[i] = v;
		return p;
	}
	
	
	/**
	 * Return a new point that is the sum of two points.
	 * 
	 * @param other		the point to add to this point.
	 * @return			the sum of these points.
	 */
	public Point add(Point other) {
		Point p = new Point(this);
		for (int i = 0; i < point.length; i++) {
			p.point[i] += other.point[i];
		}
		return p;
	}
	
	
	/**
	 * Return a new point that is the subtraction of two points.
	 * 
	 * @param other		the point to subtract from this point.
	 * @return			the value of this - other.
	 */
	public Point sub(Point other) {
		Point p = new Point(this);
		for (int i = 0; i < point.length; i++) {
			p.point[i] -= other.point[i];
		}
		return p;
	}
	
	
	/**
	 * Return this point multiplied by a scalar value.
	 * 
	 * @param v		the scalar to multiply with
	 * @return		the scaled point
	 */
	public Point mul(double v) {
		Point p = new Point(this);
		for (int i = 0; i < point.length; i++) {
			p.point[i] *= v;
		}
		return p;
	}
	
	
	/**
	 * Return the length of this coordinate.
	 * 
	 * @return	the length.
	 */
	public double length() {
		if (length < 0) {
			length = MathUtil.safeSqrt(length2());
		}
		return length;
	}
	
	
	/**
	 * Return the squared length of this coordinate.
	 * 
	 * @return	the square of the length of thie coordinate.
	 */
	public double length2() {
		if (length2 < 0) {
			length2 = 0;
			for (double p : point) {
				length2 += p * p;
			}
		}
		return length2;
	}
	
	

	/**
	 * Return the point as an array.
	 * 
	 * @return	the point as an array.
	 */
	public double[] asArray() {
		return point.clone();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (!(obj instanceof Point))
			return false;
		
		Point other = (Point) obj;
		if (this.point.length != other.point.length)
			return false;
		
		for (int i = 0; i < point.length; i++) {
			if (!MathUtil.equals(this.point[i], other.point[i]))
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int n = 0;
		for (double d : point) {
			n *= 37;
			n += (int) (d * 1000);
		}
		return n;
	}
	
	@Override
	public String toString() {
		return "Point" + Arrays.toString(point);
	}
}
