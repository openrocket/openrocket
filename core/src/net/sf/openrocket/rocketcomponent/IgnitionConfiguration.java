package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

public class IgnitionConfiguration implements FlightConfigurableParameter<IgnitionConfiguration> {
	
	public enum IgnitionEvent {
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
	
	
	private IgnitionEvent ignitionEvent = IgnitionEvent.AUTOMATIC;
	private double delay = 0;
	
	private final AbstractChangeSource listeners = new AbstractChangeSource();
	
	
	
	public IgnitionEvent getIgnitionEvent() {
		return ignitionEvent;
	}
	
	public void setIgnitionEvent(IgnitionEvent ignitionEvent) {
		if (ignitionEvent == null) {
			throw new NullPointerException("ignitionEvent is null");
		}
		if (ignitionEvent == this.ignitionEvent) {
			return;
		}
		this.ignitionEvent = ignitionEvent;
		listeners.fireChangeEvent(this);
	}
	
	
	public double getIgnitionDelay() {
		return delay;
	}
	
	public void setIgnitionDelay(double delay) {
		if (MathUtil.equals(delay, this.delay)) {
			return;
		}
		this.delay = delay;
		listeners.fireChangeEvent(this);
	}
	
	@Override
	public IgnitionConfiguration clone() {
		IgnitionConfiguration copy = new IgnitionConfiguration();
		copy.ignitionEvent = this.ignitionEvent;
		copy.delay = this.delay;
		return copy;
	}
	
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.addChangeListener(listener);
	}
	
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.removeChangeListener(listener);
	}
}
