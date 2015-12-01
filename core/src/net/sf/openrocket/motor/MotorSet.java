package net.sf.openrocket.motor;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationID;
import net.sf.openrocket.rocketcomponent.ParameterSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * FlightConfigurationSet for motors.
 * This is used for motors, where the default value is always no motor.
 */
public class MotorSet extends ParameterSet<MotorInstance> {
	public static final int DEFAULT_MOTOR_EVENT_TYPE = ComponentChangeEvent.MOTOR_CHANGE | ComponentChangeEvent.EVENT_CHANGE;
	
	public MotorSet(RocketComponent component ) {
		super(component, DEFAULT_MOTOR_EVENT_TYPE, MotorInstance.EMPTY_INSTANCE);
	}
	
	/**
	 * Construct a copy of an existing FlightConfigurationSet.
	 * 
	 * @param flightConfiguration another flightConfiguration to copy data from.
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public MotorSet(ParameterSet<MotorInstance> flightConfiguration, RocketComponent component) {
		super(flightConfiguration, component, DEFAULT_MOTOR_EVENT_TYPE);
	}
	
	@Override
	public void setDefault( MotorInstance value) {
		throw new UnsupportedOperationException("Cannot change default value of motor configuration");
	}
	
	@Override
	public String toDebug(){
		StringBuilder buffer = new StringBuilder();
		buffer.append("====== Dumping MotorConfigurationSet for mount '"+this.component.toDebugName()+" ======\n");
		buffer.append("        >> motorSet ("+this.size()+ " motors)\n");
		MotorInstance emptyInstance = this.getDefault();
		buffer.append("              >> (["+emptyInstance.toString()+"]=  @ "+ emptyInstance.getIgnitionEvent().name +"  +"+emptyInstance.getIgnitionDelay()+"sec )\n");
		
		for( FlightConfigurationID loopFCID : this.map.keySet()){
			String shortKey = loopFCID.toShortKey();
			
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

	
}
