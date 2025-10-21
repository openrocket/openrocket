package info.openrocket.core.util;

import java.io.Serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An immutable class of weighted coordinates.  The weights are non-negative.
 * 
 * Can also be used as non-weighted coordinates with weight=0.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class Coordinate implements CoordinateIF {
	private static final Logger log = LoggerFactory.getLogger(Coordinate.class);
	
	// Defined for backwards compatibility after adding clone().
	@Serial
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
	
	
	public static final CoordinateIF ZERO = new Coordinate(0, 0, 0, 0);
	public static final CoordinateIF NUL = new Coordinate(0, 0, 0, 0);
	public static final CoordinateIF NaN = new Coordinate(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	public static final CoordinateIF MAX = new Coordinate(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
			Double.MAX_VALUE);
	public static final CoordinateIF MIN = new Coordinate(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, 0.0);

	public static final CoordinateIF X_UNIT = new Coordinate(1, 0, 0);
	public static final CoordinateIF Y_UNIT = new Coordinate(0, 1, 0);
	public static final CoordinateIF Z_UNIT = new Coordinate(0, 0, 1);
	
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

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Override
	public double getWeight() {
		return weight;
	}

	@Override
	public CoordinateIF setX(double x) {
		return new Coordinate(x, this.y, this.z, this.weight);
	}

	@Override
	public CoordinateIF setY(double y) {
		return new Coordinate(this.x, y, this.z, this.weight);
	}

	@Override
	public CoordinateIF setZ(double z) {
		return new Coordinate(this.x, this.y, z, this.weight);
	}

	@Override
	public CoordinateIF setWeight(double weight) {
		return new Coordinate(this.x, this.y, this.z, weight);
	}

	@Override
	public CoordinateIF setXYZ(CoordinateIF c) {
		return new Coordinate(c.getX(), c.getY(), c.getZ(), this.weight);
	}
	
	

	@Override
	public CoordinateIF add(CoordinateIF other) {
		return new Coordinate(this.x + other.getX(), this.y + other.getY(), this.z + other.getZ(),
				this.weight + other.getWeight());
	}

	@Override
	public CoordinateIF add(double x1, double y1, double z1) {
		return new Coordinate(this.x + x1, this.y + y1, this.z + z1, this.weight);
	}

	@Override
	public CoordinateIF add(double x1, double y1, double z1, double w1) {
		return new Coordinate(this.x + x1, this.y + y1, this.z + z1, this.weight + w1);
	}

	@Override
	public CoordinateIF addScaled(CoordinateIF coord, double scale) {
		return new Coordinate(
				this.x + coord.getX() * scale,
				this.y + coord.getY() * scale,
				this.z + coord.getZ() * scale,
				this.weight + coord.getWeight() * scale);
	}
	
	@Override
	public CoordinateIF sub(CoordinateIF other) {
		return new Coordinate(this.x - other.getX(), this.y - other.getY(), this.z - other.getZ(), this.weight);
	}
	
	@Override
	public CoordinateIF sub(double x1, double y1, double z1) {
		return new Coordinate(this.x - x1, this.y - y1, this.z - z1, this.weight);
	}
	
	@Override
	public CoordinateIF multiply(double m) {
		return new Coordinate(this.x * m, this.y * m, this.z * m, this.weight * m);
	}

	@Override
	public CoordinateIF multiply(CoordinateIF other) {
		return new Coordinate(this.x * other.getX(), this.y * other.getY(), this.z * other.getZ(),
				this.weight * other.getWeight());
	}
	
	@Override
	public CoordinateIF cross(CoordinateIF other) {
		return cross(this, other);
	}
	
	/**
	 * Cross product of two Coordinates taken as vectors
	 */
	public static CoordinateIF cross(CoordinateIF a, CoordinateIF b) {
		return new Coordinate(a.getY() * b.getZ() - a.getZ() * b.getY(), a.getZ() * b.getX() -
				a.getX() * b.getZ(), a.getX() * b.getY() - a.getY() * b.getX());
	}
	
	@Override
	public double length() {
		if (length < 0) {
			length = MathUtil.safeSqrt(x * x + y * y + z * z);
		}
		return length;
	}
	
	@Override
	public CoordinateIF normalize() {
		double l = length();
		if (l < 0.0000001) {
			throw new IllegalStateException("Cannot normalize zero coordinate");
		}
		return new Coordinate(x / l, y / l, z / l, weight);
	}
	
	@Override
	public CoordinateIF average(CoordinateIF other) {
		double x1, y1, z1, w1;
		
		if (other == null)
			return this;
		
		w1 = this.weight + other.getWeight();
		if (Math.abs(w1) < MathUtil.pow2(MathUtil.EPSILON)) {
			x1 = (this.x + other.getX()) / 2;
			y1 = (this.y + other.getY()) / 2;
			z1 = (this.z + other.getZ()) / 2;
			w1 = 0;
		} else {
			x1 = (this.x * this.weight + other.getX() * other.getWeight()) / w1;
			y1 = (this.y * this.weight + other.getY() * other.getWeight()) / w1;
			z1 = (this.z * this.weight + other.getZ() * other.getWeight()) / w1;
		}
		return new Coordinate(x1, y1, z1, w1);
	}

	@Override
	public CoordinateIF interpolate(CoordinateIF other, double fraction) {
		double x1 = this.x + (other.getX() - this.x) * fraction;
		double y1 = this.y + (other.getY() - this.y) * fraction;
		double z1 = this.z + (other.getZ() - this.z) * fraction;
		double w1 = this.weight + (other.getWeight() - this.weight) * fraction;
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
		if (!(other instanceof CoordinateIF c))
			return false;

		return (MathUtil.equals(this.x, c.getX()) &&
				MathUtil.equals(this.y, c.getY()) &&
				MathUtil.equals(this.z, c.getZ()) && MathUtil.equals(this.weight, c.getWeight()));
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
			return String.format("(%.5f,%.5f,%.5f,w=%.5f)", x, y, z, weight);
		else
			return String.format("(%.5f,%.5f,%.5f)", x, y, z);
	}
	
	@Override
	public CoordinateIF clone() {
		return new Coordinate(this.x, this.y, this.z, this.weight);
	}
	
}
