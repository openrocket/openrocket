package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.motor.MotorInstance;

/**
 * FlightConfigurationSet for motors.
 * This is used for motors, where the default value is always no motor.
 */
public class MotorConfigurationSet extends FlightConfigurationSet<MotorInstance> {
	
	public static final int DEFAULT_EVENT_TYPE = ComponentChangeEvent.MOTOR_CHANGE | ComponentChangeEvent.EVENT_CHANGE;
	
	public MotorConfigurationSet(RocketComponent component, MotorInstance _value) {
		super(component, DEFAULT_EVENT_TYPE, _value);
	}
	
	/**
	 * Construct a copy of an existing FlightConfigurationSet.
	 * 
	 * @param flightConfiguration another flightConfiguration to copy data from.
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public MotorConfigurationSet(FlightConfigurationSet<MotorInstance> flightConfiguration, RocketComponent component, int eventType) {
		super(flightConfiguration, component, eventType);
	}
	
	
	@Override
	public void setDefault( MotorInstance value) {
		throw new UnsupportedOperationException("Cannot change default value of motor configuration");
	}
	
	@Override
	public void printDebug(){
		System.err.println("====== Dumping MotorConfigurationSet for mount '"+this.component.getName()+"' of type: "+this.component.getClass().getSimpleName()+" ======");
		System.err.println("        >> motorSet ("+this.size()+ " motors)");
		
		for( FlightConfigurationID loopFCID : this.map.keySet()){
			String shortKey = loopFCID.toShortKey();
			
			MotorInstance curInstance = this.map.get(loopFCID);
			String designation;
			if( MotorInstance.EMPTY_INSTANCE == curInstance){
				designation = "EMPTY_INSTANCE";
			}else{
				designation = curInstance.getMotor().getDesignation(curInstance.getEjectionDelay());
			}
			System.err.println("              >> ["+shortKey+"]= "+designation);
			
		}
	}
	
//	public void printDebug(FlightConfigurationID curFCID){
//		if( this.map.containsKey(curFCID)){
//			// no-op
//		}else{
//			String shortKey = curFCID.toShortKey();
//			MotorInstance curInstance= this.get(curFCID);
//			
//			String designation;
//			if( MotorInstance.EMPTY_INSTANCE == curInstance){
//				designation = "EMPTY_INSTANCE";
//			}else{
//				designation = curInstance.getMotor().getDesignation(curInstance.getEjectionDelay());
//			}
//			System.err.println(" Queried FCID:");
//			System.err.println("              >> ["+shortKey+"]= "+designation);
//		}		
//	}
	
}
