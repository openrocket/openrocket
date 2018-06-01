package net.sf.openrocket.motor;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.FlightConfigurableParameter;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;

/**
 * A single motor configuration.  This includes the selected motor
 * and the ejection charge delay.
 */
public class MotorConfiguration implements FlightConfigurableParameter<MotorConfiguration> {
	
	private static final Translator trans = Application.getTranslator();
	
	private final MotorMount mount;
	private final FlightConfigurationId fcid;
	private final MotorConfigurationId mid;
	
	private Motor motor = null;
	private double ejectionDelay = 0.0;

	private boolean ignitionOveride = false;
	private double ignitionDelay = 0.0;
	private IgnitionEvent ignitionEvent = IgnitionEvent.AUTOMATIC;
	
	private int modID = 0;
	
	public MotorConfiguration( final MotorMount _mount, final FlightConfigurationId _fcid ) {
		if (null == _mount ) {
			throw new NullPointerException("Provided MotorMount was null");
		}
		if (null == _fcid ) {
			throw new NullPointerException("Provided FlightConfigurationId was null");
		}

		this.mount = _mount;
		this.fcid = _fcid;
		this.mid = new MotorConfigurationId( _mount, _fcid );
        
		modID++;
	}
	
	public MotorConfiguration( final MotorMount _mount, final FlightConfigurationId _fcid, final MotorConfiguration _source ) {
		this( _mount, _fcid);
		
		if( null != _source){
			motor = _source.motor;
			ejectionDelay = _source.ejectionDelay;
			ignitionOveride = _source.ignitionOveride;
			ignitionEvent = _source.getIgnitionEvent();
			ignitionDelay = _source.getIgnitionDelay();
		}
	}
	
	public boolean hasIgnitionOverride() {
		return ignitionOveride;
	}

	public String toMotorDesignation(){
		if( motor == null ){
			return trans.get("empty");
		}else{
			return this.motor.getDesignation(this.getEjectionDelay());
		}
	}
	
	public MotorConfigurationId getID() {
		return this.mid;
	}

	public FlightConfigurationId getFCID() {
		return fcid;
	}
	
	public MotorConfigurationId getMID() {
		return mid;
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
	

	public int getMotorCount() {
		if( mount instanceof InnerTube ){
			InnerTube inner = (InnerTube) mount;
			return inner.getClusterConfiguration().getClusterCount();
		}else{
			 return 1;
		}
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
			if( this.mid.equals( omi.mid)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.mid.hashCode();
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

    @Override
    public MotorConfiguration copy( final FlightConfigurationId copyId){
        MotorConfiguration clone = new MotorConfiguration( this.mount, copyId);
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

	public String toDescription(){
		return ( this.toMotorDesignation()+
				" in: "+mount.getDebugName()+
				" ign@: "+this.toIgnitionDescription() );
	}
	
	public String toIgnitionDescription(){
		return this.ignitionEvent.getName()+" + "+this.ignitionDelay+"s ";
	}
	
	public String toDebugDetail( ) {
		StringBuilder buf = new StringBuilder();
		
		buf.append(String.format("[in: %28s][fcid %10s][mid %10s][    %8s ign@: %12s]",
				mount.getDebugName(),
				fcid.toShortKey(),
				mid.toDebug(),
				toMotorDesignation(),
				toIgnitionDescription() ));
		
		return buf.toString();
	}


}
