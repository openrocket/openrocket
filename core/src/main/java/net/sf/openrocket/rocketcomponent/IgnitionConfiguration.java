package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.motor.IgnitionEvent;

public class IgnitionConfiguration implements FlightConfigurableParameter<IgnitionConfiguration> {

	protected double ignitionDelay = 0.0;
	protected IgnitionEvent ignitionEvent = IgnitionEvent.NEVER;
	protected double ignitionTime = 0.0;

	public double getIgnitionDelay() {
		return ignitionDelay;
	}

	public void setIgnitionDelay(double ignitionDelay) {
		this.ignitionDelay = ignitionDelay;
	}

	public IgnitionEvent getIgnitionEvent() {
		return ignitionEvent;
	}

	public void setIgnitionEvent(IgnitionEvent ignitionEvent) {
		this.ignitionEvent = ignitionEvent;
	}

	public double getIgnitionTime() {
		return ignitionTime;
	}

	public void setIgnitionTime(double ignitionTime) {
		this.ignitionTime = ignitionTime;
	}

	@Override
	public IgnitionConfiguration clone() {
        return this.copy(null);
    }

    public IgnitionConfiguration copy( final FlightConfigurationId copyId) {
        IgnitionConfiguration clone = new IgnitionConfiguration();
		clone.ignitionDelay = this.ignitionDelay;
		clone.ignitionEvent = this.ignitionEvent;
		clone.ignitionTime = this.ignitionTime;
		return clone;
    }

	@Override
	public void update(){
	}

	
}
