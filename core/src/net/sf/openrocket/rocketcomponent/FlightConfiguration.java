package net.sf.openrocket.rocketcomponent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.motor.MotorInstanceConfiguration;
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
	final protected MotorInstanceConfiguration motors;
	
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
	public FlightConfiguration(final FlightConfigurationID _fcid, Rocket rocket ) {
		if( null == _fcid){
			this.fcid = new FlightConfigurationID();
		}else{
			this.fcid = _fcid;
		}
		this.rocket = rocket;
		this.isNamed = false;
		this.configurationName = "<WARN: attempt to access unset configurationName. WARN!> ";
		this.motors = new MotorInstanceConfiguration(this);		
		
		updateStageMap();
		this.motors.update();
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
	public void clearOnlyStage(final int stageNumber) {
		setStageActive(stageNumber, false);
	}
	
	/** 
	 * This method flags a stage active.  Other stages are unaffected.
	 * 
	 * @param stageNumber  stage number to activate
	 */
	public void setOnlyStage(final int stageNumber) {
		setStageActive(stageNumber, true);
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
		// DEVEL
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

	public List<MotorInstance> getActiveMotors() {
		return this.motors.getActiveMotors();
	}
	
	public Collection<MotorInstance> getAllMotors() {
		return this.motors.getAllMotors();
	}
	
	public boolean hasMotors() {
		return this.motors.hasMotors();
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
	
	public MotorInstance getMotor( final MotorInstanceId mid ){
		return this.motors.getMotorInstance( mid );
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
		
		updateStageMap();
		motors.update();
	}
	
	private void updateStageMap() {
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
				return fcid.toShortKey()+" - "+this.motors.toString();
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
		buff.append(String.format("\nDumping %2d Motors for configuration %s: \n", this.motors.getMotorCount(), this.fcid.toShortKey()));
		for( MotorInstance curMotor : this.getAllMotors()){
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

	@Override
	public String toString() {
		if( this.isNamed){
			return configurationName + "["+fcid.toShortKey()+"]";
		}else{
			return this.getName();
		}
	}

	@Override
	public void componentChanged(ComponentChangeEvent cce) {
		// update according to incoming events 
		updateStageMap();
		this.motors.update();
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
		FlightConfiguration config = new FlightConfiguration( this.fcid, this.getRocket() );
		config.listenerList = new ArrayList<EventListener>();
		config.stages.putAll( (Map<Integer, StageFlags>) this.stages);
		config.motors.populate( this.motors );
		config.cachedBounds = this.cachedBounds.clone();
		config.boundsModID = -1;
		config.refLengthModID = -1;
		rocket.addComponentChangeListener(config);
		return config;
	}
	
	
	@Override
	public int getModID() {
		return modID + rocket.getModID();
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
	
	public void step(double time, double acceleration, AtmosphericConditions cond) {
		this.motors.step( time, acceleration, cond);
	}
	
}
