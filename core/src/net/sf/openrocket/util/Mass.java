package net.sf.openrocket.util;


/**
 * This class is intended to locate a quantity (the weight) at a location (x,y,z).  Therefore, if the quantity is nonexistent, the coordinates don't matter.   Use this class to represent a: area-centroid, point-mass, or center-of-mass.
 * 
 * This class is very similar to net.sf.openrocket.util.Coordinate, but behaves differently-- especially w.r.t. the resultant weight during certain operations.
 * This class is implemented separately to preserve existing behavior of code which uses Coordinate, and is intended to eventually be a replacement for some cases.
 * 
 * @author Daniel Williams <equipoise@gmail.com>
 *
 */
public class Mass{
	public double x;
	public double y;
	public double z;
	public double w;
	
	public Mass(){ 
		this( Double.NaN);
	}
	
	public Mass( final double _value ){
		this( _value, _value, _value, _value);
	}

	public Mass( final Coordinate source){
		this( source.x, source.y, source.z, source.weight);
	}
	
	public Mass( final double _x, final double _y, final double _z, final double _weight){
		x=_x;
		y=_y;
		z=_z;
		w=_weight;
	}
	
	public boolean isNaN(){
		return  Double.isNaN(w) &&
				Double.isNaN(x) &&
				Double.isNaN(y) &&
				Double.isNaN(z);
	}
	
	public Mass add( Mass other ){
		if (other == null)
				return this;
		
		Mass result = new Mass();
		result.w = w + other.w;
		
		if ( Math.abs( result.w) < MathUtil.EPSILON) {
			result.reset(0);
		} else {
			result.x = (this.x * this.w + other.x * other.w) / result.w;
			result.y = (this.y * this.w + other.y * other.w) / result.w;
			result.z = (this.z * this.w + other.z * other.w) / result.w;
		}
		
		return result;
	}

	public Mass average( Mass other ){
		if (other == null)
				return this;
		
		Mass result = new Mass();
		final double totalWeight = w + other.w;
		
		if ( Math.abs( totalWeight) < MathUtil.EPSILON ){
			result.reset(0);
		} else {
			result.x = (this.x * this.w + other.x * other.w) / totalWeight;
			result.y = (this.y * this.w + other.y * other.w) / totalWeight;
			result.z = (this.z * this.w + other.z * other.w) / totalWeight;
			result.w = (w + other.w)/2;
		}
		
		return result;
	}
	
	/**
	 * Tests whether the coordinates are the equal.
	 * 
	 * @param other  Coordinate to compare to.
	 * @return  true if the coordinates are equal (x, y, z and weight)
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Mass))
			return false;
		
		final Mass om = (Mass) other;
		return (MathUtil.equals(this.x, om.x) &&
				MathUtil.equals(this.y, om.y) &&
				MathUtil.equals(this.z, om.z) && 
				MathUtil.equals(this.w, om.w));
	}
	
	/**
	 * Hash code method compatible with {@link #equals(Object)}.
	 */
	@Override
	public int hashCode() {
		return (int) (x*1000000 + y*10000 + z);
	}
	

	// move by the supplied deltas
	public Mass move( final double x_delta, final double y_delta, final double z_delta){
		this.x += x_delta; 
		this.y += y_delta;
		this.z += z_delta;
		//this.w;  // do not change the weight
		
		return this;
	}
	
	public Mass subtract( Mass other ){
		if (other == null)
			return this;
	
		Mass result = new Mass();
		result.w = w - other.w;
		
		if ( Math.abs( result.w ) < MathUtil.EPSILON ){
			result.reset(0);
		} else {
			result.x = (this.x * this.w - other.x * other.w) / result.w;
			result.y = (this.y * this.w - other.y * other.w) / result.w;
			result.z = (this.z * this.w - other.z * other.w) / result.w;
		}
		return result;
	}
	
	public void reset(){
		reset(Double.NaN);
	}
	
	public Mass reset( final double val){
		x=val;
		y=val;
		z=val;
		w=val;
		
		return this;
	}
	
	public Mass scaleWeight( final double scaleFactor ){
		this.w *= scaleFactor;
		return this;
	}
	
	public Coordinate toCoordinate(){
		return new Coordinate( x,y,z,w);
	}
	
	@Override
	public String toString() {
		return String.format("(%.3f,%.3f,%.3f,w=%.3f)", x, y, z, w);
	}

	public static Mass empty() {
		return new Mass(0.0);
	}
	
	public static Mass nan() {
		return new Mass( Double.NaN);
	}

	public Mass copy() {
		return new Mass( x, y, z, w);
	}
	
	
}
