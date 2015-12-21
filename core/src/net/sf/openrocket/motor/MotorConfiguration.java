package net.sf.openrocket.motor;

import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.rocketcomponent.FlightConfigurableParameter;
import net.sf.openrocket.rocketcomponent.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.MotorState;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;
import net.sf.openrocket.util.StateChangeListener;

/**
 * A single motor configuration.  This includes the selected motor
 * and the ejection charge delay.
 */
public class MotorConfiguration implements FlightConfigurableParameter<MotorConfiguration> {
	
	protected MotorMount mount = null;
	protected Motor motor = null;
	protected Coordinate position = Coordinate.ZERO;
	protected double ejectionDelay = 0.0;

	protected MotorInstanceId id = null;

	protected boolean ignitionOveride = false;
	protected double ignitionDelay = 0.0;
	protected IgnitionEvent ignitionEvent = IgnitionEvent.NEVER;
	protected double ignitionTime = 0.0;
	
	protected int modID = 0;
	
	public MotorConfiguration( Motor motor ) {
		this();
		this.motor = motor;
	}
	
	public MotorConfiguration() {
		this.id = MotorInstanceId.EMPTY_ID;
		ejectionDelay = 0.0;
		ignitionEvent = IgnitionEvent.LAUNCH;
		ignitionDelay = 0.0;
		modID++;
	}
	
	public MotorState getSimulationState() {
		MotorState state = motor.getNewInstance();
		if( ignitionOveride ) {
			state.setIgnitionTime( this.ignitionTime );
			state.setIgnitionEvent( this.ignitionEvent );
			state.setIgnitionDelay( this.ignitionDelay );
			state.setEjectionDelay( this.ejectionDelay );
		} else {
			MotorConfiguration defInstance = mount.getDefaultMotorInstance();
			state.setIgnitionTime( defInstance.ignitionTime );
			state.setIgnitionEvent( defInstance.ignitionEvent );
			state.setIgnitionDelay( defInstance.ignitionDelay );
			state.setEjectionDelay( defInstance.ejectionDelay );
		}
		state.setMount( mount );
		state.setId( id );
		return state;
	}
	
	public boolean hasIgnitionOverride() {
		return ignitionOveride;
	}
	
	public boolean isActive() {
		return motor != null;
	}
	
	public MotorInstanceId getID() {
		return this.id;
	}
	
	public void setID(final MotorInstanceId _id) {
		this.id = _id;
	}
	
	public void setMotor(Motor motor){
		this.motor = motor;
		fireChangeEvent();
	}
	
	public Motor getMotor() {
		return motor;
	}
	
	public MotorMount getMount() {
		return mount;
	}
	
	public void setMount(MotorMount mount) {
		this.mount = mount;
	}
	
	public double getEjectionDelay() {
		return this.ejectionDelay;
	}
	
	public void setEjectionDelay(double delay) {
		this.ejectionDelay = delay;
		fireChangeEvent();
	}
	
	public Coordinate getPosition() {
		return this.position;
	}
	
	public void setPosition(Coordinate _position) {
		this.position = _position;
		modID++;
		fireChangeEvent();
	}
	
	public double getIgnitionTime() {
		return this.ignitionTime;
	}

	public void useDefaultIgnition() {
		this.ignitionOveride = false;
	}
	
	public void setIgnitionTime(double _time) {
		this.ignitionTime = _time;
		this.ignitionOveride = true;
		modID++;
		fireChangeEvent();
	}
	
	public double getIgnitionDelay() {
		return this.ignitionDelay;
	}
	
	public void setIgnitionDelay(final double _delay) {
		this.ignitionDelay = _delay;
		this.ignitionOveride = true;
		fireChangeEvent();
	}
	
	public IgnitionEvent getIgnitionEvent() {
		return this.ignitionEvent;
	}
	
	public void setIgnitionEvent(final IgnitionEvent _event) {
		this.ignitionEvent = _event;
		this.ignitionOveride = true;
		fireChangeEvent();
	}
	
	public Coordinate getOffset( ){
		if( null == mount ){
			return Coordinate.NaN;
		}else{
			RocketComponent comp = (RocketComponent) mount;
			double delta_x = comp.getLength() + mount.getMotorOverhang() - this.motor.getLength();
			return new Coordinate(delta_x, 0, 0);
		}
	}
	
	public double getLongitudinalInertia() {
		if ( motor != null ) {
			double unitLongitudinalInertia = Inertia.filledCylinderLongitudinal(motor.getDiameter() / 2, motor.getLength());
			return unitLongitudinalInertia * Coordinate.ZERO.weight;
		}
		return 0.0;
	}
	
	public double getRotationalInertia() {
		if ( motor != null ) {
			double unitRotationalInertia = Inertia.filledCylinderRotational(motor.getDiameter() / 2);
			return unitRotationalInertia * Coordinate.ZERO.weight;
		}
		return 0.0;
	}

	public double getPropellantMass(){
		if ( motor != null ) {
			return (motor.getLaunchCG().weight - motor.getEmptyCG().weight);
		}
		return 0.0;
	}

	public boolean isEmpty(){
		return motor == null;
	}
	
	public boolean hasMotor(){
		return ! this.isEmpty();
	}
	
	@Override 
	public boolean equals( Object other ){
		if( null == other ){
			return false;
		}else if( other instanceof MotorConfiguration ){
			MotorConfiguration omi = (MotorConfiguration)other;
			if( this.id.equals( omi.id)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	/**
	 * Create a new instance of this motor instance.  The state of the motor is
	 * identical to this instance and can be used independently from this one.
	 */
	@Override
	public MotorConfiguration clone( ) {
		MotorConfiguration clone = new MotorConfiguration();
		clone.motor = this.motor;
		clone.mount = this.mount;
		clone.ejectionDelay = this.ejectionDelay;
		clone.ignitionOveride = this.ignitionOveride;
		clone.ignitionDelay = this.ignitionDelay;
		clone.ignitionEvent = this.ignitionEvent;
		clone.ignitionTime = this.ignitionTime;
		return clone;
	}
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
	}
	
	protected void fireChangeEvent() {
	}
	
	public int getModID() {
		return modID;
	}
	
	@Override
	public void update(){
	}
}
