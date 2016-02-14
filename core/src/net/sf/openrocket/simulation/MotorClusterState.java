package net.sf.openrocket.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.motor.IgnitionEvent;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.MotorInstanceId;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class MotorClusterState {
	
	private static final Logger log = LoggerFactory.getLogger(MotorClusterState.class);
	
	// for reference: set at initialization ONLY.
	final protected Motor motor;
	final protected MotorConfiguration config;
	final protected int motorCount;
	final protected double thrustDuration; 
	
	// for state:
	protected double ignitionTime = Double.NaN;
	protected double cutoffTime = Double.NaN;
	protected double ejectionTime = Double.NaN;
	protected ThrustState currentState = ThrustState.PREFLIGHT;
		
	public MotorClusterState(final MotorConfiguration _config) {
		log.debug(" Creating motor instance of " + _config.getDescription());
		this.config = _config;
		this.motor = _config.getMotor();
		MotorMount mount = this.config.getMount();
		if( mount instanceof InnerTube ){
			InnerTube inner = (InnerTube) mount;
			this.motorCount = inner.getClusterConfiguration().getClusterCount();
		}else{
			this.motorCount =0;
		}
		
		thrustDuration = this.motor.getBurnTimeEstimate();
		
		this.resetToPreflight();
	}
	
	public void arm( final double _armTime ){
		if( ThrustState.PREFLIGHT == currentState ){
			log.info( "igniting motor: "+this.toString()+" at t="+_armTime);
			//this.ignitionTime = _ignitionTime;
			this.currentState = this.currentState.getNext();
		}else{
			throw new IllegalStateException("Attempted to arm a motor with status="+this.currentState.getName());
		}
	}
	
	public double getIgnitionTime() {
		return ignitionTime;
	}

	public IgnitionEvent getIgnitionEvent() {
		return config.getIgnitionEvent();
	}

	public void ignite( final double _ignitionTime ){
		if( ThrustState.ARMED == currentState ){
			log.info( "igniting motor: "+this.toString()+" at t="+_ignitionTime);
			this.ignitionTime = _ignitionTime;
			this.currentState = this.currentState.getNext();
		}else{
			throw new IllegalStateException("Attempted to ignite a motor state with status="+this.currentState.getName());
		}
	}

	public void burnOut( final double _burnOutTime ){
		if( ThrustState.THRUSTING == currentState ){
			log.info( "igniting motor: "+this.toString()+" at t="+_burnOutTime);
			this.ignitionTime = _burnOutTime;
			this.currentState = this.currentState.getNext();
		}else{
			throw new IllegalStateException("Attempted to stop thrust (burn-out) a motor state with status="+this.currentState.getName());
		}		
	}
	
	public void fireEjectionCharge( final double _ejectionTime ){
		if( ThrustState.DELAYING == currentState ){
			log.info( "igniting motor: "+this.toString()+" at t="+_ejectionTime);
			this.ejectionTime = _ejectionTime;
			this.currentState = this.currentState.getNext();
		}else{
			throw new IllegalStateException("Attempted to fire an ejection charge in motor state: "+this.currentState.getName());
		}		
	}

	/**
	 * Alias for "burnOut(double)"
	 */
	public void cutOff( final double _cutoffTime ){
		burnOut( _cutoffTime );		
	}
	
	public double getEjectionDelay() {
		return config.getEjectionDelay();
	}
	
	public MotorInstanceId getID() {
		return config.getID();
	}

	public double getPropellantMass(){
		return (motor.getLaunchMass() - motor.getBurnoutMass());
	}
		
	public MotorMount getMount(){
		return config.getMount();
	}
	
	public Motor getMotor(){
		return this.motor;
	}
	
	double getCutOffTime(){
		return this.cutoffTime;
	}

	public double getMotorTime( final double _simulationTime ){
		return _simulationTime - this.getIgnitionTime();
	}
	
	public double getThrust( final double simulationTime, final AtmosphericConditions cond){
		if( this.currentState.isThrusting() ){
			double motorTime = this.getMotorTime( simulationTime);
			return this.motorCount * motor.getThrustAtMotorTime( motorTime );
		}else{
			return 0.0;
		}
	}

	public boolean isPlugged(){
		return ( this.config.getEjectionDelay() == Motor.PLUGGED_DELAY);
	}

	public boolean hasEjectionCharge(){
		return ! isPlugged();
	}

	public boolean isFinishedThrusting(){
		return currentState.isAfter( ThrustState.THRUSTING );
	}
	
	/**
	 * alias to 'resetToPreflight()'
	 */
	public void reset(){
		resetToPreflight();
	}
	
	public void resetToPreflight(){
		// i.e. in the "future"
		ignitionTime = Double.POSITIVE_INFINITY;
		cutoffTime = Double.POSITIVE_INFINITY;
		ejectionTime = Double.POSITIVE_INFINITY;
		
		currentState = ThrustState.PREFLIGHT;
	}
	
	public boolean testForIgnition( final FlightEvent _event ){
		RocketComponent mount = (RocketComponent) this.getMount();
		return getIgnitionEvent().isActivationEvent( _event, mount);
	}
	
	@Override
	public String toString(){
		return this.motor.getDesignation();
	}
	
//	public void update( final double simulationTime ){
//		final double motorTime = this.getMotorTime( simulationTime );
//		log.debug("Attempt to update this motorClusterSimulation with: ");
//		log.debug("    this.ignitionTime= "+this.ignitionTime);
//		log.debug("  this.thrustDuration= "+this.thrustDuration);
//		log.debug("  simTime = "+simulationTime);
//		log.debug("  motorTime= "+motorTime );
//			
//		log.debug( " time array = "+((ThrustCurveMotor)this.getMotor()).getTimePoints() );
//		
//		switch( this.currentState ){
//		
//	}

	
	
}