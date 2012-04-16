package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.preset.ComponentPreset;



/**
 * Class to represent a body object.  The object can be described as a function of
 * the cylindrical coordinates x and angle theta as  r = f(x,theta).  The component
 * need not be symmetrical in any way (e.g. square tube, slanted cone etc).
 *
 * It defines the methods getRadius(x,theta) and getInnerRadius(x,theta), as well
 * as get/setLength().
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public abstract class BodyComponent extends ExternalComponent {
	
	/**
	 * Default constructor.  Sets the relative position to POSITION_RELATIVE_AFTER,
	 * i.e. body components come after one another.
	 */
	public BodyComponent() {
		super(RocketComponent.Position.AFTER);
	}
	
	
	/**
	 * Get the outer radius of the component at cylindrical coordinate (x,theta).
	 *
	 * Note that the return value may be negative for a slanted object.
	 *
	 * @param x  Distance in x direction
	 * @param theta  Angle about the x-axis
	 * @return  Distance to the outer edge of the object
	 */
	public abstract double getRadius(double x, double theta);
	
	
	/**
	 * Get the inner radius of the component at cylindrical coordinate (x,theta).
	 *
	 * Note that the return value may be negative for a slanted object.
	 *
	 * @param x  Distance in x direction
	 * @param theta  Angle about the x-axis
	 * @return  Distance to the inner edge of the object
	 */
	public abstract double getInnerRadius(double x, double theta);
	
	
	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		super.loadFromPreset(preset);
	}
	
	

	/**
	 * Sets the length of the body component.
	 * <p>
	 * Note: This should be overridden by the subcomponents which need to call
	 * clearPreset().  (BodyTube allows changing length without resetting the preset.)
	 */
	public void setLength(double length) {
		if (this.length == length)
			return;
		this.length = Math.max(length, 0);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
}
