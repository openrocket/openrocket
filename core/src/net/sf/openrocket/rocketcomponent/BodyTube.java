package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.MotorConfigurationSet;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
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
	private double overhang = 0;
	private boolean isActingMount = false;
	
	private MotorConfigurationSet motors;
	
	public BodyTube() {
		this(8 * DEFAULT_RADIUS, DEFAULT_RADIUS);
		this.autoRadius = true;
	}
	
	// root ctor. Always called by other ctors
	public BodyTube(double length, double radius) {
		super();
		this.outerRadius = Math.max(radius, 0);
		this.length = Math.max(length, 0);
		motors = new MotorConfigurationSet(this);
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
	
	@Override
	public ComponentPreset.Type getPresetType() {
		return ComponentPreset.Type.BODY_TUBE;
	}
	
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
	protected void loadFromPreset(ComponentPreset preset) {
		this.autoRadius = false;
		if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
			double outerDiameter = preset.get(ComponentPreset.OUTER_DIAMETER);
			this.outerRadius = outerDiameter / 2.0;
			if (preset.has(ComponentPreset.INNER_DIAMETER)) {
				double innerDiameter = preset.get(ComponentPreset.INNER_DIAMETER);
				this.thickness = (outerDiameter - innerDiameter) / 2.0;
			}
		}
		
		super.loadFromPreset(preset);
		
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
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
		// 1/12 * (3 * (r2^2 + r1^2) + h^2)
		return (3 * (MathUtil.pow2(getOuterRadius()) + MathUtil.pow2(getInnerRadius())) + MathUtil.pow2(getLength())) / 12;
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
		double x_min_shape = 0;
		double x_max_shape = this.length;
		double r_max_shape = getOuterRadius();
		
		Coordinate[] locs = this.getLocations();
		// not strictly accurate, but this should provide an acceptable estimate for total vehicle size
		double x_min_inst = Double.MAX_VALUE;
		double x_max_inst = Double.MIN_VALUE;
		double r_max_inst = 0.0;
		
		// refactor: get component inherent bounds
		for (Coordinate cur : locs) {
			double x_cur = cur.x;
			double r_cur = MathUtil.hypot(cur.y, cur.z);
			if (x_min_inst > x_cur) {
				x_min_inst = x_cur;
			}
			if (x_max_inst < x_cur) {
				x_max_inst = x_cur;
			}
			if (r_cur > r_max_inst) {
				r_max_inst = r_cur;
			}
		}
		
		// combine the position bounds with the inherent shape bounds
		double x_min = x_min_shape + x_min_inst;
		double x_max = x_max_shape + x_max_inst;
		double r_max = r_max_shape + r_max_inst;
		
		addBoundingBox(bounds, x_min, x_max, r_max);
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
	public MotorConfiguration getDefaultMotorConfig(){
		return this.motors.getDefault();
	}
	
	@Override
	public MotorConfiguration getMotorConfig( final FlightConfigurationId fcid){
		return this.motors.get(fcid);
	}

	@Override 
	public void setMotorConfig( final MotorConfiguration newMotorConfig, final FlightConfigurationId fcid){
		if(null == newMotorConfig){
			this.motors.set( fcid, null);
		}else{
			if( this != newMotorConfig.getMount() ){
				throw new BugException(" attempt to add a MotorConfig to a second mount! ");
			}
			
			this.motors.set(fcid,newMotorConfig);
		}		

		this.isActingMount=true;
		
		// this is done automatically in the motorSet
		//fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
	}
	
	
	@Override
	public Iterator<MotorConfiguration> getMotorIterator(){
		return this.motors.iterator();
	}

	@Override
	public void cloneFlightConfiguration(FlightConfigurationId oldConfigId, FlightConfigurationId newConfigId) {
		motors.cloneFlightConfiguration(oldConfigId, newConfigId);
	}
	
	@Override
    public void setMotorMount(boolean _active){
    	if (this.isActingMount == _active)
    		return;
    	this.isActingMount = _active;
    	fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
    }

	@Override
	public boolean isMotorMount(){
		return this.isActingMount;
	}
	
	@Override
	public boolean hasMotor() {
		// the default MotorInstance is the EMPTY_INSTANCE.  
		// If the class contains more instances, at least one will have motors.
		return ( 1 < this.motors.size());
	}
		
	@Override
	public int getMotorCount() {
		return this.motors.size();
	}
		
	@Override
	public double getMotorMountDiameter() {
		return getInnerRadius() * 2;
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
	public Coordinate getMotorPosition(FlightConfigurationId id) {
		Motor motor = this.motors.get(id).getMotor();
		if (motor == null) {
			throw new IllegalArgumentException("No motor with id " + id + " defined.");
		}
		
		return new Coordinate(this.getLength() - motor.getLength() + this.getMotorOverhang());
	}

	@Override
	public String toMotorDebug(){
		return this.motors.toDebug();
	}
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		BodyTube copy = (BodyTube) super.copyWithOriginalID();
		
		copy.motors = new MotorConfigurationSet( this.motors, copy );
		return copy;
	}
}
