package net.sf.openrocket.motor;

import net.sf.openrocket.rocketcomponent.FlightConfigurableParameter;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;

/**
 * A single motor configuration.  This includes the selected motor
 * and the ejection charge delay.
 */
public class MotorConfiguration implements FlightConfigurableParameter<MotorConfiguration> {
	
	public static final String EMPTY_DESCRIPTION = "Empty Configuration".intern();

	protected final MotorMount mount;
	protected final FlightConfigurationId fcid;
	protected final MotorConfigurationId id;
	
	protected String name = "";
	protected Motor motor = null;
	protected double ejectionDelay = 0.0;

	protected boolean ignitionOveride = false;
	protected double ignitionDelay = 0.0;
	protected IgnitionEvent ignitionEvent = IgnitionEvent.NEVER;
	
	protected int modID = 0;
	
	public MotorConfiguration( final MotorMount _mount, final FlightConfigurationId _fcid ) {
		if (null == _mount ) {
			throw new NullPointerException("Provided MotorMount was null");
		}
		if (null == _fcid ) {
			throw new NullPointerException("Provided FlightConfigurationId was null");
		}

		this.mount = _mount;
		this.fcid = _fcid;
		this.id = new MotorConfigurationId( _mount, _fcid );
		
		this.motor = null;
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
	
	public MotorConfigurationId getID() {
		return this.id;
	}
		
	public void setMotor(Motor motor){
		this.motor = motor;
		updateName();
	}
	
	public Motor getMotor() {
		return motor;
	}
	
	public MotorMount getMount() {
		return mount;
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
		RocketComponent comp = (RocketComponent) mount;
		double delta_x = comp.getLength() + mount.getMotorOverhang() - this.motor.getLength();
		return new Coordinate(delta_x, 0, 0);	
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
		MotorConfiguration clone = new MotorConfiguration( this.mount, this.fcid);
		clone.motor = this.motor;
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

	private void updateName(){
		if( null != motor ){
			this.name = this.mount.getID() + "-"+ getDescription();
		}
	}
	
	public int getMotorCount() {
		if( mount instanceof InnerTube ){
			InnerTube inner = (InnerTube) mount;
			return inner.getClusterConfiguration().getClusterCount();
		}else{
			 return 1;
		}
	}
	

}
