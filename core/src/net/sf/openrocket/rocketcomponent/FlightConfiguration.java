package net.sf.openrocket.rocketcomponent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.MotorConfigurationId;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BoundingBox;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.Transformation;


/**
 * A class defining a rocket configuration.
 *     Describes active stages, and active motors.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 * @author Daniel Williams <equipoise@gmail.com>
 */
public class FlightConfiguration implements FlightConfigurableParameter<FlightConfiguration>, Monitorable {
	private static final Logger log = LoggerFactory.getLogger(FlightConfiguration.class);
	private static final Translator trans = Application.getTranslator();

    private String configurationName;
	public static String DEFAULT_CONFIG_NAME = "[{motors}]";
	
	protected final Rocket rocket;
	protected final FlightConfigurationId fcid;
	
	private static int configurationInstanceCount=0;
    // made public for testing.... there is probably a better way
	public final int configurationInstanceId;

	private class StageFlags implements Cloneable {
		public boolean active = true;
		public int stageNumber = -1;
		public String stageId;

		public StageFlags(int _num, String stageId, boolean _active) {
			this.stageNumber = _num;
			this.stageId = stageId;
			this.active = _active;
		}
		
		@Override
		public StageFlags clone(){
			return new StageFlags(this.stageNumber, this.stageId, true);
		}
	}
	
	/* Cached data */
	final protected Map<Integer, StageFlags> stages = new HashMap<>();	// Map of stage number to StageFlags of the corresponding stage
	final protected Map<MotorConfigurationId, MotorConfiguration> motors = new HashMap<MotorConfigurationId, MotorConfiguration>();
	private Map<Integer, Boolean> preloadStageActiveness = null;
	final private Collection<MotorConfiguration> activeMotors = new ConcurrentLinkedQueue<MotorConfiguration>();
	final private InstanceMap activeInstances = new InstanceMap();
	final private InstanceMap extraRenderInstances = new InstanceMap();		// Extra instances to be rendered, besides the active instances
	
	private int boundsModID = -1;
	private BoundingBox cachedBoundsAerodynamic = new BoundingBox();	// Bounding box of all aerodynamic components
	private BoundingBox cachedBounds = new BoundingBox();	// Bounding box of all components
	private double cachedLengthAerodynamic = -1;	// Rocket length of all aerodynamic components
	private double cachedLength = -1;	// Rocket length of all components
	
	private int refLengthModID = -1;
	private double cachedRefLength = -1;
	
	private int modID = 0;

	/**
	 * Create a Default configuration with the specified <code>Rocket</code>.
	 *
	 * @param rocket  the rocket
	 */
	public FlightConfiguration(final Rocket rocket) {
		this(rocket, FlightConfigurationId.DEFAULT_VALUE_FCID);
	}

	/**
	 * Create a new configuration with the specified <code>Rocket</code>.
	 * 
	 * @param _fcid  the ID this configuration should have.
	 * @param rocket  the rocket
	 */
	public FlightConfiguration(final Rocket rocket, final FlightConfigurationId _fcid ) {
		if( null == _fcid){
			this.fcid = new FlightConfigurationId();
		}else{
			this.fcid = _fcid;
		}
		this.rocket = rocket;
		this.configurationName = DEFAULT_CONFIG_NAME;
		this.configurationInstanceId = configurationInstanceCount++;
		
		updateStages();
		updateMotors();
		updateActiveInstances();
	}
	
	public Rocket getRocket() {
		return rocket;
	}
	
	
	public void clearAllStages() {
		this._setAllStages(false);
	}
	
	public void setAllStages() {
		this._setAllStages(true);
	}

	private void _setAllStages(final boolean _active) {
		for (StageFlags cur : stages.values()) {
			cur.active = _active;
		}
		updateMotors();
		updateActiveInstances();
	}

	public void copyStages(FlightConfiguration other) {
		for (StageFlags cur : other.stages.values())
			stages.put(cur.stageNumber, new StageFlags(cur.stageNumber, cur.stageId, cur.active));
		updateMotors();
		updateActiveInstances();
	}

	/**
	 * Copy only the stage activeness from another configuration.
	 * @param other the configuration to copy the stage active flags from.
	 */
	public void copyStageActiveness(FlightConfiguration other) {
		for (StageFlags flags : this.stages.values()) {
			StageFlags otherFlags = other.stages.get(flags.stageNumber);
			if (otherFlags != null) {
				flags.active = otherFlags.active;
			}
		}
		updateMotors();
		updateActiveInstances();
	}
	
	/** 
	 * This method flags a stage inactive.  Other stages are unaffected.
	 * 
	 * @param stageNumber  stage number to turn off
	 */
	public void clearStage(final int stageNumber) {
		_setStageActive( stageNumber, false );
	}

	/**
	 * This method clears a stage and all stages below (higher stage number)
	 *
	 * @param stageNumber first stage number to turn off
	 */
	public void clearStagesBelow(int stageNumber) {
		// I can't just use _setStageActive(stageNumber, false, true)
		// because that won't clear side boosters' active flags (should it?)
		for (int i = stageNumber; i < rocket.getStageCount(); i++) {
			_setStageActive(i, false, false);
		}
	}

	/**
	 * This method clears all stages above (but not including) a stage
	 *
	 * @param stageNumber first stage number to stay active
	 */
	public void clearStagesAbove(int stageNumber) {
		for (int i = 0; i < stageNumber; i++) {
			_setStageActive(i, false, false);
		}
	}
	
	/**
	 * Activates all stages as active starting from the specified component
	 * to the top-most stage in the rocket. Active stages are those stages
	 * which contribute to the mass of the rocket. Given a rocket with the
	 * following stages:
	 * 
	 * <ul>
	 *   <li>StageA - top most stage, containing nose cone etc.</li>
	 *   <li>StageB - middle stage</li>
	 *   <li>StageC - bottom stage</li>
	 * </ul>
	 * 
	 * invoking <code>FlightConfiguration.activateStagesThrough(StageB)</code>
	 * will cause both StageA and StageB to be marked as active, and StageC
	 * will be marked as inactive.
	 * 
	 * @param stage the AxialStage to activate all stages up to (inclusive)
	 */
	public void activateStagesThrough(final AxialStage stage) {
		clearAllStages();
		for (int i=0; i <= stage.getStageNumber(); i++) {
			_setStageActive(i, true);
		}
	}
	
	/** 
	 * This method flags the specified stage as active, and all other stages as inactive.
	 * 
	 * @param stageNumber  stage number to activate
	 */
	public void setOnlyStage(final int stageNumber) {
		_setAllStages(false);
		_setStageActive(stageNumber, true, false);
	}

	/**
	 * This method flags the specified stage as requested.  Substages may be affected, depending on third parameter
	 *
	 * @param stageNumber   stage number to flag
	 * @param _active       inactive (<code>false</code>) or active (<code>true</code>)
	 * @param activateSubStages whether the sub-stages of the specified stage should be activated as well.
	 */
	public void _setStageActive(final int stageNumber, final boolean _active, final boolean activateSubStages) {
		if ((0 <= stageNumber) && (stages.containsKey(stageNumber))) {
			stages.get(stageNumber).active = _active;
			if (activateSubStages) {
				// Set the active state of all the sub-stages as well.
				for (AxialStage stage : rocket.getStage(stageNumber).getSubStages()) {
					stages.get(stage.getStageNumber()).active = _active;
				}
			}
			fireChangeEvent();
			return;
		}
		log.error("error: attempt to retrieve via a bad stage number: " + stageNumber);
	}
	
	/** 
	 * This method flags the specified stage as requested.  Actives the sub-stages of the specified stage as well.
	 * 
	 * @param stageNumber   stage number to flag
	 * @param _active       inactive (<code>false</code>) or active (<code>true</code>)
	 */
	public void _setStageActive(final int stageNumber, final boolean _active ) {
		_setStageActive(stageNumber, _active, true);
	}
	
	
	public void toggleStage(final int stageNumber) {
		if ((0 <= stageNumber) && (stages.containsKey(stageNumber))) {
			StageFlags flags = stages.get(stageNumber);
			flags.active = !flags.active;
			// Set the active state of all the sub-stages as well.
			for (AxialStage stage : rocket.getStage(stageNumber).getSubStages()) {
				stages.get(stage.getStageNumber()).active = flags.active;
			}
			fireChangeEvent();
			
			return;
		}
		log.error("error: attempt to retrieve via a bad stage number: " + stageNumber);
	}

		
	/**
	 * Check whether the stage specified by the index is active.
	 */
	public boolean isStageActive(int stageNumber) {
		if( -1 == stageNumber ) {
			return true;
		}

		AxialStage stage = rocket.getStage(stageNumber);
		return stage != null && stage.getChildCount() > 0 &&		// Stages with no children are marked as inactive
				stages.get(stageNumber) != null && stages.get(stageNumber).active;
	}

	/**
	 * Preload the stage activeness of a certain stage.
	 * This method is to be used during the import and readout of an OpenRocket design file.
	 * @param stageNumber stage number to preload the stage activeness for
	 * @param isActive whether the stage should be active or not
	 */
	public void preloadStageActiveness(int stageNumber, boolean isActive) {
		if (this.preloadStageActiveness == null) {
			preloadStageActiveness = new HashMap<>();
		}
		this.preloadStageActiveness.put(stageNumber, isActive);
	}

	/**
	 * Applies preloaded stage activeness.
	 * This method should be called after the rocket has been loaded from a file.
	 */
	public void applyPreloadedStageActiveness() {
		if (preloadStageActiveness == null) {
			return;
		}
		for (int stageNumber : preloadStageActiveness.keySet()) {
			_setStageActive(stageNumber, preloadStageActiveness.get(stageNumber), false);
		}
		preloadStageActiveness.clear();
		preloadStageActiveness = null;
	}

	public Collection<RocketComponent> getAllComponents() {
		List<RocketComponent> traversalOrder = new ArrayList<RocketComponent>();
		recurseAllComponentsDepthFirst(this.rocket,traversalOrder);
		return traversalOrder;
	}

	private void recurseAllComponentsDepthFirst(RocketComponent comp, List<RocketComponent> traversalOrder){
		traversalOrder.add(comp);
		for( RocketComponent child : comp.getChildren()){
			recurseAllComponentsDepthFirst(child, traversalOrder);
		}
	}

	/** Returns all the components on core stages (i.e. centerline)
	 * 
	 * NOTE: components, NOT instances
	 */
	public ArrayList<RocketComponent> getCoreComponents() {
		Queue<RocketComponent> toProcess = new ArrayDeque<RocketComponent>();
		toProcess.offer(this.rocket);
		
		ArrayList<RocketComponent> toReturn = new ArrayList<>();
		
		while (!toProcess.isEmpty()) {
			RocketComponent comp = toProcess.poll();
			
			if (! comp.getClass().equals(Rocket.class)) {
				toReturn.add(comp);
			}
			
			for (RocketComponent child : comp.getChildren()) {
				if (child.getClass().equals(AxialStage.class)) {
					// recurse through AxialStage -- these are still centerline.
				    // however -- insist on an exact type match to disallow off-core stages
					if(isStageActive(child.getStageNumber())){
						toProcess.offer(child);
					}
				}else if( child instanceof ComponentAssembly) {
					// i.e. ParallelStage or PodSet
					// pass
				}else{
					toProcess.offer(child);
				}
				
			}
		}
		
		return toReturn;
	}
	
	// this method is deprecated because it ignores instancing of parent components (e.g. Strapons or pods )
	// depending on your context, this may or may not be what you want.
	// recommend migrating to either: `getAllComponents` or `getActiveInstances`
	@Deprecated
	public Collection<RocketComponent> getActiveComponents() {
		Queue<RocketComponent> toProcess = new ArrayDeque<RocketComponent>(this.getActiveStages());
		ArrayList<RocketComponent> toReturn = new ArrayList<>();
		
		while (!toProcess.isEmpty()) {
			RocketComponent comp = toProcess.poll();
			if (comp == null) {
				continue;
			}
			
			toReturn.add(comp);
			for (RocketComponent child : comp.getChildren()) {
				if (!(child instanceof AxialStage)) {
					toProcess.offer(child);
				}
			}
		}
		
		return toReturn;
	}

	public InstanceMap getActiveInstances() {
		return activeInstances;
	}

	/**
	 * Returns the InstanceMap of instances that need to be rendered, but are not present in {@link #getActiveInstances()}.
	 * This is the case for example for a booster that has no children. It is marked as an inactive stage, but it still needs to be rendered.
	 * @return the InstanceMap of instances that need to be rendered, but are not present in {@link #getActiveInstances()}.
	 */
	public InstanceMap getExtraRenderInstances() {
		return extraRenderInstances;
	}
	
	/*
	 * Generates a read-only, instance-aware collection of the components for this rocket & configuration
	 * 
	 *  TODO: swap in this function for the 'getActiveComponents() function, above;  ONLY WHEN READY / MATURE! 
	 */
	private void updateActiveInstances() {
		activeInstances.clear();
		extraRenderInstances.clear();
		getActiveContextListAt(this.rocket, activeInstances, Transformation.IDENTITY);
	}

	private InstanceMap getActiveContextListAt(final RocketComponent component, final InstanceMap results, final Transformation parentTransform ){

		final int instanceCount = component.getInstanceCount();
		final Coordinate[] allOffsets = component.getInstanceOffsets();
		final double[] allAngles = component.getInstanceAngles();
		
		final Transformation compLocTransform = Transformation.getTranslationTransform( component.getPosition() );
		final Transformation componentTransform = parentTransform.applyTransformation(compLocTransform);

		// generate the Instance's Context:
		for(int currentInstanceNumber=0; currentInstanceNumber < instanceCount; currentInstanceNumber++) {
			final Transformation offsetTransform = Transformation.getTranslationTransform( allOffsets[currentInstanceNumber] );
			final Transformation angleTransform = Transformation.getAxialRotation(allAngles[currentInstanceNumber]);
			final Transformation currentTransform = componentTransform.applyTransformation(offsetTransform)
				.applyTransformation(angleTransform);

			// constructs entry in-place if this component is active
			if (this.isComponentActive(component)) {
				results.emplace(component, currentInstanceNumber, currentTransform);
			} else if (component instanceof ParallelStage && stages.get(component.getStageNumber()).active) {
				// Boosters with no children are marked as inactive, but still need to be rendered.
				// See GitHub issue #1980 for more information.
				extraRenderInstances.emplace(component, currentInstanceNumber, currentTransform);
			}

			for(RocketComponent child : component.getChildren()) {
				getActiveContextListAt(child, results, currentTransform);
			}
		}

		return results;
	}

	/**
	 * Return all the stages in this configuration.
	 * @return all the stages in this configuration.
	 */
	public List<AxialStage> getAllStages() {
		List<AxialStage> stages = new ArrayList<>();
		for (StageFlags flags : this.stages.values()) {
			stages.add( rocket.getStage(flags.stageId));
		}
		return stages;
	}
	
	public List<AxialStage> getActiveStages() {
		List<AxialStage> activeStages = new ArrayList<>();
		
		for (StageFlags flags : this.stages.values()) {
			if (isStageActive(flags.stageNumber)) {
				AxialStage stage = rocket.getStage(flags.stageId);
				if (stage == null) {
					continue;
				}
				activeStages.add(stage);
			}
		}
		
		return activeStages;
	}

	public int getActiveStageCount() {
		return getActiveStages().size();
	}
	
	/**
	 * @return the component for the bottom-most center, active stage.
	 */
	public AxialStage getBottomStage() {
		AxialStage bottomStage = null;
		for (StageFlags curFlags : this.stages.values()) {
			if (isStageActive(curFlags.stageNumber)) {
				bottomStage = rocket.getStage( curFlags.stageNumber);
			}
		}
		return bottomStage;
	}
	
	public int getStageCount() {
		return stages.size();
	}
	
	/**
	 * Return the reference length associated with the current configuration.  The 
	 * reference length type is retrieved from the <code>Rocket</code>.
	 * 
	 * @return  the reference length for this configuration.
	 */
	public double getReferenceLength() {
		if (rocket.getModID() != refLengthModID) {
			refLengthModID = rocket.getModID();
			cachedRefLength = rocket.getReferenceType().getReferenceLength(this);
		}
		return cachedRefLength;
	}
	
	public double getReferenceArea() {
		return Math.PI * MathUtil.pow2(getReferenceLength() / 2);
	}
	
    public FlightConfigurationId getFlightConfigurationID() {
	   return fcid;
    }

	public FlightConfigurationId getId() {
		return fcid;
	}
	
	////////////////  Listeners  ////////////////
	
	// for outgoing events only
	protected void fireChangeEvent() {
		this.modID++;
		boundsModID = -1;
		refLengthModID = -1;
		
		updateStages();
		updateMotors();
		updateActiveInstances();
	}

	/**
	 * Update the configuration's modID, thus staging it in need to update.
	 */
	public void updateModID() {
		this.modID++;
	}
	
	private void updateStages() {
		Map<Integer, FlightConfiguration.StageFlags> stagesBackup = new HashMap<>(this.stages);
		this.stages.clear();
		for (AxialStage curStage : this.rocket.getStageList()) {
			if (curStage == null) continue;
			boolean active = true;
			for (FlightConfiguration.StageFlags flag : stagesBackup.values()) {
				if (flag.stageId.equals(curStage.getID())) {
					active = flag.active;
					break;
				}
			}
			StageFlags flagsToAdd = new StageFlags(curStage.getStageNumber(), curStage.getID(), active);
			this.stages.put(curStage.getStageNumber(), flagsToAdd);
		}
	}
	
	public boolean isNameOverridden(){
		return (!DEFAULT_CONFIG_NAME.equals(this.configurationName));
	}

	/**
	 * Return the name of this configuration, with DEFAULT_CONFIG_NAME replaced by a one line motor description.
	 * If configurationName is null, the one line motor description is returned.
	 * @return the flight configuration name
	 */
	public String getName() {
		if (configurationName == null) {
			return getOneLineMotorDescription();
		}
		return configurationName.replace(DEFAULT_CONFIG_NAME, getOneLineMotorDescription());
	}

	/**
	 * Return the raw configuration name, without replacing DEFAULT_CONFIG_NAME.
	 * If the configurationName is null, DEFAULT_CONFIG_NAME is returned.
	 * @return raw flight configuration name
	 */
	public String getNameRaw() {
		if (configurationName == null) {
			return DEFAULT_CONFIG_NAME;
		}
		return configurationName;
	}
	
	private String getOneLineMotorDescription(){
		StringBuilder buff = new StringBuilder("[");
		boolean first = true;
		int activeMotorCount = 0;
		for ( RocketComponent comp : getActiveComponents() ){
			if (( comp instanceof MotorMount )&&( ((MotorMount)comp).isMotorMount())){ 
				MotorMount mount = (MotorMount)comp;
				MotorConfiguration motorConfig = mount.getMotorConfig( fcid);
				
				if( first ){
					first = false;
				}else{
					buff.append(";");
				}
				
				if( ! motorConfig.isEmpty()){
					buff.append(motorConfig.toMotorCommonName());
					++activeMotorCount;
				}
			}
		}
		if( 0 == activeMotorCount ){
			return trans.get("noMotors");
		}
		buff.append("]");
		return buff.toString();
	}

	@Override
	public String toString() { return this.getName(); }

	/**
	 * Add a motor instance to this configuration.  
	 * 
	 * @param motorConfig			the motor instance.
	 * @throws IllegalArgumentException	if a motor with the specified ID already exists.
	 */
	public void addMotor(MotorConfiguration motorConfig) {
		if( motorConfig.isEmpty() ){
			log.error("attempt to add an empty motorConfig! ignoring. ", new IllegalArgumentException("empty MotorInstance: "+motorConfig.toDebugDetail()));
		}

		this.motors.put( motorConfig.getID(), motorConfig);
		
		modID++;
	}

	public boolean hasMotors() {
		return (0 < motors.size());
	}
	
	public Collection<MotorConfiguration> getAllMotors() {
		return this.motors.values();
	}
	
	public Collection<MotorConfiguration> getActiveMotors() {
		
		return activeMotors;
	}

	private void updateMotors() {
		motors.clear();
		
		for ( RocketComponent comp : getActiveComponents() ){
			if (( comp instanceof MotorMount )&&( ((MotorMount)comp).isMotorMount())){
				MotorMount mount = (MotorMount)comp;
				MotorConfiguration motorConfig = mount.getMotorConfig( fcid);
				if( motorConfig.isEmpty()){
					continue;
				}
				
				motors.put( motorConfig.getMID(), motorConfig);
			}
		}
		
		activeMotors.clear();
		for( MotorConfiguration config : motors.values() ){
			if( isComponentActive( config.getMount() )){
				activeMotors.add( config );
			}
		}
	}

	@Override
	public void update(){
		updateStages();
		updateMotors();
		updateActiveInstances();
	}

	/**
	 * Return true if rocket has a RecoveryDevice
	 */
	public boolean hasRecoveryDevice() {
	  if (fcid.hasError()) {
	    return false;
	  }

	  return this.getRocket().hasRecoveryDevice();
	}

	///////////////  Helper methods  ///////////////
	
	/**
	 * Return whether a component is in a currently active stages.
	 */
	public boolean isComponentActive(final RocketComponent c) {
		int stageNum = c.getStageNumber();
		return this.isStageActive( stageNum );
	}

	public boolean isComponentActive(final MotorMount c) {
		return isComponentActive( (RocketComponent) c);
	}
	
	/**
	 * Return the bounds of the current configuration.  The bounds are cached.
	 * 
	 * @return	a <code>Collection</code> containing coordinates bounding the rocket.
	 * 
	 * @deprecated Migrate to <FlightConfiguration>.getBoundingBoxAerodynamic(), when practical.
	 */
	@Deprecated 
	public Collection<Coordinate> getBounds() {
		return getBoundingBoxAerodynamic().toCollection();
	}
	
	/** 
	 * Return the bounding box of the current configuration (of aerodynamic components).
	 * 
	 * @return the rocket's bounding box (under the selected configuration)
	 */
	public BoundingBox getBoundingBoxAerodynamic() {
//		if (rocket.getModID() != boundsModID) {
		calculateBounds();
//		}
		
		if(cachedBoundsAerodynamic.isEmpty())
			cachedBoundsAerodynamic = new BoundingBox(Coordinate.ZERO,Coordinate.X_UNIT);
		
		return cachedBoundsAerodynamic;
	}

	/**
	 * Return the bounding box of the current configuration (of all components).
	 *
	 * @return the rocket's bounding box (under the selected configuration)
	 */
	public BoundingBox getBoundingBox() {
//		if (rocket.getModID() != boundsModID) {
		calculateBounds();
//		}

		if(cachedBounds.isEmpty())
			cachedBounds = new BoundingBox(Coordinate.ZERO,Coordinate.X_UNIT);

		return cachedBounds;
	}

	/**
	 * Calculates the bounds for all the active component instances
	 * in the current configuration.
	 */
	private void calculateBounds() {
		BoundingBox rocketBoundsAerodynamic = new BoundingBox();	// Bounding box of all aerodynamic components
		BoundingBox rocketBounds = new BoundingBox();				// Bounding box of all components

		InstanceMap map = getActiveInstances();
		for (Map.Entry<RocketComponent, java.util.ArrayList<InstanceContext>>  entry : map.entrySet()) {
			final RocketComponent component = entry.getKey();
			final BoundingBox componentBoundsAerodynamic = new BoundingBox();
			final BoundingBox componentBounds = new BoundingBox();
			final List<InstanceContext> contexts = entry.getValue();

			// FinSets already provide a bounding box, so let's use that.
			if (component instanceof BoxBounded) {
				final BoundingBox instanceBounds = ((BoxBounded) component).getInstanceBoundingBox();
				if (instanceBounds.isEmpty()) {
					// probably redundant
					// this component is probably non-physical (like an assembly) or has invalid bounds.  Skip.
					continue;
				}

				for (InstanceContext context : contexts) {
					if (component.isAerodynamic()) {
						componentBoundsAerodynamic.update(instanceBounds.transform(context.transform));
					}
					componentBounds.update(instanceBounds.transform(context.transform));
				}
			} else {
				// Legacy Case: These components do not implement the BoxBounded Interface.
				Collection<Coordinate> instanceCoordinates = component.getComponentBounds();
				List<RocketComponent> parsedContexts = new ArrayList<>();
				for (InstanceContext context : contexts) {
					// Don't parse the same context component twice (e.g. multiple copies in a pod set).
					if (parsedContexts.contains(context.component)) {
						continue;
					}
					Collection<Coordinate> transformedCoords = new ArrayList<>(instanceCoordinates);
					// mutating.  Transforms coordinates in place.
					context.transform.transform(instanceCoordinates);

					for (Coordinate tc : transformedCoords) {
						if (component.isAerodynamic()) {
							componentBoundsAerodynamic.update(tc);
						}
						componentBounds.update(tc);
					}
					parsedContexts.add(context.component);
				}
				parsedContexts.clear();
			}

			rocketBoundsAerodynamic.update(componentBoundsAerodynamic);
			rocketBounds.update(componentBounds);
		}
		
		boundsModID = rocket.getModID();
		cachedLengthAerodynamic = rocketBoundsAerodynamic.span().x;
		cachedLength = rocketBounds.span().x;
		/* Special case for the scenario that all of the stages are removed and are
		 * inactive. Its possible that this shouldn't be allowed, but it is currently
		 * so we'll just adjust the length here.  
		 */
		if (rocketBoundsAerodynamic.isEmpty()) {
			cachedLengthAerodynamic = 0;
		}
		if (rocketBounds.isEmpty()) {
			cachedLength = 0;
		}
		cachedBoundsAerodynamic = rocketBoundsAerodynamic;
		cachedBounds = rocketBounds;
	}
	
	/**
	 * Returns the length of the rocket configuration (only aerodynamic components), from the foremost bound X-coordinate
	 * to the aft-most X-coordinate.  The value is cached.
	 * 
	 * @return	the length of the rocket in the X-direction.
	 */
	public double getLengthAerodynamic() {
		if (rocket.getModID() != boundsModID) {
			calculateBounds();
		}
		return cachedLengthAerodynamic;
	}

	/**
	 * Returns the length of the rocket configuration (all components), from the foremost bound X-coordinate
	 * to the aft-most X-coordinate.  The value is cached.
	 *
	 * @return	the length of the rocket in the X-direction.
	 */
	public double getLength() {
		if (rocket.getModID() != boundsModID) {
			calculateBounds();
		}
		return cachedLength;
	}
	
	/**
	 * Perform a shallow-clone;  copies configuration fields only. 
	 *   
	 * Preserved:  
	 * - components
	 * - motors
	 * - configurables 
	 * 
	 */
	public FlightConfiguration clone(Rocket rocket) {
        // Note the stages are updated in the constructor call.
		FlightConfiguration clone = new FlightConfiguration( rocket, this.fcid );
		clone.setName(configurationName);
		clone.copyStageActiveness(this);
		clone.preloadStageActiveness = this.preloadStageActiveness == null ? null : new HashMap<>(this.preloadStageActiveness);
		
        clone.cachedBoundsAerodynamic = this.cachedBoundsAerodynamic.clone();
		clone.cachedBounds = this.cachedBounds.clone();
		clone.modID = this.modID;
		clone.boundsModID = -1;
		clone.refLengthModID = -1;
		return clone;
	}
		
	@Override
	public FlightConfiguration clone() {
		return this.clone(this.rocket);
	}

    /**
     * Copy all available information attached to this, and attached copies to the
     * new configuration
     *
     * @param newId attached the new configuration to this Id
     * @return the new configuration
     */
    @Override
    public FlightConfiguration copy( final FlightConfigurationId newId ) {
        // Note the stages are updated in the constructor call.
        FlightConfiguration copy = new FlightConfiguration( this.rocket, newId );
		final FlightConfigurationId copyId = copy.getId();
		
        // copy motor instances.
        for( final MotorConfiguration sourceMotor: motors.values() ){
            MotorConfiguration cloneMotor = sourceMotor.copy( copyId);
            copy.addMotor( cloneMotor);
            cloneMotor.getMount().setMotorConfig(cloneMotor, copyId);
        }

		copy.copyStages(this);
		copy.preloadStageActiveness = this.preloadStageActiveness == null ? null : new HashMap<>(this.preloadStageActiveness);
		copy.cachedBoundsAerodynamic = this.cachedBoundsAerodynamic.clone();
        copy.cachedBounds = this.cachedBounds.clone();
        copy.modID = this.modID;
        copy.boundsModID = -1;
        copy.refLengthModID = -1;
		copy.configurationName = configurationName;
        return copy;
    }

	@Override
	public int getModID() {
		return modID;
	}

	public void setName(final String newName) {
		if ((newName == null) || ("".equals(newName))) {
			this.configurationName = DEFAULT_CONFIG_NAME;
			return;
		}else if( ! this.getId().isValid()){
			return;
		}else if( newName.equals(this.configurationName)){
			return;
		}
		this.configurationName = newName;
	}
	
	@Override
	public boolean equals(Object other){
		return (( other instanceof FlightConfiguration ) &&
			    this.fcid.equals( ((FlightConfiguration)other).fcid));
	}
	
	@Override
	public int hashCode(){
		return this.fcid.hashCode();
	}
	
	public String toDebug() {
		return this.fcid.toDebug()+" (#"+configurationInstanceId+") "+ getOneLineMotorDescription();
	}
	
	// DEBUG / DEVEL
	public String toStageListDetail() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("\nDumping %d stages for config: %s: (%s)(#: %d)\n", 
				stages.size(), getName(), getId().toShortKey(), configurationInstanceId));
		final String fmt = "    [%-2s][%4s]: %6s \n";
		buf.append(String.format(fmt, "#", "?actv", "Name"));
		for (StageFlags flags : stages.values()) {
			final String stageId = flags.stageId;
			buf.append(String.format(fmt, stageId, (flags.active?" on": "off"), rocket.getStage(stageId).getName()));
		}
		buf.append("\n");
		return buf.toString();
	}
	
	// DEBUG / DEVEL
	public String toMotorDetail(){
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("\nDumping %2d Motors for configuration %s (%s)(#: %s)\n", 
				motors.size(), getName(), getId().toShortKey(), this.configurationInstanceId));
		
		for( MotorConfiguration curConfig : this.motors.values() ){
			boolean active=this.isStageActive( curConfig.getMount().getStage().getStageNumber());
			String activeString = (active?"active":"      ");
			buf.append("    "+"("+activeString+")"+curConfig.toDebugDetail()+"\n");
		}
		buf.append("\n");
		return buf.toString();
	}
	
 
}
