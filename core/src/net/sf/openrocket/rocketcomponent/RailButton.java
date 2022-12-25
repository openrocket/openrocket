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
import net.sf.openrocket.util.BoundingBox;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/** 
 * WARNING:  This class is only partially implemented.  Recommend a bit of testing before you attach it to the GUI.
 * @author widget (Daniel Williams)
 *
 */
public class RailButton extends ExternalComponent implements AnglePositionable, AxialPositionable, BoxBounded, LineInstanceable {
	
	private static final Translator trans = Application.getTranslator();
	
	/*
	 * Rail Button Dimensions (side view)
	 * 
	 *        > outer dia  <
	 *        |            |              v
	 *   ^     [[[[[[]]]]]]              flangeHeight
	 * total     >||||||<=  inner dia     ^
	 * height     ||||||            v
	 *   v     [[[[[[]]]]]]        baseHeight / standoff
	 *      ==================      ^
	 *          (body)
	 *   
	 */
	// Note:  the reference point for Rail Button Components is in the center bottom of the button. 
	protected double outerDiameter_m;
	protected double innerDiameter_m;
	protected double totalHeight_m;
	protected double flangeHeight_m;
 	protected double baseHeight_m;
	protected double screwHeight_m;


	private double radialDistance_m=0;
	protected static final AngleMethod angleMethod = AngleMethod.RELATIVE;
	private double angle_rad = Math.PI;
	private int instanceCount = 1;
	private double instanceSeparation = 0; // front-front along the positive rocket axis. i.e. [1,0,0];
	
	public RailButton(){
		super(AxialMethod.MIDDLE);
		this.outerDiameter_m = 0.0097;
		this.totalHeight_m = 0.0097;
		this.innerDiameter_m = 0.008;
		this.flangeHeight_m = 0.002;
		this.setBaseHeight(0.002);
		this.setInstanceSeparation( this.outerDiameter_m * 6);
		this.setMaterial(Databases.findMaterial(Material.Type.BULK, "Delrin"));
		super.displayOrder_side = 14;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 11;		// Order for displaying the component in the 2D back view
	}
	
	public RailButton( final double od, final double ht ) {
		this();
		this.setOuterDiameter(od);
		this.setTotalHeight(ht);
		super.displayOrder_side = 14;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 11;		// Order for displaying the component in the 2D back view
	}
	
	public RailButton( final double od, final double id, final double ht, final double _flangeHeight, final double _baseHeight ) {
		super(AxialMethod.MIDDLE);
		this.outerDiameter_m = od;
		this.totalHeight_m = ht;
		this.innerDiameter_m = id;
		this.flangeHeight_m = _flangeHeight;
		this.setBaseHeight(_baseHeight);
		this.setInstanceSeparation( od*2);
		this.setMaterial(Databases.findMaterial(Material.Type.BULK, "Delrin"));
		super.displayOrder_side = 14;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 11;		// Order for displaying the component in the 2D back view
	}

	public double getBaseHeight(){
		return this.baseHeight_m;
	}
	
	public double getOuterDiameter() {
		return this.outerDiameter_m;
	}
	
	public double getInnerDiameter() {
		return this.innerDiameter_m;
	}
	
	public double getInnerHeight() {
		return (this.totalHeight_m - this.flangeHeight_m - this.baseHeight_m);
	}
	
	public double getTotalHeight() {
		return this.totalHeight_m;
	}
	
	public double getFlangeHeight() {
		return this.flangeHeight_m;
	}

	public double getScrewHeight() {
		return this.screwHeight_m;
	}

	
	public void setBaseHeight(double newBaseHeight){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RailButton) {
				((RailButton) listener).setBaseHeight(newBaseHeight);
			}
		}

		this.baseHeight_m = Math.max(newBaseHeight, 0);
		this.baseHeight_m = Math.min(this.baseHeight_m, getMaxBaseHeight());
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	public void setFlangeHeight(double newFlangeHeight){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RailButton) {
				((RailButton) listener).setFlangeHeight(newFlangeHeight);
			}
		}

		this.flangeHeight_m = Math.max(newFlangeHeight, 0);
		this.flangeHeight_m = Math.min(this.flangeHeight_m, getMaxFlangeHeight());
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	public void setTotalHeight(double newHeight ) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RailButton) {
				((RailButton) listener).setTotalHeight(newHeight);
			}
		}

		this.totalHeight_m = Math.max(newHeight, getMinTotalHeight());

		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	public double getMaxBaseHeight() {
		return this.totalHeight_m - this.flangeHeight_m;
	}

	public double getMaxFlangeHeight() {
		return this.totalHeight_m - this.baseHeight_m;
	}

	public double getMinTotalHeight() {
		return this.baseHeight_m + this.flangeHeight_m;
	}

	public void setScrewHeight(double height) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RailButton) {
				((RailButton) listener).setScrewHeight(height);
			}
		}

		this.screwHeight_m = Math.max(height, 0);
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	public void setInnerDiameter(double newID ){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RailButton) {
				((RailButton) listener).setInnerDiameter(newID);
			}
		}

		this.innerDiameter_m = Math.min(newID, this.outerDiameter_m);
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}


	public void setOuterDiameter(double newOD ){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RailButton) {
				((RailButton) listener).setOuterDiameter(newOD);
			}
		}

		this.outerDiameter_m = newOD;
		setInnerDiameter(this.innerDiameter_m);

		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
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
	public void setAngleOffset(double angle_rad){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RailButton) {
				((RailButton) listener).setAngleOffset(angle_rad);
			}
		}

		double clamped_rad = MathUtil.clamp(angle_rad, -Math.PI, Math.PI);
		
		if (MathUtil.equals(this.angle_rad, clamped_rad))
			return;
		this.angle_rad = clamped_rad;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	
	@Override
	public void setAxialMethod( AxialMethod position) {
		super.setAxialMethod(position);
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}

	@Override
	public BoundingBox getInstanceBoundingBox(){
		BoundingBox instanceBounds = new BoundingBox();
		
		instanceBounds.update(new Coordinate(0, this.totalHeight_m + this.screwHeight_m, 0));
		instanceBounds.update(new Coordinate(0, -this.totalHeight_m - this.screwHeight_m, 0));
		
		final double r = this.getOuterDiameter() / 2;
		instanceBounds.update(new Coordinate(r, 0, r));
		instanceBounds.update(new Coordinate(-r, 0, -r));
		
		return instanceBounds;
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
			if (body instanceof BodyTube) {
				parentRadius = ((BodyTube) body).getOuterRadius();
				break;
			}
		}
		
		this.radialDistance_m = parentRadius;
	}
	
	
	@Override
	public double getComponentVolume() {
		final double volOuter = Math.PI*Math.pow( outerDiameter_m/2, 2)*flangeHeight_m;
		final double volInner = Math.PI*Math.pow( innerDiameter_m/2, 2)*getInnerHeight();
		final double volStandoff = Math.PI*Math.pow( outerDiameter_m/2, 2)* baseHeight_m;
		final double volScrew = 2f/3 * Math.PI * MathUtil.pow2(outerDiameter_m/2) * screwHeight_m;
		return volOuter + volInner + volStandoff + volScrew;
	}
	
	@Override
	public double getInstanceSeparation(){
		return this.instanceSeparation;
	}
	
	@Override
	public void setInstanceSeparation(double _separation){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RailButton) {
				((RailButton) listener).setInstanceSeparation(_separation);
			}
		}

		this.instanceSeparation = _separation;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	@Override
	public void setInstanceCount(int newCount ){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof RailButton) {
				((RailButton) listener).setInstanceCount(newCount);
			}
		}

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
		final double massBase = Math.pow(outerDiameter_m / 2, 2) * this.baseHeight_m;
		final double massInner = Math.pow(innerDiameter_m / 2, 2)* getInnerHeight();
		final double massFlange = Math.pow(outerDiameter_m / 2, 2)* this.flangeHeight_m;
		final double massScrew = 2f/3 * MathUtil.pow2(outerDiameter_m/2) * screwHeight_m;
		final double totalMass = massFlange + massInner + massBase + massScrew;
		final double baseCM = this.baseHeight_m /2;
		final double innerCM = this.baseHeight_m + this.getInnerHeight()/2;
		final double flangeCM = this.totalHeight_m - getFlangeHeight()/2;
		final double screwCM = this.totalHeight_m + 4 * this.screwHeight_m / (3 * Math.PI);
		final double heightCM = (massBase*baseCM + massInner*innerCM + massFlange*flangeCM + massScrew*screwCM)/totalMass;

		if (heightCM > this.totalHeight_m + this.screwHeight_m) {
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
//		return (3 * (MathUtil.pow2(getOuterRadius()) + MathUtil.pow2(getInnerRadius())) + MathUtil.pow2(getLengthAerodynamic())) / 12;
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

	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		super.loadFromPreset(preset);
		if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
			this.outerDiameter_m = preset.get(ComponentPreset.OUTER_DIAMETER);
		}
		if (preset.has(ComponentPreset.INNER_DIAMETER)) {
			this.innerDiameter_m = preset.get(ComponentPreset.INNER_DIAMETER);
		}
		if (preset.has(ComponentPreset.HEIGHT)) {
			this.totalHeight_m = preset.get(ComponentPreset.HEIGHT);
		}
		if (preset.has(ComponentPreset.FLANGE_HEIGHT)) {
			this.flangeHeight_m = preset.get(ComponentPreset.FLANGE_HEIGHT);
		}
		if (preset.has(ComponentPreset.BASE_HEIGHT)) {
			this.baseHeight_m = preset.get(ComponentPreset.BASE_HEIGHT);
		}
		if (preset.has(ComponentPreset.SCREW_HEIGHT)) {
			this.screwHeight_m = preset.get(ComponentPreset.SCREW_HEIGHT);
		}
		if (preset.has(ComponentPreset.CD) && preset.get(ComponentPreset.CD) > 0) {
			setCDOverridden(true);
			setOverrideCD(preset.get(ComponentPreset.CD));
		}

		double totalMass = 0;
		boolean massOverridden = false;
		if (preset.has(ComponentPreset.MASS)) {
			massOverridden = true;
			totalMass += preset.get(ComponentPreset.MASS);
		}
		if (preset.has(ComponentPreset.SCREW_MASS)) {
			massOverridden = true;
			totalMass += preset.get(ComponentPreset.SCREW_MASS);
		}
		if (preset.has(ComponentPreset.NUT_MASS)) {
			massOverridden = true;
			totalMass += preset.get(ComponentPreset.NUT_MASS);
		}
		if (massOverridden) {
			setMassOverridden(true);
			setOverrideMass(totalMass);
		}

		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
}
