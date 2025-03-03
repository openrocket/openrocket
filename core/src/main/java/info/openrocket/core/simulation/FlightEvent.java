package info.openrocket.core.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;

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
		 * A warning was raised during the execution of the simulation
		 */
		 SIM_WARN(trans.get("FlightEvent.Type.SIM_WARN")),

		/**
		 * It is impossible for the simulation proceed due to characteristics
		 * of the rocket or flight configuration
		 */
		SIM_ABORT(trans.get("FlightEvent.Type.SIM_ABORT")),
		
		/**
		 * Simulation exception thrown due to error in OpenRocket code
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
	
	
	public FlightEvent( final Type type, final double time) {
		this(type, time, null, null);
	}
	
	public FlightEvent( final Type type, final double time, final RocketComponent source) {
		this(type, time, source, null);
	}
	
	public FlightEvent( final FlightEvent sourceEvent, final RocketComponent source, final Object data) {
		this(sourceEvent.type, sourceEvent.time, source, data);
	}
	
	public FlightEvent( final Type type, final double time, final RocketComponent source, final Object data) {
		this.type = type;
		this.time = time;
		this.source = source;
		this.data = data;
		validate();
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
	 * Compares this event to another event depending on the event time.  Secondary
	 * sorting is performed on stages; lower (numerically higher) stage first.  Tertiary
	 * sorting is performed based on the event type ordinal.
	 */
	@Override
	public int compareTo(FlightEvent o) {

		// first, sort on time
		if (this.time < o.time)
			return -1;
		if (this.time > o.time)
			return 1;
		
		// second, sort on stage presence.  Events with no source go first
		if ((this.getSource() == null) && (o.getSource() != null))
			return -1;
		if ((this.getSource() != null) && (o.getSource() == null))
			return 1;

		// third, sort on stage order.  Bigger stage number goes first
		if ((this.getSource() != null) && (o.getSource() != null)) {
			if (this.getSource().getStageNumber() > o.getSource().getStageNumber())
				return -1;
			if (this.getSource().getStageNumber() < o.getSource().getStageNumber())
				return 1;
		}

		// finally, sort on event type
		return this.type.ordinal() - o.type.ordinal();
	}

	public boolean equals(FlightEvent o) {
		if ((this.type == Type.SIM_WARN) && (o.type == Type.SIM_WARN))
			return ((Warning)(this.data)).equals((Warning)(o.data));

		return this.compareTo(o) == 0;
	}
	
	@Override
	public String toString() {
		return "FlightEvent[type=" + type.name() + ",time=" + time + ",source=" + source + ",data=" + String.valueOf(data) + "]";
		
	}
	
	/** 
	 * verify that this event's state is well-formed. 
	 * 
	 * User actions should not cause these.
	 * 
	 * @return
	 */
	public void validate(){
		if(Double.isNaN(this.time)){
			throw new IllegalStateException(type.name()+" event has a NaN time!");
		}
		switch( this.type ){
		case BURNOUT:
			if( null != this.source){
				if( ! ( this.source instanceof MotorMount)){
					throw new IllegalStateException(type.name()+" events should have "
							+MotorMount.class.getSimpleName()+" type data payloads, instead of"
							+this.getSource().getClass().getSimpleName());
				}
			}
			if( null != this.data ){
				if( ! ( this.data instanceof MotorClusterState)){
					throw new IllegalStateException(type.name()+" events should have "
							+MotorClusterState.class.getSimpleName()+" type data payloads");
				}
			}
			break;
		case IGNITION:
			if( null != this.source){
				if( ! ( this.getSource() instanceof MotorMount)){
					throw new IllegalStateException(type.name()+" events should have "
							+MotorMount.class.getSimpleName()+" type data payloads, instead of"
							+this.getSource().getClass().getSimpleName());
				}
			}
			if( null != this.data ){
				if( ! ( this.data instanceof MotorClusterState)){
					throw new IllegalStateException(type.name()+"events should have "
							+MotorClusterState.class.getSimpleName()+" type data payloads");
				}
			}
			break;
		case EJECTION_CHARGE:
			if( null != this.source){
				if( ! ( this.getSource() instanceof AxialStage)){
					throw new IllegalStateException(type.name()+" events should have "
							+AxialStage.class.getSimpleName()+" type data payloads, instead of"
							+this.getSource().getClass().getSimpleName());
				}
			}
			if( null != this.data ){
				if( ! ( this.data instanceof MotorClusterState)){
					throw new IllegalStateException(type.name()+" events should have "
							+MotorClusterState.class.getSimpleName()+" type data payloads");
				}
			}
			break;
		case SIM_WARN:
			if (null != this.source) {
				// rather than making event sources take sets of components, or trying to keep them
				// in sync with the sources of Warnings, we'll require the event source to be null
				// and pull the actual sources from the Warning
				throw new IllegalStateException(type.name()+" event requires null source component; was " + this.source);
			}	
			if (( null == this.data ) || ( ! ( this.data instanceof Warning ))) {
				throw new IllegalStateException(type.name()+" events require Warning objects");
			}
			break;
		case SIM_ABORT:
			if (( null == this.data ) || ( ! ( this.data instanceof SimulationAbort ))) {
				throw new IllegalStateException(type.name()+" events require SimulationAbort objects");
			}
			break;
		case LAUNCH:
		case LIFTOFF:
		case LAUNCHROD:
		case STAGE_SEPARATION:
		case APOGEE:
		case RECOVERY_DEVICE_DEPLOYMENT:
		case GROUND_HIT:
		case SIMULATION_END:
		case ALTITUDE:
		case TUMBLE:
		case EXCEPTION:
		default:
		}
	}
	
	
}
