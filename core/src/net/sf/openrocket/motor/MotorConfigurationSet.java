package net.sf.openrocket.motor;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationID;
import net.sf.openrocket.rocketcomponent.ParameterSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * FlightConfigurationSet for motors.
 * This is used for motors, where the default value is always no motor.
 */
public class MotorConfigurationSet extends ParameterSet<MotorInstance> {
	
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
	public MotorConfigurationSet(ParameterSet<MotorInstance> flightConfiguration, RocketComponent component, int eventType) {
		super(flightConfiguration, component, eventType);
	}
	
	
	@Override
	public void setDefault( MotorInstance value) {
		throw new UnsupportedOperationException("Cannot change default value of motor configuration");
	}
	
	@Override
	public String toDebug(){
		StringBuilder buffer = new StringBuilder();
		buffer.append("====== Dumping MotorConfigurationSet for mount '"+this.component.getName()+"' of type: "+this.component.getClass().getSimpleName()+" ======\n");
		buffer.append("        >> motorSet ("+this.size()+ " motors)\n");
		MotorInstance emptyInstance = this.getDefault();
		buffer.append("              >> (["+emptyInstance.toString()+"]=  @ "+ emptyInstance.getIgnitionEvent().name +"  +"+emptyInstance.getIgnitionDelay()+"sec )\n");
		
		for( FlightConfigurationID loopFCID : this.map.keySet()){
			String shortKey = loopFCID.getShortKey();
			
			MotorInstance curInstance = this.map.get(loopFCID);
			String designation;
			if( MotorInstance.EMPTY_INSTANCE == curInstance){
				designation = "EMPTY_INSTANCE";
			}else{
				designation = curInstance.getMotor().getDesignation(curInstance.getEjectionDelay());
			}
			String ignition = curInstance.getIgnitionEvent().name;
			double delay = curInstance.getIgnitionDelay();
			if( 0 != delay ){
				ignition += " +"+delay;
			}
			buffer.append("              >> ["+shortKey+"]= "+designation+"  @ "+ignition+"\n");
		}
		return buffer.toString();
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
