
package net.sf.openrocket.util;

import java.awt.geom.Point2D;

public class Bounds {
	
	public final class DimensionBounds {
		public double max;
		public double min;
		
		public DimensionBounds(){
			reset();}
		
		public DimensionBounds( final double _max, final double _min){
			this.min = _min; this.max = _max;}
		
		public void reset(){
			max = Double.MIN_VALUE;
			min = Double.MAX_VALUE; }
		
		public double center(){
			return ((max-min)/2);
		}
		
		/* 
		 * Ensures that the bounds in this dimension cover NO MORE THAN these values
		 * 
		 */
		public void clamp( final double _min, final double _max){
			min = Math.max( min, _min);
			max = Math.min( max, _max);}
		
		
		public boolean contains( final double _testValue){
			if(( min < _testValue ) && ( max > _testValue )){
				return true;}
			return false;}
		
		public boolean containsNAN(){
			return ( Double.isNaN(max) & Double.isNaN(min));}
		
		/* 
		 * Ensures the bounds in this dimension cover NO LESS THAN these values  
		 * 
		 * passing NaN for either value propogates NaN values to this bounds itself
		 */
		public void inflate( final double _newMin, final double _newMax){
			min = Math.min( min, _newMin);
			max = Math.max( max, _newMax);}
		
		public double span(){
			return (max-min);}
	
		public void update( final double _newValue){
			if( min > _newValue ){
				min = _newValue;
			}else if( max < _newValue ){
				max = _newValue;}}
		
		@Override
		public String toString(){
			return String.format("{ min: %6.4  max: %6.4}", min, max);}
		
		public String toDebug(){
			return String.format("{ min: %6.4  max: %6.4  span: %6.4  ctr: %6.4g }", min, max, span(), center());}

	}
	
	// ==== member variables
	private final DimensionBounds[] boundList;
	
	public static final int X=0;
	public static final int Y=1;
	public static final int Z=2;
	public static final int W=2;
	
	
	public Bounds(){
		this(3);
	}
	
	public Bounds create2DBounds(){
		return new Bounds(2);
	}
	
	public Bounds create3DBounds(){
		return new Bounds(3);
	}
	
	public Bounds( final int dimensionCount){
		boundList = new DimensionBounds[dimensionCount];
		for( int i = 0; i < boundList.length; ++i){
			boundList[i] = new DimensionBounds();}	
	}
	
	public DimensionBounds getDim( final int dim ){
		return boundList[dim];
	}
	
	public DimensionBounds getX(){
		return boundList[X];
	}
	public DimensionBounds getY(){
		return boundList[Y];
	}
	public DimensionBounds getZ(){
		return boundList[Z];
	}
	
	public boolean contains( final Point2D.Double testPoint ){
		return contains( testPoint.x, testPoint.y);
	}
	
	public boolean contains( final double _x, final double _y ){
		if( boundList[X].contains(_x) && boundList[Y].contains(_y) ){
			return true;
		}
		return false;
	}

	public boolean contains( final double _x, final double _y, final double _z ){
		if( boundList[X].contains(_x) && boundList[Y].contains(_y) && boundList[Z].contains(_z)){
			return true;
		}
		return false;
	}
	
	public boolean contains( final Coordinate c ){
		return contains( c.x, c.y, c.z);
	}

	public void inflate( final int dimensionIndex, final double newMinimum, final double newMaximum){
		getDim( dimensionIndex ).inflate(newMinimum, newMaximum);
	}
	
	public void update( final double _x, final double _y){
		boundList[X].update(_x);
		boundList[Y].update(_y);
	}
	
	public void update( final Coordinate c ){
		update( c.x, c.y, c.z);
	}
	
	public void update( final double _x, final double _y, final double _z){
		boundList[X].update(_x);
		boundList[Y].update(_y);
		boundList[Z].update(_z);
	}
	
	public Point2D.Double getCenterAsPoint2D(){
		double center_x = 0; 
		double center_y = 0;
		if( 0 < boundList.length ){
			 center_x= boundList[X].center();
		}
	    if( 1 < boundList.length ){
			center_y = boundList[Y].center();
		}
		return new Point2D.Double( center_x, center_y); 
	}
	
	public Point2D.Double getSpanAsPoint2D(){
		double span_x = 0;
		double span_y = 0;
		if( 0 < boundList.length ){
			 span_x = boundList[X].span();
		}
	    if( 1 < boundList.length ){
			span_y = boundList[Y].span();
		}
		return new Point2D.Double( span_x, span_y); 
	}
	
	public double span(final int index){
		return boundList[index].span();
	}
	
	public Coordinate span(){
		return getSpanAsCoordinate();
	}
	
	public int getDimensionCount(){
		return this.boundList.length;
	}
	
	public Coordinate getCenterAsCoordinate(){
		double center_x = 0; 
		double center_y = 0;
		double center_z = 0;
		if( 0 < boundList.length ){
			 center_x= boundList[X].center();
		}
	    if( 1 < boundList.length ){
			center_y = boundList[Y].center();
		}
	    if( 2 < boundList.length ){
			center_z = boundList[Z].center();
		}
		return new Coordinate( center_x, center_y, center_z, 0.0); 
	}
	
	public Coordinate getSpanAsCoordinate(){
		double span_x = 0;
		double span_y = 0;
		double span_z = 0;
		if( 0 < boundList.length ){
			 span_x = boundList[X].span();
		}
	    if( 1 < boundList.length ){
			span_y = boundList[Y].span();
		}
	    if( 2 < boundList.length ){	
			span_z = boundList[Z].span();
		}
	    return new Coordinate(span_x, span_y, span_z);
	}
	
	public void reset(){
		for( DimensionBounds b : boundList ){
			b.reset();
		}
	}
	
	public void clamp( final int index, final double newMin, final double newMax ){
		boundList[index].clamp( newMin, newMax);
	}
	
	public void clampX( final double newMin, final double newMax ){
		clamp( X, newMin, newMax);
	}
	
	public void clampY( final double newMin, final double newMax ){
		clamp( Y, newMin, newMax);
	}
	
	
	public void clampZ( final double newMin, final double newMax ){
		clamp( Z, newMin, newMax);
	}
	
}
