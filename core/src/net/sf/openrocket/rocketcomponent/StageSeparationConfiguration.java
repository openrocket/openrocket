package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;

public class StageSeparationConfiguration implements Cloneable {

	private static final Translator trans = Application.getTranslator();

	private StageSeparationConfiguration.SeparationEvent separationEvent = StageSeparationConfiguration.SeparationEvent.UPPER_IGNITION;
	private double separationDelay = 0;

	public static enum SeparationEvent {
		//// Upper stage motor ignition
		UPPER_IGNITION(trans.get("Stage.SeparationEvent.UPPER_IGNITION")) {
			@Override
			public boolean isSeparationEvent(FlightEvent e, Stage stage) {
				if (e.getType() != FlightEvent.Type.IGNITION)
					return false;
				
				int ignition = e.getSource().getStageNumber();
				int mount = stage.getStageNumber();
				return (mount == ignition + 1);
			}
		},
		//// Current stage motor ignition
		IGNITION(trans.get("Stage.SeparationEvent.IGNITION")) {
			@Override
			public boolean isSeparationEvent(FlightEvent e, Stage stage) {
				if (e.getType() != FlightEvent.Type.IGNITION)
					return false;
				
				int ignition = e.getSource().getStageNumber();
				int mount = stage.getStageNumber();
				return (mount == ignition);
			}
		},
		//// Current stage motor burnout
		BURNOUT(trans.get("Stage.SeparationEvent.BURNOUT")) {
			@Override
			public boolean isSeparationEvent(FlightEvent e, Stage stage) {
				if (e.getType() != FlightEvent.Type.BURNOUT)
					return false;
				
				int ignition = e.getSource().getStageNumber();
				int mount = stage.getStageNumber();
				return (mount == ignition);
			}
		},
		//// Current stage ejection charge
		EJECTION(trans.get("Stage.SeparationEvent.EJECTION")) {
			@Override
			public boolean isSeparationEvent(FlightEvent e, Stage stage) {
				if (e.getType() != FlightEvent.Type.EJECTION_CHARGE)
					return false;
				
				int ignition = e.getSource().getStageNumber();
				int mount = stage.getStageNumber();
				return (mount == ignition);
			}
		},
		//// Launch
		LAUNCH(trans.get("Stage.SeparationEvent.LAUNCH")) {
			@Override
			public boolean isSeparationEvent(FlightEvent e, Stage stage) {
				return e.getType() == FlightEvent.Type.LAUNCH;
			}
		},
		//// Never
		NEVER(trans.get("Stage.SeparationEvent.NEVER")) {
			@Override
			public boolean isSeparationEvent(FlightEvent e, Stage stage) {
				return false;
			}
		},
		;
		
		
		private final String description;
		
		SeparationEvent(String description) {
			this.description = description;
		}
		
		/**
		 * Test whether a specific event is a stage separation event.
		 */
		public abstract boolean isSeparationEvent(FlightEvent e, Stage stage);
		
		@Override
		public String toString() {
			return description;
		}
	}

	public StageSeparationConfiguration.SeparationEvent getSeparationEvent() {
		return separationEvent;
	}

	public void setSeparationEvent(
			StageSeparationConfiguration.SeparationEvent separationEvent) {
		this.separationEvent = separationEvent;
	}

	public double getSeparationDelay() {
		return separationDelay;
	}

	public void setSeparationDelay(double separationDelay) {
		this.separationDelay = separationDelay;
	}

	@Override
	public String toString() {
		if ( separationDelay > 0 ) {
			return separationEvent.toString() + " +" + separationDelay + "s";
		} else {
			return separationEvent.toString();
		}
	}

	@Override
	public StageSeparationConfiguration clone() {
		StageSeparationConfiguration clone = new StageSeparationConfiguration();
		clone.separationEvent = this.separationEvent;
		clone.separationDelay = this.separationDelay;
		return clone;
	}

}
