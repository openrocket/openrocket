package net.sf.openrocket.simulation;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;


/**
 * A class that defines an event during the flight of a rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightEvent implements Comparable<FlightEvent> {
	private static final Translator trans = Application.getTranslator();
	
	/**
	 * The type of the flight event.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	public enum Type {
		/** 
		 * Rocket launch.
		 */
		LAUNCH(trans.get("FlightEvent.Type.LAUNCH")),
		/** 
		 * Ignition of a motor.  Source is the motor mount the motor of which has ignited,
		 * and the data is the MotorId of the motor instance.
		 */
		IGNITION(trans.get("FlightEvent.Type.IGNITION")),
		/**
		 * When the motor has lifted off the ground.
		 */
		LIFTOFF(trans.get("FlightEvent.Type.LIFTOFF")),
		/**
		 * Launch rod has been cleared.
		 */
		LAUNCHROD(trans.get("FlightEvent.Type.LAUNCHROD")),
		/** 
		 * Burnout of a motor.  Source is the motor mount the motor of which has burnt out,
		 * and the data is the MotorId of the motor instance.
		 */
		BURNOUT(trans.get("FlightEvent.Type.BURNOUT")),
		/** 
		 * Ejection charge of a motor fired.  Source is the motor mount the motor of
		 * which has exploded its ejection charge, and data is the MotorId of the motor instance.
		 */
		EJECTION_CHARGE(trans.get("FlightEvent.Type.EJECTION_CHARGE")),
		/** 
		 * Separation of a stage.  Source is the stage which is being separated from the upper stages.
		 */
		STAGE_SEPARATION(trans.get("FlightEvent.Type.STAGE_SEPARATION")),
		/** 
		 * Apogee has been reached.
		 */
		APOGEE(trans.get("FlightEvent.Type.APOGEE")),
		/** 
		 * Opening of a recovery device.  Source is the RecoveryComponent which has opened. 
		 */
		RECOVERY_DEVICE_DEPLOYMENT(trans.get("FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT")),
		/** 
		 * Ground has been hit after flight.
		 */
		GROUND_HIT(trans.get("FlightEvent.Type.GROUND_HIT")),
		
		/**
		 * End of simulation.  Placing this to the queue will end the simulation.
		 */
		SIMULATION_END(trans.get("FlightEvent.Type.SIMULATION_END")),
		
		/**
		 * A change in altitude has occurred.  Data is a <code>Pair<Double,Double></code>
		 * which contains the old and new altitudes.
		 */
		ALTITUDE(trans.get("FlightEvent.Type.ALTITUDE")),
		
		/**
		 * The rocket begins to tumble.
		 */
		TUMBLE(trans.get("FlightEvent.Type.TUMBLE")),
		
		/**
		 * Simulation aborted
		 */
		EXCEPTION(trans.get("FlightEvent.Type.EXCEPTION"));
		
		private final String name;
		
		private Type(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private final Type type;
	private final double time;
	private final RocketComponent source;
	private final Object data;
	
	
	public FlightEvent(Type type, double time) {
		this(type, time, null);
	}
	
	public FlightEvent(Type type, double time, RocketComponent source) {
		this(type, time, source, null);
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
	
	
	/**
	 * Return a new FlightEvent with the same information as the current event
	 * but with <code>null</code> source.  This is used to avoid memory leakage by
	 * retaining references to obsolete components.
	 * 
	 * @return	a new FlightEvent with same type, time and data.
	 */
	public FlightEvent resetSourceAndData() {
		return new FlightEvent(type, time, null, null);
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
		return "FlightEvent[type=" + type.name() + ",time=" + time + ",source=" + source + "]";
	}
}
