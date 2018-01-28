package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.position.AxialPositionable;

/**
 * A component internal to the rocket.  Internal components have no effect on the
 * the aerodynamics of a rocket, only its mass properties (though the location of the
 * components is not enforced to be within external components).  Internal components 
 * are always attached relative to the parent component, which can be internal or
 * external, or absolutely.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class InternalComponent extends RocketComponent implements AxialPositionable {

	public InternalComponent() {
		super( AxialMethod.BOTTOM);
	}
	
	
	@Override
	public void setAxialMethod(final AxialMethod newAxialMethod) {
		super.setAxialMethod(newAxialMethod);
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	

	/**
	 * Non-aerodynamic components.
	 * @return <code>false</code>
	 */
	@Override
	public final boolean isAerodynamic() {
		return false;
	}

	/**
	 * Is massive.
	 * @return <code>true</code>
	 */
	@Override
	public final boolean isMassive() {
		return true;
	}
}
