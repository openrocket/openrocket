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
	
	private double radius;
	
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

	
	public void setLength(double length) {
		length = Math.max(length, 0);
		if (MathUtil.equals(this.length, length)) {
			return;
		}
		this.length = length;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public final double getRadius() {
		return radius;
	}
	
	
	public final void setRadius(double radius) {
		radius = Math.max(radius, 0);
		if (MathUtil.equals(this.radius, radius)) {
			return;
		}
		this.radius = radius;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	
	public final double getRadialPosition() {
		return radialPosition;
	}
	
	public final void setRadialPosition(double radialPosition) {
		radialPosition = Math.max(radialPosition, 0);
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
		addBound(c, 0, radius);
		addBound(c, length, radius);
		return c;
	}
	
}
