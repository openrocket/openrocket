package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class StageSeparationConfiguration implements FlightConfigurableParameter<StageSeparationConfiguration> {
	
	public static enum SeparationEvent {
		//// Upper stage motor ignition
		UPPER_IGNITION(trans.get("Stage.SeparationEvent.UPPER_IGNITION")) {
			@Override
			public boolean isSeparationEvent(FlightEvent e, AxialStage stage) {
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
			public boolean isSeparationEvent(FlightEvent e, AxialStage stage) {
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
			public boolean isSeparationEvent(FlightEvent e, AxialStage stage) {
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
			public boolean isSeparationEvent(FlightEvent e, AxialStage stage) {
				if (e.getType() != FlightEvent.Type.EJECTION_CHARGE)
					return false;
				
				int ignition = e.getSource().getStageNumber();
				int mount = stage.getStageNumber();
				return (mount == ignition);
			}
		},
		NEVER(trans.get("Stage.SeparationEvent.NEVER")) {
			@Override
			public boolean isSeparationEvent(FlightEvent e, AxialStage stage) {
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
		public abstract boolean isSeparationEvent(FlightEvent e, AxialStage stage);
		
		@Override
		public String toString() {
			return description;
		}
	}
	
	
	private static final Translator trans = Application.getTranslator();
	
	
	private SeparationEvent separationEvent = SeparationEvent.EJECTION;
	private double separationDelay = 0;

	private final List<StageSeparationConfiguration> configListeners = new LinkedList<>();
		
	public SeparationEvent getSeparationEvent() {
		return separationEvent;
	}
	
	public void setSeparationEvent(SeparationEvent separationEvent) {
		for (StageSeparationConfiguration listener : configListeners) {
			listener.setSeparationEvent(separationEvent);
		}

		if (separationEvent == null) {
			throw new NullPointerException("separationEvent is null");
		}
		if (this.separationEvent == separationEvent) {
			return;
		}
		this.separationEvent = separationEvent;
		fireChangeEvent();
	}
	
	public double getSeparationDelay() {
		return separationDelay;
	}
	
	public void setSeparationDelay(double separationDelay) {
		for (StageSeparationConfiguration listener : configListeners) {
			listener.setSeparationDelay(separationDelay);
		}

		if (MathUtil.equals(this.separationDelay, separationDelay)) {
			return;
		}
		this.separationDelay = separationDelay;
		fireChangeEvent();
	}
	
	@Override
	public String toString() {
		if (separationDelay > 0) {
			return separationEvent.toString() + " + " + separationDelay + "s";
		} else {
			return separationEvent.toString();
		}
	}
	
	@Override
	public StageSeparationConfiguration clone() {
        return copy(null);
    }

    public StageSeparationConfiguration copy( final FlightConfigurationId copyId){
		StageSeparationConfiguration clone = new StageSeparationConfiguration();
		clone.separationEvent = this.separationEvent;
		clone.separationDelay = this.separationDelay;
		return clone;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StageSeparationConfiguration that = (StageSeparationConfiguration) o;
		return Double.compare(that.separationDelay, separationDelay) == 0 && separationEvent == that.separationEvent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(separationEvent, separationDelay);
	}

	private void fireChangeEvent() {

	}
	

	@Override
	public void update(){
	}

	/**
	 * Add a new config listener that will undergo the same configuration changes as this configuration.
	 * @param listener new config listener
	 * @return true if listener was successfully added, false if not
	 */
	public boolean addConfigListener(StageSeparationConfiguration listener) {
		if (listener == null) {
			return false;
		}
		configListeners.add(listener);
		return true;
	}

	public void removeConfigListener(StageSeparationConfiguration listener) {
		configListeners.remove(listener);
	}

	public void clearConfigListeners() {
		configListeners.clear();
	}

	public List<StageSeparationConfiguration> getConfigListeners() {
		return configListeners;
	}
	
}
