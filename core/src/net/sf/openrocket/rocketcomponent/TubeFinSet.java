package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;

public class TubeFinSet extends ExternalComponent {
	private static final Translator trans = Application.getTranslator();
	
	private final static double DEFAULT_RADIUS = 0.025;
	
	private boolean autoRadius = true; // Radius chosen automatically based on parent component
	private double outerRadius = DEFAULT_RADIUS;
	protected double thickness = 0.002;
	
	protected int fins = 6;
	
	/**
	 * Rotation angle of the first fin.  Zero corresponds to the positive y-axis.
	 */
	protected double rotation = 0;
	
	/**
	 * Rotation about the x-axis by angle this.rotation.
	 */
	protected Transformation baseRotation = Transformation.rotate_x(rotation);
	
	/**
	 * Rotation about the x-axis by 2*PI/fins.
	 */
	protected Transformation finRotation = Transformation.rotate_x(2 * Math.PI / fins);
	
	
	/**
	 * New FinSet with given number of fins and given base rotation angle.
	 * Sets the component relative position to POSITION_RELATIVE_BOTTOM,
	 * i.e. fins are positioned at the bottom of the parent component.
	 */
	public TubeFinSet() {
		super(RocketComponent.Position.BOTTOM);
		length = 0.10;
	}
	
	public void setLength(double length) {
		if (MathUtil.equals(this.length, length))
			return;
		this.length = length;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public boolean isOuterRadiusAutomatic() {
		return autoRadius;
	}
	
	/**
	 * Return the outer radius of the body tube.
	 *
	 * @return  the outside radius of the tube
	 */
	public double getOuterRadius() {
		if (autoRadius) {
			// Return auto radius from front or rear
			double r = -1;
			RocketComponent c = this.getParent();
			if (c != null) {
				if (c instanceof SymmetricComponent) {
					r = ((SymmetricComponent) c).getAftRadius();
				}
			}
			if (r < 0) {
				r = DEFAULT_RADIUS;
			} else {
				// for 5,6, and 8 fins, adjust the diameter to provide touching fins.
				switch (fins) {
				case 5:
					r *= 1.43; //  sin(36) / (1- sin(36), 36 = 360/5/2
					break;
				case 7:
					r *= 0.77; // sin(25.7) / (1- sin(25.7)
					break;
				case 8:
					r *= 0.62; // sin(22.5) / (1- sin(22.5)
					break;
				}
			}
			return r;
		}
		return outerRadius;
	}
	
	/**
	 * Set the outer radius of the body tube.  If the radius is less than the wall thickness,
	 * the wall thickness is decreased accordingly of the value of the radius.
	 * This method sets the automatic radius off.
	 *
	 * @param radius  the outside radius in standard units
	 */
	public void setOuterRadius(double radius) {
		if ((this.outerRadius == radius) && (autoRadius == false))
			return;
		
		this.autoRadius = false;
		this.outerRadius = Math.max(radius, 0);
		
		if (this.thickness > this.outerRadius)
			this.thickness = this.outerRadius;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		clearPreset();
	}
	
	/**
	 * Sets whether the radius is selected automatically or not.
	 */
	public void setOuterRadiusAutomatic(boolean auto) {
		if (autoRadius == auto)
			return;
		
		autoRadius = auto;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		clearPreset();
	}
	
	public double getInnerRadius() {
		return Math.max(getOuterRadius() - thickness, 0);
	}
	
	public void setInnerRadius(double r) {
		setThickness(getOuterRadius() - r);
	}
	
	/**
	 * Return the component wall thickness.
	 */
	public double getThickness() {
		return Math.min(thickness, getOuterRadius());
	}
	
	
	/**
	 * Set the component wall thickness.  Values greater than the maximum radius are not
	 * allowed, and will result in setting the thickness to the maximum radius.
	 */
	public void setThickness(double thickness) {
		if ((this.thickness == thickness))
			return;
		this.thickness = MathUtil.clamp(thickness, 0, getOuterRadius());
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		clearPreset();
	}
	
	
	/**
	 * Return the number of fins in the set.
	 * @return The number of fins.
	 */
	public int getFinCount() {
		return fins;
	}
	
	/**
	 * Sets the number of fins in the set.
	 * @param n The number of fins, greater of equal to one.
	 */
	public void setFinCount(int n) {
		if (fins == n)
			return;
		if (n < 1)
			n = 1;
		if (n > 8)
			n = 8;
		fins = n;
		finRotation = Transformation.rotate_x(2 * Math.PI / fins);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	/**
	 * Gets the base rotation amount of the first fin.
	 * @return The base rotation amount.
	 */
	public double getBaseRotation() {
		return rotation;
	}
	
	public double getFinRotation() {
		return 2 * Math.PI / fins;
	}
	
	/**
	 * Sets the base rotation amount of the first fin.
	 * @param r The base rotation amount.
	 */
	public void setBaseRotation(double r) {
		r = MathUtil.reduce180(r);
		if (MathUtil.equals(r, rotation))
			return;
		rotation = r;
		baseRotation = Transformation.rotate_x(rotation);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public Transformation getBaseRotationTransformation() {
		return baseRotation;
	}
	
	public Transformation getFinRotationTransformation() {
		return finRotation;
	}
	
	@Override
	public void setRelativePosition(RocketComponent.Position position) {
		super.setRelativePosition(position);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	@Override
	public void setPositionValue(double value) {
		super.setPositionValue(value);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public double getComponentVolume() {
		double or = getOuterRadius();
		double ir = getInnerRadius();
		double volume = or * or - ir * ir;
		volume *= Math.PI;
		volume *= length;
		volume *= fins;
		return volume;
	}
	
	@Override
	public String getComponentName() {
		//// Tube Fin Set
		return trans.get("TubeFinSet.TubeFinSet");
	}
	
	@Override
	public Coordinate getComponentCG() {
		double mass = getComponentMass(); // safe
		double halflength = length / 2;
		
		if (fins == 1) {
			return baseRotation.transform(new Coordinate(halflength, getOuterRadius() + getBodyRadius(), 0, mass));
		} else {
			return baseRotation.transform(new Coordinate(halflength, 0, 0, mass));
		}
		
	}
	
	@Override
	public double getLongitudinalUnitInertia() {
		// Logitudinal Unit Inertia for a single tube fin.
		// 1/12 * (3 * (r1^2 + r2^2) + h^2)
		final double inertia = (3 * (MathUtil.pow2(getOuterRadius()) + MathUtil.pow2(getInnerRadius())) + MathUtil.pow2(getLength())) / 12;
		if (fins == 1) {
			return inertia;
		}
		
		// translate each to the center of mass.
		final double hypot = getOuterRadius() + getBodyRadius();
		final double finrotation = 2 * Math.PI / fins;
		double angularoffset = 0.0;
		double totalInertia = 0.0;
		for (int i = 0; i < fins; i++) {
			double offset = hypot * Math.cos(angularoffset);
			totalInertia += inertia + MathUtil.pow2(offset);
			angularoffset += finrotation;
		}
		return totalInertia;
	}
	
	@Override
	public double getRotationalUnitInertia() {
		// The rotational inertia of a single fin about its center.
		// 1/2 * (r1^2 + r2^2)
		double icentermass = (MathUtil.pow2(getInnerRadius()) + MathUtil.pow2(getOuterRadius())) / 2;
		if (fins == 1) {
			return icentermass;
		} else {
			// Use parallel axis rule and multiply by number of fins.
			return fins * (icentermass + MathUtil.pow2(getOuterRadius()) + getBodyRadius());
		}
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.BODY_TUBE;
	}
	
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Collection<Coordinate> getComponentBounds() {
		List<Coordinate> bounds = new ArrayList<Coordinate>();
		double r = getBodyRadius();
		
		addBound(bounds, 0, 2 * (r + outerRadius));
		addBound(bounds, length, 2 * (r + outerRadius));
		
		return bounds;
	}
	
	/**
	 * Return the radius of the BodyComponent the fin set is situated on.  Currently
	 * only supports SymmetricComponents and returns the radius at the starting point of the
	 * root chord.
	 *  
	 * @return  radius of the underlying BodyComponent or 0 if none exists.
	 */
	public double getBodyRadius() {
		RocketComponent s;
		
		s = this.getParent();
		while (s != null) {
			if (s instanceof SymmetricComponent) {
				double x = this.toRelative(new Coordinate(0, 0, 0), s)[0].x;
				return ((SymmetricComponent) s).getRadius(x);
			}
			s = s.getParent();
		}
		return 0;
	}
	
}
