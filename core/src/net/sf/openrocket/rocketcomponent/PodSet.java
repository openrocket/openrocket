package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;

public class PodSet extends ComponentAssembly implements RingInstanceable {
	
	private static final Translator trans = Application.getTranslator();
	//private static final Logger log = LoggerFactory.getLogger(PodSet.class);
	
	protected int count = 1;

	protected double angularSeparation = Math.PI;
	protected double angularPosition_rad = 0;
	protected double radialPosition_m = 0;
	
	public PodSet() {
		this.count = 2;
		this.relativePosition = Position.BOTTOM;
	}
	
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
	@Override
	public String getComponentName() {
		//// Stage
		return trans.get("PodSet.PodSet");
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
		
		if (this.isAfter()) {
			return super.getLocations();
		} else {
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
		
	}
	
	@Override
	public boolean isAfter() {
		return false;
	}
	
	@Override
	public double getPositionValue() {
		mutex.verify();
		
		return this.getAxialOffset();
	}
	
	/** 
	 * Stages may be positioned relative to other stages. In that case, this will set the stage number 
	 * against which this stage is positioned.
	 * 
	 * @return the stage number which this stage is positioned relative to
	 */
	public int getRelativeToStage() {
		if (null == this.parent) {
			return -1;
		} else if (this.parent instanceof PodSet) {
			return this.parent.parent.getChildPosition(this.parent);
		}
		
		return -1;
	}
	
	@Override
	public double getAxialOffset() {
		double returnValue = Double.NaN;
		
		if (this.isAfter()){
			// remember the implicit (this instanceof Stage)
			throw new BugException("found a Stage on centerline, but not positioned as AFTER.  Please fix this! " + this.getName() + "  is " + this.getRelativePosition().name());
		} else {
			returnValue = super.asPositionValue(this.relativePosition);
		}
		
		if (0.000001 > Math.abs(returnValue)) {
			returnValue = 0.0;
		}
		
		return returnValue;
	}

	@Override
	public double getAngularOffset() {
		return this.angularPosition_rad;
	}

	@Override
	public String getPatternName(){
		return (this.getInstanceCount() + "-ring");
	}
	
	

	@Override
	public double getRadialOffset() {
		return this.radialPosition_m;
	}

	@Override
	public boolean getAutoRadialOffset(){
		return false;
	}

	@Override
	public int getInstanceCount() {
		return this.count;
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
	protected StringBuilder toDebugDetail() {
		StringBuilder buf = super.toDebugDetail();
		//		if (-1 == this.getRelativeToStage()) {
		//			System.err.println("      >>refStageName: " + null + "\n");
		//		} else {
		//			Stage refStage = (Stage) this.parent;
		//			System.err.println("      >>refStageName: " + refStage.getName() + "\n");
		//			System.err.println("      ..refCenterX: " + refStage.position.x + "\n");
		//			System.err.println("      ..refLength: " + refStage.getLength() + "\n");
		//		}
		return buf;
	}

	@Override
	public void setAngularOffset(double angle_rad) {
		mutex.verify();
		this.angularPosition_rad = angle_rad;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);		
	}

	@Override
	public void setRadialOffset(double radius_m) {
		mutex.verify();
		this.radialPosition_m = radius_m;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
}
