package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;

public class MotorConfiguration implements Cloneable {

	private MotorConfiguration.IgnitionEvent ignitionEvent;
	private Double ignitionDelay;
	private Motor motor = null;
	private Double ejectionDelay = 0d;
	
	/**
	 * Factory method which constructs a MotorConfiguration object which is suitable for
	 * use as the defaults for the MotorMount.  In particular, it has Automatic ignitionEvent
	 * and 0 ignitionDelay.

	 * @return
	 */
	static MotorConfiguration makeDefaultMotorConfiguration() {
		MotorConfiguration defaults = new MotorConfiguration();
		defaults.ignitionDelay = 0d;
		defaults.ignitionEvent = MotorConfiguration.IgnitionEvent.AUTOMATIC;
		return defaults;
	}
	

	/**
	 * Construct a MotorConfiguration object which is suitable for use by per flight configuration
	 * scenarios.  ignitionEvent and ignitionDelay are null to indicate that one should rely on the
	 * default value.
	 */
	MotorConfiguration() {
	}
	
	public MotorConfiguration.IgnitionEvent getIgnitionEvent() {
		return ignitionEvent;
	}

	public void setIgnitionEvent(MotorConfiguration.IgnitionEvent ignitionEvent) {
		this.ignitionEvent = ignitionEvent;
	}

	public Double getIgnitionDelay() {
		return ignitionDelay;
	}

	public void setIgnitionDelay(Double ignitionDelay) {
		this.ignitionDelay = ignitionDelay;
	}

	public Motor getMotor() {
		return motor;
	}

	public void setMotor(Motor motor) {
		this.motor = motor;
	}

	public Double getEjectionDelay() {
		return ejectionDelay;
	}

	public void setEjectionDelay(Double ejectionDelay) {
		this.ejectionDelay = ejectionDelay;
	}

	@Override
	protected MotorConfiguration clone() {
		MotorConfiguration clone = new MotorConfiguration();
		clone.motor = motor;
		clone.ejectionDelay = ejectionDelay;
		clone.ignitionDelay = ignitionDelay;
		clone.ignitionEvent = ignitionEvent;
		return clone;
	}



	public static enum IgnitionEvent {
		//// Automatic (launch or ejection charge)
		AUTOMATIC("MotorMount.IgnitionEvent.AUTOMATIC") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				int count = source.getRocket().getStageCount();
				int stage = source.getStageNumber();
				
				if (stage == count - 1) {
					return LAUNCH.isActivationEvent(e, source);
				} else {
					return EJECTION_CHARGE.isActivationEvent(e, source);
				}
			}
		},
		//// Launch
		LAUNCH("MotorMount.IgnitionEvent.LAUNCH") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				return (e.getType() == FlightEvent.Type.LAUNCH);
			}
		},
		//// First ejection charge of previous stage
		EJECTION_CHARGE("MotorMount.IgnitionEvent.EJECTION_CHARGE") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.EJECTION_CHARGE)
					return false;
				
				int charge = e.getSource().getStageNumber();
				int mount = source.getStageNumber();
				return (mount + 1 == charge);
			}
		},
		//// First burnout of previous stage
		BURNOUT("MotorMount.IgnitionEvent.BURNOUT") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				if (e.getType() != FlightEvent.Type.BURNOUT)
					return false;
				
				int charge = e.getSource().getStageNumber();
				int mount = source.getStageNumber();
				return (mount + 1 == charge);
			}
		},
		//// Never
		NEVER("MotorMount.IgnitionEvent.NEVER") {
			@Override
			public boolean isActivationEvent(FlightEvent e, RocketComponent source) {
				return false;
			}
		},
		;
		
	
		private static final Translator trans = Application.getTranslator();
		private final String description;
		
		IgnitionEvent(String description) {
			this.description = description;
		}
		
		public abstract boolean isActivationEvent(FlightEvent e, RocketComponent source);
		
		@Override
		public String toString() {
			return trans.get(description);
		}
	}

}
