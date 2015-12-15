package net.sf.openrocket.simulation;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.motor.MotorInstanceId;
import net.sf.openrocket.rocketcomponent.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;

public interface MotorState {

	public void step(double nextTime, double acceleration, AtmosphericConditions cond);
	public double getThrust();
	public boolean isActive();

	public double getIgnitionTime();
	public void setIgnitionTime( double ignitionTime );
	
	public void setMount(MotorMount mount);
	public MotorMount getMount();

	public void setId(MotorInstanceId id);
	public MotorInstanceId getID();
	
	public IgnitionEvent getIgnitionEvent();
	public void setIgnitionEvent( IgnitionEvent event );
	
	public double getIgnitionDelay();
	public void setIgnitionDelay( double delay );

	public double getEjectionDelay();
	public void setEjectionDelay( double delay);
}
