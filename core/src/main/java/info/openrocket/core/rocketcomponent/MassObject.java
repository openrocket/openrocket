package info.openrocket.core.rocketcomponent;

import static info.openrocket.core.util.MathUtil.pow2;

import java.util.ArrayList;
import java.util.Collection;

import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;

/**
 * A MassObject is an internal component that can a specific weight, but not
 * necessarily a strictly bound shape. It is
 * represented as a homogeneous cylinder and drawn in the rocket figure with
 * rounded corners.
 * <p/>
 * Subclasses of this class need only implement the {@link #getComponentMass()},
 * {@link #getComponentName()} and {@link
 * #isCompatible(RocketComponent)} methods.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class MassObject extends InternalComponent {

	protected double radius;
	private boolean autoRadius = false;
	private double volume; // (Packed) volume of the object

	private double radialPosition;
	private double radialDirection;

	private double shiftY = 0;
	private double shiftZ = 0;

	public MassObject() {
		this(0.025, 0.0125);
	}

	public MassObject(double length, double radius) {
		super();

		this.length = length;
		this.radius = radius;
		updateVolume(radius);

		this.setAxialMethod(AxialMethod.TOP);
		this.setAxialOffset(0.0);
	}

	@Override
	public boolean isAfter() {
		return false;
	}

	private void updateVolume(double radius) {
		volume = Math.pow(radius, 2) * length; // Math.PI left out, not needed
	}

	@Override
	public double getLength() {
		if (autoRadius) {
			length = getAutoLength();
		}
		return length;
	}

	public void setLength(double length) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof MassObject) {
				((MassObject) listener).setLength(length);
			}
		}

		length = Math.max(length, 0);

		if (MathUtil.equals(this.length, length)) {
			return;
		}
		this.length = length;
		updateVolume(autoRadius ? getAutoRadius() : radius);

		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	/**
	 * Calculate the length from the current volume.
	 */
	private double getAutoLength() {
		// Calculate the volume using the auto radius and the new "auto" length, and
		// transform that back
		// to the non auto radius situation to set this.length (the volume in both
		// situations is the same).
		return volume / Math.pow(radius, 2);
	}

	public double getRadius() {
		if (autoRadius) {
			radius = getAutoRadius();
			length = getAutoLength();
		}
		return radius;
	}

	/**
	 * Return the radius determined by its parent component.
	 * 
	 * @return the radius determined by its parent component
	 */
	public double getAutoRadius() {
		if (parent == null) {
			return radius;
		}
		if (parent instanceof NoseCone) {
			return ((NoseCone) parent).getBaseRadius();
		} else if (parent instanceof Transition) {
			double foreRadius = ((Transition) parent).getForeRadius();
			double aftRadius = ((Transition) parent).getAftRadius();
			return (Math.max(foreRadius, aftRadius));
		} else if (parent instanceof BodyComponent) {
			return ((BodyComponent) parent).getInnerRadius();
		} else if (parent instanceof RingComponent) {
			return ((RingComponent) parent).getInnerRadius();
		}

		return radius;
	}

	public void setRadius(double radius) {
		radius = Math.max(radius, 0);

		for (RocketComponent listener : configListeners) {
			if (listener instanceof MassObject) {
				((MassObject) listener).setRadius(radius);
			}
		}

		if (MathUtil.equals(this.radius, radius) && (!autoRadius))
			return;

		this.autoRadius = false;
		this.radius = radius;
		updateVolume(radius);
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	public boolean isRadiusAutomatic() {
		return autoRadius;
	}

	public void setRadiusAutomatic(boolean auto) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof MassObject) {
				((MassObject) listener).setRadiusAutomatic(auto);
			}
		}

		if (autoRadius == auto)
			return;

		autoRadius = auto;

		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	public final double getRadialPosition() {
		return radialPosition;
	}

	public final void setRadialPosition(double radialPosition) {
		radialPosition = Math.max(radialPosition, 0);

		for (RocketComponent listener : configListeners) {
			if (listener instanceof MassObject) {
				((MassObject) listener).setRadialPosition(radialPosition);
			}
		}

		if (MathUtil.equals(this.radialPosition, radialPosition)) {
			return;
		}
		this.radialPosition = radialPosition;
		shiftY = radialPosition * Math.cos(radialDirection);
		shiftZ = radialPosition * Math.sin(radialDirection);
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	public final double getRadialDirection() {
		return radialDirection;
	}

	public final void setRadialDirection(double radialDirection) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof MassObject) {
				((MassObject) listener).setRadialDirection(radialDirection);
			}
		}

		radialDirection = MathUtil.reducePi(radialDirection);
		if (MathUtil.equals(this.radialDirection, radialDirection)) {
			return;
		}
		this.radialDirection = radialDirection;
		shiftY = radialPosition * Math.cos(radialDirection);
		shiftZ = radialPosition * Math.sin(radialDirection);
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	/**
	 * Shift the coordinates according to the radial position and direction.
	 */
	// @Override
	// protected
	// final Coordinate[] shiftCoordinates(Coordinate[] array) {
	// for (int i = 0; i < array.length; i++) {
	// array[i] = array[i].add(0, shiftY, shiftZ);
	// }
	// return array;
	// }

	@Override
	public final Coordinate getComponentCG() {
		return new Coordinate(getLength() / 2, shiftY, shiftZ, getComponentMass());
	}

	@Override
	public final double getLongitudinalUnitInertia() {
		return (3 * pow2(getRadius()) + pow2(getLength())) / 12;
	}

	@Override
	public final double getRotationalUnitInertia() {
		return pow2(getRadius()) / 2;
	}

	@Override
	public final Collection<Coordinate> getComponentBounds() {
		Collection<Coordinate> c = new ArrayList<Coordinate>();
		addBound(c, 0, getRadius());
		addBound(c, getLength(), getRadius());
		return c;
	}

}
