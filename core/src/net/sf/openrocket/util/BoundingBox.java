package net.sf.openrocket.util;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

public class BoundingBox {
	public Coordinate min;
	public Coordinate max;

	public BoundingBox() {
	    clear();
	}
	
	public BoundingBox( Coordinate _min, Coordinate _max) {
		this();
		this.min = _min.clone();
		this.max = _max.clone();
	}
	
	public void clear() {
		min = Coordinate.MAX.setWeight( 0.0);
		max = Coordinate.MIN.setWeight( 0.0);
	}
	
	@Override
	public BoundingBox clone() {
		return new BoundingBox( this.min, this.max );
	}

	public boolean isEmpty(){
		if( (min.x > max.x) ||
			(min.y > max.y) ||
			(min.z > max.z)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Generate a new bounding box, transformed by the given transformation matrix
	 *
	 * Note: This implementation is not _exact_!  Do not use this for Aerodynamic, Mass or Simulation calculations....
	 *       But it will be sufficiently close for UI purposes.
	 *
 	 * @return a new box, transform by the given transform
	 */
	public BoundingBox transform(Transformation transform){
		final Coordinate p1 = transform.transform(this.min);
		final Coordinate p2 = transform.transform(this.max);

		final BoundingBox newBox = new BoundingBox();
		newBox.update(p1);
		newBox.update(p2);

		return newBox;
	}

	private void update_x_min( final double xVal) {
		if( min.x > xVal)
			min = min.setX( xVal );
	}
	
	private void update_y_min( final double yVal) {
		if( min.y > yVal )
			min = min.setY( yVal );
	}
	
	private void update_z_min( final double zVal) {
		if( min.z > zVal )
			min = min.setZ( zVal );
	}
	
	private void update_x_max( final double xVal) {
		if( max.x < xVal )
			max = max.setX( xVal );
	}
	
	private void update_y_max( final double yVal) {
		if( max.y < yVal )
			max = max.setY( yVal );
	}
	
	private void update_z_max( final double zVal) {
		if( max.z < zVal )
			max = max.setZ( zVal );
	}
	
	public BoundingBox update( final double val) {
		update_x_min(val);
		update_y_min(val);
		update_z_min(val);
		
		update_x_max(val);
		update_y_max(val);
		update_z_max(val);
		return this;
	}

	public BoundingBox update( Coordinate c ) {
		update_x_min(c.x);
		update_y_min(c.y);
		update_z_min(c.z);

		update_x_max(c.x);
		update_y_max(c.y);
		update_z_max(c.z);

		return this;
	}

    public BoundingBox update( Rectangle2D rect ) {
	    update_x_min(rect.getMinX());
	    update_y_min(rect.getMinY());
	    update_x_max(rect.getMaxX());
	    update_y_max(rect.getMaxY());
	    return this;
	}
	
	public BoundingBox update( final Coordinate[] list ) {
		for( Coordinate c: list ) {
			update( c );
		}
		return this;
	}
	
	public BoundingBox update( Collection<Coordinate> list ) {
		for( Coordinate c: list ) {
			update( c );
		}
       return this; 
    }
	
	public BoundingBox update( BoundingBox other ) {
		if(other.isEmpty()){
			return this;
		}
		update_x_min(other.min.x);
		update_y_min(other.min.y);
		update_z_min(other.min.z);
		
		update_x_max(other.max.x);
		update_y_max(other.max.y);
		update_z_max(other.max.z);
		return this;
	}
	
	public Coordinate span() { return max.sub( min ); }

	public Coordinate[] toArray() {
		return new Coordinate[] { this.min, this.max };
	}
	
	public Collection<Coordinate> toCollection(){
		Collection<Coordinate> toReturn = new ArrayList<>();
		toReturn.add( this.max);
		toReturn.add( this.min);
		return toReturn;
	}
	
	public Rectangle2D toRectangle() {
		return new Rectangle2D.Double(min.x, min.y, (max.x-min.x), (max.y - min.y));
    }
	
	@Override
	public String toString() {
		return String.format("[( %g, %g, %g) < ( %g, %g, %g)]",
				min.x, min.y, min.z,
				max.x, max.y, max.z );
	}
}
