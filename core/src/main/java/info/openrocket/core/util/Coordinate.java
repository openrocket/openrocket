package info.openrocket.core.util;

import java.io.Serializable;

/**
 * An interface for weighted 3D coordinates.
 *
 * The weight is a non-negative value that can be used for weighted averages
 * of coordinates.  A weight of zero indicates an unweighted coordinate.
 **/
public interface Coordinate extends Cloneable, Serializable {
	/**
	 * Get the X coordinate.
	 * @return the x coordinate
	 */
	double getX();

	/**
	 * Set the X coordinate.
	 * @param x the x coordinate
	 * @return the coordinate with modified x value
	 */
	Coordinate setX(double x);

	/**
	 * Get the Y coordinate.
	 * @return the y coordinate
	 */
	double getY();

	/**
	 * Set the Y coordinate.
	 * @param y the y coordinate
	 * @return the coordinate with modified y value
	 */
	Coordinate setY(double y);

	/**
	 * Get the Z coordinate.
	 * @return the z coordinate
	 */
	double getZ();

	/**
	 * Set the Z coordinate.
	 * @param z the z coordinate
	 * @return the coordinate with modified z value
	 */
	Coordinate setZ(double z);

	/**
	 * Get the weight.
	 * @return the weight
	 */
	double getWeight();

	/**
	 * Set the weight.
	 * @param weight the weight
	 * @return the coordinate with modified weight
	 */
	Coordinate setWeight(double weight);

	/**
	 * Set the x, y, z coordinates from another Coordinate.
	 * @param c the other Coordinate
	 * @return the coordinate with modified x, y, z values (weight unchanged)
	 */
	Coordinate setXYZ(Coordinate c);

	/**
	 * Check whether the coordinate is weighted (weight > 0).
	 * @return true if the weight is greater than zero
	 */
	default boolean isWeighted() {
		return getWeight() > 0.0;
	}

	/**
	 * Check whether any of the coordinate values is NaN.
	 *
	 * @return	true if the x, y, z or weight is NaN
	 */
	default boolean isNaN() {
		return Double.isNaN(getX()) || Double.isNaN(getY()) || Double.isNaN(getZ()) || Double.isNaN(getWeight());
	}

	/**
	 * Distance from the origin to the Coordinate.
	 */
	default double length() {
		return MathUtil.safeSqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
	}

	/**
	 * Square of the distance from the origin to the Coordinate.
	 */
	default double length2() {
		final double x = getX(), y = getY(), z = getZ();
		return x*x + y*y + z*z;
	}

	/**
	 * Add the coordinate and weight of two coordinates.
	 *
	 * @param other  the other <code>Coordinate</code>
	 * @return		 the sum of the coordinates
	 */
	Coordinate add(Coordinate other);

	Coordinate add(double x1, double y1, double z1);

	Coordinate add(double x1, double y1, double z1, double w1);

	/**
	 * Scale the coordinate and add to this coordinate.
	 * @param coord the coordinate to scale and add
	 * @param scale the scale factor
	 * @return the result
	 */
	Coordinate addScaled(Coordinate coord, double scale);

	/**
	 * Subtract a Coordinate from this Coordinate.  The weight of the resulting Coordinate
	 * is the same as of this Coordinate; i.e. the weight of the argument is ignored.
	 *
	 * @param other  Coordinate to subtract from this.
	 * @return  The result
	 */
	Coordinate sub(Coordinate other);

	/**
	 * Subtract the specified values from this Coordinate.  The weight of the result
	 * is the same as the weight of this Coordinate.
	 *
	 * @param x1   	x value to subtract
	 * @param y1	y value to subtract
	 * @param z1	z value to subtract
	 * @return		the result.
	 */
	Coordinate sub(double x1, double y1, double z1);

	/**
	 * Multiply the <code>Coordinate</code> with a scalar.  All coordinates and the
	 * weight are multiplied by the given scalar.

	 * @param m  Factor to multiply by.
	 * @return   The product.
	 */
	Coordinate multiply(double m);

	/**
	 * Multiply the <code>Coordinate</code> with another <Coordinate> component-by-component
	 *
	 * @param other the other Coordinate
	 * @return   The product.
	 */
	Coordinate multiply(Coordinate other);

	/**
	 * Dot product of two Coordinates, taken as vectors.  Equal to
	 * x1*x2+y1*y2+z1*z2
	 * @param other  Coordinate to take product with.
	 * @return   The dot product.
	 */
	default double dot(Coordinate other) {
		return getX()*other.getX() + getY()*other.getY() + getZ()*other.getZ();
	}

	/**
	 * Dot product of two Coordinates.
	 */
	static double dot(Coordinate v1, Coordinate v2) {
		return v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ();
	}

	/**
	 * Cross product of two Coordinates taken as vectors
	 */
	Coordinate cross(Coordinate other);

	/**
	 * Returns a new coordinate which has the same direction from the origin as this
	 * coordinate but is at a distance of one.  If this coordinate is the origin,
	 * this method throws an <code>IllegalStateException</code>.  The weight of the
	 * coordinate is unchanged.
	 *
	 * @return   the coordinate normalized to distance one of the origin.
	 * @throws   IllegalStateException  if this coordinate is the origin.
	 */
	Coordinate normalize();

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
	Coordinate average(Coordinate other);

	/**
	 * Interpolate between two coordinates.  The fraction is the weight of the other coordinate.
	 * @param other Coordinate to interpolate to.
	 * @param fraction Interpolation fraction (0 = this, 1 = other).
	 * @return Interpolated coordinate.
	 */
	Coordinate interpolate(Coordinate other, double fraction);

	/**
	 * Return the largest of the absolute values of the coordinates.  This can be
	 * used as a norm of the vector that is faster to calculate than the
	 * 2-norm.
	 *
	 * @return	the largest absolute value of (x,y,z)
	 */
	default double max() {
		return MathUtil.max(Math.abs(getX()), Math.abs(getY()), Math.abs(getZ()));
	}

	/** Convenience to obtain an immutable snapshot. */
	default ImmutableCoordinate toImmutable() {
		return new ImmutableCoordinate(getX(), getY(), getZ(), getWeight());
	}

	/**
	 * High-precision output, for use with verifying calculations
 	 */
	default String toPreciseString() {
		return String.format("cm= %.8fg @[%.8f,%.8f,%.8f]", getWeight(), getX(), getY(), getZ());
	}

	Coordinate clone();
}
