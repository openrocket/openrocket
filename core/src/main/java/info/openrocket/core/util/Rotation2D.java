package info.openrocket.core.util;

public class Rotation2D {

	public static final Rotation2D ID = new Rotation2D(0.0, 1.0);

	public final double sin, cos;

	public Rotation2D(double angle) {
		this(Math.sin(angle), Math.cos(angle));
	}

	public Rotation2D(double sin, double cos) {
		this.sin = sin;
		this.cos = cos;
	}

	public CoordinateIF rotateX(CoordinateIF c) {
		return new Coordinate(c.getX(), cos * c.getY() - sin * c.getZ(), cos * c.getZ() + sin * c.getY(), c.getWeight());
	}

	public CoordinateIF rotateY(CoordinateIF c) {
		return new Coordinate(cos * c.getX() + sin * c.getZ(), c.getY(), cos * c.getZ() - sin * c.getX(), c.getWeight());
	}

	public CoordinateIF rotateZ(CoordinateIF c) {
		return new Coordinate(cos * c.getX() - sin * c.getY(), cos * c.getY() + sin * c.getX(), c.getZ(), c.getWeight());
	}

	public MutableCoordinate rotateZInPlace(MutableCoordinate c) {
		double x = c.getX();
		double y = c.getY();
		double newX = cos * x - sin * y;
		double newY = cos * y + sin * x;
		return c.set(newX, newY, c.getZ(), c.getWeight());
	}

	public CoordinateIF invRotateX(CoordinateIF c) {
		return new Coordinate(c.getX(), cos * c.getY() + sin * c.getZ(), cos * c.getZ() - sin * c.getY(), c.getWeight());
	}

	public CoordinateIF invRotateY(CoordinateIF c) {
		return new Coordinate(cos * c.getX() - sin * c.getZ(), c.getY(), cos * c.getZ() + sin * c.getX(), c.getWeight());
	}

	public CoordinateIF invRotateZ(CoordinateIF c) {
		return new Coordinate(cos * c.getX() + sin * c.getY(), cos * c.getY() - sin * c.getX(), c.getZ(), c.getWeight());
	}

}
