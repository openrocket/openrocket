package net.sf.openrocket.util;

import static net.sf.openrocket.util.MathUtil.pow2;
public final class Inertia {

	private Inertia() {
	}
	
	
	/**
	 * Return the rotational unit moment of inertia of a solid cylinder.
	 * 
	 * @param radius	the radius of the cylinder.
	 */
	public static double filledCylinderRotational(double radius) {
		return pow2(radius) / 2;
	}
	
	/**
	 * Return the longitudinal unit moment of inertia of a solid cylinder,
	 * relative to the midpoint lengthwise.
	 * 
	 * @param radius	the radius of the cylinder.
	 * @param length	the total length of the cylinder (reference at midpoint)
	 */
	public static double filledCylinderLongitudinal(double radius, double length) {
		return (3*pow2(radius) + pow2(length))/12;
	}
	
	
	/**
	 * Return the unit moment of inertia that is shifted from the CG of an object
	 * by a specified distance.  The rotation axis are parallel.
	 * 
	 * @param cgInertia		the unit moment of inertia through the CG of the object
	 * @param distance		the distance to shift the rotation axis
	 */
	public static double shift(double cgInertia, double distance) {
		return cgInertia + pow2(distance);
	}
	
}
