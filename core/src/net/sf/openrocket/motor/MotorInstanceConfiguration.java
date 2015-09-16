package net.sf.openrocket.motor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Monitorable;

/**
 * A configuration of motor instances identified by a string id.  Each motor instance has
 * an individual position, ingition time etc.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MotorInstanceConfiguration implements Cloneable, Iterable<MotorInstance>, Monitorable {
	protected final HashMap<MotorId, MotorInstance> motors = new HashMap<MotorId, MotorInstance>();
	
	private int modID = 0;
	
	private MotorInstanceConfiguration() {
	}
	
	/**
	 * Create a new motor instance configuration for the rocket configuration.
	 *
	 * @param configuration		the rocket configuration.
	 */
	public MotorInstanceConfiguration(Configuration configuration) {
		// motors == this
		final String flightConfigId = configuration.getFlightConfigurationID();
		
		Iterator<RocketComponent> iterator = configuration.getRocket().iterator(false);
		while (iterator.hasNext()) {
			RocketComponent component = iterator.next();
			if (component instanceof MotorMount) {
				MotorMount mount = (MotorMount) component;
				
				MotorConfiguration motorConfig = mount.getMotorConfiguration().get(flightConfigId);
				IgnitionConfiguration ignitionConfig = mount.getIgnitionConfiguration().get(flightConfigId);
				Motor motor = motorConfig.getMotor();
				
				if (motor != null) {
					int count = mount.getMotorConfiguration().size();
					Coordinate[] positions = component.toAbsolute(mount.getMotorPosition(flightConfigId));
					for (int i = 0; i < positions.length; i++) {
						Coordinate position = positions[i];
						MotorId id = new MotorId(component.getID(), i + 1);
						MotorInstance inst = motor.getInstance();
						inst.setID(id);
						inst.setEjectionDelay(motorConfig.getEjectionDelay());
						inst.setMount(mount);
						inst.setIgnitionDelay(ignitionConfig.getIgnitionDelay());
						inst.setIgnitionEvent(ignitionConfig.getIgnitionEvent());
						inst.setPosition(position);
						
						motors.put(id, inst);
					}
				}
			}
		}
		
		
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
		MotorId id = motor.getID();
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
	
	public Set<MotorId> getMotorIDs() {
		return this.motors.keySet();
	}
	
	public MotorInstance getMotorInstance(MotorId id) {
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
			clone.motors.put(motor.id, motor.clone());
		}
		clone.modID = this.modID;
		return clone;
	}
	
	@Override
	public Iterator<MotorInstance> iterator() {
		return this.motors.values().iterator();
	}
	
	
}
