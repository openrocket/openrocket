package net.sf.openrocket.motor;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FlightConfigurableParameterSet;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * FlightConfigurationSet for motors.
 * This is used for motors, where the default value is always no motor.
 */
public class MotorConfigurationSet extends FlightConfigurableParameterSet<MotorConfiguration> {
	public static final int DEFAULT_MOTOR_EVENT_TYPE = ComponentChangeEvent.MOTOR_CHANGE | ComponentChangeEvent.EVENT_CHANGE;
	
	public MotorConfigurationSet(final MotorMount mount ) {
		super( new MotorConfiguration( mount, FlightConfigurationId.DEFAULT_VALUE_FCID ));
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
		final MotorMount mnt = this.getDefault().getMount();
		buffer.append("====== Dumping MotorConfigurationSet: "+this.size()+ " motors in "+mnt.getDebugName()+" \n");
		
		for( FlightConfigurationId loopFCID : this.map.keySet()){
			MotorConfiguration curConfig = this.map.get(loopFCID);
			if( this.isDefault(loopFCID)){
				buffer.append( " [DEFAULT] "+curConfig.toDebugDetail()+"\n");
			}else{
				buffer.append( "           "+curConfig.toDebugDetail() +"\n");
			}
		}
		return buffer.toString();
	}

	
}
