package net.sf.openrocket.motor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Monitorable;

/**
 * A configuration of motor instances identified by a string id.  Each motor instance has
 * an individual position, ingition time etc.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MotorInstanceConfiguration implements Cloneable, Iterable<MotorInstance>, Monitorable {
	protected final HashMap<MotorInstanceId, MotorInstance> motors = new HashMap<MotorInstanceId, MotorInstance>();
	protected final FlightConfiguration config;
	private int modID = 0;
	
	private MotorInstanceConfiguration() {
		this.config = null;
	}
	
	/**
	 * Create a new motor instance configuration for the rocket configuration.
	 *
	 * @param configuration		the rocket configuration.
	 */
	public MotorInstanceConfiguration( final FlightConfiguration _configuration) {
		this.config = _configuration;
//		final FlightConfigurationID fcid = configuration.getFlightConfigurationID();
		
		update();
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
		MotorInstanceId id = motor.getMotorID();
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
	
	/**
	 * Step all of the motor instances to the specified time minus their ignition time.
	 * @param time	the "global" time
	 */
	public void step(double time, double acceleration, AtmosphericConditions cond) {
		for (MotorInstance inst : motors.values()) {
			double t = time - inst.getIgnitionTime();
			if (t >= 0) {
				inst.step(t, acceleration, cond);
			}
		}
		modID++;
	}
	
	@Override
	public int getModID() {
		int id = modID;
		for (MotorInstance motor : motors.values()) {
			id += motor.getModID();
		}
		return id;
	}
	
	/**
	 * Return a copy of this motor instance configuration with independent motor instances
	 * from this instance.
	 */
	@Override
	public MotorInstanceConfiguration clone() {
		MotorInstanceConfiguration clone = new MotorInstanceConfiguration();
		for (MotorInstance motor : this.motors.values()) {
			clone.motors.put(motor.getMotorID(), motor.clone());
		}
		clone.modID = this.modID;
		return clone;
	}
	
	@Override
	public Iterator<MotorInstance> iterator() {
		return this.motors.values().iterator();
	}
	
	public List<MotorInstance> getActiveMotors() {
		List<MotorInstance> activeList = new ArrayList<MotorInstance>();
		for( MotorInstance inst : this.motors.values() ){
			if( inst.isActive() ){
				activeList.add( inst );
			}
		}
		
		return activeList;
	}
	
	public void populate( final MotorInstanceConfiguration source){
		this.motors.putAll( source.motors );
	}
	
	public void update() {
		this.motors.clear();
		
		for ( RocketComponent comp : this.config.getActiveComponents() ){
			if ( comp instanceof MotorMount ){ 
				MotorMount mount = (MotorMount)comp;
				MotorInstance inst = mount.getMotorInstance(this.config.getFlightConfigurationID());
				
				// this merely accounts for instancing of this component:
				// int instancCount = comp.getInstanceCount();

				// we account for instances this way because it counts *all* the instancing between here 
				// and the rocket root.
				Coordinate[] instanceLocations= comp.getLocations();
				
//					System.err.println(String.format(",,,,,,,,       : %s (%s)",  
//		                       inst.getMotor().getDigest(), inst.getMotorID() ));
				int instanceNumber = 0;
				for (  Coordinate curMountLocation : instanceLocations ){
						MotorInstance curInstance = inst.clone();
						curInstance.setID( new MotorInstanceId( comp.getName(), instanceNumber+1) );
						
						// motor location w/in mount: parent.refpoint -> motor.refpoint 
						Coordinate curMotorOffset = curInstance.getOffset();
						curInstance.setPosition( curMountLocation.add(curMotorOffset) );
						this.motors.put( curInstance.getMotorID(), curInstance);
							
						// vvvv DEVEL vvvv
//								System.err.println(String.format(",,,,,,,,[ %2d]:  %s. (%s)",
//										instanceNumber, curInstance.getMotor().getDigest(), curInstance));
						// ^^^^ DEVEL ^^^^
						instanceNumber ++;
				}
				 
			}
		}
		//System.err.println("returning "+toReturn.size()+" active motor instances for this configuration: "+this.fcid.getShortKey());
		//System.err.println(this.rocket.getConfigurationSet().toDebug());
	}
	
}
