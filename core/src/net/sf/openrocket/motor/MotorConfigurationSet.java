package net.sf.openrocket.motor;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.FlightConfigurableParameterSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * FlightConfigurationSet for motors.
 * This is used for motors, where the default value is always no motor.
 */
public class MotorConfigurationSet extends FlightConfigurableParameterSet<MotorConfiguration> {
	public static final int DEFAULT_MOTOR_EVENT_TYPE = ComponentChangeEvent.MOTOR_CHANGE | ComponentChangeEvent.EVENT_CHANGE;
	
	public MotorConfigurationSet(RocketComponent component ) {
		super( new MotorConfiguration());
	}
	
	/**
	 * Construct a copy of an existing FlightConfigurationSet.
	 * 
	 * @param configSet another flightConfiguration to copy data from.
	 * @param component		the rocket component on which events are fired when the parameter values are changed
	 * @param eventType		the event type that will be fired on changes
	 */
	public MotorConfigurationSet(FlightConfigurableParameterSet<MotorConfiguration> configSet, RocketComponent component) {
		super(configSet);
	}
	
	@Override
	public void setDefault( MotorConfiguration value) {
		throw new UnsupportedOperationException("Cannot change default value of motor configuration");
	}
	
	@Override
	public String toDebug(){
		StringBuilder buffer = new StringBuilder();
		buffer.append("====== Dumping MotorConfigurationSet for mount ("+this.size()+ " motors)\n");
		MotorConfiguration emptyInstance = this.getDefault();
		buffer.append("              >> (["+emptyInstance.toString()+"]=  @ "+ emptyInstance.getIgnitionEvent().name +"  +"+emptyInstance.getIgnitionDelay()+"sec )\n");
		
		for( FlightConfigurationId loopFCID : this.map.keySet()){
			String shortKey = loopFCID.toShortKey();
			
			MotorConfiguration curInstance = this.map.get(loopFCID);
			String designation;
			if( null == curInstance.getMotor() ){
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
