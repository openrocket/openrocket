package net.sf.openrocket.masscalc;

import static net.sf.openrocket.util.MathUtil.pow2;

import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

// implements a simplified, diagonal MOI
public class RigidBody {
	public final Coordinate cm;
	public final double Ixx;
	public final double Iyy;
	public final double Izz;
	
	public static final RigidBody EMPTY = new RigidBody( Coordinate.ZERO, 0., 0., 0.);
	
	public RigidBody( Coordinate _cm, double I_axial, double I_long ){
		this( _cm, I_axial, I_long, I_long );
	}
	
	public RigidBody( Coordinate _cm, double Ixx, double Iyy, double Izz){
		if(( 0 > Ixx)||( 0 > Iyy)||( 0 > Izz)){
			throw new BugException("  attempted to initialize an InertiaMatrix with a negative inertia value.");
		}
		this.cm=_cm;
		this.Ixx = Ixx;
		this.Iyy = Iyy;
		this.Izz = Izz;
	}
	
	public RigidBody add( RigidBody that){
		final Coordinate newCM = this.cm.average( that.cm);
		
		RigidBody movedThis = this.rebase( newCM );
		RigidBody movedThat = that.rebase( newCM );
		
		final double newIxx = movedThis.Ixx + movedThat.Ixx;
		final double newIyy = movedThis.Iyy + movedThat.Iyy;
		final double newIzz = movedThis.Izz + movedThat.Izz;
		
		return new RigidBody( newCM, newIxx, newIyy, newIzz );
	}
	
	public Coordinate getCenterOfMass() { return cm; }
	public Coordinate getCM() { return cm; }
	public double getIyy(){ return Iyy; }
	public double getIxx(){ return Ixx; }
	public double getIzz(){ return Izz; }
	public double getLongitudinalInertia() { return Iyy; }
	public double getMass() { return this.cm.weight; }
	public double getRotationalInertia() { return Ixx; }
	
	
	public boolean isEmpty() {
		if( RigidBody.EMPTY == this) {
			return true;
		}
		return RigidBody.EMPTY.equals( this ); 
	}

	@Override
	public int hashCode() {
		return (int) (Double.doubleToLongBits(this.Ixx) ^ Double.doubleToLongBits(this.Iyy) ^ Double.doubleToLongBits(this.Ixx) );
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof RigidBody))
			return false;
		
		RigidBody other = (RigidBody) obj;
		return (MathUtil.equals(this.Ixx, other.Ixx) && MathUtil.equals(this.Iyy, other.Iyy) &&
				MathUtil.equals(this.Izz, other.Izz)) ;
	}

	public RigidBody rebase( final Coordinate newLocation ){
		final Coordinate delta = this.cm.sub( newLocation ).setWeight(0.);
		double x2 = pow2(delta.x);
		double y2 = pow2(delta.y);
		double z2 = pow2(delta.z);
		
//		final double radialDistanceSquared = (y2 + z2);
//		final double axialDistanceSquared = x2;
//	
//		System.err.println(String.format("              - CM_new = %.8f @[ %.8f, %.8f, %.8f ]", eachGlobal.cm.weight, eachGlobal.cm.x, eachGlobal.cm.y, eachGlobal.cm.z ));
//		System.err.println(String.format("              - each: base:       { %.12f    %.12f }", eachLocal.xx, eachLocal.xx ));
//		System.err.println(String.format("              - each: carrected:  { %.12f    %.12f }", eachGlobal.xx, eachGlobal.yy ));		

		// See: Parallel Axis Theorem in the function comments.
		// I = I + m L^2;    L = sqrt( y^2 + z^2);
		//   ergo:    I = I + m (y^2 + z^2);
		double newIxx = this.Ixx + cm.weight*(y2 + z2);
		double newIyy = this.Iyy + cm.weight*(x2 + z2);
		double newIzz = this.Izz + cm.weight*(x2 + y2);
		
		// MOI about the reference point 
		return new RigidBody( newLocation, newIxx, newIyy, newIzz);
	}
	
	@Override
	public String toString() {
		return toCMString()+" // "+toMOIString();
	}

	public String toCMString() {
		return String.format("CoM: %.8fg @[%.8f,%.8f,%.8f]", cm.weight, cm.x, cm.y, cm.z);
	}

	public String toMOIString() {
		return String.format("MOI: [ %.8f, %.8f, %.8f]", Ixx, Iyy, Izz ); 
	}

	/**
	 * This function returns a <b>copy</b> of this MassData translated to a new location via 
	 * a simplified model. 
	 * 
	 * Assuming rotations are independent, and occur perpendicular to the principal axes, 
	 * The above can be simplified to produce a diagonal newMOI in 
	 * the form of the parallel axis theorem: 
	 *     [ oldMOI + m*d^2, ...]  
	 * 
	 * For the full version of the equations, see: 
	 * [1] https://en.wikipedia.org/wiki/Parallel_axis_theorem#Tensor_generalization 
	 * [2] http://www.kwon3d.com/theory/moi/triten.html
	 * 
	 * 
	 * @param delta vector position from center of mass to desired reference location
	 * 
	 * @return MassData the new MassData instance
	 */
	public RigidBody translateInertia( final Coordinate delta ){
		final Coordinate newLocation = this.cm.add( delta );
		return rebase( newLocation );
	}



}
