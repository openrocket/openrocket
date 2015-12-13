package net.sf.openrocket.masscalc;

import static net.sf.openrocket.util.MathUtil.pow2;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/**
 * An immutable value object containing the mass data of a component, assembly or entire rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 * @author Daniel Williams <equipoise@gmail.com>
 */
public class MassData {
	private static final double MIN_MASS = MathUtil.EPSILON;
	 
	/* ASSUME: a MassData instance implicitly describes a bodies w.r.t. a reference point 
	 *     a) the cm locates the body from the reference point
	 *     b) each MOI is about the reference point.   
	 */ 
	public final Coordinate cm;
	
	// Moment of Inertias:
	//    x-axis: through the rocket's nose
	//    y,z axes: mutually perpendicular to each other and the x-axis. Because a rocket 
	//        is (mostly) axisymmetric, y-axis and z-axis placement is arbitrary.
	
	// MOI about the Center of Mass
	private final InertiaMatrix I_cm;
	
	// implements a simplified, diagonal MOI
	private class InertiaMatrix {
		public final double xx;
		public final double yy;
		public final double zz;
		
		public InertiaMatrix( double Ixx, double Iyy, double Izz){
			if(( 0 > Ixx)||( 0 > Iyy)||( 0 > Izz)){
				throw new BugException("  attempted to initialize an InertiaMatrix with a negative inertia value.");
			}
			this.xx = Ixx;
			this.yy = Iyy;
			this.zz = Izz;
		}
		
		public InertiaMatrix add( InertiaMatrix other){
			return new InertiaMatrix( this.xx + other.xx, this.yy + other.yy, this.zz + other.zz);
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
		private InertiaMatrix translateInertia( final Coordinate delta, final double mass){
			double x2 = pow2(delta.x);
			double y2 = pow2(delta.y);
			double z2 = pow2(delta.z);
			
			// See: Parallel Axis Theorem in the function comments.
			// I = I + m L^2;    L = sqrt( y^2 + z^2);
			//   ergo:    I = I + m (y^2 + z^2);
			double newIxx = this.xx + mass*(y2  + z2);
			double newIyy = this.yy + mass*(x2  + z2);
			double newIzz = this.zz + mass*(x2  + y2);
			
			// MOI about the reference point 
			InertiaMatrix toReturn=new InertiaMatrix( newIxx, newIyy, newIzz);
			return toReturn;
		}
		
		@Override
		public int hashCode() {
			return (int) (Double.doubleToLongBits(this.xx) ^ Double.doubleToLongBits(this.yy) ^ Double.doubleToLongBits(this.xx) );
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof InertiaMatrix))
				return false;
			
			InertiaMatrix other = (InertiaMatrix) obj;
			return (MathUtil.equals(this.xx, other.xx) && MathUtil.equals(this.yy, other.yy) &&
					MathUtil.equals(this.zz, other.zz)) ;
		}

	}
	
	public static final MassData ZERO_DATA = new MassData(Coordinate.ZERO, 0, 0, 0);
	
	public MassData() {
		this.cm = Coordinate.ZERO;
		this.I_cm = new InertiaMatrix(0,0,0);
	}
	

	/**
	 * Return a new instance of MassData which is a sum of this and the other.  
     *
	 * @param childData second mass data to combine with this
	 * 
	 * @return MassData the new MassData instance
	 */
	public MassData add(MassData body2){
		MassData body1 = this;
		
		// the combined Center of mass is defined to be 'point 3'
		Coordinate combinedCM = body1.cm.average( body2.cm );
		
		// transform InertiaMatrix from it's previous frame to the common frame
		Coordinate delta1 = combinedCM.sub( body1.cm).setWeight(0);
		InertiaMatrix I1_at_3 = body1.I_cm.translateInertia(delta1, body1.getMass());

		// transform InertiaMatrix from it's previous frame to the common frame
		Coordinate delta2 = combinedCM.sub( body2.cm).setWeight(0);
		InertiaMatrix I2_at_3 = body2.I_cm.translateInertia(delta2, body2.getMass());
		
		// once both are in the same frame, simply add them
		InertiaMatrix combinedMOI = I1_at_3.add(I2_at_3);
		MassData sumData = new MassData( combinedCM, combinedMOI);
		
		{ // vvvv DEVEL vvvv
//			System.err.println("    ?? body1:  "+  body1.toDebug() );
//			System.err.println("       delta 1=>3:  "+  delta1);
//			System.err.println(String.format("       >> 1@3: ==                                [ %g, %g, %g ]",
//					I1_at_3.xx, I1_at_3.yy, I1_at_3.zz)); 
//			
//			System.err.println("    ?? body2:  "+  body2.toDebug() );
//			System.err.println("       delta 2=>3:  "+  delta2);
//			System.err.println(String.format("       >> 2@3:                                   [ %g, %g, %g ]",
//					I2_at_3.xx, I2_at_3.yy, I2_at_3.zz)); 
//			System.err.println("    ?? asbly3: "+sumData.toDebug()+"\n");
			
//			InertiaMatrix rev1 = It1.translateInertia(delta1.multiply(-1), body1.getMass());
//			System.err.println(String.format("        !!XX orig: %s\n",  childDataChild.toDebug() ));
//			System.err.println(String.format("%s!!XX revr: %s\n", indent, reverse.toDebug() ));
		}
		
		return sumData;
	}
	
	private MassData(Coordinate newCM, InertiaMatrix newMOI){
		this.cm = newCM;
		this.I_cm = newMOI;
	}
	
	public MassData( RocketComponent component ){
		//MassData parentData = new MassData( parentCM, parentIx, parentIt);
		
		// Calculate data for this component
		Coordinate newCM = component.getComponentCG();
		double mass = newCM.weight;
		if ( mass < MIN_MASS){
			newCM = newCM .setWeight(MIN_MASS);
			mass = MIN_MASS;
		}
		this.cm = newCM;
		double Ix = component.getRotationalUnitInertia() * mass;
		double It = component.getLongitudinalUnitInertia() * mass;
		this.I_cm = new InertiaMatrix( Ix, It, It);
	}
	
	public MassData(Coordinate newCM, double newIxx, double newIyy, double newIzz){
		if (newCM == null) {
			throw new IllegalArgumentException("CM is null");
		}
		
		this.cm = newCM;
		this.I_cm = new InertiaMatrix(newIxx, newIyy, newIzz);
	}
	
	public MassData(final Coordinate cg, final double rotationalInertia, final double longitudinalInertia) {
		if (cg == null) {
			throw new IllegalArgumentException("cg is null");
		}
		this.cm = cg;
		double newIxx = rotationalInertia;
		double newIyy = longitudinalInertia;
		double newIzz = longitudinalInertia;
		
		this.I_cm = new InertiaMatrix( newIxx, newIyy, newIzz);
	}

	public double getMass(){
		return cm.weight;
	}
	
	public Coordinate getCG() {
		return cm;
	}

	public Coordinate getCM() {
		return cm;
	}
	
	public double getIyy(){
		return I_cm.yy;
	}
	public double getLongitudinalInertia() {
		return I_cm.yy;
	}
	
	public double getIxx(){
		return I_cm.xx;
	}
	public double getRotationalInertia() {
		return I_cm.xx;
	}
	
	public double getIzz(){
		return I_cm.zz;
	}
	
	// this is a tacked-on hack.
	double m_p = Double.NaN; 
	public void setPropellantMass( final double _mp){
		this.m_p = _mp;
	}
	
	public double getPropellantMass(){
		return this.m_p;
	}
	
	/**
	 * Return a new instance of MassData moved by the delta vector supplied.
	 * This function is intended to move the REFERENCE POINT, not the CM, and will leave 
	 * the Inertia matrix completely unchanged.
	 *   
	 * ASSUME: MassData implicity describe their respective bodies w.r.t 0,0,0) 
	 *     a) the cm locates the body from the reference point
	 *     b) each MOI is about the reference point.   
	 * 
	 * @param delta vector from current reference point to new/desired reference point (mass is ignored)
	 * 
	 * @return MassData a new MassData instance, locating the same CM from a different reference point.
	 */
	public MassData move( final Coordinate delta ){
		MassData body1 = this;
		
		// don't change the mass, just move it.
		Coordinate newCM = body1.cm.add( delta );

		MassData newData = new MassData( newCM, this.I_cm);
		
		{ // DEVEL-DEBUG
//			System.err.println("    ?? body1:  "+  body1.toDebug() );
//			System.err.println("       delta:  "+  delta);
//			System.err.println("    ?? asbly3: "+newData.toDebug()+"\n");
		}
		return newData;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MassData))
			return false;
		
		MassData other = (MassData) obj;
		return (this.cm.equals(other.cm) && (this.I_cm.equals(other.I_cm)));
	}
	
	@Override
	public int hashCode() {
		return (int) (cm.hashCode() ^ I_cm.hashCode() );
	}

	public String toCMDebug(){
		return String.format("cm= %6.4fg@[%g,%g,%g]", cm.weight, cm.x, cm.y, cm.z); 
	}
	
	public String toDebug(){
		return toCMDebug()+"  " + 
				String.format("I_cm=[ %g, %g, %g ]", I_cm.xx, I_cm.yy, I_cm.zz); 
	}

	@Override
	public String toString() {
		return "MassData [cg=" + cm + ", longitudinalInertia=" + getIyy()
				+ ", rotationalInertia=" + getIxx() + "]";
	}

}
