package net.sf.openrocket.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.MotorInstanceId;
import net.sf.openrocket.rocketcomponent.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.util.BugException;

public class MotorSimulation {
	
	private static final Logger log = LoggerFactory.getLogger(MotorSimulation.class);
	
	// for reference: set at initialization ONLY.
	final protected Motor motor;
	final protected MotorConfiguration config;
	final protected double thrustDuration; 
	
	// for state:
	protected double ignitionTime = Double.NaN;
	protected double cutoffTime = Double.NaN;
	protected double ejectionTime = Double.NaN;
	protected MotorState currentState = MotorState.PREFLIGHT;
		
	public MotorSimulation(final MotorConfiguration _config) {
		log.debug(" Creating motor instance of " + _config.getDescription());
		this.config = _config;
		this.motor = _config.getMotor();
		thrustDuration = this.motor.getBurnTimeEstimate();
		
		this.resetToPreflight();
	}
	
	@Override
	public MotorSimulation clone() {
		try {
			return (MotorSimulation) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException", e);
		}
	}

	
	public void arm( final double _armTime ){
		if( MotorState.PREFLIGHT == currentState ){
			log.info( "igniting motor: "+this.toString()+" at t="+_armTime);
			//this.ignitionTime = _ignitionTime;
			this.currentState = this.currentState.getNext();
		}else{
			throw new IllegalStateException("Attempted to arm a motor with status="+this.currentState.getName());
		}
	}
	
	public boolean isFinishedThrusting(){
		return currentState.isAfter( MotorState.THRUSTING );
	}
	
	public double getIgnitionTime() {
		return ignitionTime;
	}

	public IgnitionEvent getIgnitionEvent() {
		return config.getIgnitionEvent();
	}

	
	public void ignite( final double _ignitionTime ){
		if( MotorState.ARMED == currentState ){
			log.info( "igniting motor: "+this.toString()+" at t="+_ignitionTime);
			this.ignitionTime = _ignitionTime;
			this.currentState = this.currentState.getNext();
		}else{
			throw new IllegalStateException("Attempted to ignite a motor state with status="+this.currentState.getName());
		}		
	}

	public void burnOut( final double _burnOutTime ){
		if( MotorState.THRUSTING == currentState ){
			log.info( "igniting motor: "+this.toString()+" at t="+_burnOutTime);
			this.ignitionTime = _burnOutTime;
			this.currentState = this.currentState.getNext();
		}else{
			throw new IllegalStateException("Attempted to stop thrust (burn-out) a motor state with status="+this.currentState.getName());
		}		
	}
	
	public void fireEjectionCharge( final double _ejectionTime ){
		if( MotorState.DELAYING == currentState ){
			log.info( "igniting motor: "+this.toString()+" at t="+_ejectionTime);
			this.ejectionTime = _ejectionTime;
			this.currentState = this.currentState.getNext();
		}else{
			throw new IllegalStateException("Attempted to fire an ejection charge in motor state: "+this.currentState.getName());
		}		
	}

	/* 
	 * Alias for "burnOut(double)"
	 */
	public void cutOff( final double _cutoffTime ){
		burnOut( _cutoffTime );		
	}

	public double getIgnitionDelay() {
		return config.getEjectionDelay();
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
	
//	public boolean isActive( ) {
//		return this.currentState.isActive();
//	}
	
	public MotorMount getMount(){
		return config.getMount();
	}
	
	public Motor getMotor(){
		return this.motor;
	}
	
	double getCutOffTime(){
		return this.cutoffTime;
	}
	
	public double getThrust( final double simTime, final AtmosphericConditions cond){
		if( this.currentState.isThrusting() ){
			return motor.getThrustAtMotorTime( simTime - this.getIgnitionTime());
		}else{
			return 0.0;
		}
	}
	
	public void resetToPreflight(){
		ignitionTime = Double.NaN;
		cutoffTime = Double.NaN;
		ejectionTime = Double.NaN;
		currentState = MotorState.PREFLIGHT;
	}
	
	@Override
	public String toString(){
		return this.motor.getDesignation();
	}
	
}
