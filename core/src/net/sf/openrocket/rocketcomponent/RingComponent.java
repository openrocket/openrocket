package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


/**
 * An inner component that consists of a hollow cylindrical component.  This can be
 * an inner tube, tube coupler, centering ring, bulkhead etc.
 *
 * The properties include the inner and outer radii, length and radial position.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class RingComponent extends StructuralComponent implements Coaxial {
	
	protected boolean outerRadiusAutomatic = false;
	protected boolean innerRadiusAutomatic = false;
	

	protected double radialDirection = 0;
	protected double radialPosition = 0;
	
	private double shiftY = 0;
	private double shiftZ = 0;
	

	@Override
	public abstract double getOuterRadius();
	
	@Override
	public abstract void setOuterRadius(double r);
	
	@Override
	public abstract double getInnerRadius();
	
	@Override
	public abstract void setInnerRadius(double r);
	
	@Override
	public abstract double getThickness();
	
	public abstract void setThickness(double thickness);
	
	
	public final boolean isOuterRadiusAutomatic() {
		return outerRadiusAutomatic;
	}
	
	// Setter is protected, subclasses may make it public
	protected void setOuterRadiusAutomatic(boolean auto) {
		if (auto == outerRadiusAutomatic)
			return;
		outerRadiusAutomatic = auto;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public final boolean isInnerRadiusAutomatic() {
		return innerRadiusAutomatic;
	}
	
	// Setter is protected, subclasses may make it public
	protected void setInnerRadiusAutomatic(boolean auto) {
		if (auto == innerRadiusAutomatic)
			return;
		innerRadiusAutomatic = auto;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	


	public final void setLength(double length) {
		double l = Math.max(length, 0);
		if (this.length == l)
			return;
		
		this.length = l;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	/**
	 * Return the radial direction of displacement of the component.  Direction 0
	 * is equivalent to the Y-direction.
	 *
	 * @return  the radial direction.
	 */
	public double getRadialDirection() {
		return radialDirection;
	}
	
	/**
	 * Set the radial direction of displacement of the component.  Direction 0
	 * is equivalent to the Y-direction.
	 *
	 * @param dir  the radial direction.
	 */
	public void setRadialDirection(double dir) {
		dir = MathUtil.reducePi(dir);
		if (radialDirection == dir)
			return;
		radialDirection = dir;
		shiftY = radialPosition * Math.cos(radialDirection);
		shiftZ = radialPosition * Math.sin(radialDirection);
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	


	/**
	 * Return the radial position of the component.  The position is the distance
	 * of the center of the component from the center of the parent component.
	 *
	 * @return  the radial position.
	 */
	public double getRadialPosition() {
		return radialPosition;
	}
	
	/**
	 * Set the radial position of the component.  The position is the distance
	 * of the center of the component from the center of the parent component.
	 *
	 * @param pos  the radial position.
	 */
	public void setRadialPosition(double pos) {
		pos = Math.max(pos, 0);
		if (radialPosition == pos)
			return;
		radialPosition = pos;
		shiftY = radialPosition * Math.cos(radialDirection);
		shiftZ = radialPosition * Math.sin(radialDirection);
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	public double getRadialShiftY() {
		return shiftY;
	}
	
	public double getRadialShiftZ() {
		return shiftZ;
	}
	
	public void setRadialShift(double y, double z) {
		radialPosition = Math.hypot(y, z);
		radialDirection = Math.atan2(z, y);
		
		// Re-calculate to ensure consistency
		shiftY = radialPosition * Math.cos(radialDirection);
		shiftZ = radialPosition * Math.sin(radialDirection);
		assert (MathUtil.equals(y, shiftY));
		assert (MathUtil.equals(z, shiftZ));
		
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	@Override
	public Collection<Coordinate> getComponentBounds() {
		List<Coordinate> bounds = new ArrayList<Coordinate>();
		addBound(bounds, 0, getOuterRadius());
		addBound(bounds, length, getOuterRadius());
		return bounds;
	}
	
	@Override
	public Coordinate getComponentCG() {
		Coordinate cg = Coordinate.ZERO;
		final int instanceCount = getInstanceCount();
		final double instanceMass =  ringMass(getOuterRadius(), getInnerRadius(), getLength(), getMaterial().getDensity());

		if (1 == instanceCount ) {
			cg = new Coordinate( length/2, 0, 0, instanceMass );
		}else{
			for( Coordinate c : getInstanceOffsets() ) {
				c = c.setWeight( instanceMass );
				cg = cg.average(c); 
			}
			cg = cg.add( length/2, 0, 0);
		}
		return cg;
	}
	
	@Override
	public double getComponentMass() {
		return ringMass(getOuterRadius(), getInnerRadius(), getLength(),
				getMaterial().getDensity()) * getInstanceCount();
	}
	
	
	@Override
	public double getLongitudinalUnitInertia() {
		return ringLongitudinalUnitInertia(getOuterRadius(), getInnerRadius(), getLength());
	}
	
	@Override
	public double getRotationalUnitInertia() {
		return ringRotationalUnitInertia(getOuterRadius(), getInnerRadius());
	}
	
}
