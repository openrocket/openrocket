package net.sf.openrocket.util;


public class Quaternion implements Cloneable {

	protected double w, x, y, z;
	protected int modCount = 0;
	
	public Quaternion() {
		this(1,0,0,0);
	}
	
	public Quaternion(double w, double x, double y, double z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	public static Quaternion rotation(Coordinate rotation) {
		double length = rotation.length();
		if (length < 0.000001) {
			return new Quaternion(1,0,0,0);
		}
		double sin = Math.sin(length/2);
		double cos = Math.cos(length/2);
		return new Quaternion(cos, 
				sin*rotation.x/length, sin*rotation.y/length, sin*rotation.z/length);
	}
	
	public static Quaternion rotation(Coordinate axis, double angle) {
		Coordinate a = axis.normalize();
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		return new Quaternion(cos, sin*a.x, sin*a.y, sin*a.z);
	}

	
	public double getW() {
		return w;
	}

	public void setW(double w) {
		this.w = w;
		modCount++;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
		modCount++;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
		modCount++;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
		modCount++;
	}
	
	
	public void setAll(double w, double x, double y, double z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
		modCount++;
	}

	
	/**
	 * Multiply this quaternion by the other quaternion from the right side.  This
	 * calculates the product  <code>this = this * other</code>.
	 * 
	 * @param other   the quaternion to multiply this quaternion by.
	 * @return		  this quaternion.
	 */
	public Quaternion multiplyRight(Quaternion other) {
		double w = (this.w*other.w - this.x*other.x - this.y*other.y - this.z*other.z);
		double x = (this.w*other.x + this.x*other.w + this.y*other.z - this.z*other.y);
		double y = (this.w*other.y + this.y*other.w + this.z*other.x - this.x*other.z);
		double z = (this.w*other.z + this.z*other.w + this.x*other.y - this.y*other.x);
		
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	/**
	 * Multiply this quaternion by the other quaternion from the left side.  This
	 * calculates the product  <code>this = other * this</code>.
	 * 
	 * @param other   the quaternion to multiply this quaternion by.
	 * @return		  this quaternion.
	 */
	public Quaternion multiplyLeft(Quaternion other) {
		/*  other(abcd) * this(wxyz)  */
		
		double w = (other.w*this.w - other.x*this.x - other.y*this.y - other.z*this.z);
		double x = (other.w*this.x + other.x*this.w + other.y*this.z - other.z*this.y);
		double y = (other.w*this.y + other.y*this.w + other.z*this.x - other.x*this.z);
		double z = (other.w*this.z + other.z*this.w + other.x*this.y - other.y*this.x);
		
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	

	


	@Override
	public Quaternion clone() {
		try {
			return (Quaternion) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("CloneNotSupportedException encountered");
		}
	}



	/**
	 * Normalize this quaternion.  After the call the norm of the quaternion is exactly
	 * one.  If this quaternion is the zero quaternion, throws
	 * <code>IllegalStateException</code>.  Returns this quaternion.
	 * 
	 * @return   this quaternion.
	 * @throws   IllegalStateException  if the norm of this quaternion is zero.
	 */
	public Quaternion normalize() {
		double norm = norm();
		if (norm < 0.0000001) {
			throw new IllegalStateException("attempting to normalize zero-quaternion");
		}
		x /= norm;
		y /= norm;
		z /= norm;
		w /= norm;
		return this;
	}
	
	
	/**
	 * Normalize this quaternion if the norm is more than 1ppm from one.
	 * 
	 * @return	this quaternion.
	 * @throws   IllegalStateException  if the norm of this quaternion is zero.
	 */
	public Quaternion normalizeIfNecessary() {
		double n2 = norm2();
		if (n2 < 0.999999 || n2 > 1.000001)
			normalize();
		return this;
	}
	
	
	
	/**
	 * Return the norm of this quaternion.  
	 * 
	 * @return   the norm of this quaternion sqrt(w^2 + x^2 + y^2 + z^2).
	 */
	public double norm() {
		return Math.sqrt(x*x + y*y + z*z + w*w);
	}
	
	/**
	 * Return the square of the norm of this quaternion.
	 * 
	 * @return	the square of the norm of this quaternion (w^2 + x^2 + y^2 + z^2).
	 */
	public double norm2() {
		return x*x + y*y + z*z + w*w;
	}
	
	
	public Coordinate rotate(Coordinate coord) {
		double a,b,c,d;
		
		assert(Math.abs(norm2()-1) < 0.00001) : "Quaternion not unit length: "+this;
		
		a = - x * coord.x - y * coord.y - z * coord.z;  // w
		b =   w * coord.x + y * coord.z - z * coord.y;  // x i
		c =   w * coord.y - x * coord.z + z * coord.x;  // y j
		d =   w * coord.z + x * coord.y - y * coord.x;  // z k
		
		assert(MathUtil.equals(a*w + b*x + c*y + d*z, 0)) : 
			("Should be zero: " + (a*w - b*x - c*y - d*z) + " in " + this + " c=" + coord);
				
		return new Coordinate(
				- a*x + b*w - c*z + d*y,
				- a*y + b*z + c*w - d*x,
				- a*z - b*y + c*x + d*w,
				coord.weight
		);
	}
	
	public Coordinate invRotate(Coordinate coord) {
		double a,b,c,d;

		assert(Math.abs(norm2()-1) < 0.00001) : "Quaternion not unit length: "+this;

		a = + x * coord.x + y * coord.y + z * coord.z;
		b =   w * coord.x - y * coord.z + z * coord.y;
		c =   w * coord.y + x * coord.z - z * coord.x;
		d =   w * coord.z - x * coord.y + y * coord.x;
		
		assert(MathUtil.equals(a*w - b*x - c*y - d*z, 0)): 
			("Should be zero: " + (a*w - b*x - c*y - d*z) + " in " + this + " c=" + coord);
		
		return new Coordinate(
				a*x + b*w + c*z - d*y,
				a*y - b*z + c*w + d*x,
				a*z + b*y - c*x + d*w,
				coord.weight
		);
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
				2*(w*y + x*z),
				2*(y*z - w*x),
				w*w - x*x - y*y + z*z
		);
	}
	
	
	@Override
	public String toString() {
		return String.format("Quaternion[%f,%f,%f,%f,norm=%f]",w,x,y,z,this.norm());
	}
	
	public static void main(String[] arg) {
		
		Quaternion q = new Quaternion(Math.random()-0.5,Math.random()-0.5,
				Math.random()-0.5,Math.random()-0.5);
		q.normalize();
		
		q = new Quaternion(-0.998717,0.000000,0.050649,-0.000000);
		
		Coordinate coord = new Coordinate(10*(Math.random()-0.5), 
				10*(Math.random()-0.5), 10*(Math.random()-0.5));
		
		System.out.println("Quaternion: "+q);
		System.out.println("Coordinate: "+coord);
		coord = q.invRotate(coord);
		System.out.println("Rotated: "+ coord);
		coord = q.rotate(coord);
		System.out.println("Back:       "+coord);
		
//		Coordinate c = new Coordinate(0,1,0);
//		Coordinate rot = new Coordinate(Math.PI/4,0,0);
//		
//		System.out.println("Before: "+c);
//		c = rotation(rot).invRotate(c);
//		System.out.println("After: "+c);
		
		
	}
	
}
