package net.sf.openrocket.util;

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
		return new Coordinate(c.x, cos*c.y - sin*c.z, cos*c.z + sin*c.y, c.weight);
	}
	
	public Coordinate rotateY(Coordinate c) {
		return new Coordinate(cos*c.x + sin*c.z, c.y, cos*c.z - sin*c.x, c.weight);
	}
	
	public Coordinate rotateZ(Coordinate c) {
		return new Coordinate(cos*c.x - sin*c.y, cos*c.y + sin*c.x, c.z, c.weight);
	}
	

	public Coordinate invRotateX(Coordinate c) {
		return new Coordinate(c.x, cos*c.y + sin*c.z, cos*c.z - sin*c.y, c.weight);
	}
	
	public Coordinate invRotateY(Coordinate c) {
		return new Coordinate(cos*c.x - sin*c.z, c.y, cos*c.z + sin*c.x, c.weight);
	}
	
	public Coordinate invRotateZ(Coordinate c) {
		return new Coordinate(cos*c.x + sin*c.y, cos*c.y - sin*c.x, c.z, c.weight);
	}
	

	
	public static void main(String arg[]) {
		Coordinate c = new Coordinate(1,1,1,2.5);
		Rotation2D rot = new Rotation2D(Math.PI/4);
		
		System.out.println("X: "+rot.rotateX(c));
		System.out.println("Y: "+rot.rotateY(c));
		System.out.println("Z: "+rot.rotateZ(c));
		System.out.println("invX: "+rot.invRotateX(c));
		System.out.println("invY: "+rot.invRotateY(c));
		System.out.println("invZ: "+rot.invRotateZ(c));
	}
	
}
