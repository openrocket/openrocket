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
public class WeightVector {
	public double x;
	public double y;
	public double z;
	public double w;
	
	public WeightVector(){ 
		this( Double.NaN);
	}
	
	public WeightVector( final double _value ){
		this( _value, _value, _value, _value);
	}
	
	public WeightVector( final double _x, final double _y, final double _z, final double _weight){
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
	
	public WeightVector add( WeightVector other ){
		if (other == null)
				return this;
		
		WeightVector result = new WeightVector();
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
	
	public WeightVector subtract( WeightVector other ){
		if (other == null)
			return this;
	
		WeightVector result = new WeightVector();
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
	
	public WeightVector move( final double _x, final double _y, final double _z){
		this.x -= _x; 
		this.y -= _y;
		this.z -= _z;
		//this.w;  // do not change the weight
		
		return this;
	}
	
	public WeightVector average( WeightVector other ){
		if (other == null)
				return this;
		
		WeightVector result = new WeightVector();
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
	
	public void reset(){
		reset(Double.NaN);
	}
	
	public WeightVector reset( final double val){
		x=val;
		y=val;
		z=val;
		w=val;
		
		return this;
	}
	
}
