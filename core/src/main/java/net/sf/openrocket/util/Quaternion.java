package net.sf.openrocket.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An immutable quaternion class.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Quaternion {
	private static final Logger log = LoggerFactory.getLogger(Quaternion.class);
	

	////////  Debug section
	/*
	 * Debugging info.  If openrocket.debug.quaternioncount is defined, a line is
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
	
	////////  End debug section
	


	private final double w, x, y, z;
	private double norm = -1;
	
	
	/**
	 * Construct a new "one" quaternion.  This is equivalent to <code>new Quaternion(1,0,0,0)</code>.
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
	 * Create a rotation quaternion corresponding to the rotation vector.  The rotation axis is
	 * the direction of vector, and rotation angle is the length of the vector.
	 * <p>
	 * The cost of the operation is approximately that of computing the length of the coordinate
	 * and computing two trigonometric functions.
	 * 
	 * @param rotation	the rotation vector
	 * @return			the quaternion corresponding to the rotation vector
	 */
	public static Quaternion rotation(Coordinate rotation) {
		double length = rotation.length();
		if (length < 0.000001) {
			return new Quaternion(1, 0, 0, 0);
		}
		double sin = Math.sin(length / 2);
		double cos = Math.cos(length / 2);
		return new Quaternion(cos,
				sin * rotation.x / length, sin * rotation.y / length, sin * rotation.z / length);
	}
	
	/**
	 * Create a rotation quaternion corresponding to the rotation around the provided vector with
	 * the provided angle.
	 * 
	 * @param axis		the rotation axis
	 * @param angle		the rotation angle
	 * @return			the corresponding quaternion
	 */
	public static Quaternion rotation(Coordinate axis, double angle) {
		Coordinate a = axis.normalize();
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		return new Quaternion(cos, sin * a.x, sin * a.y, sin * a.z);
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
	 * @return	true if w, x, y or z is NaN
	 */
	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || Double.isNaN(w);
	}
	
	
	/**
	 * Multiply this quaternion by the other quaternion from the right side.  This
	 * calculates the product  <code>result = this * other</code>.
	 * 
	 * @param other   the quaternion to multiply this quaternion by.
	 * @return		  this quaternion.
	 */
	public Quaternion multiplyRight(Quaternion other) {
		double newW = (this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z);
		double newX = (this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y);
		double newY = (this.w * other.y + this.y * other.w + this.z * other.x - this.x * other.z);
		double newZ = (this.w * other.z + this.z * other.w + this.x * other.y - this.y * other.x);
		
		return new Quaternion(newW, newX, newY, newZ);
	}
	
	/**
	 * Multiply this quaternion by the other quaternion from the left side.  This
	 * calculates the product  <code>result = other * this</code>.
	 * 
	 * @param other   the quaternion to multiply this quaternion by.
	 * @return		  this quaternion.
	 */
	public Quaternion multiplyLeft(Quaternion other) {
		/*  other(abcd) * this(wxyz)  */

		double newW = (other.w * this.w - other.x * this.x - other.y * this.y - other.z * this.z);
		double newX = (other.w * this.x + other.x * this.w + other.y * this.z - other.z * this.y);
		double newY = (other.w * this.y + other.y * this.w + other.z * this.x - other.x * this.z);
		double newZ = (other.w * this.z + other.z * this.w + other.x * this.y - other.y * this.x);
		
		return new Quaternion(newW, newX, newY, newZ);
	}
	
	



	/**
	 * Return a normalized version of this quaternion.  If this quaternion is the zero quaternion, throws
	 * <code>IllegalStateException</code>.
	 * 
	 * @return   a normalized version of this quaternion.
	 * @throws   IllegalStateException  if the norm of this quaternion is zero.
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
	 * @return	this quaternion or a normalized version of this quaternion.
	 * @throws   IllegalStateException  if the norm of this quaternion is zero.
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
	 * @return   the norm of this quaternion sqrt(w^2 + x^2 + y^2 + z^2).
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
	 * @return	the square of the norm of this quaternion (w^2 + x^2 + y^2 + z^2).
	 */
	public double norm2() {
		return x * x + y * y + z * z + w * w;
	}
	
	
	/**
	 * Perform a coordinate rotation using this unit quaternion.  The result is
	 * <code>this * coord * this^(-1)</code>.
	 * <p>
	 * This method assumes that the norm of this quaternion is one.
	 * 
	 * @param coord		the coordinate to rotate.
	 * @return			the rotated coordinate.
	 */
	public Coordinate rotate(Coordinate coord) {
		double a, b, c, d;
		
		assert (Math.abs(norm2() - 1) < 0.00001) : "Quaternion not unit length: " + this;
		

		//  (a,b,c,d) = this * coord = (w,x,y,z) * (0,cx,cy,cz)
		a = -x * coord.x - y * coord.y - z * coord.z; // w
		b = w * coord.x + y * coord.z - z * coord.y; // x i
		c = w * coord.y - x * coord.z + z * coord.x; // y j
		d = w * coord.z + x * coord.y - y * coord.x; // z k
		

		//  return = (a,b,c,d) * (this)^-1 = (a,b,c,d) * (w,-x,-y,-z)
		
		// Assert that the w-value is zero
		assert (Math.abs(a * w + b * x + c * y + d * z) < coord.max() * MathUtil.EPSILON) : ("Should be zero: " + (a * w + b * x + c * y + d * z) + " in " + this + " c=" + coord);
		
		return new Coordinate(
				-a * x + b * w - c * z + d * y,
				-a * y + b * z + c * w - d * x,
				-a * z - b * y + c * x + d * w,
				coord.weight);
	}
	
	/**
	 * Perform an inverse coordinate rotation using this unit quaternion.  The result is
	 * <code>this^(-1) * coord * this</code>.
	 * <p>
	 * This method assumes that the norm of this quaternion is one.
	 * 
	 * @param coord		the coordinate to rotate.
	 * @return			the rotated coordinate.
	 */
	public Coordinate invRotate(Coordinate coord) {
		double a, b, c, d;
		
		assert (Math.abs(norm2() - 1) < 0.00001) : "Quaternion not unit length: " + this;
		
		//  (a,b,c,d) = (this)^-1 * coord = (w,-x,-y,-z) * (0,cx,cy,cz)
		a = +x * coord.x + y * coord.y + z * coord.z;
		b = w * coord.x - y * coord.z + z * coord.y;
		c = w * coord.y + x * coord.z - z * coord.x;
		d = w * coord.z - x * coord.y + y * coord.x;
		

		//  return = (a,b,c,d) * this = (a,b,c,d) * (w,x,y,z)
		assert (Math.abs(a * w - b * x - c * y - d * z) < Math.max(coord.max(), 1) * MathUtil.EPSILON) : ("Should be zero: " + (a * w - b * x - c * y - d * z) + " in " + this + " c=" + coord);
		
		return new Coordinate(
				a * x + b * w + c * z - d * y,
				a * y - b * z + c * w + d * x,
				a * z + b * y - c * x + d * w,
				coord.weight);
	}
	
	
	/**
	 * Rotate the coordinate (0,0,1) using this quaternion.  The result is returned
	 * as a Coordinate.  This method is equivalent to calling
	 * <code>q.rotate(new Coordinate(0,0,1))</code> but requires only about half of the 
	 * multiplications.
	 * 
	 * @return	The coordinate (0,0,1) rotated using this quaternion.
	 */
	public Coordinate rotateZ() {
		return new Coordinate(
				2 * (w * y + x * z),
				2 * (y * z - w * x),
				w * w - x * x - y * y + z * z);
	}
	
	
	@Override
	public String toString() {
		return String.format("Quaternion[%f,%f,%f,%f,norm=%f]", w, x, y, z, this.norm());
	}
	
}
