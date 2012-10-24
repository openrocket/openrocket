package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.simulation.FlightEvent;

public class StageSeparationConfiguration {

	private StageSeparationConfiguration.SeparationEvent separationEvent = StageSeparationConfiguration.SeparationEvent.UPPER_IGNITION;
	private double separationDelay = 0;

	public static enum SeparationEvent {
		//// Upper stage motor ignition
		UPPER_IGNITION("Stage.SeparationEvent.UPPER_IGNITION") {
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
		IGNITION("Stage.SeparationEvent.IGNITION") {
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
		BURNOUT("Stage.SeparationEvent.BURNOUT") {
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
		EJECTION("Stage.SeparationEvent.EJECTION") {
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
		LAUNCH("Stage.SeparationEvent.LAUNCH") {
			@Override
			public boolean isSeparationEvent(FlightEvent e, Stage stage) {
				return e.getType() == FlightEvent.Type.LAUNCH;
			}
		},
		//// Never
		NEVER("Stage.SeparationEvent.NEVER") {
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
			return Stage.trans.get(description);
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

}
