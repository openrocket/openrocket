package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

public class ParallelStage extends AxialStage implements FlightConfigurableComponent, RingInstanceable {
	
	private static final Translator trans = Application.getTranslator();
	//private static final Logger log = LoggerFactory.getLogger(BoosterSet.class);
	
	protected int count = 1;

	protected double angularSeparation = Math.PI;
	protected double angularPosition_rad = 0;
	protected boolean autoRadialPosition = false;
	protected double radialPosition_m = 0;
	
	public ParallelStage() {
		this.count = 2;
		this.relativePosition = Position.BOTTOM;
		this.angularSeparation = Math.PI * 2 / this.count;
	}
	
	public ParallelStage( final int _count ){
		this();
		
		this.count = _count;
		this.angularSeparation = Math.PI * 2 / this.count;
	}
	
	@Override
	public String getComponentName() {
		//// Stage
		return trans.get("BoosterSet.BoosterSet");
	}
	
	// not strictly accurate, but this should provide an acceptable estimate for total vehicle size 
	@Override
	public Collection<Coordinate> getComponentBounds() {
		Collection<Coordinate> bounds = new ArrayList<Coordinate>(8);
		double x_min = Double.MAX_VALUE;
		double x_max = Double.MIN_VALUE;
		double r_max = 0;
		
		Coordinate[] instanceLocations = this.getLocations();
		
		for (Coordinate currentInstanceLocation : instanceLocations) {
			if (x_min > (currentInstanceLocation.x)) {
				x_min = currentInstanceLocation.x;
			}
			if (x_max < (currentInstanceLocation.x + this.length)) {
				x_max = currentInstanceLocation.x + this.length;
			}
			if (r_max < (this.getRadialOffset())) {
				r_max = this.getRadialOffset();
			}
		}
		addBound(bounds, x_min, r_max);
		addBound(bounds, x_max, r_max);
		
		return bounds;
	}
	
	/**
	 * Check whether the given type can be added to this component.  A Stage allows
	 * only BodyComponents to be added.
	 *
	 * @param type The RocketComponent class type to add.
	 *
	 * @return Whether such a component can be added.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return BodyComponent.class.isAssignableFrom(type);
	}
	
	@Override
	public void copyFlightConfiguration(FlightConfigurationId oldConfigId, FlightConfigurationId newConfigId) {
		this.separations.copyFlightConfiguration(oldConfigId, newConfigId);
	}
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		ParallelStage copy = (ParallelStage) (super.copyWithOriginalID());
		return copy;
	}

	@Override
	public double getAngularOffset() {
		return this.angularPosition_rad;
	}

	@Override
	public int getInstanceCount() {
		return this.count;
	}
	
	@Override
	public boolean isAfter(){ 
		return false;
	}
	
	@Override
	public boolean isLaunchStage(){
		return true;
	}

	@Override 
	public void setInstanceCount( final int newCount ){
		mutex.verify();
		if ( newCount < 1) {
			// there must be at least one instance....   
			return;
		}
		
        this.count = newCount;
        this.angularSeparation = Math.PI * 2 / this.count;
        fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public double getRadialOffset() {
		return this.radialPosition_m;
	}
	
	@Override
	public Coordinate[] getInstanceOffsets(){
		checkState();
		
		final double radius = this.radialPosition_m;
		final double startAngle = this.angularPosition_rad;
		final double angleIncr = this.angularSeparation;
		Coordinate center = Coordinate.ZERO;
		
		double curAngle = startAngle;
		Coordinate[] toReturn = new Coordinate[this.count];
		for (int instanceNumber = 0; instanceNumber < this.count; instanceNumber++) {
			final double curY = radius * Math.cos(curAngle);
			final double curZ = radius * Math.sin(curAngle);
			toReturn[instanceNumber] = center.add(0, curY, curZ );
			
			curAngle += angleIncr;
		}
		
		return toReturn;
	}
	
	@Override
	public Coordinate[] getLocations() {
		if (null == this.parent) {
			throw new BugException(" Attempted to get absolute position Vector of a Stage without a parent. ");
		}
		
		Coordinate[] parentInstances = this.parent.getLocations();
		if (1 != parentInstances.length) {
			throw new BugException(" OpenRocket does not (yet) support external stages attached to external stages. " +
					"(assumed reason for getting multiple parent locations into an external stage.)");
		}
		
		final Coordinate center = parentInstances[0].add( this.position);
		Coordinate[] instanceLocations = this.getInstanceOffsets();
		Coordinate[] toReturn = new Coordinate[ instanceLocations.length];
		for( int i = 0; i < toReturn.length; i++){
			toReturn[i] = center.add( instanceLocations[i]); 
		}
		
		return toReturn;
	}
	
	@Override
	public String getPatternName(){
		return (this.getInstanceCount() + "-ring");
	}
	
	@Override
	public void setRelativePositionMethod(final Position _newPosition) {
		if (null == this.parent) {
			throw new NullPointerException(" a Stage requires a parent before any positioning! ");
		}
		
		super.setRelativePosition(_newPosition);
		
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	@Override
	public boolean getAutoRadialOffset(){
		return this.autoRadialPosition;
	}
	
	public void setAutoRadialOffset( final boolean enabled ){
		this.autoRadialPosition = enabled;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);	
	}
	
	@Override
	public void setRadialOffset(final double radius) {
		mutex.verify();
		this.radialPosition_m = radius;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);	
	}

	@Override
	public void setAngularOffset(final double angle_rad) {
		mutex.verify();
		this.angularPosition_rad = MathUtil.reduce180( angle_rad);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
		
	@Override
	protected void update() {
		super.update();
		
		if( this.autoRadialPosition ){
			ComponentAssembly parentAssembly = (ComponentAssembly)this.parent;
			if( null == parentAssembly ){
				this.radialPosition_m = this.getOuterRadius();
			}else{
				this.radialPosition_m = this.getOuterRadius() + parentAssembly.getOuterRadius();
			}
		}
	}
	
	
	
}
