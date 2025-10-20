package info.openrocket.core.util;

/**
 * A mutable counterpart to {@link Coordinate} intended strictly for hot-path computations
 * where reusing vector instances avoids excessive garbage. Unlike {@code Coordinate},
 * this class is stateful and <em>not</em> thread-safe. Callers must take care never to
 * expose a {@code MutableCoordinate} outside the scope where it is being mutated, and
 * should convert back to an immutable {@link Coordinate} before publishing values to
 * other parts of the system.
 */
public final class MutableCoordinate {

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

	public MutableCoordinate set(Coordinate coord) {
		return set(coord.x, coord.y, coord.z, coord.weight);
	}

	public MutableCoordinate set(MutableCoordinate coord) {
		return set(coord.x, coord.y, coord.z, coord.weight);
	}

	public MutableCoordinate clear() {
		return set(0, 0, 0, 0);
	}

	public MutableCoordinate add(double dx, double dy, double dz) {
		this.x += dx;
		this.y += dy;
		this.z += dz;
		return this;
	}

	public MutableCoordinate add(double dx, double dy, double dz, double dWeight) {
		this.x += dx;
		this.y += dy;
		this.z += dz;
		this.weight += dWeight;
		return this;
	}

	public MutableCoordinate add(Coordinate coord) {
		return add(coord.x, coord.y, coord.z, coord.weight);
	}

	public MutableCoordinate add(MutableCoordinate coord) {
		return add(coord.x, coord.y, coord.z, coord.weight);
	}

	public MutableCoordinate addScaled(Coordinate coord, double scale) {
		this.x += coord.x * scale;
		this.y += coord.y * scale;
		this.z += coord.z * scale;
		this.weight += coord.weight * scale;
		return this;
	}

	public MutableCoordinate addScaled(MutableCoordinate coord, double scale) {
		this.x += coord.x * scale;
		this.y += coord.y * scale;
		this.z += coord.z * scale;
		this.weight += coord.weight * scale;
		return this;
	}

	public MutableCoordinate scale(double scale) {
		this.x *= scale;
		this.y *= scale;
		this.z *= scale;
		this.weight *= scale;
		return this;
	}

	public MutableCoordinate negate() {
		return scale(-1);
	}

	public double dot(Coordinate coord) {
		return x * coord.x + y * coord.y + z * coord.z;
	}

	public double length2() {
		return x * x + y * y + z * z;
	}

	public double length() {
		return MathUtil.safeSqrt(length2());
	}

	public MutableCoordinate setWeight(double weight) {
		this.weight = weight;
		return this;
	}

	public Coordinate toCoordinate() {
		return new Coordinate(x, y, z, weight);
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

	public double getWeight() {
		return weight;
	}
}
