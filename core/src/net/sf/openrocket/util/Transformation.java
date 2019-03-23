package net.sf.openrocket.util;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Defines an affine transformation of the form  A*x+c,  where x and c are Coordinates and
 * A is a 3x3 matrix.
 * 
 * The Transformations are immutable.  All modification methods return a new transformation.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class Transformation implements java.io.Serializable {

	public static final double ANGLE_EPSILON = 0.000000001;
	
	public static final Transformation IDENTITY = new Transformation();
	
	public static final Transformation PROJECT_XY = 
		new Transformation(new double[][]{{1,0,0},{0,1,0},{0,0,0}});
	public static final Transformation PROJECT_YZ = 
		new Transformation(new double[][]{{0,0,0},{0,1,0},{0,0,1}});
	public static final Transformation PROJECT_XZ = 
		new Transformation(new double[][]{{1,0,0},{0,0,0},{0,0,1}});
	
	
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	
	private final Coordinate translate;
	private final double[][] rotation = new double[3][3];
	
	static public Transformation getTranslationTransform( double x, double y, double z) {
		return new Transformation(new Coordinate(x,y,z));
	}
	static public Transformation getTranslationTransform( final Coordinate translate ){
		return new Transformation( translate );
	}

	/**
	 * Create identity transformation.
	 */
	private Transformation() {
		translate = new Coordinate(0,0,0);
		rotation[X][X]=1;
		rotation[Y][Y]=1;
		rotation[Z][Z]=1;
	}
	
	/**
	 * Create transformation with only translation.
	 *
	 * @param x Translation in x-axis.
	 * @param y Translation in y-axis.
	 * @param z Translation in z-axis.
	 */
	public Transformation(double x,double y,double z) {
		translate = new Coordinate(x,y,z);
		rotation[X][X]=1;
		rotation[Y][Y]=1;
		rotation[Z][Z]=1;
	}

	/**
	 * Create transformation with only translation.
	 * @param translation  The translation term.
	 */
	public Transformation(Coordinate translation) {
		this.translate = translation;
		rotation[X][X]=1;
		rotation[Y][Y]=1;
		rotation[Z][Z]=1;
	}
	
	/**
	 * Create transformation with given rotation matrix and translation.
	 * @param rotation
	 * @param translation
	 */
	public Transformation(double[][] rotation, Coordinate translation) {
		for (int i=0; i<3; i++)
			for (int j=0; j<3; j++)
				this.rotation[i][j] = rotation[i][j];
		this.translate = translation;
	}
	
	
	/**
	 * Create transformation with given rotation matrix and translation.
	 * @param rotation
	 * @param translation
	 */
	public Transformation(double[][] rotation) {
		for (int i=0; i<3; i++)
			for (int j=0; j<3; j++)
				this.rotation[i][j] = rotation[i][j];
		this.translate = Coordinate.NUL;
	}
	

	
	
	
	/**
	 * Transform a coordinate according to this transformation.
	 * 
	 * @param orig	the coordinate to transform.
	 * @return		the result.
	 */
	public Coordinate transform(Coordinate orig) {
		final double x,y,z;

		x = rotation[X][X]*orig.x + rotation[X][Y]*orig.y + rotation[X][Z]*orig.z + translate.x;
		y = rotation[Y][X]*orig.x + rotation[Y][Y]*orig.y + rotation[Y][Z]*orig.z + translate.y;
		z = rotation[Z][X]*orig.x + rotation[Z][Y]*orig.y + rotation[Z][Z]*orig.z + translate.z;
		
		return new Coordinate(x,y,z,orig.weight);
	}
	
	/**
	 * Transform an array of coordinates.  The transformed coordinates are stored
	 * in the same array, and the array is returned.
	 * 
	 * @param orig	the coordinates to transform.
	 * @return		<code>orig</code>, with the coordinates transformed.
	 */
	public Coordinate[] transform(Coordinate[] orig) {
		for (int i=0; i < orig.length; i++) {
			orig[i] = transform(orig[i]);
		}
		return orig;
	}
	
	/**
	 * Transforms all coordinates in a Collection.  The original coordinate elements are
	 * removed from the set and replaced with the transformed ones.  The Collection given
	 * must implement the .clear() and .addAll() methods.
	 * 
	 * @param set  Collection of coordinates to transform.
	 */
	public void transform(Collection<Coordinate> set) {
		ArrayList<Coordinate> temp = new ArrayList<Coordinate>(set.size());
		Iterator<Coordinate> iter = set.iterator();
		while (iter.hasNext())
			temp.add(this.transform(iter.next()));
		set.clear();
		set.addAll(temp);
	}

	/**
	 * Applies only the linear transformation  A*x
	 * @param orig  Coordinate to transform.
	 */
	public Coordinate linearTransform(Coordinate orig) {
		final double x,y,z;

		x = rotation[X][X]*orig.x + rotation[X][Y]*orig.y + rotation[X][Z]*orig.z;
		y = rotation[Y][X]*orig.x + rotation[Y][Y]*orig.y + rotation[Y][Z]*orig.z;
		z = rotation[Z][X]*orig.x + rotation[Z][Y]*orig.y + rotation[Z][Z]*orig.z;
		
		return new Coordinate(x,y,z,orig.weight);
	}
	
	/**
	 * Applies the given transformation before this tranformation.  The resulting 
	 * transformation result.transform(c) will equal this.transform(other.transform(c)).
	 * 
	 * @param other  Transformation to apply
	 * @return   The new transformation
	 */
	public Transformation applyTransformation(Transformation other) {
		// other = Ax+b
		// this = Cx+d
		// C(Ax+b)+d = CAx + Cb+d
		
		// Translational portion
		Transformation combined = new Transformation(
				this.linearTransform(other.translate).add(this.translate)
		);
		
		// Linear portion
		for (int i=0; i<3; i++) {
			final double x,y,z;
			x = rotation[i][X];
			y = rotation[i][Y];
			z = rotation[i][Z];
			combined.rotation[i][X] = 
				x*other.rotation[X][X] + y*other.rotation[Y][X] + z*other.rotation[Z][X];
			combined.rotation[i][Y] = 
				x*other.rotation[X][Y] + y*other.rotation[Y][Y] + z*other.rotation[Z][Y];
			combined.rotation[i][Z] = 
				x*other.rotation[X][Z] + y*other.rotation[Y][Z] + z*other.rotation[Z][Z];
		}
		return combined;
	}
	
	/**
	 * Returns a rotation around the rocket's long axis
	 * 
	 * @param theta rotation around rocket axis, in radians
	 */
	static public Transformation getAxialRotation( double theta ) {
		return Transformation.rotate_x(theta);
	}
	
	/**
	 * Rotate around x-axis a given angle.
	 * @param theta  The angle to rotate in radians.
	 * @return  The transformation.
	 */
	public static Transformation rotate_x(double theta) {
		if( ANGLE_EPSILON > Math.abs(theta)) {
			return Transformation.IDENTITY;
		}
		return new Transformation(new double[][]{
				{1,0,0},
				{0,Math.cos(theta),-Math.sin(theta)},
				{0,Math.sin(theta),Math.cos(theta)}});
	}
	
	/**
	 * Rotate around y-axis a given angle.
	 * @param theta  The angle to rotate in radians.
	 * @return  The transformation.
	 */
	public static Transformation rotate_y(double theta) {
		if( ANGLE_EPSILON > Math.abs(theta)) {
			return Transformation.IDENTITY;
		}
		return new Transformation(new double[][]{
				{Math.cos(theta),0,Math.sin(theta)},
				{0,1,0},
				{-Math.sin(theta),0,Math.cos(theta)}});
	}
	
	/**
	 * Rotate around z-axis a given angle.
	 * @param theta  The angle to rotate in radians.
	 * @return  The transformation.
	 */
	public static Transformation rotate_z(double theta) {
		if( ANGLE_EPSILON > Math.abs(theta)) {
			return Transformation.IDENTITY;
		}
		return new Transformation(new double[][]{
				{Math.cos(theta),-Math.sin(theta),0},
				{Math.sin(theta),Math.cos(theta),0},
				{0,0,1}});
	}
	
	
	public boolean isIdentity() {
		if( this == Transformation.IDENTITY ) {
			return true;
		}
		return this.equals( Transformation.IDENTITY );
	}
	
	
	public void print(String... str) {
		for (String s: str) {
			System.out.println(s);
		}
		System.out.printf("[%3.2f %3.2f %3.2f]   [%3.2f]\n",
				rotation[X][X],rotation[X][Y],rotation[X][Z],translate.x);
		System.out.printf("[%3.2f %3.2f %3.2f] + [%3.2f]\n",
				rotation[Y][X],rotation[Y][Y],rotation[Y][Z],translate.y);
		System.out.printf("[%3.2f %3.2f %3.2f]   [%3.2f]\n",
				rotation[Z][X],rotation[Z][Y],rotation[Z][Z],translate.z);
		System.out.println();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(String.format("[%3.2f %3.2f %3.2f]   [%3.2f]\n",
				rotation[X][X],rotation[X][Y],rotation[X][Z],translate.x));
		sb.append(String.format("[%3.2f %3.2f %3.2f] + [%3.2f]\n",
				rotation[Y][X],rotation[Y][Y],rotation[Y][Z],translate.y));
		sb.append(String.format("[%3.2f %3.2f %3.2f]   [%3.2f]\n",
				rotation[Z][X],rotation[Z][Y],rotation[Z][Z],translate.z));
		return sb.toString();
	}
	

	/**
	 * Rotation matrix is constructed from Euler angles, in a z-x-z order
	 * 
	 * $  y = f(x) = R_z( R_x( R_z( x ))) + v $
	 * 
	 * @param alpha rotation around z    (in radians)
	 * @param beta  rotation around x'   (in radians)
	 * @param gamma rotation around z'   (in radians)
	 */
	static public Transformation getEulerAngle313Transform( double alpha, double beta, double gamma ) {
		return new Transformation( new double[][]{
				{
					(Math.cos(alpha)*Math.cos(gamma) - Math.sin(alpha)*Math.cos(beta)*Math.sin(gamma)),
				    (-Math.cos(alpha)*Math.sin(gamma) - Math.sin(alpha)*Math.cos(beta)*Math.cos(gamma)),
					(Math.sin(alpha)*Math.sin(beta))
				},{
					(Math.sin(alpha)*Math.cos(gamma) + Math.cos(alpha)*Math.cos(beta)*Math.sin(gamma)),
					(-Math.sin(alpha)*Math.sin(gamma) + Math.cos(alpha)*Math.cos(beta)*Math.cos(gamma)),
					(-Math.cos(alpha)*Math.sin(beta))
				},{
					(Math.sin(beta)*Math.sin(gamma)),
					(Math.sin(beta)*Math.cos(gamma)),
					(Math.cos(beta))
				}
			},
			Coordinate.ZERO);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Transformation))
			return false;
		Transformation o = (Transformation)other;
		for (int i=0; i<3; i++) {
			for (int j=0; j<3; j++) {
				if (!MathUtil.equals(this.rotation[i][j], o.rotation[i][j]))
					return false;
			}
		}
		return this.translate.equals(o.translate);
	}

	@Override
	public int hashCode() {
		long bits = 0;
		for(int i=0;i<Z;++i) {
			for(int j=0;j<Z;++j) {
				Double.doubleToLongBits( rotation[i][j] );
			}
		}
		bits ^= translate.hashCode();
		return (int)(bits ^ (bits >>> 32));
	}

	/**
	 * 
	 * 
	 * m = [  m[0]  m[4] m[ 8]  m[12] ] = [ 1 0 0 1 ]
	 *     [  m[1]  m[5] m[ 9]  m[13] ]   [ 0 1 0 1 ]
	 *     [  m[2]  m[6] m[10]  m[14] ]   [ 0 0 1 1 ]
	 *     [  m[3]  m[7] m[11]  m[15] ]   [ 0 0 0 1 ]
	 * 
	 * @return
	 */
	public DoubleBuffer getGLMatrix() {
		double[] data = new double[]{1,0,0,0,0,1,0,0,0,0,1,0,1,1,1,1};
		
		// output array is in column-major order
		// https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glLoadMatrix.xml
		for( int i=0; i<3; ++i) {
			for( int j=0; j<3; ++j) {
				data[i+j*4] = this.rotation[i][j];
			}
		}
		
		data[12] = this.translate.x; 
		data[13] = this.translate.y;
		data[14] = this.translate.z;
		
		return DoubleBuffer.wrap(data);		
	}
	
	public Coordinate getTranslationVector() {
		return this.translate;
	}

	/**
	 * Calculate X, Y, and Z rotation angles from rotation matrices
	 */
	public double getXrotation() {
		return Math.atan2((rotation[2][1] - rotation[1][2])/2.0,
						  (rotation[1][1] + rotation[2][2])/2.0);
	}

	public double getYrotation() {
		return Math.atan2((rotation[0][2] - rotation[2][0])/2.0,
						  (rotation[0][0] + rotation[2][2])/2.0);
	}

	public double getZrotation() {
		return Math.atan2((rotation[1][0] - rotation[0][1])/2.0,
						  (rotation[0][0] + rotation[1][1])/2.0);

	}

	
}
