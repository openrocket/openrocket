package info.openrocket.core.util;

/**
 * A mutable counterpart to {@link CoordinateIF} intended strictly for hot-path computations
 * where reusing vector instances avoids excessive garbage. Unlike {@code Coordinate},
 * this class is stateful and <em>not</em> thread-safe. Callers must take care never to
 * expose a {@code MutableCoordinate} outside the scope where it is being mutated, and
 * should convert back to an immutable {@link CoordinateIF} before publishing values to
 * other parts of the system.
 */
public final class MutableCoordinate implements CoordinateIF {

	public static final CoordinateIF ZERO = new MutableCoordinate(0, 0, 0, 0);
	public static final CoordinateIF NUL = new MutableCoordinate(0, 0, 0, 0);
	public static final CoordinateIF NaN = new MutableCoordinate(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	public static final CoordinateIF MAX = new MutableCoordinate(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
			Double.MAX_VALUE);
	public static final CoordinateIF MIN = new MutableCoordinate(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, 0.0);

	public static final CoordinateIF X_UNIT = new MutableCoordinate(1, 0, 0);
	public static final CoordinateIF Y_UNIT = new MutableCoordinate(0, 1, 0);
	public static final CoordinateIF Z_UNIT = new MutableCoordinate(0, 0, 1);

	private double x;
	private double y;
	private double z;
	private double weight;

	public MutableCoordinate() {
		this(0, 0, 0, 0);
	}

	public MutableCoordinate(double x, double y, double z) {
		this(x, y, z, 0);
	}

	public MutableCoordinate(double x, double y, double z, double weight) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.weight = weight;
	}

	public MutableCoordinate set(double x, double y, double z) {
		return set(x, y, z, this.weight);
	}

	public MutableCoordinate set(double x, double y, double z, double weight) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.weight = weight;
		return this;
	}

	public CoordinateIF set(CoordinateIF coord) {
		return set(coord.getX(), coord.getY(), coord.getZ(), coord.getWeight());
	}

	public CoordinateIF set(MutableCoordinate coord) {
		return set(coord.x, coord.y, coord.z, coord.weight);
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public CoordinateIF setX(double x) {
		this.x = x;
		return this;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public CoordinateIF setY(double y) {
		this.y = y;
		return this;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Override
	public CoordinateIF setZ(double z) {
		this.z = z;
		return this;
	}

	@Override
	public double getWeight() {
		return weight;
	}

	@Override
	public CoordinateIF setWeight(double weight) {
		this.weight = weight;
		return this;
	}

	@Override
	public CoordinateIF setXYZ(CoordinateIF c) {
		this.x = c.getX();
		this.y = c.getY();
		this.z = c.getZ();
		return this;
	}

	public CoordinateIF clear() {
		return set(0, 0, 0, 0);
	}

	@Override
	public CoordinateIF add(CoordinateIF coord) {
		return add(coord.getX(), coord.getY(), coord.getZ(), coord.getWeight());
	}

	@Override
	public CoordinateIF add(double dx, double dy, double dz) {
		this.x += dx;
		this.y += dy;
		this.z += dz;
		return this;
	}

	@Override
	public CoordinateIF add(double dx, double dy, double dz, double dWeight) {
		this.x += dx;
		this.y += dy;
		this.z += dz;
		this.weight += dWeight;
		return this;
	}

	@Override
	public CoordinateIF addScaled(CoordinateIF coord, double scale) {
		this.x += coord.getX() * scale;
		this.y += coord.getY() * scale;
		this.z += coord.getZ() * scale;
		this.weight += coord.getWeight() * scale;
		return this;
	}

	@Override
	public CoordinateIF sub(CoordinateIF other) {
		this.x -= other.getX();
		this.y -= other.getY();
		this.z -= other.getZ();
		this.weight -= other.getWeight();
		return this;
	}

	@Override
	public CoordinateIF sub(double x1, double y1, double z1) {
		this.x -= x1;
		this.y -= y1;
		this.z -= z1;
		return this;
	}

	@Override
	public CoordinateIF multiply(double m) {
		this.x *= m;
		this.y *= m;
		this.z *= m;
		this.weight *= m;
		return this;
	}

	@Override
	public CoordinateIF multiply(CoordinateIF other) {
		this.x *= other.getX();
		this.y *= other.getY();
		this.z *= other.getZ();
		this.weight *= other.getWeight();
		return this;
	}

	@Override
	public CoordinateIF cross(CoordinateIF other) {
		double newX = this.y * other.getZ() - this.z * other.getY();
		double newY = this.z * other.getX() - this.x * other.getZ();
		double newZ = this.x * other.getY() - this.y * other.getX();
		this.x = newX;
		this.y = newY;
		this.z = newZ;
		// Weight is unchanged
		return this;
	}

	/**
	 * Cross product of two Coordinates taken as vectors
	 */
	public static CoordinateIF cross(CoordinateIF a, CoordinateIF b) {
		// I know this isn't mutable, but I don't know which coordinate to modify
		return new MutableCoordinate(a.getY() * b.getZ() - a.getZ() * b.getY(), a.getZ() * b.getX() -
				a.getX() * b.getZ(), a.getX() * b.getY() - a.getY() * b.getX());
	}

	@Override
	public CoordinateIF normalize() {
		double l = length();
		if (l < 0.0000001) {
			throw new IllegalStateException("Cannot normalize zero coordinate");
		}
		this.x /= l;
		this.y /= l;
		this.z /= l;
		return this;
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
		this.x = x1;
		this.y = y1;
		this.z = z1;
		this.weight = w1;
		return this;
	}

	@Override
	public CoordinateIF interpolate(CoordinateIF other, double fraction) {
		double x1 = this.x + (other.getX() - this.x) * fraction;
		double y1 = this.y + (other.getY() - this.y) * fraction;
		double z1 = this.z + (other.getZ() - this.z) * fraction;
		double w1 = this.weight + (other.getWeight() - this.weight) * fraction;
		this.x = x1;
		this.y = y1;
		this.z = z1;
		this.weight = w1;
		return this;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MutableCoordinate c)) {
			return false;
		}
		return MathUtil.equals(this.x, c.getX()) &&
				MathUtil.equals(this.y, c.getY()) &&
				MathUtil.equals(this.z, c.getZ()) &&
				MathUtil.equals(this.weight, c.getWeight());
	}

	@Override
	public String toString() {
		if (isWeighted())
			return String.format("(%.5f,%.5f,%.5f,w=%.5f)", x, y, z, weight);
		else
			return String.format("(%.5f,%.5f,%.5f)", x, y, z);
	}

	@Override
	public int hashCode() {
		return (int) ((x + y + z) * 100000);
	}

	@Override
	public CoordinateIF clone() {
		return new MutableCoordinate(this.x, this.y, this.z, this.weight);
	}
}
