package net.sf.openrocket.util;

import java.io.Serializable;

/**
 * An immutable class of weighted coordinates.  The weights are non-negative.
 * 
 * Can also be used as non-weighted coordinates with weight=0.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class Coordinate implements Serializable {
	
	////////  Debug section
	/*
	 * Debugging info.  If openrocket.debug.coordinatecount is defined, a line is
	 * printed every 1000000 instantiations (or as many as defined).
	 */
	private static final boolean COUNT_DEBUG;
	private static final int COUNT_DIFF;
	static {
		String str = System.getProperty("openrocket.debug.coordinatecount", null);
		int diff = 0;
		if (str == null) {
			COUNT_DEBUG = false;
			COUNT_DIFF = 0;
		} else {
			COUNT_DEBUG = true;
			try {
				diff = Integer.parseInt(str);
			} catch (NumberFormatException ignore) { }
			if (diff < 1000)
				diff = 1000000;
			COUNT_DIFF = diff;
		}
	}
	
	private static int count = 0;
	{
		// Debug count
		if (COUNT_DEBUG) {
			count++;
			if ((count % COUNT_DIFF) == 0) {
				System.out.println("Coordinate instantiated " + count + " times.");
			}
		}
	}
	
	////////  End debug section
	
	
	
	public static final Coordinate NUL = new Coordinate(0,0,0,0);
	public static final Coordinate NaN = new Coordinate(Double.NaN,Double.NaN,
			Double.NaN,Double.NaN);

	public final double x,y,z;
	public final double weight;
	
	
	private double length = -1;  /* Cached when calculated */
	
	
	

	public Coordinate() {
		this(0,0,0,0);
	}
	
	public Coordinate(double x) {
		this(x,0,0,0);
	}
	
	public Coordinate(double x, double y) {
		this(x,y,0,0);
	}
	
	public Coordinate(double x, double y, double z) {
		this(x,y,z,0);
	}
	public Coordinate(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.weight=w;
		
	}

	
	public boolean isWeighted() {
		return (weight != 0);
	}
	
	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || Double.isNaN(weight);
	}
	
	public Coordinate setX(double x) {
		return new Coordinate(x,this.y,this.z,this.weight);
	}
	
	public Coordinate setY(double y) {
		return new Coordinate(this.x,y,this.z,this.weight);
	}
	
	public Coordinate setZ(double z) {
		return new Coordinate(this.x,this.y,z,this.weight);
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
		return new Coordinate(this.x+other.x, this.y+other.y, this.z+other.z, 
				this.weight+other.weight);
	}
	
	public Coordinate add(double x, double y, double z) {
		return new Coordinate(this.x+x, this.y+y, this.z+z, this.weight);
	}

	public Coordinate add(double x, double y, double z, double weight) {
		return new Coordinate(this.x+x, this.y+y, this.z+z, this.weight+weight);
	}

	/**
	 * Subtract a Coordinate from this Coordinate.  The weight of the resulting Coordinate
	 * is the same as of this Coordinate, the weight of the argument is ignored.
	 * 
	 * @param other  Coordinate to subtract from this.
	 * @return  The result
	 */
	public Coordinate sub(Coordinate other) {
		return new Coordinate(this.x-other.x, this.y-other.y, this.z-other.z, this.weight);
	}

	/**
	 * Subtract the specified values from this Coordinate.  The weight of the result
	 * is the same as the weight of this Coordinate.
	 * 
	 * @param x   	x value to subtract
	 * @param y		y value to subtract
	 * @param z		z value to subtract
	 * @return		the result.
	 */
	public Coordinate sub(double x, double y, double z) {
		return new Coordinate(this.x - x, this.y - y, this.z - z, this.weight);
	}
	
	
	/**
	 * Multiply the <code>Coordinate</code> with a scalar.  All coordinates and the
	 * weight are multiplied by the given scalar.

	 * @param m  Factor to multiply by.
	 * @return   The product. 
	 */
	public Coordinate multiply(double m) {
		return new Coordinate(this.x*m, this.y*m, this.z*m, this.weight*m);
	}

	/**
	 * Dot product of two Coordinates, taken as vectors.  Equal to
	 * x1*x2+y1*y2+z1*z2
	 * @param other  Coordinate to take product with.
	 * @return   The dot product.
	 */
	public double dot(Coordinate other) {
		return this.x*other.x + this.y*other.y + this.z*other.z;
	}
	/**
	 * Dot product of two Coordinates.
	 */
	public static double dot(Coordinate v1, Coordinate v2) {
		return v1.x*v2.x + v1.y*v2.y + v1.z*v2.z;
	}

	/**
	 * Distance from the origin to the Coordinate.
	 */
	public double length() {
		if (length < 0) {
			length = Math.sqrt(x*x+y*y+z*z); 
		}
		return length;
	}
	
	/**
	 * Square of the distance from the origin to the Coordinate.
	 */
	public double length2() {
		return x*x+y*y+z*z;
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
		return new Coordinate(x/l, y/l, z/l, weight);
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
		double x,y,z,w;
		
		if (other == null)
			return this;
		
		w = this.weight + other.weight;
		if (Math.abs(w) < MathUtil.pow2(MathUtil.EPSILON)) {
			x = (this.x+other.x)/2;
			y = (this.y+other.y)/2;
			z = (this.z+other.z)/2;
			w = 0;
		} else {
			x = (this.x*this.weight + other.x*other.weight)/w;
			y = (this.y*this.weight + other.y*other.weight)/w;
			z = (this.z*this.weight + other.z*other.weight)/w;
		}
		return new Coordinate(x,y,z,w);
	}
	
	
	/**
	 * Tests whether the coordinates (not weight!) are the same.
	 * 
	 * Compares only the (x,y,z) coordinates, NOT the weight.  Coordinate comparison is
	 * done to the precision of COMPARISON_DELTA.
	 * 
	 * @param other  Coordinate to compare to.
	 * @return  true if the coordinates are equal
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Coordinate))
			return false;
		
		final Coordinate c = (Coordinate)other;
		return (MathUtil.equals(this.x, c.x) &&
				MathUtil.equals(this.y, c.y) &&
				MathUtil.equals(this.z, c.z));
	}
	
	/**
	 * Hash code method compatible with {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return (int)((x+y+z)*100000);
	}
	
	
	@Override
	public String toString() {
		if (isWeighted())
			return String.format("(%.3f,%.3f,%.3f,w=%.3f)", x,y,z,weight);
		else
			return String.format("(%.3f,%.3f,%.3f)", x,y,z);
	}
	
	
}
