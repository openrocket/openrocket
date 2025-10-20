package info.openrocket.core.util;

/**
 * A mutable counterpart to {@link Coordinate} intended strictly for hot-path computations
 * where reusing vector instances avoids excessive garbage. Unlike {@code Coordinate},
 * this class is stateful and <em>not</em> thread-safe. Callers must take care never to
 * expose a {@code MutableCoordinate} outside the scope where it is being mutated, and
 * should convert back to an immutable {@link Coordinate} before publishing values to
 * other parts of the system.
 */
public final class MutableCoordinate implements Coordinate {

	public static final Coordinate ZERO = new MutableCoordinate(0, 0, 0, 0);
	public static final Coordinate NUL = new MutableCoordinate(0, 0, 0, 0);
	public static final Coordinate NaN = new MutableCoordinate(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
	public static final Coordinate MAX = new MutableCoordinate(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
			Double.MAX_VALUE);
	public static final Coordinate MIN = new MutableCoordinate(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, 0.0);

	public static final Coordinate X_UNIT = new MutableCoordinate(1, 0, 0);
	public static final Coordinate Y_UNIT = new MutableCoordinate(0, 1, 0);
	public static final Coordinate Z_UNIT = new MutableCoordinate(0, 0, 1);

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

	public Coordinate set(Coordinate coord) {
		return set(coord.getX(), coord.getY(), coord.getZ(), coord.getWeight());
	}

	public Coordinate set(MutableCoordinate coord) {
		return set(coord.x, coord.y, coord.z, coord.weight);
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public Coordinate setX(double x) {
		return null;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public Coordinate setY(double y) {
		return null;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Override
	public Coordinate setZ(double z) {
		return null;
	}

	@Override
	public double getWeight() {
		return weight;
	}

	@Override
	public Coordinate setWeight(double weight) {
		this.weight = weight;
		return this;
	}

	@Override
	public Coordinate setXYZ(Coordinate c) {
		this.x = c.getX();
		this.y = c.getY();
		this.z = c.getZ();
		return this;
	}

	public Coordinate clear() {
		return set(0, 0, 0, 0);
	}

	@Override
	public Coordinate add(Coordinate coord) {
		return add(coord.getX(), coord.getY(), coord.getZ(), coord.getWeight());
	}

	@Override
	public Coordinate add(double dx, double dy, double dz) {
		this.x += dx;
		this.y += dy;
		this.z += dz;
		return this;
	}

	@Override
	public Coordinate add(double dx, double dy, double dz, double dWeight) {
		this.x += dx;
		this.y += dy;
		this.z += dz;
		this.weight += dWeight;
		return this;
	}

	@Override
	public Coordinate addScaled(Coordinate coord, double scale) {
		this.x += coord.getX() * scale;
		this.y += coord.getY() * scale;
		this.z += coord.getZ() * scale;
		this.weight += coord.getWeight() * scale;
		return this;
	}

	@Override
	public Coordinate sub(Coordinate other) {
		this.x -= other.getX();
		this.y -= other.getY();
		this.z -= other.getZ();
		this.weight -= other.getWeight();
		return this;
	}

	@Override
	public Coordinate sub(double x1, double y1, double z1) {
		this.x -= x1;
		this.y -= y1;
		this.z -= z1;
		return this;
	}

	@Override
	public Coordinate multiply(double m) {
		this.x *= m;
		this.y *= m;
		this.z *= m;
		this.weight *= m;
		return this;
	}

	@Override
	public Coordinate multiply(Coordinate other) {
		this.x *= other.getX();
		this.y *= other.getY();
		this.z *= other.getZ();
		this.weight *= other.getWeight();
		return this;
	}

	@Override
	public Coordinate cross(Coordinate other) {
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
	public static Coordinate cross(Coordinate a, Coordinate b) {
		// I know this isn't mutable, but I don't know which coordinate to modify
		return new MutableCoordinate(a.getY() * b.getZ() - a.getZ() * b.getY(), a.getZ() * b.getX() -
				a.getX() * b.getZ(), a.getX() * b.getY() - a.getY() * b.getX());
	}

	@Override
	public Coordinate normalize() {
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
	public Coordinate average(Coordinate other) {
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
	public Coordinate interpolate(Coordinate other, double fraction) {
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

	public Coordinate scale(double scale) {
		this.x *= scale;
		this.y *= scale;
		this.z *= scale;
		this.weight *= scale;
		return this;
	}

	@Override
	public Coordinate clone() {
		return new MutableCoordinate(this.x, this.y, this.z, this.weight);
	}
}
