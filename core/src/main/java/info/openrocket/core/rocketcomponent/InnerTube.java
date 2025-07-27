package info.openrocket.core.rocketcomponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.openrocket.core.util.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.MotorConfigurationSet;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.rocketcomponent.position.AxialPositionable;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;

/**
 * This class defines an inner tube that can be used as a motor mount. The
 * component
 * may also be clustered.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class InnerTube extends ThicknessRingComponent
		implements AxialPositionable, BoxBounded, Clusterable, RadialParent, MotorMount, InsideColorComponent {
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(InnerTube.class);
	
	private ClusterConfiguration cluster = ClusterConfiguration.SINGLE;
	private double clusterScale = 1.0;
	private double clusterRotation = 0.0;
	
	private double overhang = 0;
	private boolean isActingMount;
	private MotorConfigurationSet motors;

	private InsideColorComponentHandler insideColorComponentHandler = new InsideColorComponentHandler(this);
	
	/**
	 * Main constructor.
	 */
	public InnerTube() {
		// A-C motor size:
		this.setOuterRadius(0.019 / 2);
		this.setInnerRadius(0.018 / 2);
		this.setLength(0.070);
		
		motors = new MotorConfigurationSet(this);

		super.displayOrder_side = 5;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 14;		// Order for displaying the component in the 2D back view
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
	public String getPatternName() {
		return this.cluster.getXMLName();
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
	public void setClusterConfiguration( final ClusterConfiguration cluster) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof InnerTube) {
				((InnerTube) listener).setClusterConfiguration(cluster);
			}
		}

		if( cluster == this.cluster){
			// no change
			return;
		}else{
			this.cluster = cluster;
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		}
	}
	
	public BoundingBox getInstanceBoundingBox(){
		BoundingBox instanceBounds = new BoundingBox();
		
		instanceBounds.update(new Coordinate(this.getLength(), 0,0));
		
		final double r = getOuterRadius();
		instanceBounds.update(new Coordinate(0,r,r));
		instanceBounds.update(new Coordinate(0,-r,-r));
		
		return instanceBounds;
	}
	
	@Override
	public int getInstanceCount() {
		return cluster.getClusterCount();
	}
	
	@Override
	public void setInstanceCount( final int newCount ){
		log.error("Programmer Error:  cannot set the instance count of an InnerTube directly."+
				"  Please set setClusterConfiguration(ClusterConfiguration) instead.",
				new UnsupportedOperationException("InnerTube.setInstanceCount(..) on an"+this.getClass().getSimpleName()));
	}

	@Override
	public boolean isAfter(){
		return false;
	}

	/**
	 * Get the cluster scaling.  A value of 1.0 indicates that the tubes are packed
	 * touching each other, larger values separate the tubes and smaller values
	 * pack inside each other.
	 */
	public double getClusterScale() {
		mutex.verify();
		return clusterScale;
	}

	/**
	 * Set the cluster scaling.
	 * @see #getClusterScale()
	 */
	public void setClusterScale(double scale) {
		scale = Math.max(scale, 0);

		for (RocketComponent listener : configListeners) {
			if (listener instanceof InnerTube) {
				((InnerTube) listener).setClusterScale(scale);
			}
		}

		if (MathUtil.equals(clusterScale, scale))
			return;
		clusterScale = scale;
		fireComponentChangeEvent(new ComponentChangeEvent(this, ComponentChangeEvent.MASS_CHANGE));
	}

	/**
	 * Get the cluster scaling as an absolute distance measurement.  A value of 0 indicates that the tubes are packed
	 * touching each other, larger values separate the tubes and smaller values pack inside each other.
	 */
	public double getClusterScaleAbsolute() {
		return (getClusterScale() - 1) * getOuterRadius() * 2;
	}

	/**
	 * Set the absolute cluster scaling (in terms of distance).
	 * @see #getClusterScaleAbsolute()
	 */
	public void setClusterScaleAbsolute(double scale) {
		double scaleRel = scale / (getOuterRadius() * 2) + 1;
		setClusterScale(scaleRel);
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
		for (RocketComponent listener : configListeners) {
			if (listener instanceof InnerTube) {
				((InnerTube) listener).setClusterRotation(rotation);
			}
		}

		rotation = MathUtil.reducePi(rotation);
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
		List<Coordinate> list = new ArrayList<>(getInstanceCount());
		List<Double> points = cluster.getPoints(clusterRotation - getRadialDirection());
		double separation = getClusterSeparation();
		double yOffset = this.radialPosition * Math.cos(this.radialDirection);
		double zOffset = this.radialPosition * Math.sin(this.radialDirection);
		for (int i = 0; i < points.size() / 2; i++) {
			list.add(new Coordinate(0, points.get(2 * i) * separation + yOffset, points.get(2 * i + 1) * separation + zOffset));
		}
		return list;
	}
	
	@Override
	public Coordinate[] getInstanceOffsets(){
		List<Coordinate> points = getClusterPoints();
		return points.toArray(new Coordinate[0]);
	}
	
//	@Override
//	protected Coordinate[] shiftCoordinates(Coordinate[] array) {
//		array = super.shiftCoordinates(array);
//		
//		int count = getClusterCount();
//		if (count == 1)
//			return array;
//		
//		List<Coordinate> points = getClusterPoints();
//		if (points.size() != count) {
//			throw new BugException("Inconsistent cluster configuration, cluster count=" + count +
//					" point count=" + points.size());
//		}
//		Coordinate[] newArray = new Coordinate[array.length * count];
//		for (int i = 0; i < array.length; i++) {
//			for (int j = 0; j < count; j++) {
//				newArray[i * count + j] = array[i].add(points.get(j));
//			}
//		}
//		
//		return newArray;
//	}
	
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
	public void setMotorConfig( final MotorConfiguration newMotorConfig, final FlightConfigurationId fcid){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof MotorMount) {
				((MotorMount) listener).setMotorConfig(newMotorConfig, fcid);
			}
		}

		if((null == newMotorConfig)){
			this.motors.set( fcid, null);
		}else{
			if( this != newMotorConfig.getMount() ){
				throw new BugException(" attempt to add a MotorConfig to a second mount!");
			}
			
			this.motors.set(fcid, newMotorConfig);
		}

		this.isActingMount = true;
	}
	
	@Override
	public Iterator<MotorConfiguration> getMotorIterator(){
		return this.motors.iterator();
	}
	
	@Override
	public void copyFlightConfiguration(FlightConfigurationId oldConfigId, FlightConfigurationId newConfigId) {
		motors.copyFlightConfiguration(oldConfigId, newConfigId);
	}
	
	@Override
	public void reset( final FlightConfigurationId fcid){
		this.motors.reset(fcid);
	}
	
	@Override
    public void setMotorMount(boolean _active){
		for (RocketComponent listener : configListeners) {
			if (listener instanceof MotorMount) {
				((MotorMount) listener).setMotorMount(_active);
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
		// the default MotorInstance is the EMPTY_INSTANCE.  If we have more than that, then the other instance will have a motor.
		return this.motors.size() > 0;
	}
	
	@Override
	public double getMotorMountDiameter() {
		return getInnerRadius() * 2;
	}
	
	@Override
	public int getMotorCount() {
		return this.getClusterConfiguration().getClusterCount();
	}

	@Override
	public int getMotorCountIncludingAssemblyCopies() {
		// Get the parent assemblies of the motor mount, and multiply the data by the number of instances
		int multiplier = 1;
		List<RocketComponent> parents = getParentAssemblies();
		for (RocketComponent parent : parents) {
			multiplier *= parent.getInstanceCount();
		}

		int count = getMotorCount();
		return count * multiplier;
	}
	
	
	@Override
	public double getMotorOverhang() {
		return overhang;
	}
	
	@Override
	public void setMotorOverhang(double overhang) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof MotorMount) {
				((MotorMount) listener).setMotorOverhang(overhang);
			}
		}

		if (MathUtil.equals(this.overhang, overhang))
			return;
		this.overhang = overhang;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public Coordinate getMotorPosition(FlightConfigurationId id) {
		Motor motor = motors.get(id).getMotor();
		if (motor == null) {
			throw new IllegalArgumentException("No motor with id " + id + " defined.");
		}
		
		return new Coordinate(this.getLength() - motor.getLength() + this.getMotorOverhang());
	}
	
	@Override
	protected RocketComponent copyWithOriginalID() {
		InnerTube copy = (InnerTube) super.copyWithOriginalID();
		if( copy == this ){
			new IllegalArgumentException(" copyWithOriginalID should return a different instance! ");
		}
		if( copy.motors == this.motors ){
			new IllegalArgumentException(" copyWithOriginalID should produce different motorSet instances! ");
		}
		
		copy.motors = new MotorConfigurationSet( this.motors, copy );
		
		return copy;
	}

	
	/**
	 * For a given coordinate that represents one tube in a cluster, create an instance of that tube.  Must be called
	 * once for each tube in the cluster.
	 *
	 * @param coord        the coordinate of the clustered tube to create
	 * @param splitName    the name of the individual tube
	 * @param theInnerTube the 'parent' from which this tube will be created.
	 *
	 * @return an instance of an inner tube that represents ONE of the clustered tubes in the cluster represented
	 *  by <code>theInnerTube</code>
	 */
	public static InnerTube makeIndividualClusterComponent(Coordinate coord, String splitName, RocketComponent theInnerTube) {
		InnerTube copy = (InnerTube) theInnerTube.copy();
		copy.clearConfigListeners();
		copy.setClusterConfiguration(ClusterConfiguration.SINGLE);
		copy.setClusterRotation(0.0);
		copy.setClusterScale(1.0);
		copy.setRadialShift(coord.y, coord.z);
		copy.setName(splitName);
		return copy;
	}
	
	@Override
	public String toMotorDebug( ){
		return this.motors.toDebug();
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
		if (listener instanceof InnerTube) {
			MotorConfiguration config = ((InnerTube) listener).getDefaultMotorConfig();
			success = success && getDefaultMotorConfig().addConfigListener(config);
			return success;
		}
		return false;
	}

	@Override
	public void removeConfigListener(RocketComponent listener) {
		super.removeConfigListener(listener);
		if (listener instanceof InnerTube) {
			MotorConfiguration config = ((InnerTube) listener).getDefaultMotorConfig();
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
