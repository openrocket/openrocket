package net.sf.openrocket.util;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An immutable class of weighted coordinates.  The weights are non-negative.
 * 
 * Can also be used as non-weighted coordinates with weight=0.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class Coordinate implements Cloneable, Serializable {
	private static final Logger log = LoggerFactory.getLogger(Coordinate.class);
	
	// Defined for backwards compatibility after adding clone().
	static final long serialVersionUID = 585574649794259293L;
	
	////////  Debug section
	/*
	 * Debugging info.  If openrocket.debug.coordinatecount is defined, a line is
	 * printed every 1000000 instantiations (or as many as defined).
	 */
	private static final boolean COUNT_DEBUG;
	private static final int COUNT_DIFF;
	static {
		String str = System.getProperty("openrocket.debug.coordinatecount");
		int diff = 0;
		if (str == null) {
			COUNT_DEBUG = false;
			COUNT_DIFF = 0;
		} else {
			COUNT_DEBUG = true;
			try {
				diff = Integer.parseInt(str);
			} catch (NumberFormatException ignore) {
			}
			if (diff < 1000)
				diff = 1000000;
			COUNT_DIFF = diff;
		}
	}
	
	private static int count = 0;
	{
		// Debug count
		if (COUNT_DEBUG) {
			synchronized (Coordinate.class) {
				count++;
				if ((count % COUNT_DIFF) == 0) {
					log.debug("Coordinate instantiated " + count + " times.");
				}
			}
		}
	}
	
	////////  End debug section
	
	
	
	public static final Coordinate NUL = new Coordinate(0, 0, 0, 0);
	public static final Coordinate NaN = new Coordinate(Double.NaN, Double.NaN,
			Double.NaN, Double.NaN);
	
	public final double x, y, z;
	public final double weight;
	
	
	private double length = -1; /* Cached when calculated */
	
	
	public Coordinate() {
		this(0, 0, 0, 0);
	}
	
	public Coordinate(double x) {
		this(x, 0, 0, 0);
	}
	
	public Coordinate(double x, double y) {
		this(x, y, 0, 0);
	}
	
	public Coordinate(double x, double y, double z) {
		this(x, y, z, 0);
	}
	
	public Coordinate(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.weight = w;
		
	}
	
	
	public boolean isWeighted() {
		return (weight != 0);
	}
	
	/**
	 * Check whether any of the coordinate values is NaN.
	 * 
	 * @return	true if the x, y, z or weight is NaN
	 */
	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || Double.isNaN(weight);
	}
	
	public Coordinate setX(double x) {
		return new Coordinate(x, this.y, this.z, this.weight);
	}
	
	public Coordinate setY(double y) {
		return new Coordinate(this.x, y, this.z, this.weight);
	}
	
	public Coordinate setZ(double z) {
		return new Coordinate(this.x, this.y, z, this.weight);
	}
	
	public Coordinate setWeight(double weight) {
		return new Coordinate(this.x, this.y, this.z, weight);
	}
	
	public Coordinate setXYZ(Coordinate c) {
		return new Coordinate(c.x, c.y, c.z, this.weight);
	}
	
	
	/**
	 * Add the coordinate and weight of two coordinates.
	 * 
	 * @param other  the other <code>Coordinate</code>
	 * @return		 the sum of the coordinates
	 */
	public Coordinate add(Coordinate other) {
		return new Coordinate(this.x + other.x, this.y + other.y, this.z + other.z,
				this.weight + other.weight);
	}
	
	public Coordinate add(double x1, double y1, double z1) {
		return new Coordinate(this.x + x1, this.y + y1, this.z + z1, this.weight);
	}
	
	public Coordinate add(double x1, double y1, double z1, double w1) {
		return new Coordinate(this.x + x1, this.y + y1, this.z + z1, this.weight + w1);
	}
	
	/**
	 * Subtract a Coordinate from this Coordinate.  The weight of the resulting Coordinate
	 * is the same as of this Coordinate, the weight of the argument is ignored.
	 * 
	 * @param other  Coordinate to subtract from this.
	 * @return  The result
	 */
	public Coordinate sub(Coordinate other) {
		return new Coordinate(this.x - other.x, this.y - other.y, this.z - other.z, this.weight);
	}
	
	/**
	 * Subtract the specified values from this Coordinate.  The weight of the result
	 * is the same as the weight of this Coordinate.
	 * 
	 * @param x1   	x value to subtract
	 * @param y1	y value to subtract
	 * @param z1	z value to subtract
	 * @return		the result.
	 */
	public Coordinate sub(double x1, double y1, double z1) {
		return new Coordinate(this.x - x1, this.y - y1, this.z - z1, this.weight);
	}
	
	
	/**
	 * Multiply the <code>Coordinate</code> with a scalar.  All coordinates and the
	 * weight are multiplied by the given scalar.

	 * @param m  Factor to multiply by.
	 * @return   The product. 
	 */
	public Coordinate multiply(double m) {
		return new Coordinate(this.x * m, this.y * m, this.z * m, this.weight * m);
	}
	
	/**
	 * Dot product of two Coordinates, taken as vectors.  Equal to
	 * x1*x2+y1*y2+z1*z2
	 * @param other  Coordinate to take product with.
	 * @return   The dot product.
	 */
	public double dot(Coordinate other) {
		return this.x * other.x + this.y * other.y + this.z * other.z;
	}
	
	/**
	 * Dot product of two Coordinates.
	 */
	public static double dot(Coordinate v1, Coordinate v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}
	
	/**
	 * Cross product of two Coordinates taken as vectors
	 */
	public Coordinate cross(Coordinate other) {
		return cross(this, other);
	}
	
	/**
	 * Cross product of two Coordinates taken as vectors
	 */
	public static Coordinate cross(Coordinate a, Coordinate b) {
		return new Coordinate(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
	}
	
	/**
	 * Distance from the origin to the Coordinate.
	 */
	public double length() {
		if (length < 0) {
			length = MathUtil.safeSqrt(x * x + y * y + z * z);
		}
		return length;
	}
	
	/**
	 * Square of the distance from the origin to the Coordinate.
	 */
	public double length2() {
		return x * x + y * y + z * z;
	}
	
	
	/**
	 * Return the largest of the absolute values of the coordinates.  This can be
	 * used as a norm of the vector that is faster to calculate than the
	 * 2-norm.
	 * 
	 * @return	the largest absolute value of (x,y,z)
	 */
	public double max() {
		return MathUtil.max(Math.abs(x), Math.abs(y), Math.abs(z));
	}
	
	
	/**
	 * Returns a new coordinate which has the same direction from the origin as this
	 * coordinate but is at a distance of one.  If this coordinate is the origin,
	 * this method throws an <code>IllegalStateException</code>.  The weight of the
	 * coordinate is unchanged.
	 * 
	 * @return   the coordinate normalized to distance one of the origin.
	 * @throws   IllegalStateException  if this coordinate is the origin.
	 */
	public Coordinate normalize() {
		double l = length();
		if (l < 0.0000001) {
			throw new IllegalStateException("Cannot normalize zero coordinate");
		}
		return new Coordinate(x / l, y / l, z / l, weight);
	}
	
	
	
	
	/**
	 * Weighted average of two coordinates.  If either of the weights are positive,
	 * the result is the weighted average of the coordinates and the weight is the sum
	 * of the original weights.  If the sum of the weights is zero (and especially if
	 * both of the weights are zero), the result is the unweighted average of the 
	 * coordinates with weight zero.
	 * <p>
	 * If <code>other</code> is <code>null</code> then this <code>Coordinate</code> is
	 * returned.
	 */
	public Coordinate average(Coordinate other) {
		double x1, y1, z1, w1;
		
		if (other == null)
			return this;
		
		w1 = this.weight + other.weight;
		if (Math.abs(w1) < MathUtil.pow2(MathUtil.EPSILON)) {
			x1 = (this.x + other.x) / 2;
			y1 = (this.y + other.y) / 2;
			z1 = (this.z + other.z) / 2;
			w1 = 0;
		} else {
			x1 = (this.x * this.weight + other.x * other.weight) / w1;
			y1 = (this.y * this.weight + other.y * other.weight) / w1;
			z1 = (this.z * this.weight + other.z * other.weight) / w1;
		}
		return new Coordinate(x1, y1, z1, w1);
	}
	
	
	/**
	 * Tests whether the coordinates are the equal.
	 * 
	 * @param other  Coordinate to compare to.
	 * @return  true if the coordinates are equal (x, y, z and weight)
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Coordinate))
			return false;
		
		final Coordinate c = (Coordinate) other;
		return (MathUtil.equals(this.x, c.x) &&
				MathUtil.equals(this.y, c.y) &&
				MathUtil.equals(this.z, c.z) && MathUtil.equals(this.weight, c.weight));
	}
	
	/**
	 * Hash code method compatible with {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return (int) ((x + y + z) * 100000);
	}
	
	
	@Override
	public String toString() {
		if (isWeighted())
			return String.format("(%.3f,%.3f,%.3f,w=%.3f)", x, y, z, weight);
		else
			return String.format("(%.3f,%.3f,%.3f)", x, y, z);
	}
	
	@Override
	public Coordinate clone() {
		return new Coordinate(this.x, this.y, this.z, this.weight);
	}
	
}
