package net.sf.openrocket.rocketcomponent;

import java.util.EventObject;
import java.util.List;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

public class StageSeparationConfiguration implements FlightConfigurableParameter<StageSeparationConfiguration> {
	
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
	
	
	private static final Translator trans = Application.getTranslator();
	
	private final List<StateChangeListener> listeners = new ArrayList<StateChangeListener>();
	
	private SeparationEvent separationEvent = SeparationEvent.UPPER_IGNITION;
	private double separationDelay = 0;
	
	
	public SeparationEvent getSeparationEvent() {
		return separationEvent;
	}
	
	public void setSeparationEvent(SeparationEvent separationEvent) {
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
		StageSeparationConfiguration clone = new StageSeparationConfiguration();
		clone.separationEvent = this.separationEvent;
		clone.separationDelay = this.separationDelay;
		return clone;
	}
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}
	
	private void fireChangeEvent() {
		EventObject event = new EventObject(this);
		Object[] list = listeners.toArray();
		for (Object l : list) {
			((StateChangeListener) l).stateChanged(event);
		}
	}
	
}
