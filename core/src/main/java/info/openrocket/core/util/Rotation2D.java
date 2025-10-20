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

	public Coordinate rotateX(Coordinate c) {
		return new ImmutableCoordinate(c.getX(), cos * c.getY() - sin * c.getZ(), cos * c.getZ() + sin * c.getY(), c.getWeight());
	}

	public Coordinate rotateY(Coordinate c) {
		return new ImmutableCoordinate(cos * c.getX() + sin * c.getZ(), c.getY(), cos * c.getZ() - sin * c.getX(), c.getWeight());
	}

	public Coordinate rotateZ(Coordinate c) {
		return new ImmutableCoordinate(cos * c.getX() - sin * c.getY(), cos * c.getY() + sin * c.getX(), c.getZ(), c.getWeight());
	}

	public Coordinate invRotateX(Coordinate c) {
		return new ImmutableCoordinate(c.getX(), cos * c.getY() + sin * c.getZ(), cos * c.getZ() - sin * c.getY(), c.getWeight());
	}

	public Coordinate invRotateY(Coordinate c) {
		return new ImmutableCoordinate(cos * c.getX() - sin * c.getZ(), c.getY(), cos * c.getZ() + sin * c.getX(), c.getWeight());
	}

	public Coordinate invRotateZ(Coordinate c) {
		return new ImmutableCoordinate(cos * c.getX() + sin * c.getY(), cos * c.getY() - sin * c.getX(), c.getZ(), c.getWeight());
	}

}
