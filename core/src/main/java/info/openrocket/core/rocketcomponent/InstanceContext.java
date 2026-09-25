package info.openrocket.core.rocketcomponent;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.Transformation;

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
		return component.hashCode();
	}

	public InstanceContext(final RocketComponent _component, final int _instanceNumber,
			final Transformation _transform) {
		this(_component, _instanceNumber, _transform, Transformation.IDENTITY);
	}

	/**
	 * Creates the context for one physical component instance.
	 *
	 * @param _component component represented by this context
	 * @param _instanceNumber instance number relative to its parent
	 * @param _transform transform from component coordinates to rocket coordinates
	 * @param _parentTransform transform from parent coordinates to rocket coordinates
	 */
	public InstanceContext(final RocketComponent _component, final int _instanceNumber,
			final Transformation _transform, final Transformation _parentTransform) {
		component = _component;
		instanceNumber = _instanceNumber;
		transform = _transform;
		parentTransform = _parentTransform;
	}

	@Override
	public String toString() {
		return String.format("Context for %s #%d", component.toString(), instanceNumber);
	}

	public CoordinateIF getLocation() {
		return transform.transform(Coordinate.ZERO);
	}

	/**
	 * Returns the transform of the physical parent instance.  This allows consumers
	 * to aggregate a component's own instances without descending the component tree.
	 *
	 * @return transform from parent coordinates to rocket coordinates
	 */
	public Transformation getParentTransform() {
		return parentTransform;
	}

	// =========== Instance Member Variables ========================

	// ==== public ====
	final public RocketComponent component;
	final public int instanceNumber;
	final public Transformation transform;
	private final Transformation parentTransform;

	// =========== Private Instance Functions ========================

}
