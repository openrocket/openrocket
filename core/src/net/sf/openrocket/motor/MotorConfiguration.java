package net.sf.openrocket.motor;

import net.sf.openrocket.rocketcomponent.FlightConfigurableParameter;
import net.sf.openrocket.rocketcomponent.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;

/**
 * A single motor configuration.  This includes the selected motor
 * and the ejection charge delay.
 */
public class MotorConfiguration implements FlightConfigurableParameter<MotorConfiguration> {
	
	public static final String EMPTY_DESCRIPTION = "Empty Configuration";

	// set at config time
	protected MotorMount mount = null;
	protected Motor motor = null;
	protected double ejectionDelay = 0.0;

	protected MotorInstanceId id = null;

	protected boolean ignitionOveride = false;
	protected double ignitionDelay = 0.0;
	protected IgnitionEvent ignitionEvent = IgnitionEvent.NEVER;
	
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
	
	public boolean hasIgnitionOverride() {
		return ignitionOveride;
	}
	
	public String getDescription(){
		if( motor == null ){
			return EMPTY_DESCRIPTION;
		}else{
			return this.motor.getDesignation() + "-" + (int)this.getEjectionDelay();
		}
	}
	
	public MotorInstanceId getID() {
		return this.id;
	}
	
	public void setID(final MotorInstanceId _id) {
		this.id = _id;
	}
	
	public void setMotor(Motor motor){
		this.motor = motor;
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
	}
	
	public Coordinate getPosition(){
		return new Coordinate( getX(), 0, 0);
	}
	
	public double getX(){
		if( isEmpty()){
			return 0.0;
		}
		return mount.getLength() - motor.getLength() + mount.getMotorOverhang();
	}
	
	public void useDefaultIgnition() {
		this.ignitionOveride = false;
	}
	
	public double getIgnitionDelay() {
		return this.ignitionDelay;
	}
	
	public void setIgnitionDelay(final double _delay) {
		this.ignitionDelay = _delay;
		this.ignitionOveride = true;
	}
	
	public IgnitionEvent getIgnitionEvent() {
		return this.ignitionEvent;
	}
	
	public void setIgnitionEvent(final IgnitionEvent _event) {
		this.ignitionEvent = _event;
		this.ignitionOveride = true;
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
	
	public double getUnitLongitudinalInertia() {
		if ( motor != null ) {
			return Inertia.filledCylinderLongitudinal(motor.getDiameter() / 2, motor.getLength());
		}
		return 0.0;
	}
	
	public double getUnitRotationalInertia() {
		if ( motor != null ) {
			return Inertia.filledCylinderRotational(motor.getDiameter() / 2);
		}
		return 0.0;
	}

	public double getPropellantMass(){
		if ( motor != null ) {
			return (motor.getLaunchMass() - motor.getBurnoutMass());
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
		return clone;
	}
	
	public int getModID() {
		return modID;
	}
	
	@Override
	public void update(){
	}
}
