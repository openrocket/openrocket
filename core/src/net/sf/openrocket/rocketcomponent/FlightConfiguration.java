package net.sf.openrocket.rocketcomponent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

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

    private String configurationName=null;
	
	protected final Rocket rocket;
	protected final FlightConfigurationId fcid;
	
	private static int instanceCount=0;
    // made public for testing.... there is probably a better way
	public final int instanceNumber;

	private class StageFlags implements Cloneable {
		public boolean active = true;
		//public int prev = -1;
		public int stageNumber = -1;
		
		public StageFlags( int _num, boolean _active) {
			this.stageNumber = _num;
			this.active = _active;
		}
		
		@Override
		public StageFlags clone(){
			return new StageFlags( this.stageNumber, true);
		}
	}
	
	/* Cached data */
	final protected HashMap<Integer, StageFlags> stages = new HashMap<Integer, StageFlags>();
	final protected HashMap<MotorConfigurationId, MotorConfiguration> motors = new HashMap<MotorConfigurationId, MotorConfiguration>();
	
	private int boundsModID = -1;
	private BoundingBox cachedBounds = new BoundingBox();
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
		this.configurationName = null;
		this.instanceNumber = instanceCount++;
		
		updateStages();
		updateMotors();
	}
	
	public Rocket getRocket() {
		return rocket;
	}
	
	
	public void clearAllStages() {
		this._setAllStages(false);
		this.updateMotors();
	}
	
	public void setAllStages() {
		this._setAllStages(true);
		this.updateMotors();
	}
		
	private void _setAllStages(final boolean _active) {
		for (StageFlags cur : stages.values()) {
			cur.active = _active;
		}
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
	 * This method flags the specified stage as active, and all other stages as inactive.
	 * 
	 * @param stageNumber  stage number to activate
	 */
	public void setOnlyStage(final int stageNumber) {
		_setAllStages(false);
		_setStageActive(stageNumber, true);
		updateMotors();
	}
	
	/** 
	 * This method flags the specified stage as requested.  Other stages are unaffected.
	 * 
	 * @param stageNumber   stage number to flag
	 * @param _active       inactive (<code>false</code>) or active (<code>true</code>)
	 */
	private void _setStageActive(final int stageNumber, final boolean _active ) {
		if ((0 <= stageNumber) && (stages.containsKey(stageNumber))) {
			stages.get(stageNumber).active = _active;
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
		this.updateMotors();
		log.error("error: attempt to retrieve via a bad stage number: " + stageNumber);
	}

		
	/**
	 * Check whether the stage specified by the index is active.
	 */
	public boolean isStageActive(int stageNumber) {
		if( -1 == stageNumber ) {
			return false;
		}
		
		return stages.get(stageNumber).active;
	}
	
	
	// this method is deprecated because it ignores instancing of parent components (e.g. Strapons or pods )
	// if you're calling this method, you're probably not getting the numbers you expect.
	@Deprecated
	public Collection<RocketComponent> getActiveComponents() {
		Queue<RocketComponent> toProcess = new ArrayDeque<RocketComponent>(this.getActiveStages());
		ArrayList<RocketComponent> toReturn = new ArrayList<>();
		
		while (!toProcess.isEmpty()) {
			RocketComponent comp = toProcess.poll();
			
			toReturn.add(comp);
			for (RocketComponent child : comp.getChildren()) {
				if (!(child instanceof AxialStage)) {
					toProcess.offer(child);
				}
			}
		}
		
		return toReturn;
	}
	
	public List<AxialStage> getActiveStages() {
		List<AxialStage> activeStages = new ArrayList<>();
		
		for (StageFlags flags : this.stages.values()) {
			if (flags.active) {
				activeStages.add( rocket.getStage( flags.stageNumber) );
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
	 * @return the component for the bottom-most center, active stage.
	 */
	public AxialStage getBottomStage() {
		AxialStage bottomStage = null;
		for (StageFlags curFlags : this.stages.values()) {
			if (curFlags.active) {
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
	}
	
	private void updateStages() {
        if (this.rocket.getStageCount() == this.stages.size()) {
            return;
        }
	                		
		this.stages.clear();
		for (AxialStage curStage : this.rocket.getStageList()) {
			
			StageFlags flagsToAdd = new StageFlags( curStage.getStageNumber(), true);
			this.stages.put(curStage.getStageNumber(), flagsToAdd);
		}
	}
	
	public boolean isNameOverridden(){
		return (null != this.configurationName );
	}
	
	public String getName() {
		if( null == configurationName){
			return this.getOneLineMotorDescription();
		}else{
			return configurationName;
		}
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
					buff.append( motorConfig.toMotorDescription());
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
		Collection<MotorConfiguration> activeMotors = new ArrayList<MotorConfiguration>();
		for( MotorConfiguration config : this.motors.values() ){
			if( isComponentActive( config.getMount() )){
				activeMotors.add( config );
			}
		}
		
		return activeMotors;
	}

	private void updateMotors() {
		this.motors.clear();
		
		for ( RocketComponent comp : getActiveComponents() ){
			if (( comp instanceof MotorMount )&&( ((MotorMount)comp).isMotorMount())){
				MotorMount mount = (MotorMount)comp;
				MotorConfiguration motorConfig = mount.getMotorConfig( fcid);
				if( motorConfig.isEmpty()){
					continue;
				}
				
				this.motors.put( motorConfig.getMID(), motorConfig);
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
	 */
	public Collection<Coordinate> getBounds() {
		if (rocket.getModID() != boundsModID) {
			boundsModID = rocket.getModID();
			
			BoundingBox bounds = new BoundingBox();
			
			for (RocketComponent component : this.getActiveComponents()) {
				BoundingBox componentBounds = new BoundingBox( component.getComponentBounds() );				
				
				bounds.compare( componentBounds );
			}
			
			cachedLength = bounds.span().x;

			cachedBounds.compare( bounds );
		}
		
		return cachedBounds.toCollection();
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
	 * Perform a shallow-clone;  copies configuration fields only. 
	 *   
	 * Preserved:  
	 * - components
	 * - motors
	 * - configurables 
	 * 
	 */
	@Override
	public FlightConfiguration clone() {

        // Note the stages are updated in the constructor call.
		FlightConfiguration clone = new FlightConfiguration( this.rocket, this.fcid );
		clone.setName(configurationName);
		
        clone.cachedBounds = this.cachedBounds.clone();
		clone.modID = this.modID;
		clone.boundsModID = -1;
		clone.refLengthModID = -1;
		return clone;
	}

    /**
     * Copy all available information attached to this, and attached copies to the
     * new configuration
     *
     * @param copyId attached the new configuration to this Id
     * @return the new configuration
     */
    @Override
    public FlightConfiguration copy( final FlightConfigurationId copyId ) {
        // Note the stages are updated in the constructor call.
        FlightConfiguration copy= new FlightConfiguration( this.rocket, copyId );

        // copy motor instances.
        for( final MotorConfiguration sourceMotor: motors.values() ){
            MotorConfiguration cloneMotor = sourceMotor.copy( copyId);
            copy.addMotor( cloneMotor);
            cloneMotor.getMount().setMotorConfig(cloneMotor, copyId);
        }

        copy.cachedBounds = this.cachedBounds.clone();
        copy.modID = this.modID;
        copy.boundsModID = -1;
        copy.refLengthModID = -1;
        return copy;
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
			this.configurationName = null;
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
		return this.fcid.toDebug()+" (#"+instanceNumber+") "+ getOneLineMotorDescription();
	}
	
	// DEBUG / DEVEL
	public String toStageListDetail() {
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("\nDumping %d stages for config: %s: (%s)(#: %d)\n", 
				stages.size(), getName(), getId().toShortKey(), instanceNumber));
		final String fmt = "    [%-2s][%4s]: %6s \n";
		buf.append(String.format(fmt, "#", "?actv", "Name"));
		for (StageFlags flags : stages.values()) {
			final int stageNumber = flags.stageNumber;
			buf.append(String.format(fmt, stageNumber, (flags.active?" on": "off"), rocket.getStage( stageNumber).getName()));
		}
		buf.append("\n");
		return buf.toString();
	}
	
	// DEBUG / DEVEL
	public String toMotorDetail(){
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("\nDumping %2d Motors for configuration %s (%s)(#: %s)\n", 
				motors.size(), getName(), getId().toShortKey(), this.instanceNumber));
		
		for( MotorConfiguration curConfig : this.motors.values() ){
			boolean active=this.isStageActive( curConfig.getMount().getStage().getStageNumber());
			String activeString = (active?"active":"      ");
			buf.append("    "+"("+activeString+")"+curConfig.toDebugDetail()+"\n");
		}
		buf.append("\n");
		return buf.toString();
	}
	
 
}
