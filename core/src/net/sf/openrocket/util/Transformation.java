package net.sf.openrocket.util;

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

	
	public static final Transformation IDENTITY =
		new Transformation();
	
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
	
	/**
	 * Create identity transformation.
	 */
	public Transformation() {
		translate = new Coordinate(0,0,0);
		rotation[X][X]=1;
		rotation[Y][Y]=1;
		rotation[Z][Z]=1;
	}
	
	/**
	 * Create transformation with only translation.
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
	 * Rotate around x-axis a given angle.
	 * @param theta  The angle to rotate in radians.
	 * @return  The transformation.
	 */
	public static Transformation rotate_x(double theta) {
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
		return new Transformation(new double[][]{
				{Math.cos(theta),-Math.sin(theta),0},
				{Math.sin(theta),Math.cos(theta),0},
				{0,0,1}});
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
	
}
