package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


/**
 * Rocket body tube component.  Has only two parameters, a radius and length.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class BodyTube extends SymmetricComponent implements MotorMount, Coaxial {
	private static final Translator trans = Application.getTranslator();
	
	private double outerRadius = 0;
	private boolean autoRadius = false; // Radius chosen automatically based on parent component
	
	// When changing the inner radius, thickness is modified
	
	private boolean motorMount = false;
	private HashMap<String, Double> ejectionDelays = new HashMap<String, Double>();
	private HashMap<String, Motor> motors = new HashMap<String, Motor>();
	private IgnitionEvent ignitionEvent = IgnitionEvent.AUTOMATIC;
	private double ignitionDelay = 0;
	private double overhang = 0;
	
	

	public BodyTube() {
		super();
		this.length = 8 * DEFAULT_RADIUS;
		this.outerRadius = DEFAULT_RADIUS;
		this.autoRadius = true;
	}
	
	public BodyTube(double length, double radius) {
		super();
		this.outerRadius = Math.max(radius, 0);
		this.length = Math.max(length, 0);
	}
	
	
	public BodyTube(double length, double radius, boolean filled) {
		this(length, radius);
		this.filled = filled;
	}
	
	public BodyTube(double length, double radius, double thickness) {
		this(length, radius);
		this.filled = false;
		this.thickness = thickness;
	}
	
	
	/************  Get/set component parameter methods ************/
	
	/**
	 * Return the outer radius of the body tube.
	 *
	 * @return  the outside radius of the tube
	 */
	@Override
	public double getOuterRadius() {
		if (autoRadius) {
			// Return auto radius from front or rear
			double r = -1;
			SymmetricComponent c = this.getPreviousSymmetricComponent();
			if (c != null) {
				r = c.getFrontAutoRadius();
			}
			if (r < 0) {
				c = this.getNextSymmetricComponent();
				if (c != null) {
					r = c.getRearAutoRadius();
				}
			}
			if (r < 0)
				r = DEFAULT_RADIUS;
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
	@Override
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
	 * Returns whether the radius is selected automatically or not.
	 * Returns false also in case automatic radius selection is not possible.
	 */
	public boolean isOuterRadiusAutomatic() {
		return autoRadius;
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
	
	
	@Override
	protected void loadFromPreset(RocketComponent preset) {
		super.loadFromPreset(preset);
		BodyTube bt = (BodyTube) preset;
		this.autoRadius = false;
		this.outerRadius = bt.getOuterRadius();
		this.thickness = (bt.getOuterRadius() - bt.getInnerRadius());
		this.length = bt.getLength();

		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		
	}
	@Override
	public double getAftRadius() {
		return getOuterRadius();
	}
	
	@Override
	public double getForeRadius() {
		return getOuterRadius();
	}
	
	@Override
	public boolean isAftRadiusAutomatic() {
		return isOuterRadiusAutomatic();
	}
	
	@Override
	public boolean isForeRadiusAutomatic() {
		return isOuterRadiusAutomatic();
	}
	
	

	@Override
	protected double getFrontAutoRadius() {
		if (isOuterRadiusAutomatic()) {
			// Search for previous SymmetricComponent
			SymmetricComponent c = this.getPreviousSymmetricComponent();
			if (c != null) {
				return c.getFrontAutoRadius();
			} else {
				return -1;
			}
		}
		return getOuterRadius();
	}
	
	@Override
	protected double getRearAutoRadius() {
		if (isOuterRadiusAutomatic()) {
			// Search for next SymmetricComponent
			SymmetricComponent c = this.getNextSymmetricComponent();
			if (c != null) {
				return c.getRearAutoRadius();
			} else {
				return -1;
			}
		}
		return getOuterRadius();
	}
	
	



	@Override
	public double getInnerRadius() {
		if (filled)
			return 0;
		return Math.max(getOuterRadius() - thickness, 0);
	}
	
	@Override
	public void setInnerRadius(double r) {
		setThickness(getOuterRadius() - r);
	}
	
	


	/**
	 * Return the component name.
	 */
	@Override
	public String getComponentName() {
		//// Body tube
		return trans.get("BodyTube.BodyTube");
	}
	
	
	/************ Component calculations ***********/
	
	// From SymmetricComponent
	/**
	 * Returns the outer radius at the position x.  This returns the same value as getOuterRadius().
	 */
	@Override
	public double getRadius(double x) {
		return getOuterRadius();
	}
	
	/**
	 * Returns the inner radius at the position x.  If the tube is filled, returns always zero.
	 */
	@Override
	public double getInnerRadius(double x) {
		if (filled)
			return 0.0;
		else
			return Math.max(getOuterRadius() - thickness, 0);
	}
	
	
	/**
	 * Returns the body tube's center of gravity.
	 */
	@Override
	public Coordinate getComponentCG() {
		return new Coordinate(length / 2, 0, 0, getComponentMass());
	}
	
	/**
	 * Returns the body tube's volume.
	 */
	@Override
	public double getComponentVolume() {
		double r = getOuterRadius();
		if (filled)
			return getFilledVolume(r, length);
		else
			return getFilledVolume(r, length) - getFilledVolume(getInnerRadius(0), length);
	}
	
	
	@Override
	public double getLongitudinalUnitInertia() {
		// 1/12 * (3 * (r1^2 + r2^2) + h^2)
		return (3 * (MathUtil.pow2(getInnerRadius())) + MathUtil.pow2(getOuterRadius()) + MathUtil.pow2(getLength())) / 12;
	}
	
	@Override
	public double getRotationalUnitInertia() {
		// 1/2 * (r1^2 + r2^2)
		return (MathUtil.pow2(getInnerRadius()) + MathUtil.pow2(getOuterRadius())) / 2;
	}
	
	


	/**
	 * Helper function for cylinder volume.
	 */
	private static double getFilledVolume(double r, double l) {
		return Math.PI * r * r * l;
	}
	
	
	/**
	 * Adds bounding coordinates to the given set.  The body tube will fit within the
	 * convex hull of the points.
	 *
	 * Currently the points are simply a rectangular box around the body tube.
	 */
	@Override
	public Collection<Coordinate> getComponentBounds() {
		Collection<Coordinate> bounds = new ArrayList<Coordinate>(8);
		double r = getOuterRadius();
		addBound(bounds, 0, r);
		addBound(bounds, length, r);
		return bounds;
	}
	
	

	/**
	 * Check whether the given type can be added to this component.  BodyTubes allow any
	 * InternalComponents or ExternalComponents, excluding BodyComponents, to be added.
	 *
	 * @param type  The RocketComponent class type to add.
	 * @return      Whether such a component can be added.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		if (InternalComponent.class.isAssignableFrom(type))
			return true;
		if (ExternalComponent.class.isAssignableFrom(type) &&
				!BodyComponent.class.isAssignableFrom(type))
			return true;
		return false;
	}
	
	////////////////  Motor mount  /////////////////
	
	@Override
	public boolean isMotorMount() {
		return motorMount;
	}
	
	@Override
	public void setMotorMount(boolean mount) {
		if (motorMount == mount)
			return;
		motorMount = mount;
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
	}
	
	@Override
	public Motor getMotor(String id) {
		if (id == null)
			return null;
		
		// Check whether the id is valid for the current rocket
		RocketComponent root = this.getRoot();
		if (!(root instanceof Rocket))
			return null;
		if (!((Rocket) root).isMotorConfigurationID(id))
			return null;
		
		return motors.get(id);
	}
	
	@Override
	public void setMotor(String id, Motor motor) {
		if (id == null) {
			if (motor != null) {
				throw new IllegalArgumentException("Cannot set non-null motor for id null");
			}
		}
		Motor current = motors.get(id);
		if ((motor == null && current == null) ||
				(motor != null && motor.equals(current)))
			return;
		motors.put(id, motor);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
	}
	
	@Override
	public double getMotorDelay(String id) {
		Double delay = ejectionDelays.get(id);
		if (delay == null)
			return Motor.PLUGGED;
		return delay;
	}
	
	@Override
	public void setMotorDelay(String id, double delay) {
		ejectionDelays.put(id, delay);
		fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
	}
	
	@Override
	public int getMotorCount() {
		return 1;
	}
	
	@Override
	public double getMotorMountDiameter() {
		return getInnerRadius() * 2;
	}
	
	@Override
	public IgnitionEvent getIgnitionEvent() {
		return ignitionEvent;
	}
	
	@Override
	public void setIgnitionEvent(IgnitionEvent event) {
		if (ignitionEvent == event)
			return;
		ignitionEvent = event;
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	
	@Override
	public double getIgnitionDelay() {
		return ignitionDelay;
	}
	
	@Override
	public void setIgnitionDelay(double delay) {
		if (MathUtil.equals(delay, ignitionDelay))
			return;
		ignitionDelay = delay;
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	
	@Override
	public double getMotorOverhang() {
		return overhang;
	}
	
	@Override
	public void setMotorOverhang(double overhang) {
		if (MathUtil.equals(this.overhang, overhang))
			return;
		this.overhang = overhang;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	@Override
	public Coordinate getMotorPosition(String id) {
		Motor motor = motors.get(id);
		if (motor == null) {
			throw new IllegalArgumentException("No motor with id " + id + " defined.");
		}
		
		return new Coordinate(this.getLength() - motor.getLength() + this.getMotorOverhang());
	}
	
	


	/*
	 * (non-Javadoc)
	 * Copy the motor and ejection delay HashMaps.
	 *
	 * @see rocketcomponent.RocketComponent#copy()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected RocketComponent copyWithOriginalID() {
		RocketComponent c = super.copyWithOriginalID();
		((BodyTube) c).motors = (HashMap<String, Motor>) motors.clone();
		((BodyTube) c).ejectionDelays = (HashMap<String, Double>) ejectionDelays.clone();
		return c;
	}
}
