package net.sf.openrocket.rocketcomponent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.motor.MotorInstanceId;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.StateChangeListener;


/**
 * A class defining a rocket configuration, including which stages are active.
 * 
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightConfiguration implements FlightConfigurableParameter<FlightConfiguration>, ChangeSource, ComponentChangeListener, Monitorable {
	private static final Logger log = LoggerFactory.getLogger(FlightConfiguration.class);
	
	public final static String DEFAULT_CONFIGURATION_NAME = "Default Configuration";
	
	protected boolean isNamed = false;
	protected String configurationName;
	
	protected final Rocket rocket;
	protected final FlightConfigurationID fcid;
	private List<EventListener> listenerList = new ArrayList<EventListener>();
	
	protected class StageFlags {
		public boolean active = true;
		public int prev = -1;
		public AxialStage stage = null;
		
		public StageFlags(AxialStage _stage, int _prev, boolean _active) {
			this.stage = _stage;
			this.prev = _prev;
			this.active = _active;
		}
	}
	
	/* Cached data */
	final protected HashMap<Integer, StageFlags> stages = new HashMap<Integer, StageFlags>();
	protected final HashMap<MotorInstanceId, MotorInstance> motors = new HashMap<MotorInstanceId, MotorInstance>();
	
	private int boundsModID = -1;
	private ArrayList<Coordinate> cachedBounds = new ArrayList<Coordinate>();
	private double cachedLength = -1;
	
	private int refLengthModID = -1;
	private double cachedRefLength = -1;
	
	private int modID = 0;
	
	/**
	 * Create a new configuration with the specified <code>Rocket</code>.
	 * 
	 * @param _fcid  the ID this configuration should have.
	 * @param rocket  the rocket
	 */
	public FlightConfiguration(final Rocket rocket, final FlightConfigurationID _fcid ) {
		if( null == _fcid){
			this.fcid = new FlightConfigurationID();
		}else{
			this.fcid = _fcid;
		}
		this.rocket = rocket;
		this.isNamed = false;
		this.configurationName = "<WARN: attempt to access unset configurationName. WARN!> ";
		
		updateStages();
		updateMotors();
		rocket.addComponentChangeListener(this);
	}
	
	public Rocket getRocket() {
		return rocket;
	}
	
	
	public void clearAllStages() {
		this.setAllStages(false);
	}
	
	public void setAllStages() {
		this.setAllStages(true);
	}
	
	public void setAllStages(final boolean _value) {
		for (StageFlags cur : stages.values()) {
			cur.active = _value;
		}
		fireChangeEvent();
	}
	
	/** 
	 * This method flags a stage inactive.  Other stages are unaffected.
	 * 
	 * @param stageNumber  stage number to inactivate
	 */
	public void clearStage(final int stageNumber) {
		setStageActive( stageNumber, false);
	}
	
	/** 
	 * This method flags the specified stage as active, and all other stages as inactive.
	 * 
	 * @param stageNumber  stage number to activate
	 */
	public void setOnlyStage(final int stageNumber) {
		setAllStages(false);
		setStageActive(stageNumber, true);
		fireChangeEvent();
	}
	
	/** 
	 * This method flags the specified stage as requested.  Other stages are unaffected.
	 * 
	 * @param stageNumber   stage number to flag
	 * @param _active       inactive (<code>false</code>) or active (<code>true</code>)
	 */
	public void setStageActive(final int stageNumber, final boolean _active) {
		if ((0 <= stageNumber) && (stages.containsKey(stageNumber))) {
			stages.get(stageNumber).active = _active;
			fireChangeEvent();
			return;
		}
		log.error("error: attempt to retrieve via a bad stage number: " + stageNumber);
	}
	
	
	public void toggleStage(final int stageNumber) {
		if ((0 <= stageNumber) && (stages.containsKey(stageNumber))) {
			StageFlags flags = stages.get(stageNumber);
			flags.active = !flags.active;
			fireChangeEvent();
			return;
		}
		log.error("error: attempt to retrieve via a bad stage number: " + stageNumber);
	}

		
	/**
	 * Check whether the stage specified by the index is active.
	 */
	public boolean isStageActive(int stageNumber) {
		if (stageNumber >= this.rocket.getStageCount()) {
			return false;
		}
		
		if( ! stages.containsKey(stageNumber)){
			throw new IllegalArgumentException(" Configuration does not contain stage number: "+stageNumber);
		}
		
		return stages.get(stageNumber).active;
	}
	
	public Collection<RocketComponent> getActiveComponents() {
		Queue<RocketComponent> toProcess = new ArrayDeque<RocketComponent>(this.getActiveStages());
		ArrayList<RocketComponent> toReturn = new ArrayList<RocketComponent>();
		
		while (!toProcess.isEmpty()) {
			RocketComponent comp = toProcess.poll();
			
			toReturn.add(comp);
			for (RocketComponent child : comp.getChildren()) {
				if (child instanceof AxialStage) {
					continue;
				} else {
					toProcess.offer(child);
				}
			}
		}
		
		return toReturn;
	}
	
	public List<AxialStage> getActiveStages() {
		List<AxialStage> activeStages = new ArrayList<AxialStage>();
		
		for (StageFlags flags : this.stages.values()) {
			if (flags.active) {
				activeStages.add(flags.stage);
			}
		}
		
		return activeStages;
	}
	
	public int getActiveStageCount() {
		int activeCount = 0;
		for (StageFlags cur : this.stages.values()) {
			if (cur.active) {
				activeCount++;
			}
		}
		return activeCount;
	}
	
	/** 
	 * Retrieve the bottom-most active stage.
	 * @return 
	 */
	public AxialStage getBottomStage() {
		AxialStage bottomStage = null;
		for (StageFlags curFlags : this.stages.values()) {
			if (curFlags.active) {
				bottomStage = curFlags.stage;
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
	
	public FlightConfigurationID getFlightConfigurationID() {
		return fcid;
	}
	
	public FlightConfigurationID getId() {
		return getFlightConfigurationID();
	}
	
	/**
	 * Removes the listener connection to the rocket and listeners of this object.
	 * This configuration may not be used after a call to this method!
	 */
	public void release() {
		rocket.removeComponentChangeListener(this);
		listenerList = new ArrayList<EventListener>();
	}
	
	////////////////  Listeners  ////////////////
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listenerList.add(listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listenerList.remove(listener);
	}
	
	// for outgoing events only
	protected void fireChangeEvent() {
		EventObject e = new EventObject(this);
		
		this.modID++;
		boundsModID = -1;
		refLengthModID = -1;
		
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] listeners = listenerList.toArray(new EventListener[0]);
		for (EventListener l : listeners) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(e);
			}
		}
		
		updateStages();
		updateMotors();
	}
	
	private void updateStages() {
		if (this.rocket.getStageCount() == this.stages.size()) {
			// no changes needed
			return;
		}
		
		this.stages.clear();
		for (AxialStage curStage : this.rocket.getStageList()) {
			int prevStageNum = curStage.getStageNumber() - 1;
			if (curStage.getParent() instanceof AxialStage) {
				prevStageNum = curStage.getParent().getStageNumber();
			}
			StageFlags flagsToAdd = new StageFlags(curStage, prevStageNum, true);
			this.stages.put(curStage.getStageNumber(), flagsToAdd);
		}
	}
	
	public boolean isNameOverridden(){
		return isNamed;
	}
	
	public String getName() {
		if( isNamed ){
			return configurationName;
		}else{
			if( this.hasMotors()){
				return fcid.toShortKey()+" - "+this.getMotorsOneline();
			}else{
				return fcid.getFullKeyText();
			}
		}
	}
	
	public String toShort() {
		return this.fcid.toShortKey();
	}
	
	// DEBUG / DEVEL
	public String toDebug() {
		return toMotorDetail();
	}
	
	// DEBUG / DEVEL
	public String toStageListDetail() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("\nDumping stage config: \n"));
		for (StageFlags flags : this.stages.values()) {
			AxialStage curStage = flags.stage;
			buf.append(String.format("    [%2d]: %s: %24s\n", curStage.getStageNumber(), curStage.getName(), (flags.active?" on": "off")));
		}
		buf.append("\n\n");
		return buf.toString();
	}
	
	// DEBUG / DEVEL
	public String toMotorDetail(){
		StringBuilder buff = new StringBuilder();
		buff.append(String.format("\nDumping %2d Motors for configuration %s: \n", this.motors.size(), this));
		for( MotorInstance curMotor : this.motors.values() ){
			if( curMotor.isEmpty() ){
				buff.append( String.format( "    ..[%8s] <empty> \n", curMotor.getID().toShortKey()));
			}else{
				buff.append( String.format( "    ..[%8s] (%s) %10s (in: %s)\n",
										curMotor.getID().toShortKey(),
										(curMotor.isActive()? " on": "off"),
										curMotor.getMotor().getDesignation(),
										((RocketComponent)curMotor.getMount()).toDebugName() ));
			}
		}
		return buff.toString();
		
	}
	
	

	
	public String getMotorsOneline(){
		StringBuilder buff = new StringBuilder("[");
		boolean first = true;
		for ( RocketComponent comp : getActiveComponents() ){
			if (( comp instanceof MotorMount )&&( ((MotorMount)comp).isMotorMount())){ 
				MotorMount mount = (MotorMount)comp;
				MotorInstance inst = mount.getMotorInstance( fcid);
				
				if( first ){
					first = false;
				}else{
					buff.append(";");
				}
				
				if( ! inst.isEmpty()){
					buff.append( inst.getMotor().getDesignation());
				}
			}
		}
		buff.append("]");
		return buff.toString();
	}

	

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public void componentChanged(ComponentChangeEvent cce) {
		// update according to incoming events 
		updateStages();
		updateMotors();
	}
	
	/**
	 * Add a motor instance to this configuration.  The motor is placed at
	 * the specified position and with an infinite ignition time (never ignited).
	 * 
	 * @param id			the ID of this motor instance.
	 * @param motor			the motor instance.
	 * @param mount			the motor mount containing this motor
	 * @param ignitionEvent	the ignition event for the motor
	 * @param ignitionDelay	the ignition delay for the motor
	 * @param position		the position of the motor in absolute coordinates.
	 * @throws IllegalArgumentException	if a motor with the specified ID already exists.
	 */
	//	public void addMotor(MotorId _id, Motor _motor, double _ejectionDelay, MotorMount _mount,
	//			IgnitionEvent _ignitionEvent, double _ignitionDelay, Coordinate _position) {
	//		
	//		MotorInstance instanceToAdd = new MotorInstance(_id, _motor, _mount, _ejectionDelay,
	//				_ignitionEvent, _ignitionDelay, _position);
	//		
	//		
	//		//		this.ids.add(id);
	//		//		this.motors.add(motor);
	//		//		this.ejectionDelays.add(ejectionDelay);
	//		//		this.mounts.add(mount);
	//		//		this.ignitionEvents.add(ignitionEvent);
	//		//		this.ignitionDelays.add(ignitionDelay);
	//		//		this.positions.add(position);
	//		//		this.ignitionTimes.add(Double.POSITIVE_INFINITY);
	//	}
	
	
	/**
	 * Add a motor instance to this configuration.  
	 * 
	 * @param motor			the motor instance.
	 * @throws IllegalArgumentException	if a motor with the specified ID already exists.
	 */
	public void addMotor(MotorInstance motor) {
		if( motor.isEmpty() ){
			throw new IllegalArgumentException("MotorInstance is empty.");
		}
		MotorInstanceId id = motor.getID();
		if (this.motors.containsKey(id)) {
			throw new IllegalArgumentException("MotorInstanceConfiguration already " +
					"contains a motor with id " + id);
		}
		this.motors.put(id, motor);
		
		modID++;
	}
	
	public Collection<MotorInstance> getAllMotors() {
		return motors.values();
	}

	public int getMotorCount() {
		return motors.size();
	}
	
	public Set<MotorInstanceId> getMotorIDs() {
		return this.motors.keySet();
	}
	
	public MotorInstance getMotorInstance(MotorInstanceId id) {
		return motors.get(id);
	}
	
	public boolean hasMotors() {
		return (0 < motors.size());
	}
	
	public Collection<MotorInstance> getActiveMotors() {
		List<MotorInstance> activeList = new ArrayList<MotorInstance>();
		for( MotorInstance inst : this.motors.values() ){
			if( inst.isActive() ){
				activeList.add( inst );
			}
		}
		
		return activeList;
	}
	
	public void updateMotors() {
		this.motors.clear();
		
		for ( RocketComponent compMount : getActiveComponents() ){
			if (( compMount instanceof MotorMount )&&( ((MotorMount)compMount).isMotorMount())){
				MotorMount mount = (MotorMount)compMount;
				MotorInstance sourceInstance = mount.getMotorInstance( fcid);
				if( sourceInstance.isEmpty()){
					continue;
				}

				// this merely accounts for instancing of *this* component:
				// int instancCount = comp.getInstanceCount();

				// this includes *all* the instancing between here and the rocket root.
				Coordinate[] instanceLocations= compMount.getLocations();
				
				sourceInstance.reset();
				int instanceNumber = 1;
				//final int instanceCount = instanceLocations.length;
				for (  Coordinate curMountLocation : instanceLocations ){
					MotorInstance cloneInstance = sourceInstance.clone();
					cloneInstance.setID( new MotorInstanceId( compMount.getName(), instanceNumber) );
					
					// motor location w/in mount: parent.refpoint -> motor.refpoint 
					Coordinate curMotorOffset = cloneInstance.getOffset();
					cloneInstance.setPosition( curMountLocation.add(curMotorOffset) );
					this.motors.put( cloneInstance.getID(), cloneInstance);
					
					instanceNumber ++;
				}
				 
			}
		}
		//System.err.println("returning "+toReturn.size()+" active motor instances for this configuration: "+this.fcid.getShortKey());
		//System.err.println(this.rocket.getConfigurationSet().toDebug());
	}

	///////////////  Helper methods  ///////////////
	
	/**
	 * Return whether a component is in the currently active stages.
	 */
	public boolean isComponentActive(final RocketComponent c) {
		int stageNum = c.getStageNumber();
		return this.isStageActive( stageNum );
	}
	
	/**
	 * Return the bounds of the current configuration.  The bounds are cached.
	 * 
	 * @return	a <code>Collection</code> containing coordinates bounding the rocket.
	 */
	public Collection<Coordinate> getBounds() {
		if (rocket.getModID() != boundsModID) {
			boundsModID = rocket.getModID();
			cachedBounds.clear();
			
			double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
			for (RocketComponent component : this.getActiveComponents()) {
				for (Coordinate coord : component.getComponentBounds()) {
					cachedBounds.add(coord);
					if (coord.x < minX)
						minX = coord.x;
					if (coord.x > maxX)
						maxX = coord.x;
				}
			}
			
			if (Double.isInfinite(minX) || Double.isInfinite(maxX)) {
				cachedLength = 0;
			} else {
				cachedLength = maxX - minX;
			}
		}
		return cachedBounds.clone();
	}
	
	
	/**
	 * Returns the length of the rocket configuration, from the foremost bound X-coordinate
	 * to the aft-most X-coordinate.  The value is cached.
	 * 
	 * @return	the length of the rocket in the X-direction.
	 */
	public double getLength() {
		if (rocket.getModID() != boundsModID)
			getBounds(); // Calculates the length
			
		return cachedLength;
	}
	
	/**
	 * Perform a deep-clone.  The object references are also cloned and no
	 * listeners are listening on the cloned object.  The rocket instance remains the same.
	 */
	@Override
	public FlightConfiguration clone() {
		FlightConfiguration clone = new FlightConfiguration( this.getRocket(), this.fcid );
		clone.setName(this.fcid.toShortKey()+" - clone");
		clone.listenerList = new ArrayList<EventListener>();
		clone.stages.putAll( (Map<Integer, StageFlags>) this.stages);
		for( MotorInstance mi : this.motors.values()){
			clone.motors.put( mi.getID(), mi.clone());
        }
		clone.cachedBounds = this.cachedBounds.clone();
		clone.modID = this.modID;
		clone.boundsModID = -1;
		clone.refLengthModID = -1;
		rocket.addComponentChangeListener(clone);
		return clone;
	}
	
	@Override
	public int getModID() {
		// TODO: this doesn't seem consistent...
		int id = modID;
//		for (MotorInstance motor : motors.values()) {
//			id += motor.getModID();
//		}
		id += rocket.getModID();
		return id; 
	}

	public void setName( final String newName) {
		if( null == newName ){
			this.isNamed = false;
		}else if( "".equals(newName)){
			return;
		}else if( ! this.getFlightConfigurationID().isValid()){
			return;
		}else if( newName.equals(this.configurationName)){
			return;
		}
		this.isNamed = true;
		this.configurationName = newName;
	}

}
