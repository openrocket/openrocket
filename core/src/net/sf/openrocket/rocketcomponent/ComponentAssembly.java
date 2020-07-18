package net.sf.openrocket.rocketcomponent;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.sf.openrocket.util.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.position.AxialPositionable;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;



/**
 * A base of component assemblies.
 * <p>
 * Note that the mass and CG overrides of the <code>ComponentAssembly</code> class
 * overrides all sibling mass/CG as well as its own.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class ComponentAssembly extends RocketComponent implements AxialPositionable, BoxBounded {
	private static final Logger log = LoggerFactory.getLogger(ComponentAssembly.class);
	
	/**
	 * Sets the position of the components to POSITION_RELATIVE_AFTER.
	 * (Should have no effect.)
	 */
	public ComponentAssembly() {
		super( AxialMethod.AFTER);
	}

	public ComponentAssembly( final AxialMethod initialAxialMethod) {
		super(initialAxialMethod);
	}

	@Override
	public boolean allowsChildren(){
		return true;
	}
	
	@Override
	public double getAxialOffset() {
		return getAxialOffset( this.axialMethod );
	}

	/**
	 * Null method (ComponentAssembly has no bounds of itself).
	 */
	@Override
	public Collection<Coordinate> getComponentBounds() {
		return Collections.emptyList();
	}

	/**
	 * Null method (ComponentAssembly has no mass of itself).
	 */
	@Override
	public Coordinate getComponentCG() {
		return Coordinate.ZERO;
	}
	
	/**
	 * Null method (ComponentAssembly has no mass of itself).
	 */
	@Override
	public double getComponentMass() {
		return 0;
	}

	public BoundingBox getInstanceBoundingBox (){ return new BoundingBox(); }

	/**
	 * Null method (ComponentAssembly has no mass of itself).
	 */
	@Override
	public double getLongitudinalUnitInertia() {
		return 0;
	}
	
	@Override
	public boolean getOverrideSubcomponents() {
		return true;
	}
	
	/**
	 * Null method (ComponentAssembly has no mass of itself).
	 */
	@Override
	public double getRotationalUnitInertia() {
		return 0;
	}

	public double getBoundingRadius(){
		double outerRadius=0;
		for( RocketComponent comp : children ){
			double thisRadius=0;
			if( comp instanceof BodyTube ){
				thisRadius = ((BodyTube)comp).getOuterRadius(); 
			}else if( comp instanceof Transition ){
				Transition trans = (Transition)comp;
				thisRadius = Math.max( trans.getForeRadius(), trans.getAftRadius()); 
			}
			
			outerRadius = Math.max( outerRadius, thisRadius);
		}
		return outerRadius;
	}
	
	/**
	 * Components have no aerodynamic effect, so return false.
	 */
	@Override
	public boolean isAerodynamic() {
		return false;
	}
	
	/**
	 * Component have no effect on mass, so return false (even though the override values
	 * may have an effect).
	 */
	@Override
	public boolean isMassive() {
		return false;
	}

	@Override
	public boolean isAxisymmetric(){
		return !(2 == this.getInstanceCount());
	}
	
	@Override
	public void setAxialOffset(final double _pos) {
		this.updateBounds();
		super.setAxialOffset(this.axialMethod, _pos);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
		
	@Override
	public void setAxialMethod( final AxialMethod newMethod ) {
		if (null == this.parent) {
			throw new NullPointerException(" a Stage requires a parent before any positioning! ");
		}
		if ((this instanceof ParallelStage ) || ( this instanceof PodSet )){
			if (AxialMethod.AFTER == newMethod) {
				log.warn("Stages (or Pods) cannot be relative to other stages via AFTER! Ignoring.");
				super.setAxialMethod(AxialMethod.TOP);
			} else {
				super.setAxialMethod(newMethod);
			}
		}else if( this.getClass().equals( AxialStage.class)){
			// Centerline stages must be set via AFTER-- regardless of what was requested:
			super.setAxialMethod(AxialMethod.AFTER);
		}else{
			throw new BugException("Unrecognized subclass of Component Assembly.  Please update this method.");
		}
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	@Override
	public void setOverrideSubcomponents(boolean override) {
		// No-op
	}
	
	@Override
	public boolean isOverrideSubcomponentsEnabled() {
		return false;
	}
	
	@Override
	protected void update() {
		this.updateBounds();
		if (this.isAfter()){
			this.setAfter();
		} else {
			super.update();
		}
		
		this.updateChildSequence();
		
		return;
	}
	
	
	@Override
	public void updateBounds() {
		// currently only updates the length 
		this.length = 0;
		Iterator<RocketComponent> childIterator = this.getChildren().iterator();
		while (childIterator.hasNext()) {
			RocketComponent curChild = childIterator.next();
			if(curChild.isAfter()){
				this.length += curChild.getLength();
			}
		}
		
	}
	
	protected void updateChildSequence() {
		for( RocketComponent curChild : this.children ) {
			if(AxialMethod.AFTER == curChild.getAxialMethod()){
				curChild.setAfter();
			}
		}
	}
	
	
}
