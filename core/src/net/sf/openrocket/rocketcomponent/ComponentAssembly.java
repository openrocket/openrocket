package net.sf.openrocket.rocketcomponent;

import java.util.Collection;
import java.util.Collections;

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

	/**
	 * Sets the position of the components to POSITION_RELATIVE_AFTER.
	 * (Should have no effect.)
	 */
	public ComponentAssembly() {
		super(RocketComponent.Position.AFTER);
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
	public boolean getOverrideSubcomponents() {
		return true;
	}

	@Override
	public void setOverrideSubcomponents(boolean override) {
		// No-op
	}
	
	@Override
	public boolean isOverrideSubcomponentsEnabled() {
		return false;
	}
	
}
