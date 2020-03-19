package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

/**
 * 
 * @author teyrana (aka Daniel Williams) <equipoise@gmail.com> 
 *
 */
public class InstanceContext {
	
	// =========== Public Functions ========================
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		InstanceContext other = (InstanceContext) obj;
		return (component.equals(other.component) && transform.equals(other.transform));
	}
	
	@Override
	public int hashCode() {
		return (int) (component.hashCode());
	}

	public InstanceContext(final RocketComponent _component, final boolean _active, final int _instanceNumber, final Transformation _transform) {
		component = _component;
		active = _active;
		instanceNumber = _instanceNumber;
		transform = _transform;
		
	}

	@Override
	public String toString() {
		return String.format("Context for %s #%d", component.toString(), instanceNumber);
	}

	public Coordinate getLocation() {
		return transform.transform(Coordinate.ZERO);
	}
	
	// =========== Instance Member Variables ========================
	

	// ==== public ====
	final public RocketComponent component;
	final public boolean active;
	final public int instanceNumber;
	final public Transformation transform;
	
	// =========== Private Instance Functions ========================

	
}

