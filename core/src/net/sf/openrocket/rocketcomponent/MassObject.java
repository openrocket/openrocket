package net.sf.openrocket.rocketcomponent;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


/**
 * A MassObject is an internal component that can a specific weight, but not necessarily a strictly bound shape.  It is
 * represented as a homogeneous cylinder and drawn in the rocket figure with rounded corners.
 * <p/>
 * Subclasses of this class need only implement the {@link #getComponentMass()}, {@link #getComponentName()} and {@link
 * #isCompatible(RocketComponent)} methods.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class MassObject extends InternalComponent {
	
	protected double radius;
	private boolean autoRadius = false;
	
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
		
		this.setAxialMethod( AxialMethod.TOP);
		this.setAxialOffset(0.0);
	}
	
	@Override
	public boolean isAfter(){ 
		return false;
	}

	@Override
	public double getLength() {
		if (this.autoRadius) {
			// Calculate the volume using the non auto radius and the non auto length, and transform that back
			// to the auto radius situation to get the auto radius length (the volume in both situations is the same).
			double volume = Math.pow(this.radius, 2) * this.length;	// Math.PI left out, not needed
			return volume / Math.pow(getRadius(), 2);
		}
		return length;
	}

	public double getLengthNoAuto() {
		return length;
	}

	/**
	 * Set the length, ignoring the auto radius setting.
	 * @param length new length
	 */
	public void setLengthNoAuto(double length) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof MassObject) {
				((MassObject) listener).setLengthNoAuto(length);
			}
		}

		length = Math.max(length, 0);
		if (MathUtil.equals(this.length, length)) {
			return;
		}
		this.length = length;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}

	public void setLength(double length) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof MassObject) {
				((MassObject) listener).setLength(length);
			}
		}

		length = Math.max(length, 0);
		if (this.autoRadius) {
			// Calculate the volume using the auto radius and the new "auto" length, and transform that back
			// to the non auto radius situation to set this.length (the volume in both situations is the same).
			double volume = Math.pow(getRadius(), 2) * length;		// Math.PI left out, not needed
			double newLength = volume / Math.pow(this.radius, 2);
			if (MathUtil.equals(this.length, newLength))
				return;
			this.length = newLength;
		} else {
			if (MathUtil.equals(this.length, length)) {
				return;
			}
			this.length = length;
		}
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public double getRadius() {
		if (autoRadius) {
			if (parent == null) {
				return radius;
			}
			if (parent instanceof NoseCone) {
				return ((NoseCone) parent).getAftRadius();
			} else if (parent instanceof Transition) {
				double foreRadius = ((Transition) parent).getForeRadius();
				double aftRadius = ((Transition) parent).getAftRadius();
				return (Math.max(foreRadius, aftRadius));
			} else if (parent instanceof BodyComponent) {
				return ((BodyComponent) parent).getInnerRadius();
			} else if (parent instanceof RingComponent) {
				return ((RingComponent) parent).getInnerRadius();
			}
		}
		return radius;
	}

	public double getRadiusNoAuto() {
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

		// Set the length
		double volume = (Math.PI * Math.pow(getRadius(), 2) * length);
		length = volume / (Math.PI * Math.pow(getRadius(), 2));

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
//	@Override
//	protected
//	final Coordinate[] shiftCoordinates(Coordinate[] array) {
//		for (int i = 0; i < array.length; i++) {
//			array[i] = array[i].add(0, shiftY, shiftZ);
//		}
//		return array;
//	}
	
	@Override
	public final Coordinate getComponentCG() {
		return new Coordinate(length / 2, shiftY, shiftZ, getComponentMass());
	}
	
	@Override
	public final double getLongitudinalUnitInertia() {
		return (3 * pow2(radius) + pow2(length)) / 12;
	}
	
	@Override
	public final double getRotationalUnitInertia() {
		return pow2(radius) / 2;
	}
	
	@Override
	public final Collection<Coordinate> getComponentBounds() {
		Collection<Coordinate> c = new ArrayList<Coordinate>();
		addBound(c, 0, getRadius());
		addBound(c, getLength(), getRadius());
		return c;
	}
	
}
