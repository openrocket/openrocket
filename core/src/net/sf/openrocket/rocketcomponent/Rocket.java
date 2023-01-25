package net.sf.openrocket.rocketcomponent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BoundingBox;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.UniqueID;


/**
 * Base for all rocket components.  This is the "starting point" for all rocket trees.
 * It provides the actual implementations of several methods defined in RocketComponent
 * (eg. the rocket listener lists) and the methods defined in RocketComponent call these.
 * It also defines some other methods that concern the whole rocket, and helper methods
 * that keep information about the program state.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
@SuppressWarnings("serial")
public class Rocket extends ComponentAssembly {
	private static final Logger log = LoggerFactory.getLogger(Rocket.class);
	private static final Translator trans = Application.getTranslator();
	
	protected static final double DEFAULT_REFERENCE_LENGTH = 0.01;

	/**
	 * List of component change listeners.
	 */
	private Set<EventListener> listenerList = new HashSet<>();
	
	/**
	 * When freezeList != null, events are not dispatched but stored in the list.
	 * When the structure is thawed, a single combined event will be fired.
	 */
	private List<ComponentChangeEvent> freezeList = null;
	
	
	private int modID;
	private int massModID;
	private int aeroModID;
	private int treeModID;
	private int functionalModID;
	
	private boolean eventsEnabled=false;
	
	private ReferenceType refType = ReferenceType.MAXIMUM; // Set in constructor
	private double customReferenceLength = DEFAULT_REFERENCE_LENGTH;
	
	
	private String designer = "";
	private String revision = "";
	
	
	// Flight configuration list
	private FlightConfiguration selectedConfiguration;
	private FlightConfigurableParameterSet<FlightConfiguration> configSet;
	private ConcurrentHashMap<Integer, AxialStage> stageMap = new ConcurrentHashMap<>();
	
	// Does the rocket have a perfect finish (a notable amount of laminar flow)
	private boolean perfectFinish = false;
	
	
	/////////////  Constructor  /////////////
	
	public Rocket() {
	    super(AxialMethod.ABSOLUTE);
		modID = UniqueID.next();
		massModID = modID;
		aeroModID = modID;
		treeModID = modID;
		functionalModID = modID;

		// must be after the hashmaps :P 
		final FlightConfiguration defaultConfig = new FlightConfiguration(this, FlightConfigurationId.DEFAULT_VALUE_FCID);
		configSet = new FlightConfigurableParameterSet<>( defaultConfig );
		this.selectedConfiguration = defaultConfig;
	}

	/**
	 * Return a bounding box enveloping the rocket.  By definition, the bounding box is a convex hull.
	 *
	 * Note: this function gets the bounding box for the entire rocket.
	 *
	 * @return    Return a bounding box enveloping the rocket
	 */
	public BoundingBox getBoundingBox (){ return selectedConfiguration.getBoundingBoxAerodynamic(); }

	public String getDesigner() {
		checkState();
		return designer;
	}
	
	public void setDesigner(String s) {
		if (s == null)
			s = "";
		designer = s;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	public String getRevision() {
		checkState();
		return revision;
	}
	
	public void setRevision(String s) {
		if (s == null)
			s = "";
		revision = s;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	/**
	 * Return the number of stages in this rocket.
	 *
	 * @return   the number of stages in this rocket.
	 */
	public int getStageCount() {
		checkState();
		return this.stageMap.size();
	}
	
	/**
	 * Return the non-negative modification ID of this rocket.  The ID is changed
	 * every time any change occurs in the rocket.  This can be used to check
	 * whether it is necessary to void cached data in cases where listeners can not
	 * or should not be used.
	 * <p>
	 * Three other modification IDs are also available, {@link #getMassModID()},
	 * {@link #getAerodynamicModID()} {@link #getTreeModID()}, which change every time
	 * a mass change, aerodynamic change, or tree change occur.  Even though the values
	 * of the different modification ID's may be equal, they should be treated totally
	 * separate.
	 * <p>
	 * Note that undo events restore the modification IDs that were in use at the
	 * corresponding undo level.  Subsequent modifications, however, produce modIDs
	 * distinct from those already used.
	 *
	 * @return   a unique ID number for this modification state.
	 */
	public int getModID() {
		return modID;
	}
	
	/**
	 * Return the non-negative mass modification ID of this rocket.  See
	 * {@link #getModID()} for details.
	 *
	 * @return   a unique ID number for this mass-modification state.
	 */
	public int getMassModID() {
		return massModID;
	}
	
	/**
	 * Return the non-negative aerodynamic modification ID of this rocket.  See
	 * {@link #getModID()} for details.
	 *
	 * @return   a unique ID number for this aerodynamic-modification state.
	 */
	public int getAerodynamicModID() {
		return aeroModID;
	}
	
	/**
	 * Return the non-negative tree modification ID of this rocket.  See
	 * {@link #getModID()} for details.
	 *
	 * @return   a unique ID number for this tree-modification state.
	 */
	public int getTreeModID() {
		return treeModID;
	}
	
	/**
	 * Return the non-negative functional modificationID of this rocket.
	 * This changes every time a functional change occurs.
	 *
	 * @return	a unique ID number for this functional modification state.
	 */
	public int getFunctionalModID() {
		return functionalModID;
	}
	
	public Collection<AxialStage> getStageList() {
		return this.stageMap.values();
	}

	public AxialStage getStage( final int stageNumber ) {
		return this.stageMap.get( stageNumber);
	}

	public AxialStage getStage(final String stageId) {
		for (AxialStage stage : getStageList()) {
			if (stage.getID().equals(stageId)) {
				return stage;
			}
		}
		return null;
	}

	/**
	 * Get the topmost stage (including boosters), only taking into account active stages from the flight configuration.
	 * @param config flight configuration dictating which stages are active
	 * @return the topmost active stage, or null if there are no active stages.
	 */
	public AxialStage getTopmostStage(FlightConfiguration config) {
		if (config == null) return null;

		for (AxialStage stage : getStageList()) {
			if (config.isStageActive(stage.getStageNumber())) {
				return stage;
			}
		}
		return null;
	}

	/**
	 * Get the bottommost stage, only taking into account active stages from the flight configuration.
	 * @param config flight configuration dictating which stages are active
	 * @return the bottommost active stage, or null if there are no active stages.
	 */
	public AxialStage getBottomCoreStage(FlightConfiguration config) {
		if (config == null) return null;

		for (int i = getChildCount() - 1; i >= 0; i--) {
			if (getChild(i) instanceof AxialStage && config.isStageActive(getChild(i).getStageNumber())) {
				return (AxialStage) getChild(i);
			}
		}
		return null;
	}
	
	@Override
	public int getStageNumber() {
		// invalid, error value
		return -1;
	}
	private int getNewStageNumber() {
		int guess = 0;
		while (stageMap.containsKey(guess)) {
			guess++;
		}
		return guess;
	}

    /*package-local*/ void trackStage(final AxialStage newStage) {
		int stageNumber = newStage.getStageNumber();
		AxialStage value = stageMap.get(stageNumber);
		
		if (newStage.equals(value)) {
			// stage is already added
			if( newStage != value ){
				// but the value is the wrong instance
				stageMap.put(stageNumber, newStage);
			}
			return;
		} else {
			stageNumber = getNewStageNumber();
			newStage.setStageNumber(stageNumber);
			this.stageMap.put(stageNumber, newStage);
		}
	}

    /*package-local*/ void forgetStage(final AxialStage oldStage) {
		this.stageMap.remove(oldStage.getStageNumber());
	}

	@Override
	public void setAxialMethod(final AxialMethod newAxialMethod) {
		this.axialMethod = AxialMethod.ABSOLUTE;
	}

	@Override
    public void setAxialOffset( final double requestOffset ) {
    	this.axialOffset = 0.;
		this.position = Coordinate.ZERO;
    }

	public ReferenceType getReferenceType() {
		checkState();
		return refType;
	}
	
	public void setReferenceType(ReferenceType type) {
		if (refType == type)
			return;
		refType = type;
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}

	@Override
	public double getLength() {
		return selectedConfiguration.getLength();
	}
	
	
	public double getCustomReferenceLength() {
		checkState();
		return customReferenceLength;
	}
	
	public void setCustomReferenceLength(double length) {
		if (MathUtil.equals(customReferenceLength, length))
			return;
		
		this.customReferenceLength = Math.max(length, 0.001);
		
		if (refType == ReferenceType.CUSTOM) {
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
		}
	}

	@Override
	public double getBoundingRadius() {
		double bounding = 0;
		for (RocketComponent comp : children) {
			if (comp instanceof ComponentAssembly) {
				bounding = Math.max(bounding, ((ComponentAssembly) comp).getBoundingRadius());
			}
		}
		return bounding;
	}
	
	
	
	
	
	/**
	 * Set whether the rocket has a perfect finish.  This will affect whether the
	 * boundary layer is assumed to be fully turbulent or not.
	 *
	 * @param perfectFinish		whether the finish is perfect.
	 */
	public void setPerfectFinish(boolean perfectFinish) {
		if (this.perfectFinish == perfectFinish)
			return;
		this.perfectFinish = perfectFinish;
		fireComponentChangeEvent(ComponentChangeEvent.AERODYNAMIC_CHANGE);
	}
	
	
	
	/**
	 * Get whether the rocket has a perfect finish.
	 *
	 * @return the perfectFinish
	 */
	public boolean isPerfectFinish() {
		return perfectFinish;
	}
	
	
	
	/**
	 * Make a shallow copy of the Rocket structure.  This method is exposed as public to allow
	 * for undo/redo system functionality.
	 * 
	 * note:  the <hashmap>.clone() function returns a shallow copy-- which is probably appropriate. 
	 */
	@Override
	public Rocket copyWithOriginalID() {
		final Rocket copyRocket = (Rocket) super.copyWithOriginalID();

		// Rocket copy is cloned, so non-trivial members must be cloned as well:
		copyRocket.stageMap = new ConcurrentHashMap<>();
		for( Map.Entry<Integer,AxialStage> entry : this.stageMap.entrySet()){
			final AxialStage stage = (AxialStage)copyRocket.findComponent(entry.getValue().getID());
			copyRocket.stageMap.put(entry.getKey(), stage);
		}

		// these flight configurations need to reference the _new_ Rocket copy
		// the default value needs to be explicitly set, because it has different semantics
		copyRocket.configSet = new FlightConfigurableParameterSet<>(new FlightConfiguration(copyRocket));
		for (FlightConfigurationId key : this.configSet.getIds()) {
			copyRocket.configSet.set(key, new FlightConfiguration(copyRocket, key));
		}

		copyRocket.selectedConfiguration = copyRocket.configSet.get( this.getSelectedConfiguration().getId());
		copyRocket.listenerList = new HashSet<>();
		
		return copyRocket;
	}
	
	public int getFlightConfigurationCount() {
		checkState();
		return this.configSet.size();
	}
	
	/**
	 * Load the rocket structure from the source.  The method loads the fields of this
	 * Rocket object and copies the references to siblings from the <code>source</code>.
	 * The object <code>source</code> should not be used after this call, as it is in
	 * an illegal state!
	 * <p>
	 * This method is meant to be used in conjunction with undo/redo functionality,
	 * and therefore fires an UNDO_EVENT, masked with all applicable mass/aerodynamic/tree
	 * changes.
	 */
	public void loadFrom(Rocket source) {
		
		// Store list of components to invalidate after event has been fired
		List<RocketComponent> toInvalidate = this.copyFrom(source);
		
		int type = ComponentChangeEvent.UNDO_CHANGE | ComponentChangeEvent.NONFUNCTIONAL_CHANGE;
		if (this.massModID != source.massModID)
			type |= ComponentChangeEvent.MASS_CHANGE;
		if (this.aeroModID != source.aeroModID)
			type |= ComponentChangeEvent.AERODYNAMIC_CHANGE;
		// Loading a rocket is always a tree change since the component objects change
		type |= ComponentChangeEvent.TREE_CHANGE;
		
		this.modID = source.modID;
		this.massModID = source.massModID;
		this.aeroModID = source.aeroModID;
		this.treeModID = source.treeModID;
		this.functionalModID = source.functionalModID;
		this.refType = source.refType;
		this.customReferenceLength = source.customReferenceLength;
		this.stageMap = source.stageMap;

		// these flight configurations need to reference the _this_ Rocket:
		this.configSet.reset();
		this.configSet.setDefault(new FlightConfiguration(this));
		for (FlightConfigurationId key : source.configSet.map.keySet()) {
			this.configSet.set(key, new FlightConfiguration(this, key));
		}
		this.selectedConfiguration = this.configSet.get(source.getSelectedConfiguration().getId());

		this.perfectFinish = source.perfectFinish;
		
		this.checkComponentStructure();
		
		fireComponentChangeEvent(type);
		
		// Invalidate obsolete components after event
		for (RocketComponent c : toInvalidate) {
			c.invalidate();
		}
	}
	
	
	
	
	///////  Implement the ComponentChangeListener lists
	
	/**
	 * Creates a new EventListenerList for this component.  This is necessary when cloning
	 * the structure.
	 */
	public void resetListeners() {
		//		System.out.println("RESETTING LISTENER LIST of Rocket "+this);
		listenerList = new HashSet<EventListener>();
	}
	
	
	public void printListeners() {
		int i = 0;
		for (EventListener l : listenerList) {
			System.out.println("  " + (i) + ": " + l);
			i++;
		}
	}
	
	@Override
	public void addComponentChangeListener(ComponentChangeListener l) {
		checkState();

		listenerList.add(l);

		log.trace("Added ComponentChangeListener " + l + ", current number of listeners is " + listenerList.size());
	}
	
	@Override
	public void removeComponentChangeListener(ComponentChangeListener l) {
		listenerList.remove(l);
		log.trace("Removed ComponentChangeListener " + l + ", current number of listeners is " + listenerList.size());
	}

	/**
	 * Fires a ComponentChangeEvent of the given type.  The source of the event is set to
	 * this rocket.
	 *
	 * @param type  Type of event
	 * @param ids IDs of the flight configurations to update, or null to update all.
	 * @see #fireComponentChangeEvent(ComponentChangeEvent)
	 */
	public void fireComponentChangeEvent(int type, final FlightConfigurationId[] ids) {
		fireComponentChangeEvent(new ComponentChangeEvent(this, type), ids);
	}

	/**
	 * Fires a ComponentChangeEvent of the given type.  The source of the event is set to
	 * this rocket.
	 *
	 * @param type  Type of event
	 * @param id ID of the flight configurations to update, or null to update all.
	 * @see #fireComponentChangeEvent(ComponentChangeEvent)
	 */
	public void fireComponentChangeEvent(int type, FlightConfigurationId id) {
		fireComponentChangeEvent(type, new FlightConfigurationId[]{ id });
	}

	protected void fireComponentChangeEvent(ComponentChangeEvent cce, final FlightConfigurationId[] ids) {
		if (!this.eventsEnabled) {
			return;
		}

		mutex.lock("fireComponentChangeEvent");
		try {
			checkState();

			// Update modification ID's only for normal (not undo/redo) events
			if (!cce.isUndoChange()) {
				modID = UniqueID.next();
				if (cce.isMassChange())
					massModID = modID;
				if (cce.isAerodynamicChange())
					aeroModID = modID;
				if (cce.isTreeChange())
					treeModID = modID;
				if (cce.isFunctionalChange()) {
					functionalModID = modID;
					updateConfigurationsModID(ids);
				}
			}

			// Check whether frozen
			if (freezeList != null) {
				log.debug("Rocket is in frozen state, adding event " + cce + " info freeze list");
				freezeList.add(cce);
				return;
			}

			// Notify all components first
			Iterator<RocketComponent> iterator = this.iterator(true);
			while (iterator.hasNext()) {
				RocketComponent next = iterator.next();
				next.componentChanged(cce);
			}
			updateConfigurations(ids);

			notifyAllListeners(cce);

		} finally {
			mutex.unlock("fireComponentChangeEvent");
		}
	}
	
	@Override
	protected void fireComponentChangeEvent(ComponentChangeEvent cce) {
		fireComponentChangeEvent(cce, null);
	}
	
	@Override
	public void update(){
		updateStageNumbers();
		updateStageMap();
		updateConfigurations();
	}

	/**
	 * Update all the stage numbers based on their position in the component tree
	 */
	private void updateStageNumbers() {
		int stageNr = 0;
		for (AxialStage stage : getSubStages()) {
			forgetStage(stage);
			stage.setStageNumber(stageNr);
			stageNr++;
		}
	}

	private void updateStageMap(){
		for (AxialStage stage : getSubStages() ){
			trackStage(stage);
		}
	}

	/**
	 * Update the modIDs of the supplied flight configurations.
	 * @param ids IDs of the flight configurations to update, or null to update all.
	 */
	private void updateConfigurationsModID(FlightConfigurationId[] ids) {
		if (ids == null) {
			for (FlightConfiguration config : configSet) {
				config.updateModID();
			}
			return;
		}
		for (FlightConfiguration config : configSet) {
			for (FlightConfigurationId id : ids) {
				if (config.getId().equals(id)) {
					config.updateModID();
					break;
				}
			}
		}
	}

	/**
	 * Update the flight configurations.
	 * @param ids IDs of the flight configurations to update, or null to update all.
	 */
	private void updateConfigurations(FlightConfigurationId[] ids) {
		if (ids == null) {
			for (FlightConfiguration config : configSet) {
				config.update();
			}
			return;
		}
		for (FlightConfiguration config : configSet) {
			for (FlightConfigurationId id : ids) {
				if (config.getId().equals(id)) {
					config.update();
					break;
				}
			}
		}
	}
	
	private void updateConfigurations() {
		updateConfigurations(null);
	}
	
	
	private void notifyAllListeners(final ComponentChangeEvent cce){
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listenerList.toArray(new EventListener[0]);
		for (EventListener l : list) {
            { // vvvv DEVEL vvvv
                //System.err.println("notifying listener.  (type= "+l.getClass().getSimpleName()+")");
                //System.err.println("                     (type= "+l.getClass().getName()+")");
            } // ^^^^ DEVEL ^^^^

            if (l instanceof ComponentChangeListener) {
				((ComponentChangeListener) l).componentChanged(cce);
			} else if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(cce);
			}
		}
	}
	
	/**
	 * Freezes the rocket structure from firing any events.  This may be performed to
	 * combine several actions on the structure into a single large action.
	 * <code>thaw()</code> must always be called afterwards.
	 *
	 * NOTE:  Always use a try/finally to ensure <code>thaw()</code> is called:
	 * <pre>
	 *     Rocket r = c.getRocket();
	 *     try {
	 *         r.freeze();
	 *         // do stuff
	 *     } finally {
	 *         r.thaw();
	 *     }
	 * </pre>
	 *
	 * @see #thaw()
	 */
	public void freeze() {
		checkState();
		if (freezeList == null) {
			freezeList = new LinkedList<>();
			log.debug("Freezing Rocket");
		} else {
			Application.getExceptionHandler().handleErrorCondition("Attempting to freeze Rocket when it is already frozen, " +
					"freezeList=" + freezeList);
		}
	}
	
	/**
	 * Thaws a frozen rocket structure and fires a combination of the events fired during
	 * the freeze.  The event type is a combination of those fired and the source is the
	 * last component to have been an event source.
	 *
	 * @see #freeze()
	 */
	public void thaw() {
		checkState();
		if (freezeList == null) {
			Application.getExceptionHandler().handleErrorCondition("Attempting to thaw Rocket when it is not frozen");
			return;
		}
		if (freezeList.size() == 0) {
			log.warn("Thawing rocket with no changes made");
			freezeList = null;
			return;
		}
		
		log.debug("Thawing rocket, freezeList=" + freezeList);
		
		int type = 0;
		Object c = null;
		for (ComponentChangeEvent e : freezeList) {
			type = type | e.getType();
			c = e.getSource();
		}
		freezeList = null;
		
		fireComponentChangeEvent(new ComponentChangeEvent((RocketComponent) c, type));
	}
	
	
	
	
	////////  Motor configurations  ////////
	
	
	/**
	 * Return the currently selected configuration.  This should be used in the user interface
	 * to ensure a consistent rocket configuration between dialogs.  It should NOT
	 * be used in simulations not relating to the UI.
	 *
	 * @return   the current {@link FlightConfiguration}.
	 */
	public FlightConfiguration getSelectedConfiguration() {
		checkState();
        return selectedConfiguration;
	}
	
	public int getConfigurationCount(){
		return this.configSet.size();
	}
	
	public List<FlightConfigurationId> getIds(){
		return configSet.getIds();
	}

	/**
	 * Remove a flight configuration ID from the configuration IDs.  The
     * <code>FlightConfigurationId.DEFAULT_VALUE_FCID</code> ID cannot be removed,
     * and an attempt to remove it will be silently ignored.
	 *
	 * @param fcid   the flight configuration ID to remove
	 */
	public void removeFlightConfiguration(final FlightConfigurationId fcid) {
		checkState();
		if( fcid.hasError() ){
			return;
		}
				
		if( selectedConfiguration.getId().equals( fcid)){
			selectedConfiguration = configSet.getDefault();
		}
		
		// removed any component configuration tied to this FCID
		Iterator<RocketComponent> iterator = this.iterator();
		while (iterator.hasNext()) {
			RocketComponent comp = iterator.next();
			
			if (comp instanceof FlightConfigurableComponent){
				FlightConfigurableComponent confbl = (FlightConfigurableComponent)comp;
				confbl.reset( fcid);
			}
		}
				
		// Get current configuration:
		this.configSet.reset(fcid);
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	/**
	 * Check whether <code>id</code> is a valid motor configuration ID.
	 *
	 * @param id	the configuration ID.
	 * @return		whether a motor configuration with that ID exists.
	 */
	public boolean containsFlightConfigurationID(final FlightConfigurationId id) {
		checkState();
		if( id.hasError() ){
			return false;
		}
		return configSet.containsId( id);
	}
	
	
	/**
	 * Check whether the given motor configuration ID has motors defined for it.
	 *
	 * @param fcid	the FlightConfigurationID containing the motor (may be invalid).
	 * @return		whether any motors are defined for it.
	 */
	public boolean hasMotors(FlightConfigurationId fcid) {
		checkState();
		if( fcid.hasError() ){
			return false;
		}
		
		Iterator<RocketComponent> iterator = this.iterator();
		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();
			
			if (c instanceof MotorMount) {
				MotorMount mount = (MotorMount) c;
				if (!mount.isMotorMount())
					continue;
				if (mount.getMotorConfig(fcid).getMotor() != null) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return a flight configuration.  If the supplied id does not have a specific instance, the default is returned.  
	 *
	 * @param fcid the flight configuration id
	 * @return	FlightConfiguration instance
	 */
	public FlightConfiguration createFlightConfiguration( final FlightConfigurationId fcid) {
		checkState();

        if( null == fcid ){
            // fall-through to the default case:
            // ...creating a FlightConfiguration( null ) just allocates a fresh new FCID
		}else if( fcid.hasError() ){
			return configSet.getDefault();
		}else if( configSet.containsId(fcid)){
			return configSet.get(fcid);
		}
        FlightConfiguration nextConfig = new FlightConfiguration(this, fcid);
        this.configSet.set(nextConfig.getId(), nextConfig);
        fireComponentChangeEvent(ComponentChangeEvent.TREE_CHANGE);
        return nextConfig;
	}

	/**
	 * Return all the flight configurations of this rocket.
	 * @return all the flight configurations of this rocket.
	 */
	public FlightConfigurableParameterSet<FlightConfiguration> getFlightConfigurations() {
		return this.configSet;
	}
	
	
	/**
	 * Return a flight configuration.  If the supplied id does not have a specific instance, the default is returned.  
	 *
	 * @param fcid   the flight configuration id
	 * @return	   a FlightConfiguration instance 
	 */
	public FlightConfiguration getFlightConfiguration(final FlightConfigurationId fcid) {
		checkState();
		return this.configSet.get(fcid);
	}

	public FlightConfiguration getFlightConfigurationByIndex(final int configIndex) {
		return getFlightConfigurationByIndex( configIndex, false);
	}
		
	/**
	 * Return a flight configuration.  If the supplied index is out of bounds, an exception is thrown.
	 * If the default instance is allowed, the default will be at index 0. 
	 *
	 * @param 	allowDefault 	Whether to allow returning the default instance
	 * @param 	configIndex 	The flight configuration index number
	 * @return	FlightConfiguration instance
	 */
	public FlightConfiguration getFlightConfigurationByIndex( int configIndex, final boolean allowDefault ) {
		if( allowDefault ){
			if( 0 == configIndex ){
				return configSet.getDefault();
			}
			--configIndex;
		}
		return this.configSet.get( this.getId(configIndex));
	}

	public FlightConfigurationId getId( final int configIndex) {
		List<FlightConfigurationId> idList = configSet.getIds();
		return idList.get(configIndex);
	}

	public void setSelectedConfiguration(final FlightConfigurationId selectId) {
		checkState();
		
		if( selectId.equals( selectedConfiguration.getFlightConfigurationID())){
			// if desired configuration is already selected, skip the event
			return;
		}
		
		this.selectedConfiguration = this.configSet.get( selectId );
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}

	/**
	 * Associate the given ID and flight configuration.
	 * <code>null</code> or an empty string.
	 *
	 * @param fcid	the flight configuration id
	 * @param newConfig new FlightConfiguration to store
	 */
	public void setFlightConfiguration(final FlightConfigurationId fcid, FlightConfiguration newConfig) {
		checkState();
		if( fcid.hasError() ){
			log.error("attempt to set a 'fcid = config' with a error fcid.  Ignored.", new IllegalArgumentException("error id:"+fcid));
			return;
		}

		if (null == newConfig){
			configSet.reset( fcid);
		}else if( fcid.equals( configSet.get(fcid).getFlightConfigurationID())){
			// this mapping already exists; skip the event
			return;
		}else{
			configSet.set(fcid, newConfig);
		}
		fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	////////  Obligatory component information
	@Override
	public String getComponentName() {
		//// Rocket
		return trans.get("Rocket.compname.Rocket");
	}
	

	
	/**
	 * Allows only <code>AxialStage</code> components to be added to the type Rocket.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return (AxialStage.class.equals(type));
	}

	/** 
	 * STUB.  would enable the monitoring, relay and production of events in this rocket instance.
	 */
	public void enableEvents() {
		this.enableEvents(true);
		this.update();
	}
	
	/** 
	 * STUB.  would enable the monitoring, relay and production of events in this rocket instance.
	 */
	public void enableEvents( final boolean _enable ) {
		if( this.eventsEnabled && _enable){
			return;
		}else if( _enable ){
			this.eventsEnabled = true;
			this.fireComponentChangeEvent(ComponentChangeEvent.AEROMASS_CHANGE);
		}else{
			this.eventsEnabled = false;
		}
	}
	
	public String toDebugConfigs(){
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("====== Dumping %d Configurations from rocket: %s ======\n", 
				this.getConfigurationCount(), this.getName()));
		final String fmt = "    [%12s]: %s\n";
		for( FlightConfiguration config : this.configSet ){
			String shortKey = config.getId().toShortKey();
			if( this.selectedConfiguration.equals( config)){
				shortKey = "=>" + shortKey;
			}
			buf.append(String.format(fmt, shortKey, config.getNameRaw() ));
		}
		return buf.toString();
	}

	public FlightConfiguration getEmptyConfiguration() {
		return this.configSet.getDefault();
	}
	
}
