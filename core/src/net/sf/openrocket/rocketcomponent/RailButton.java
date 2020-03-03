package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.rocketcomponent.position.AngleMethod;
import net.sf.openrocket.rocketcomponent.position.AnglePositionable;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.position.AxialPositionable;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/** 
 * WARNING:  This class is only partially implemented.  Recommend a bit of testing before you attach it to the GUI.
 * @author widget (Daniel Williams)
 *
 */
public class RailButton extends ExternalComponent implements AnglePositionable, AxialPositionable, LineInstanceable {
	
	private static final Translator trans = Application.getTranslator();
	
	// NOTE: Rail Button ARE NOT STANDARD -- They vary by manufacturer, and model.
	// These presets have appropriate dimensions for each rail size, given the Rail Buttons contribute so little to flying properties. 
	public static final RailButton ROUND_1010 = make1010Button();
	public static final RailButton ROUND_1515 = make1515Button();
	
	/*
	 * Rail Button Dimensions (side view)
	 * 
	 *        > outer dia  <
	 *        |            |              v
	 *   ^     [[[[[[]]]]]]              flangeHeight
	 * total     >||||||<=  inner dia     ^
	 * height     ||||||            v
	 *   v     [[[[[[]]]]]]        standoff == baseHeight 
	 *      ==================      ^
	 *          (body)
	 *   
	 */
	// Note:  the reference point for Rail Button Components is in the center bottom of the button. 
	protected double outerDiameter_m;
	protected double totalHeight_m;
	protected double innerDiameter_m;
	protected double flangeHeight_m;
 	protected double standoff_m;
	
	protected final static double MINIMUM_STANDOFF= 0.001;

	private double radialDistance_m=0;
	protected static final AngleMethod angleMethod = AngleMethod.RELATIVE;
	private double angle_rad = 0;
	private int instanceCount = 1;
	private double instanceSeparation = 0; // front-front along the positive rocket axis. i.e. [1,0,0];
	
	public RailButton(){
		super(AxialMethod.MIDDLE);
		this.outerDiameter_m = 0.0097;
		this.totalHeight_m = 0.0097;		
		this.innerDiameter_m = 0.008;
		this.flangeHeight_m = 0.002;
		this.setStandoff( 0.002);
		this.setInstanceSeparation( this.outerDiameter_m * 6);
		this.setMaterial(Databases.findMaterial(Material.Type.BULK, "Delrin"));
	}
	
	public RailButton( final double od, final double ht ) {
		this();
		this.setOuterDiameter( od);
		this.setTotalHeight( ht);
	}
	
	public RailButton( final double od, final double id, final double ht, final double flangeThickness, final double _standoff ) {
		super(AxialMethod.MIDDLE);
		this.outerDiameter_m = od;
		this.totalHeight_m = ht;
		this.innerDiameter_m = id;
		this.flangeHeight_m = flangeThickness;
		this.setStandoff( _standoff);
		this.setInstanceSeparation( od*2);
		this.setMaterial(Databases.findMaterial(Material.Type.BULK, "Delrin"));
	}
	
	private static final RailButton make1010Button(){
		final double id = 0.008; // guess
		final double od = 0.0097;
		final double ht = 0.0097;
		final double thickness = 0.002; // guess
		final double standoff = 0.002; // guess
		RailButton rb1010 = new RailButton( od, id, ht, thickness, standoff);
		rb1010.setMassOverridden(true);
		rb1010.setOverrideMass(0.0019);
		
		rb1010.setInstanceCount(1);
		rb1010.setInstanceSeparation( od*6 );
		return rb1010;
	}
	
	private static final RailButton make1515Button(){
		final double id = 0.012; // guess
		final double od = 0.016;
		final double ht = 0.0173;
		final double thickness = 0.0032; // guess
		final double standoff = 0.0032;  // guess
		RailButton rb1010 = new RailButton( od, id, ht, thickness, standoff);
		rb1010.setMassOverridden(true);
		rb1010.setOverrideMass(0.0077);
		
		return rb1010;
	}
	
	public double getStandoff(){
		return this.standoff_m;
	}

	public double getBaseHeight(){
		return this.getStandoff();
	}
	
	public double getOuterDiameter() {
		return this.outerDiameter_m;
	}
	
	public double getInnerDiameter() {
		return this.innerDiameter_m;
	}
	
	public double getInnerHeight() {
		return (this.totalHeight_m - this.flangeHeight_m - this.standoff_m);
	}
	
	public double getTotalHeight() {
		return this.totalHeight_m;
	}
	
	public double getFlangeHeight() {
		return this.flangeHeight_m;
	}
	
	
	public void setStandoff( final double newStandoff){
		this.standoff_m = Math.max( newStandoff, RailButton.MINIMUM_STANDOFF );
	}

	public void setInnerDiameter( final double newID ){
		this.innerDiameter_m = newID;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}


	public void setOuterDiameter( final double newOD ){
		this.outerDiameter_m = newOD;
		
		// devel
		this.innerDiameter_m = newOD*0.8;
		this.setInstanceSeparation( newOD*6);

		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	public void setTotalHeight( final double newHeight ) {
		this.totalHeight_m = newHeight;
		
		// devel
		this.flangeHeight_m = newHeight*0.25;
		this.setStandoff( newHeight*0.25);
		
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public void setThickness( final double newThickness ) {
		this.flangeHeight_m = newThickness;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public boolean isAerodynamic(){
		// TODO: implement aerodynamics
		return false;
	}
	
	@Override
	public double getAngleOffset(){
		return angle_rad;
	}
	
	@Override
	public AngleMethod getAngleMethod() {
		return RailButton.angleMethod;
	}

	@Override
	public void setAngleMethod(AngleMethod newMethod) {
		// no-op
	}
	
	
	@Override
	public void setAngleOffset(final double angle_rad){
		double clamped_rad = MathUtil.clamp(angle_rad, -Math.PI, Math.PI);
		
		if (MathUtil.equals(this.angle_rad, clamped_rad))
			return;
		this.angle_rad = clamped_rad;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	
	@Override
	public void setAxialMethod( AxialMethod position) {
		super.setAxialMethod(position);
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	@Override
	public Coordinate[] getInstanceOffsets(){
		Coordinate[] toReturn = new Coordinate[this.getInstanceCount()];
		
		final double yOffset = Math.cos(this.angle_rad) * ( this.radialDistance_m );
		final double zOffset = Math.sin(this.angle_rad) * ( this.radialDistance_m );
		
		for ( int index=0; index < this.getInstanceCount(); index++){
			toReturn[index] = new Coordinate(index*this.instanceSeparation, yOffset, zOffset);
		}
		
		return toReturn;
	}
	
	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.RAIL_BUTTON;
	}
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		super.componentChanged(e);
		
		RocketComponent body;
		double parentRadius=0;
		
		for (body = this.getParent(); body != null; body = body.getParent()) {
			if (body instanceof BodyTube)
				parentRadius = ((BodyTube) body).getOuterRadius();
		}
		
		this.radialDistance_m = parentRadius;
	}
	
	
	@Override
	public double getComponentVolume() {
		final double volOuter = Math.PI*Math.pow( outerDiameter_m/2, 2)*flangeHeight_m;
		final double volInner = Math.PI*Math.pow( innerDiameter_m/2, 2)*getInnerHeight();
		final double volStandoff = Math.PI*Math.pow( outerDiameter_m/2, 2)*standoff_m;
		return (volOuter+
				volInner+
				volStandoff);
	}
	
	@Override
	public double getInstanceSeparation(){
		return this.instanceSeparation;
	}
	
	@Override
	public void setInstanceSeparation(final double _separation){
		this.instanceSeparation = _separation;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	@Override
	public void setInstanceCount( final int newCount ){
		if( 0 < newCount ){
			this.instanceCount = newCount;
		}
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public int getInstanceCount(){
		return this.instanceCount;
	}

	@Override
	public String getPatternName(){
		return (this.getInstanceCount() + "-Line");
	}

	@Override
	public Collection<Coordinate> getComponentBounds() {
		final double r = outerDiameter_m / 2.0;
		ArrayList<Coordinate> set = new ArrayList<Coordinate>();
		set.add(new Coordinate(r, totalHeight_m, r));
		set.add(new Coordinate(r, totalHeight_m, -r));
		set.add(new Coordinate(r, 0, r));
		set.add(new Coordinate(r, 0, -r));
		set.add(new Coordinate(-r, 0, r));
		set.add(new Coordinate(-r, 0, -r));
		set.add(new Coordinate(-r, totalHeight_m, r));
		set.add(new Coordinate(-r, totalHeight_m, -r));
		return set;
	}
	
	@Override
	public Coordinate getComponentCG() {
		// Math.PI and density are assumed constant through calculation, and thus may be factored out. 
		final double volumeFlange = Math.pow( outerDiameter_m/2, 2)*flangeHeight_m;
		final double volumeInner = Math.pow( innerDiameter_m/2, 2)*(getInnerHeight());
		final double volumeStandoff = Math.pow( outerDiameter_m/2, 2)*this.standoff_m;
		final double totalVolume = volumeFlange + volumeInner + volumeStandoff;
		final double heightCM = (volumeFlange*( this.totalHeight_m-getFlangeHeight()/2) + volumeInner*( this.standoff_m + this.getInnerHeight()/2) + volumeStandoff*(this.standoff_m/2))/totalVolume;

		if( heightCM > this.totalHeight_m ){
			throw new BugException(" bug found while computing the CG of a RailButton: "+this.getName()+"\n height of CG: "+heightCM);
		}
		
		final double CMy = Math.cos(this.angle_rad)*heightCM;
		final double CMz = Math.sin(this.angle_rad)*heightCM;
		
		return new Coordinate( 0, CMy, CMz, getComponentMass());
	}
	
	@Override
	public String getComponentName() {
		return trans.get("RailButton.RailButton");
	}
	
	@Override
	public double getLongitudinalUnitInertia() {
		// 1/12 * (3 * (r2^2 + r1^2) + h^2)
//		return (3 * (MathUtil.pow2(getOuterRadius()) + MathUtil.pow2(getInnerRadius())) + MathUtil.pow2(getLength())) / 12;
		return 0.0;
	}
	
	@Override
	public double getRotationalUnitInertia() {
		// 1/2 * (r1^2 + r2^2)
//		return (MathUtil.pow2(getInnerRadius()) + MathUtil.pow2(getOuterRadius())) / 2;
		return 0.0;
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		// Allow nothing to be attached to a LaunchButton
		return false;
	}

}
