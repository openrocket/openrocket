package info.openrocket.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An immutable quaternion class.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Quaternion implements Cloneable {
	private static final Logger log = LoggerFactory.getLogger(Quaternion.class);

	//////// Debug section
	/*
	 * Debugging info. If openrocket.debug.quaternioncount is defined, a line is
	 * printed every 1000000 instantiations (or as many as defined).
	 */
	private static final boolean COUNT_DEBUG;
	private static final int COUNT_DIFF;
	static {
		String str = System.getProperty("openrocket.debug.quaternioncount");
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
			synchronized (Quaternion.class) {
				count++;
				if ((count % COUNT_DIFF) == 0) {
					log.debug("Quaternion instantiated " + count + " times.");
				}
			}
		}
	}

	//////// End debug section

	private final double w, x, y, z;
	private double norm = -1;

	/**
	 * Construct a new "one" quaternion. This is equivalent to
	 * <code>new Quaternion(1,0,0,0)</code>.
	 */
	public Quaternion() {
		this(1, 0, 0, 0);
	}

	public Quaternion(double w, double x, double y, double z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Create a rotation quaternion corresponding to the rotation vector. The
	 * rotation axis is
	 * the direction of vector, and rotation angle is the length of the vector.
	 * <p>
	 * The cost of the operation is approximately that of computing the length of
	 * the coordinate
	 * and computing two trigonometric functions.
	 * 
	 * @param rotation the rotation vector
	 * @return the quaternion corresponding to the rotation vector
	 */
	public static Quaternion rotation(Coordinate rotation) {
		double length = rotation.length();
		if (length < 0.000001) {
			return new Quaternion(1, 0, 0, 0);
		}
		double sin = Math.sin(length / 2);
		double cos = Math.cos(length / 2);
		return new Quaternion(cos,
				sin * rotation.getX() / length, sin * rotation.getY() / length, sin * rotation.getZ() / length);
	}

	/**
	 * Create a rotation quaternion corresponding to the rotation around the
	 * provided vector with
	 * the provided angle.
	 * 
	 * @param axis  the rotation axis
	 * @param angle the rotation angle
	 * @return the corresponding quaternion
	 */
	public static Quaternion rotation(Coordinate axis, double angle) {
		Coordinate a = axis.normalize();
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		return new Quaternion(cos, sin * a.getX(), sin * a.getY(), sin * a.getZ());
	}

	public double getW() {
		return w;
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
	 * Check whether any of the quaternion values is NaN.
	 * 
	 * @return true if w, x, y or z is NaN
	 */
	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || Double.isNaN(w);
	}

	/**
	 * Multiply this quaternion by the other quaternion from the right side. This
	 * calculates the product <code>result = this * other</code>.
	 * 
	 * @param other the quaternion to multiply this quaternion by.
	 * @return this quaternion.
	 */
	public Quaternion multiplyRight(Quaternion other) {
		double newW = (this.w * other.w - this.getX() * other.getX() - this.getY() * other.getY() - this.getZ() * other.getZ());
		double newX = (this.w * other.getX() + this.getX() * other.w + this.getY() * other.getZ() - this.getZ() * other.getY());
		double newY = (this.w * other.getY() + this.getY() * other.w + this.getZ() * other.getX() - this.getX() * other.getZ());
		double newZ = (this.w * other.getZ() + this.getZ() * other.w + this.getX() * other.getY() - this.getY() * other.getX());

		return new Quaternion(newW, newX, newY, newZ);
	}

	/**
	 * Multiply this quaternion by the other quaternion from the left side. This
	 * calculates the product <code>result = other * this</code>.
	 * 
	 * @param other the quaternion to multiply this quaternion by.
	 * @return this quaternion.
	 */
	public Quaternion multiplyLeft(Quaternion other) {
		/* other(abcd) * this(wxyz) */

		double newW = (other.w * this.w - other.getX() * this.getX() - other.getY() * this.getY() - other.getZ() * this.getZ());
		double newX = (other.w * this.getX() + other.getX() * this.w + other.getY() * this.getZ() - other.getZ() * this.getY());
		double newY = (other.w * this.getY() + other.getY() * this.w + other.getZ() * this.getX() - other.getX() * this.getZ());
		double newZ = (other.w * this.getZ() + other.getZ() * this.w + other.getX() * this.getY() - other.getY() * this.getX());

		return new Quaternion(newW, newX, newY, newZ);
	}

	/**
	 * Return a normalized version of this quaternion. If this quaternion is the
	 * zero quaternion, throws
	 * <code>IllegalStateException</code>.
	 * 
	 * @return a normalized version of this quaternion.
	 * @throws IllegalStateException if the norm of this quaternion is zero.
	 */
	public Quaternion normalize() {
		double n = norm();
		if (n < 0.0000001) {
			throw new IllegalStateException("attempting to normalize zero-quaternion");
		}
		return new Quaternion(w / n, x / n, y / n, z / n);
	}

	/**
	 * Normalize the quaternion if the norm is more than 1ppm from one.
	 * 
	 * @return this quaternion or a normalized version of this quaternion.
	 * @throws IllegalStateException if the norm of this quaternion is zero.
	 */
	public Quaternion normalizeIfNecessary() {
		double n2 = norm2();
		if (n2 < 0.999999 || n2 > 1.000001) {
			return normalize();
		} else {
			return this;
		}
	}

	/**
	 * Return the norm of this quaternion.
	 * 
	 * @return the norm of this quaternion sqrt(w^2 + x^2 + y^2 + z^2).
	 */
	public double norm() {
		if (norm < 0) {
			norm = MathUtil.safeSqrt(x * x + y * y + z * z + w * w);
		}
		return norm;
	}

	/**
	 * Return the square of the norm of this quaternion.
	 * 
	 * @return the square of the norm of this quaternion (w^2 + x^2 + y^2 + z^2).
	 */
	public double norm2() {
		return x * x + y * y + z * z + w * w;
	}

	/**
	 * Perform a coordinate rotation using this unit quaternion. The result is
	 * <code>this * coord * this^(-1)</code>.
	 * <p>
	 * This method assumes that the norm of this quaternion is one.
	 * 
	 * @param coord the coordinate to rotate.
	 * @return the rotated coordinate.
	 */
	public Coordinate rotate(Coordinate coord) {
		double a, b, c, d;

		assert (Math.abs(norm2() - 1) < 0.00001) : "Quaternion not unit length: " + this;

		// (a,b,c,d) = this * coord = (w,x,y,z) * (0,cx,cy,cz)
		a = -x * coord.getX() - y * coord.getY() - z * coord.getZ(); // w
		b = w * coord.getX() + y * coord.getZ() - z * coord.getY(); // x i
		c = w * coord.getY() - x * coord.getZ() + z * coord.getX(); // y j
		d = w * coord.getZ() + x * coord.getY() - y * coord.getX(); // z k

		// return = (a,b,c,d) * (this)^-1 = (a,b,c,d) * (w,-x,-y,-z)

		// Assert that the w-value is zero
		assert (Math.abs(a * w + b * x + c * y + d * z) <= coord.max() * MathUtil.EPSILON)
				: ("Should be zero: " + (a * w + b * x + c * y + d * z) + " in " + this + " c=" + coord);

		return new ImmutableCoordinate(
				-a * x + b * w - c * z + d * y,
				-a * y + b * z + c * w - d * x,
				-a * z - b * y + c * x + d * w,
				coord.getWeight());
	}

	/**
	 * Perform an inverse coordinate rotation using this unit quaternion. The result
	 * is
	 * <code>this^(-1) * coord * this</code>.
	 * <p>
	 * This method assumes that the norm of this quaternion is one.
	 * 
	 * @param coord the coordinate to rotate.
	 * @return the rotated coordinate.
	 */
	public Coordinate invRotate(Coordinate coord) {
		double a, b, c, d;

		assert (Math.abs(norm2() - 1) < 0.00001) : "Quaternion not unit length: " + this;

		// (a,b,c,d) = (this)^-1 * coord = (w,-x,-y,-z) * (0,cx,cy,cz)
		a = x * coord.getX() + y * coord.getY() + z * coord.getZ();
		b = w * coord.getX() - y * coord.getZ() + z * coord.getY();
		c = w * coord.getY() + x * coord.getZ() - z * coord.getX();
		d = w * coord.getZ() - x * coord.getY() + y * coord.getX();

		// return = (a,b,c,d) * this = (a,b,c,d) * (w,x,y,z)
		assert (Math.abs(a * w - b * x - c * y - d * z) < Math.max(coord.max(), 1) * MathUtil.EPSILON)
				: ("Should be zero: " + (a * w - b * x - c * y - d * z) + " in " + this + " c=" + coord);

		return new ImmutableCoordinate(
				a * x + b * w + c * z - d * y,
				a * y - b * z + c * w + d * x,
				a * z + b * y - c * x + d * w,
				coord.getWeight());
	}

	/**
	 * Rotate the coordinate (0,0,1) using this quaternion. The result is returned
	 * as a Coordinate. This method is equivalent to calling
	 * <code>q.rotate(new ImmutableCoordinate(0,0,1))</code> but requires only about half of
	 * the
	 * multiplications.
	 * 
	 * @return The coordinate (0,0,1) rotated using this quaternion.
	 */
	public Coordinate rotateZ() {
		return new ImmutableCoordinate(
				2 * (w * y + x * z),
				2 * (y * z - w * x),
				w * w - x * x - y * y + z * z);
	}

	@Override
	public String toString() {
		return String.format("Quaternion[%f,%f,%f,%f,norm=%f]", w, x, y, z, this.norm());
	}


	/**
	 * Creates and returns a copy of this quaternion.
	 * Being an immutable class, this method returns a new Quaternion instance
	 * containing the same w, x, y, z component values as this quaternion.
	 *
	 * @return a new Quaternion instance with identical component values
	 */
	@Override
    public Quaternion clone() {
		return new Quaternion(w, x, y, z);
	}


}
