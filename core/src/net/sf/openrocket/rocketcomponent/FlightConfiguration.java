package net.sf.openrocket.rocketcomponent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.MotorInstanceId;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Monitorable;


/**
 * A class defining a rocket configuration.
 *     Describes active stages, and active motors.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 * @author Daniel Williams <equipoise@gmail.com>
 */
public class FlightConfiguration implements FlightConfigurableParameter<FlightConfiguration>, Monitorable {
	private static final Logger log = LoggerFactory.getLogger(FlightConfiguration.class);
	
	public final static String DEFAULT_CONFIGURATION_NAME = "Default Configuration";
	
	protected boolean isNamed = false;
	protected String configurationName;
	
	protected final Rocket rocket;
	protected final FlightConfigurationId fcid;
	
	protected static int instanceCount=0;
	public final int instanceNumber;

	protected class StageFlags implements Cloneable {
		public boolean active = true;
		public int prev = -1;
		public AxialStage stage = null;
		
		public StageFlags(AxialStage _stage, int _prev, boolean _active) {
			this.stage = _stage;
			this.prev = _prev;
			this.active = _active;
		}
		
		public int getKey(){
			return this.stage.getStageNumber();
		}
		
		@Override
		public StageFlags clone(){
			return new StageFlags( this.stage, this.prev, true);
		}
		
	}
	
	/* Cached data */
	final protected HashMap<Integer, StageFlags> stages = new HashMap<Integer, StageFlags>();
	protected final HashMap<MotorInstanceId, MotorConfiguration> motors = new HashMap<MotorInstanceId, MotorConfiguration>();
	
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
	public FlightConfiguration(final Rocket rocket, final FlightConfigurationId _fcid ) {
		if( null == _fcid){
			this.fcid = new FlightConfigurationId();
		}else{
			this.fcid = _fcid;
		}
		this.rocket = rocket;
		this.isNamed = false;
		this.configurationName = "<WARN: attempt to access unset configurationName. WARN!> ";
		this.instanceNumber = instanceCount++;
		
		updateStages();
		updateMotors();
	}
	
	public Rocket getRocket() {
		return rocket;
	}
	
	
	public void clearAllStages() {
		this.setAllStages(false, true);
	}
	
	public void setAllStages() {
		this.setAllStages(true, true);
	}
		
	private void setAllStages(final boolean _active, final boolean updateRequired ) {
		for (StageFlags cur : stages.values()) {
			cur.active = _active;
		}
		if( updateRequired ){
			update();
		}
	}
	
	/** 
	 * This method flags a stage inactive.  Other stages are unaffected.
	 * 
	 * @param stageNumber  stage number to inactivate
	 */
	public void clearStage(final int stageNumber) {
		setStageActive( stageNumber, false, true);
	}
	
	/** 
	 * This method flags the specified stage as active, and all other stages as inactive.
	 * 
	 * @param stageNumber  stage number to activate
	 */
	public void setOnlyStage(final int stageNumber) {
		setAllStages(false, false);
		setStageActive(stageNumber, true, true);
	}
	
	/** 
	 * This method flags the specified stage as requested.  Other stages are unaffected.
	 * 
	 * @param stageNumber   stage number to flag
	 * @param _active       inactive (<code>false</code>) or active (<code>true</code>)
	 */
	public void setStageActive(final int stageNumber, final boolean _active ) {
		this.setStageActive(stageNumber, _active, true );
	}
	
	private void setStageActive(final int stageNumber, final boolean _active, final boolean updateRequired ) {
		if ((0 <= stageNumber) && (stages.containsKey(stageNumber))) {
			stages.get(stageNumber).active = _active;
			if( updateRequired  ){
				update();
			}
			return;
		}
		log.error("error: attempt to retrieve via a bad stage number: " + stageNumber);
	}
	
	
	public void toggleStage(final int stageNumber) {
		if ((0 <= stageNumber) && (stages.containsKey(stageNumber))) {
			StageFlags flags = stages.get(stageNumber);
			flags.active = !flags.active;
			return;
		}
		log.error("error: attempt to retrieve via a bad stage number: " + stageNumber);
	}

		
	/**
	 * Check whether the stage specified by the index is active.
	 */
	public boolean isStageActive(int stageNumber) {
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
	
	public FlightConfigurationId getFlightConfigurationID() {
		return fcid;
	}
	
	public FlightConfigurationId getId() {
		return getFlightConfigurationID();
	}
	
	////////////////  Listeners  ////////////////
	
	// for outgoing events only
	protected void fireChangeEvent() {
		this.modID++;
		boundsModID = -1;
		refLengthModID = -1;
		
		updateStages();
		updateMotors();
	}
	
	protected void updateStages() {
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
				return fcid.toDebug()+" - "+this.getOnelineMotorDescription();
			}else{
				return fcid.getFullKeyText();
			}
		}
	}
	
	public String getOnelineMotorDescription(){
		StringBuilder buff = new StringBuilder("[");
		boolean first = true;
		int activeMotorCount = 0;
		for ( RocketComponent comp : getActiveComponents() ){
			if (( comp instanceof MotorMount )&&( ((MotorMount)comp).isMotorMount())){ 
				MotorMount mount = (MotorMount)comp;
				MotorConfiguration inst = mount.getMotorInstance( fcid);
				
				if( first ){
					first = false;
				}else{
					buff.append(";");
				}
				
				if( ! inst.isEmpty()){
					buff.append( inst.getMotor().getDesignation());
					++activeMotorCount;
				}
			}
		}
		if( 0 == activeMotorCount ){
			buff.append(" n/a ");
		}
		buff.append("]");
		return buff.toString();
	}

	@Override
	public String toString() {
		return this.getName();
	}


	/**
	 * Add a motor instance to this configuration.  
	 * 
	 * @param motor			the motor instance.
	 * @throws IllegalArgumentException	if a motor with the specified ID already exists.
	 */
	public void addMotor(MotorConfiguration motor) {
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
	
	public Set<MotorInstanceId> getMotorIDs() {
		return motors.keySet();
	}
	
	public MotorConfiguration getMotorInstance(MotorInstanceId id) {
		return motors.get(id);
	}
	
	public boolean hasMotors() {
		return (0 < motors.size());
	}
	
	public Collection<MotorConfiguration> getActiveMotors() {
		return motors.values();
	}

	protected void updateMotors() {
		this.motors.clear();
		
		for ( RocketComponent compMount : getActiveComponents() ){
			if (( compMount instanceof MotorMount )&&( ((MotorMount)compMount).isMotorMount())){
				MotorMount mount = (MotorMount)compMount;
				MotorConfiguration sourceConfig = mount.getMotorInstance( fcid);
				if( sourceConfig.isEmpty()){
					continue;
				}
				
				this.motors.put( sourceConfig.getID(), sourceConfig);
			}
		}
	}

	@Override
	public void update(){
		updateStages();
		updateMotors();
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
		// Note the motors and stages are updated in the constructor call.
		FlightConfiguration clone = new FlightConfiguration( this.getRocket(), this.fcid );
		clone.setName("clone[#"+clone.instanceNumber+"]"+clone.fcid.toShortKey());
		//	log.error(">> Why am I being cloned!?", new IllegalStateException(this.toDebug()+" >to> "+clone.toDebug()));
		
		
		// DO NOT UPDATE this.stages or this.motors;
		// these are are updated correctly on their own.
		
		clone.cachedBounds = this.cachedBounds.clone();
		clone.modID = this.modID;
		clone.boundsModID = -1;
		clone.refLengthModID = -1;
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
		if(( null == newName ) ||( "".equals(newName))){
			this.isNamed= false;
			return;
		}else if( ! this.getFlightConfigurationID().isValid()){
			return;
		}else if( newName.equals(this.configurationName)){
			return;
		}
		this.isNamed = true;
		this.configurationName = newName;
	}
	
	@Override
	public boolean equals(Object other){
		if( other instanceof FlightConfiguration ){
			return this.fcid.equals( ((FlightConfiguration)other).fcid);
		}
		return false;	
	}
	
	@Override
	public int hashCode(){
		return this.fcid.hashCode();
	}
	
	
	public String toDebug() {
		return this.fcid.toDebug()+" (#"+instanceNumber+")";
	}
	
	// DEBUG / DEVEL
	public String toStageListDetail() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("\nDumping %d stages for config: %s: (#: %d)\n", this.stages.size(), this.getName(), this.instanceNumber));
		final String fmt = "    [%-2s][%4s]: %6s \n";
		buf.append(String.format(fmt, "#", "?actv", "Name"));
		for (StageFlags flags : this.stages.values()) {
			AxialStage curStage = flags.stage;
			buf.append(String.format(fmt, curStage.getStageNumber(), (flags.active?" on": "off"), curStage.getName()));
		}
		buf.append("\n");
		return buf.toString();
	}
	
	// DEBUG / DEVEL
	public String toMotorDetail(){
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("\nDumping %2d Motors for configuration %s: (#: %s)\n", this.motors.size(), this, this.instanceNumber));
		final String fmt = "    ..[%-8s] <%6s>    %-12s %-20s\n";
		buf.append(String.format(fmt, "Motor Id", "?active", "Mtr Desig","Mount"));
		for( MotorConfiguration curConfig : this.motors.values() ){
			MotorMount mount = curConfig.getMount();
			
			String motorId = curConfig.getID().toShortKey();
			String activeDescr = (curConfig.isActive()? "active": "inactv");
			String motorDesig;
			if( curConfig.isEmpty() ){
				motorDesig = "(empty)";
			}else{
				motorDesig = curConfig.getMotor().getDesignation();
			}
			String mountName = ((RocketComponent)mount).getName();
			
			buf.append(String.format( fmt, motorId, activeDescr, motorDesig, mountName));
		}
		buf.append("\n");
		return buf.toString();
	}
	
 
}
