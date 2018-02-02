package net.sf.openrocket.util;

import java.util.ArrayList;
import java.util.Collection;

public class BoundingBox {
	public Coordinate min;
	public Coordinate max;
	
	public BoundingBox() {
		min = Coordinate.MAX.setWeight( 0.0);
		max = Coordinate.MIN.setWeight( 0.0);
	}
	
	public BoundingBox( Coordinate _min, Coordinate _max) {
		this();
		this.min = _min.clone();
		this.max = _max.clone();
	}
	
	public BoundingBox( Coordinate[] list ) {
		this();
		this.compare( list);
	}
	
	public BoundingBox( Collection<Coordinate> list ) {
		this();
		this.compare( list.toArray( new Coordinate[0] ));
	}
	
	@Override
	public BoundingBox clone() {
		return new BoundingBox( this.min, this.max );
	}
	
	public void compare( Coordinate c ) {
		compare_against_min(c);
		compare_against_max(c);
	}
	
	protected void compare_against_min( Coordinate c ) {
		if( min.x > c.x )
			min = min.setX( c.x );
		if( min.y > c.y )
			min = min.setY( c.y );
		if( min.z > c.z )
			min = min.setZ( c.z );
	}
	
	protected void compare_against_max( Coordinate c) {
		if( max.x < c.x )
			max = max.setX( c.x );
		if( max.y < c.y )
			max = max.setY( c.y );
		if( max.z < c.z )
			max = max.setZ( c.z );
	}

	public BoundingBox compare( Coordinate[] list ) {
		for( Coordinate c: list ) {
			compare( c );
		}
		return this;
	}
	
	public void compare( BoundingBox other ) {
		compare_against_min( other.min);
		compare_against_max( other.max);
	}
	
	public Coordinate span() { return max.sub( min ); }

	public Coordinate[] toArray() {
		return new Coordinate[] { this.min, this.max };
	}
	
	public Collection<Coordinate> toCollection(){
		Collection<Coordinate> toReturn = new ArrayList<Coordinate>();
		toReturn.add( this.max);
		toReturn.add( this.min);
		return toReturn;
	}
	
	@Override
	public String toString() {
//		return String.format("[( %6.4f, %6.4f, %6.4f) < ( %6.4f, %6.4f, %6.4f)]",
		return String.format("[( %g, %g, %g) < ( %g, %g, %g)]",
				min.x, min.y, min.z,
				max.x, max.y, max.z );
	}
}
