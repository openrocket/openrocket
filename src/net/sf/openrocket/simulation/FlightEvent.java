package net.sf.openrocket.simulation;

import net.sf.openrocket.rocketcomponent.RocketComponent;


/**
 * A class that defines an event during the flight of a rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightEvent implements Comparable<FlightEvent> {

	/**
	 * The type of the flight event.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	public enum Type {
		/** 
		 * Rocket launch.
		 */
		LAUNCH,
		/**
		 * When the motor has lifted off the ground.
		 */
		LIFTOFF,
		/**
		 * Launch rod has been cleared.
		 */
		LAUNCHROD,
		/** 
		 * Ignition of a motor.  Source is the motor mount the motor of which has ignited. 
		 */
		IGNITION,
		/** 
		 * Burnout of a motor.  Source is the motor mount the motor of which has burnt out. 
		 */
		BURNOUT,
		/** 
		 * Ejection charge of a motor fired.  Source is the motor mount the motor of
		 * which has exploded its ejection charge. 
		 */
		EJECTION_CHARGE,
		/** 
		 * Opening of a recovery device.  Source is the RecoveryComponent which has opened. 
		 */
		RECOVERY_DEVICE_DEPLOYMENT,
		/** 
		 * Separation of a stage.  Source is the stage which has separated all lower stages. 
		 */
		STAGE_SEPARATION,
		/** 
		 * Apogee has been reached.
		 */
		APOGEE,
		/** 
		 * Ground has been hit after flight.
		 */
		GROUND_HIT,
		
		/**
		 * End of simulation.  Placing this to the queue will end the simulation.
		 */
		SIMULATION_END,
		
		/**
		 * A change in altitude has occurred.  Data is a <code>Pair<Double,Double></code>
		 * which contains the old and new altitudes.
		 */
		ALTITUDE
	}

	private final Type type;
	private final double time;
	private final RocketComponent source;
	private final Object data;

	
	public FlightEvent(Type type, double time) {
		this(type, time, null);
	}
	
	public FlightEvent(Type type, double time, RocketComponent source) {
		this(type,time,source,null);
	}

	public FlightEvent(Type type, double time, RocketComponent source, Object data) {
		this.type = type;
		this.time = time;
		this.source = source;
		this.data = data;
	}
	

	
	public Type getType() {
		return type;
	}
	
	public double getTime() {
		return time;
	}
	
	public RocketComponent getSource() {
		return source;
	}
	
	public Object getData() {
		return data;
	}
	
	
	public FlightEvent resetSource() {
		return new FlightEvent(type, time, null, data);
	}

	/**
	 * Compares this event to another event depending on the event time.  Secondary
	 * sorting is performed based on the event type ordinal.
	 */
	@Override
	public int compareTo(FlightEvent o) {
		if (this.time < o.time)
			return -1;
		if (this.time > o.time)
			return 1;
		
		return this.type.ordinal() - o.type.ordinal();
	}
	
	@Override
	public String toString() {
		return "FlightEvent[type="+type.toString()+",time="+time+",source="+source+"]";
	}
}
