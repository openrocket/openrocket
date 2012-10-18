package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


/**
 * This class defines an inner tube that can be used as a motor mount.  The component
 * may also be clustered.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class InnerTube extends ThicknessRingComponent implements Clusterable, RadialParent, MotorMount {
	private static final Translator trans = Application.getTranslator();
	
	private ClusterConfiguration cluster = ClusterConfiguration.SINGLE;
	private double clusterScale = 1.0;
	private double clusterRotation = 0.0;
	
	private boolean motorMount = false;
	private double overhang = 0;

	private BaseMotorMount baseMotorMount = new BaseMotorMount();
	
	/**
	 * Main constructor.
	 */
	public InnerTube() {
		// A-C motor size:
		this.setOuterRadius(0.019 / 2);
		this.setInnerRadius(0.018 / 2);
		this.setLength(0.070);
	}
	
	
	@Override
	public double getInnerRadius(double x) {
		return getInnerRadius();
	}
	
	
	@Override
	public double getOuterRadius(double x) {
		return getOuterRadius();
	}
	
	
	@Override
	public String getComponentName() {
		//// Inner Tube
		return trans.get("InnerTube.InnerTube");
	}
	
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
	/**
	 * Allow all InternalComponents to be added to this component.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return InternalComponent.class.isAssignableFrom(type);
	}
	
	@Override
	public ComponentPreset.Type getPresetType() {
		return ComponentPreset.Type.BODY_TUBE;
	}

	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		if ( preset.has(ComponentPreset.OUTER_DIAMETER) )  {
			double outerDiameter = preset.get(ComponentPreset.OUTER_DIAMETER);
			this.outerRadius = outerDiameter/2.0;
			if ( preset.has(ComponentPreset.INNER_DIAMETER) ) {
				double innerDiameter = preset.get(ComponentPreset.INNER_DIAMETER);
				this.thickness = (outerDiameter-innerDiameter) / 2.0;
			}
		}

		super.loadFromPreset(preset);

		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}


	/////////////  Cluster methods  //////////////
	
	/**
	 * Get the current cluster configuration.
	 * @return  The current cluster configuration.
	 */
	@Override
	public ClusterConfiguration getClusterConfiguration() {
		return cluster;
	}
	
	/**
	 * Set the current cluster configuration.
	 * @param cluster  The cluster configuration.
	 */
	@Override
	public void setClusterConfiguration(ClusterConfiguration cluster) {
		this.cluster = cluster;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	/**
	 * Return the number of tubes in the cluster.
	 * @return Number of tubes in the current cluster.
	 */
	@Override
	public int getClusterCount() {
		return cluster.getClusterCount();
	}
	
	/**
	 * Get the cluster scaling.  A value of 1.0 indicates that the tubes are packed
	 * touching each other, larger values separate the tubes and smaller values
	 * pack inside each other.
	 */
	public double getClusterScale() {
		return clusterScale;
	}
	
	/**
	 * Set the cluster scaling.
	 * @see #getClusterScale()
	 */
	public void setClusterScale(double scale) {
		scale = Math.max(scale, 0);
		if (MathUtil.equals(clusterScale, scale))
			return;
		clusterScale = scale;
		fireComponentChangeEvent(new ComponentChangeEvent(this, ComponentChangeEvent.MASS_CHANGE));
	}
	
	

	/**
	 * @return the clusterRotation
	 */
	public double getClusterRotation() {
		return clusterRotation;
	}
	
	
	/**
	 * @param rotation the clusterRotation to set
	 */
	public void setClusterRotation(double rotation) {
		rotation = MathUtil.reduce180(rotation);
		if (clusterRotation == rotation)
			return;
		this.clusterRotation = rotation;
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	
	/**
	 * Return the distance between the closest two cluster inner tube center points.
	 * This is equivalent to the cluster scale multiplied by the tube diameter.
	 */
	@Override
	public double getClusterSeparation() {
		return 2 * getOuterRadius() * clusterScale;
	}
	
	
	public List<Coordinate> getClusterPoints() {
		List<Coordinate> list = new ArrayList<Coordinate>(getClusterCount());
		List<Double> points = cluster.getPoints(clusterRotation - getRadialDirection());
		double separation = getClusterSeparation();
		for (int i = 0; i < points.size() / 2; i++) {
			list.add(new Coordinate(0, points.get(2 * i) * separation, points.get(2 * i + 1) * separation));
		}
		return list;
	}
	
	
	@Override
	public Coordinate[] shiftCoordinates(Coordinate[] array) {
		array = super.shiftCoordinates(array);
		
		int count = getClusterCount();
		if (count == 1)
			return array;
		
		List<Coordinate> points = getClusterPoints();
		if (points.size() != count) {
			throw new BugException("Inconsistent cluster configuration, cluster count=" + count +
					" point count=" + points.size());
		}
		Coordinate[] newArray = new Coordinate[array.length * count];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < count; j++) {
				newArray[i * count + j] = array[i].add(points.get(j));
			}
		}
		
		return newArray;
	}
	
	////////////////  Motor mount  /////////////////
	

	@Override
	public MotorConfiguration getFlightConfiguration(String configId) {
		return baseMotorMount.getFlightConfiguration(configId);
	}


	@Override
	public void setFlightConfiguration(String configId,	MotorConfiguration config) {
		baseMotorMount.setFlightConfiguration(configId, config);
		fireComponentChangeEvent(ComponentChangeEvent.ALL_CHANGE);
	}


	@Override
	public void cloneFlightConfiguration(String oldConfigId, String newConfigId) {
		baseMotorMount.cloneFlightConfiguration(oldConfigId, newConfigId);
	}


	@Override
	public MotorConfiguration getDefaultFlightConfiguration() {
		return baseMotorMount.getDefaultFlightConfiguration();
	}


	@Override
	public void setDefaultFlightConfiguration(MotorConfiguration config) {
		baseMotorMount.setDefaultFlightConfiguration(config);
		fireComponentChangeEvent(ComponentChangeEvent.ALL_CHANGE);
	}


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
	
	
	@Deprecated
	@Override
	public int getMotorCount() {
		return getClusterCount();
	}
	
	@Override
	public double getMotorMountDiameter() {
		return getInnerRadius() * 2;
	}
	
	// FIXME - rename to getDefaultIgnitionEvent
	@Override
	public MotorConfiguration.IgnitionEvent getIgnitionEvent() {
		return getDefaultFlightConfiguration().getIgnitionEvent();
	}
	
	// FIXME
	@Override
	public void setIgnitionEvent(MotorConfiguration.IgnitionEvent event) {
		MotorConfiguration.IgnitionEvent ignitionEvent = getIgnitionEvent();
		if (ignitionEvent == event)
			return;
		getDefaultFlightConfiguration().setIgnitionEvent(event);
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	// FIXME
	@Override
	public double getIgnitionDelay() {
		return getDefaultFlightConfiguration().getIgnitionDelay();
	}
	
	// FIXME
	@Override
	public void setIgnitionDelay(double delay) {
		double ignitionDelay = getIgnitionDelay();
		if (MathUtil.equals(delay, ignitionDelay))
			return;
		getDefaultFlightConfiguration().setIgnitionDelay(delay);
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	@Override
	public Motor getMotor(String id) {
		return baseMotorMount.getMotor(id);
	}

	@Override
	public void setMotor(String id, Motor motor) {
		if (baseMotorMount.setMotor(id, motor) ) {
			fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
		}
	}


	public double getMotorDelay(String id) {
		return baseMotorMount.getMotorDelay(id);
	}


	public void setMotorDelay(String id, double delay) {
		if (baseMotorMount.setMotorDelay(id, delay) ) {
			fireComponentChangeEvent(ComponentChangeEvent.MOTOR_CHANGE);
		}
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
		Motor motor = getMotor(id);
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
		((InnerTube) c).baseMotorMount = baseMotorMount.clone();
		return c;
	}
	
}