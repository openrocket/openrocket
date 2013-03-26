package net.sf.openrocket.motor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Monitorable;

/**
 * A configuration of motor instances identified by a string id.  Each motor instance has
 * an individual position, ingition time etc.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class MotorInstanceConfiguration implements Monitorable, Cloneable {
	
	private final List<MotorId> ids = new ArrayList<MotorId>();
	private final List<MotorId> unmodifiableIds = Collections.unmodifiableList(ids);
	private final List<MotorInstance> motors = new ArrayList<MotorInstance>();
	private final List<Double> ejectionDelays = new ArrayList<Double>();
	private final List<MotorMount> mounts = new ArrayList<MotorMount>();
	private final List<IgnitionConfiguration.IgnitionEvent> ignitionEvents = new ArrayList<IgnitionConfiguration.IgnitionEvent>();
	private final List<Double> ignitionDelays = new ArrayList<Double>();
	private final List<Coordinate> positions = new ArrayList<Coordinate>();
	private final List<Double> ignitionTimes = new ArrayList<Double>();
	
	
	private int modID = 0;
	
	
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
	public void addMotor(MotorId id, MotorInstance motor, double ejectionDelay, MotorMount mount,
			IgnitionEvent ignitionEvent, double ignitionDelay, Coordinate position) {
		if (this.ids.contains(id)) {
			throw new IllegalArgumentException("MotorInstanceConfiguration already " +
					"contains a motor with id " + id);
		}
		this.ids.add(id);
		this.motors.add(motor);
		this.ejectionDelays.add(ejectionDelay);
		this.mounts.add(mount);
		this.ignitionEvents.add(ignitionEvent);
		this.ignitionDelays.add(ignitionDelay);
		this.positions.add(position);
		this.ignitionTimes.add(Double.POSITIVE_INFINITY);
		modID++;
	}
	
	/**
	 * Return a list of all motor IDs in this configuration (not only ones in active stages).
	 */
	public List<MotorId> getMotorIDs() {
		return unmodifiableIds;
	}
	
	public MotorInstance getMotorInstance(MotorId id) {
		return motors.get(indexOf(id));
	}
	
	public double getEjectionDelay(MotorId id) {
		return ejectionDelays.get(indexOf(id));
	}
	
	public MotorMount getMotorMount(MotorId id) {
		return mounts.get(indexOf(id));
	}
	
	public Coordinate getMotorPosition(MotorId id) {
		return positions.get(indexOf(id));
	}
	
	public void setMotorPosition(MotorId id, Coordinate position) {
		positions.set(indexOf(id), position);
		modID++;
	}
	
	public double getMotorIgnitionTime(MotorId id) {
		return ignitionTimes.get(indexOf(id));
	}
	
	public void setMotorIgnitionTime(MotorId id, double time) {
		this.ignitionTimes.set(indexOf(id), time);
		modID++;
	}
	
	public double getMotorIgnitionDelay(MotorId id) {
		return ignitionDelays.get(indexOf(id));
	}
	
	public IgnitionEvent getMotorIgnitionEvent(MotorId id) {
		return ignitionEvents.get(indexOf(id));
	}
	
	
	private int indexOf(MotorId id) {
		int index = ids.indexOf(id);
		if (index < 0) {
			throw new IllegalArgumentException("MotorInstanceConfiguration does not " +
					"contain a motor with id " + id);
		}
		return index;
	}
	
	
	
	/**
	 * Step all of the motor instances to the specified time minus their ignition time.
	 * @param time	the "global" time
	 */
	public void step(double time, double acceleration, AtmosphericConditions cond) {
		for (int i = 0; i < motors.size(); i++) {
			double t = time - ignitionTimes.get(i);
			if (t >= 0) {
				motors.get(i).step(t, acceleration, cond);
			}
		}
		modID++;
	}
	
	@Override
	public int getModID() {
		int id = modID;
		for (MotorInstance motor : motors) {
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
		clone.ids.addAll(this.ids);
		clone.mounts.addAll(this.mounts);
		clone.positions.addAll(this.positions);
		clone.ejectionDelays.addAll(this.ejectionDelays);
		clone.ignitionTimes.addAll(this.ignitionTimes);
		clone.ignitionEvents.addAll(this.ignitionEvents);
		clone.ignitionDelays.addAll(this.ignitionDelays);
		for (MotorInstance motor : this.motors) {
			clone.motors.add(motor.clone());
		}
		clone.modID = this.modID;
		return clone;
	}
	
}
