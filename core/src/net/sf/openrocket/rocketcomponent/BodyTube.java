package net.sf.openrocket.rocketcomponent;

import java.util.Iterator;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.MotorConfigurationSet;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.*;


/**
 * Rocket body tube component.  Has only two parameters, a radius and length.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class BodyTube extends SymmetricComponent implements BoxBounded, MotorMount, Coaxial, InsideColorComponent {
	private static final Translator trans = Application.getTranslator();
	
	private double outerRadius = 0;
	private boolean autoRadius = false; // Radius chosen automatically based on parent component

	private SymmetricComponent refComp = null;	// Reference component that is used for the autoRadius
	
	// When changing the inner radius, thickness is modified
	private double overhang = 0;
	private boolean isActingMount = false;
	
	private MotorConfigurationSet motors;

	private InsideColorComponentHandler insideColorComponentHandler = new InsideColorComponentHandler(this);
	
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
		super.displayOrder_side = 0;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 1;		// Order for displaying the component in the 2D back view
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
			// Don't use the radius of a component who already has its auto diameter enabled
			if (c != null && !c.usesNextCompAutomatic()) {
				r = c.getFrontAutoRadius();
				refComp = c;
			}
			if (r < 0) {
				c = this.getNextSymmetricComponent();
				// Don't use the radius of a component who already has its auto diameter enabled
				if (c != null && !c.usesPreviousCompAutomatic()) {
					r = c.getRearAutoRadius();
					refComp = c;
				}
			}
			if (r < 0)
				r = DEFAULT_RADIUS;
			return r;
		}
		return outerRadius;
	}

	/**
	 * Return the outer radius that was manually entered, so not the value that the component received from automatic
	 * outer radius.
	 */
	public double getOuterRadiusNoAutomatic() {
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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof BodyTube) {
				((BodyTube) listener).setOuterRadius(radius);
			}
		}

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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof BodyTube) {
				((BodyTube) listener).setOuterRadiusAutomatic(auto);
			}
		}

		if (autoRadius == auto)
			return;
		
		autoRadius = auto;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
		clearPreset();
	}

	@Override
	public boolean usesPreviousCompAutomatic() {
		return isOuterRadiusAutomatic() && refComp == getPreviousSymmetricComponent();
	}

	@Override
	public boolean usesNextCompAutomatic() {
		return isOuterRadiusAutomatic() && refComp == getNextSymmetricComponent();
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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof BodyTube) {
				((BodyTube) listener).setInnerRadius(r);
			}
		}

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

	public BoundingBox getInstanceBoundingBox(){
		BoundingBox instanceBounds = new BoundingBox();

		instanceBounds.update(new Coordinate(this.getLength(), 0,0));

		final double r = getOuterRadius();
		instanceBounds.update(new Coordinate(0,r,r));
		instanceBounds.update(new Coordinate(0,-r,-r));

		return instanceBounds;
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
		if (ParallelStage.class.isAssignableFrom(type))
			return true;
		if (PodSet.class.isAssignableFrom(type))
			return true;
		
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
	public MotorConfigurationSet getMotorConfigurationSet() {
		return this.motors;
	}
	
	@Override
	public MotorConfiguration getMotorConfig( final FlightConfigurationId fcid){
		return this.motors.get(fcid);
	}

	@Override 
	public void setMotorConfig(MotorConfiguration newMotorConfig, FlightConfigurationId fcid){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof BodyTube) {
				if (newMotorConfig != null) {
					BodyTube tube = (BodyTube) listener;
					MotorConfiguration config = tube.getMotorConfig(fcid);
					config.copyFrom(newMotorConfig);
				}
			}
		}

		if(null == newMotorConfig){
			this.motors.set( fcid, null);
		}else{
			if( this != newMotorConfig.getMount() ){
				throw new BugException(" attempt to add a MotorConfig to a second mount! ");
			}
			
			this.motors.set(fcid,newMotorConfig);
		}		

		this.isActingMount=true;
	}
	
	
	@Override
	public Iterator<MotorConfiguration> getMotorIterator(){
		return this.motors.iterator();
	}
	
	@Override
	public void reset( final FlightConfigurationId fcid){
		this.motors.reset(fcid);
	}

	@Override
	public void copyFlightConfiguration(FlightConfigurationId oldConfigId, FlightConfigurationId newConfigId) {
		motors.copyFlightConfiguration(oldConfigId, newConfigId);
	}
	
	@Override
    public void setMotorMount(boolean _active){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof BodyTube) {
				((BodyTube) listener).setMotorMount(_active);
			}
		}

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
		return this.getClusterConfiguration().getClusterCount();
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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof BodyTube) {
				((BodyTube) listener).setMotorOverhang(overhang);
			}
		}

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

	@Override
	public ClusterConfiguration getClusterConfiguration() {
		return ClusterConfiguration.SINGLE;
	}


	@Override
	public InsideColorComponentHandler getInsideColorComponentHandler() {
		return this.insideColorComponentHandler;
	}

	@Override
	public void setInsideColorComponentHandler(InsideColorComponentHandler handler) {
		this.insideColorComponentHandler = handler;
	}

	@Override
	public boolean addConfigListener(RocketComponent listener) {
		boolean success = super.addConfigListener(listener);
		if (listener instanceof BodyTube) {
			MotorConfiguration config = ((BodyTube) listener).getDefaultMotorConfig();
			success = success && getDefaultMotorConfig().addConfigListener(config);
			return success;
		}
		return false;
	}

	@Override
	public void removeConfigListener(RocketComponent listener) {
		super.removeConfigListener(listener);
		if (listener instanceof BodyTube) {
			MotorConfiguration config = ((BodyTube) listener).getDefaultMotorConfig();
			getDefaultMotorConfig().removeConfigListener(config);
		}
	}

	@Override
	public void clearConfigListeners() {
		super.clearConfigListeners();
		// The motor config also has listeners, so clear them as well
		getDefaultMotorConfig().clearConfigListeners();
	}
}
