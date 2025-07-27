package info.openrocket.core.motor;

import java.util.Locale;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;

public enum IgnitionEvent {

	//// Automatic (launch or ejection charge)
	AUTOMATIC("AUTOMATIC", "MotorMount.IgnitionEvent.AUTOMATIC") {
		@Override
		public boolean isActivationEvent(FlightConfiguration config, FlightEvent testEvent,
				RocketComponent targetComponent) {
			AxialStage targetStage = targetComponent.getStage();

			if (targetStage.isLaunchStage(config)) {
				return LAUNCH.isActivationEvent(config, testEvent, targetComponent);
			} else {
				return EJECTION_CHARGE.isActivationEvent(config, testEvent, targetComponent);
			}
		}
	},
	LAUNCH("LAUNCH", "MotorMount.IgnitionEvent.LAUNCH") {
		@Override
		public boolean isActivationEvent(FlightConfiguration config, FlightEvent fe, RocketComponent source) {
			return (fe.getType() == FlightEvent.Type.LAUNCH);
		}
	},
	EJECTION_CHARGE("EJECTION_CHARGE", "MotorMount.IgnitionEvent.EJECTION_CHARGE") {
		@Override
		public boolean isActivationEvent(FlightConfiguration config, FlightEvent testEvent,
				RocketComponent targetComponent) {
			if (testEvent.getType() != FlightEvent.Type.EJECTION_CHARGE) {
				return false;
			}

			AxialStage targetStage = targetComponent.getStage();
			AxialStage eventStage = testEvent.getSource().getStage();
			AxialStage eventParentStage = eventStage.getUpperStage();
			return (targetStage.equals(eventParentStage));
		}
	},
	BURNOUT("BURNOUT", "MotorMount.IgnitionEvent.BURNOUT") {
		@Override
		public boolean isActivationEvent(FlightConfiguration config, FlightEvent testEvent,
				RocketComponent targetComponent) {
			if (testEvent.getType() != FlightEvent.Type.BURNOUT)
				return false;

			AxialStage targetStage = targetComponent.getStage();
			AxialStage eventStage = testEvent.getSource().getStage();
			AxialStage eventParentStage = eventStage.getUpperStage();
			return (targetStage.equals(eventParentStage));
		}
	},
	NEVER("NEVER", "MotorMount.IgnitionEvent.NEVER");

	private static final Translator trans = Application.getTranslator();
	public final String name;
	private final String translationKey;
	protected String description = null;

	// public static final IgnitionEvent[] events = {AUTOMATIC, LAUNCH,
	// EJECTION_CHARGE, BURNOUT, NEVER};

	public boolean isActivationEvent(FlightConfiguration config, FlightEvent fe, RocketComponent source) {
		// default behavior. Also for the NEVER case.
		return false;
	}

	private IgnitionEvent(final String _name, final String _key) {
		this.name = _name;
		this.translationKey = _key;
	}

	public boolean equals(final String content) {
		String comparator = this.name.toLowerCase(Locale.ENGLISH).replaceAll("_", "");
		return comparator.equals(content);
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		if (null == this.description) {
			this.description = trans.get(this.translationKey);
		}
		return this.description;
	}

}
