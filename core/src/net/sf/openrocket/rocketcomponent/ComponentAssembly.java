package net.sf.openrocket.rocketcomponent;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.util.Coordinate;



/**
 * A base of component assemblies.
 * <p>
 * Note that the mass and CG overrides of the <code>ComponentAssembly</code> class
 * overrides all sibling mass/CG as well as its own.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class ComponentAssembly extends RocketComponent {
	private static final Logger log = LoggerFactory.getLogger(ComponentAssembly.class);
	
	/**
	 * Sets the position of the components to POSITION_RELATIVE_AFTER.
	 * (Should have no effect.)
	 */
	public ComponentAssembly() {
		super(RocketComponent.Position.AFTER);
	}
	
	@Override
	public double getAxialOffset() {
		return super.asPositionValue(this.relativePosition);
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
		return Coordinate.NUL;
	}
	
	/**
	 * Null method (ComponentAssembly has no mass of itself).
	 */
	@Override
	public double getComponentMass() {
		return 0;
	}
	
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
	public void setAxialOffset(final double _pos) {
		this.updateBounds();
		//		System.err.println("updating axial position for boosters: " + this.getName() + " ( " + this.getComponentName() + ")");
		//		System.err.println("       requesting offset: " + _pos + " via: " + this.relativePosition.name());
		super.setAxialOffset(this.relativePosition, _pos);
		//		System.err.println("       resultant offset: " + this.position.x + " via: " + this.relativePosition.name());
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public void setInstanceCount(final int _count) {
		mutex.verify();
		if (this.isCenterline()) {
			return;
		}
		
		if (_count < 2) {
			// there must be at least one instance....   
			return;
		}
		
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public void setRelativePositionMethod(final Position _newPosition) {
		if (null == this.parent) {
			throw new NullPointerException(" a Stage requires a parent before any positioning! ");
		}
		if (this.isCenterline()) {
			// Centerline stages must be set via AFTER-- regardless of what was requested:
			super.setRelativePosition(Position.AFTER);
		} else if (this.parent instanceof PodSet) {
			if (Position.AFTER == _newPosition) {
				log.warn("Stages cannot be relative to other stages via AFTER! Ignoring.");
				super.setRelativePosition(Position.TOP);
			} else {
				super.setRelativePosition(_newPosition);
			}
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
		if (null == this.parent) {
			return;
		}
		
		this.updateBounds();
		if (this.isCenterline()) {
			// stages which are directly children of the rocket are inline, and positioned
			int childNumber = this.parent.getChildPosition(this);
			if (0 == childNumber) {
				this.setAfter(this.parent);
			} else {
				RocketComponent prevStage = this.parent.getChild(childNumber - 1);
				this.setAfter(prevStage);
			}
		} else {
			// this path is for 'external' assemblies: pods and boosters
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
			if (curChild.isCenterline()) {
				this.length += curChild.getLength();
			}
		}
		
	}
	
	protected void updateChildSequence() {
		Iterator<RocketComponent> childIterator = this.getChildren().iterator();
		RocketComponent prevComp = null;
		while (childIterator.hasNext()) {
			RocketComponent curChild = childIterator.next();
			if (curChild.isCenterline()) {
				curChild.setAfter(prevComp);
				prevComp = curChild;
			}
		}
	}
	
	
}
