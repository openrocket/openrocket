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
	
	protected int instanceCount = 1;

	protected double angularSeparation = Math.PI;
	protected double angularPosition_rad = 0;
	protected boolean autoRadialPosition = false;
	protected double radialPosition_m = 0;
	
	public ParallelStage() {
		this.instanceCount = 2;
		this.relativePosition = Position.BOTTOM;
		this.angularSeparation = Math.PI * 2 / this.instanceCount;
	}
	
	public ParallelStage( final int _count ){
		this();
		
		this.instanceCount = _count;
		this.angularSeparation = Math.PI * 2 / this.instanceCount;
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
		return this.instanceCount;
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
		
        this.instanceCount = newCount;
        this.angularSeparation = Math.PI * 2 / this.instanceCount;
        fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public double getRadialOffset() {
		return this.radialPosition_m;
	}

	@Override
	public double[] getInstanceAngles(){
		final double baseAngle = getAngularOffset();
		final double incrAngle = getInstanceAngleIncrement();
		
		double[] result = new double[ getInstanceCount()]; 
		for( int i=0; i<getInstanceCount(); ++i){
			result[i] = baseAngle + incrAngle*i;
		}
		
		return result;
	}
	
    @Override
    public double getInstanceAngleIncrement(){
    	return this.angularSeparation;
    }
	
	@Override
	public Coordinate[] getInstanceOffsets(){
		checkState();
		
		Coordinate[] toReturn = new Coordinate[this.instanceCount];
		final double[] angles = getInstanceAngles();
		for (int instanceNumber = 0; instanceNumber < this.instanceCount; instanceNumber++) {
			final double curY = this.radialPosition_m * Math.cos(angles[instanceNumber]);
			final double curZ = this.radialPosition_m * Math.sin(angles[instanceNumber]);
			toReturn[instanceNumber] = new Coordinate(0, curY, curZ );
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
	
	@Override
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

		if( this.autoRadialPosition){
			if( null == this.parent ){
				this.radialPosition_m = this.getOuterRadius();
			}else if( BodyTube.class.isAssignableFrom(this.parent.getClass())) {
				BodyTube parentBody = (BodyTube)this.parent;
				this.radialPosition_m = this.getOuterRadius() + parentBody.getOuterRadius();				
			}
		}
	}
	
	
	
}
