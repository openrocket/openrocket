package net.sf.openrocket.util;

import java.io.Serializable;

/**
 * An immutable class of weighted coordinates.  The weights are non-negative.
 * 
 * Can also be used as non-weighted coordinates with weight=0.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class MutableCoordinate implements Serializable {
	public static final MutableCoordinate NUL = new MutableCoordinate(0,0,0,0);
	public static final MutableCoordinate NaN = new MutableCoordinate(Double.NaN,Double.NaN,
			Double.NaN,Double.NaN);
	public static final double COMPARISON_DELTA = 0.000001;
	private double x,y,z;
	private double weight;
	
	
	/* Count and report the number of times a Coordinate is constructed: */
//	private static int count=0;
//	{
//		count++;
//		if ((count % 1000) == 0) {
//			System.out.println("Coordinate instantiated "+count+" times");
//		}
//	}
	

	public MutableCoordinate() {
		x=0;
		y=0;
		z=0;
		weight=0;
	}
	
	public MutableCoordinate(double x) {
		this.x = x;
		this.y = 0;
		this.z = 0;
		weight = 0;
	}
	
	public MutableCoordinate(double x, double y) {
		this.x = x;
		this.y = y;
		this.z = 0;
		weight = 0;
	}
	
	public MutableCoordinate(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		weight = 0;
	}
	public MutableCoordinate(double x, double y, double z, double w) {
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
	
	public MutableCoordinate setX(double x) {
		return new MutableCoordinate(x,this.y,this.z,this.weight);
	}
	
	public MutableCoordinate setY(double y) {
		return new MutableCoordinate(this.x,y,this.z,this.weight);
	}
	
	public MutableCoordinate setZ(double z) {
		return new MutableCoordinate(this.x,this.y,z,this.weight);
	}
	
	public MutableCoordinate setWeight(double weight) {
		return new MutableCoordinate(this.x, this.y, this.z, weight);
	}
	
	public MutableCoordinate setXYZ(MutableCoordinate c) {
		return new MutableCoordinate(c.x, c.y, c.z, this.weight);
	}
	
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getZ() {
		return z;
	}

	
	/**
	 * Add the coordinate and weight of two coordinates.
	 * 
	 * @param other  the other <code>Coordinate</code>
	 * @return		 the sum of the coordinates
	 */
	public MutableCoordinate add(MutableCoordinate other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		this.weight += other.weight;
		return this;
	}
	
	public MutableCoordinate add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public MutableCoordinate add(double x, double y, double z, double weight) {
		return new MutableCoordinate(this.x+x, this.y+y, this.z+z, this.weight+weight);
	}

	/**
	 * Subtract a Coordinate from this Coordinate.  The weight of the resulting Coordinate
	 * is the same as of this Coordinate, the weight of the argument is ignored.
	 * 
	 * @param other  Coordinate to subtract from this.
	 * @return  The result
	 */
	public MutableCoordinate sub(MutableCoordinate other) {
		return new MutableCoordinate(this.x-other.x, this.y-other.y, this.z-other.z, this.weight);
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
	public MutableCoordinate sub(double x, double y, double z) {
		return new MutableCoordinate(this.x - x, this.y - y, this.z - z, this.weight);
	}
	
	
	/**
	 * Multiply the <code>Coordinate</code> with a scalar.  All coordinates and the
	 * weight are multiplied by the given scalar.

	 * @param m  Factor to multiply by.
	 * @return   The product. 
	 */
	public MutableCoordinate multiply(double m) {
		return new MutableCoordinate(this.x*m, this.y*m, this.z*m, this.weight*m);
	}

	/**
	 * Dot product of two Coordinates, taken as vectors.  Equal to
	 * x1*x2+y1*y2+z1*z2
	 * @param other  Coordinate to take product with.
	 * @return   The dot product.
	 */
	public double dot(MutableCoordinate other) {
		return this.x*other.x + this.y*other.y + this.z*other.z;
	}
	/**
	 * Dot product of two Coordinates.
	 */
	public static double dot(MutableCoordinate v1, MutableCoordinate v2) {
		return v1.x*v2.x + v1.y*v2.y + v1.z*v2.z;
	}

	/**
	 * Distance from the origin to the Coordinate.
	 */
	public double length() {
		return Math.sqrt(x*x+y*y+z*z);
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
	public MutableCoordinate normalize() {
		double l = length();
		if (l < 0.0000001) {
			throw new IllegalStateException("Cannot normalize zero coordinate");
		}
		return new MutableCoordinate(x/l, y/l, z/l, weight);
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
	public MutableCoordinate average(MutableCoordinate other) {
		double x,y,z,w;
		
		if (other == null)
			return this;
		
		w = this.weight + other.weight;
		if (MathUtil.equals(w, 0)) {
			x = (this.x+other.x)/2;
			y = (this.y+other.y)/2;
			z = (this.z+other.z)/2;
			w = 0;
		} else {
			x = (this.x*this.weight + other.x*other.weight)/w;
			y = (this.y*this.weight + other.y*other.weight)/w;
			z = (this.z*this.weight + other.z*other.weight)/w;
		}
		return new MutableCoordinate(x,y,z,w);
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
		if (!(other instanceof MutableCoordinate))
			return false;
		
		final MutableCoordinate c = (MutableCoordinate)other;
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
	
	
	
	public static void main(String[] arg) {
		double a=1.2;
		double x;
		MutableCoordinate c;
		long t1, t2;
		
		x = 0;
		t1 = System.nanoTime();
		for (int i=0; i < 100000000; i++) {
			x = x + a;
		}
		t2 = System.nanoTime();
		System.out.println("Value: "+x);
		System.out.println("Plain addition: "+ ((t2-t1+500000)/1000000) + " ms");
		
		c = MutableCoordinate.NUL;
		t1 = System.nanoTime();
		for (int i=0; i < 100000000; i++) {
			c = c.add(a,0,0);
		}
		t2 = System.nanoTime();
		System.out.println("Value: "+c.x);
		System.out.println("Coordinate addition: "+ ((t2-t1+500000)/1000000) + " ms");
		
	}
	
}
