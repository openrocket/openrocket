package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

public class Stage extends ComponentAssembly {
	
	private static final Translator trans = Application.getTranslator();
	
	
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
			return trans.get(description);
		}
	};
	
	
	private SeparationEvent separationEvent = SeparationEvent.UPPER_IGNITION;
	private double separationDelay = 0;
	
	
	@Override
	public String getComponentName() {
		//// Stage
		return trans.get("Stage.Stage");
	}
	
	
	public SeparationEvent getSeparationEvent() {
		return separationEvent;
	}
	
	
	public void setSeparationEvent(SeparationEvent separationEvent) {
		if (separationEvent == this.separationEvent)
			return;
		this.separationEvent = separationEvent;
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	
	public double getSeparationDelay() {
		return separationDelay;
	}
	
	
	public void setSeparationDelay(double separationDelay) {
		if (MathUtil.equals(separationDelay, this.separationDelay))
			return;
		this.separationDelay = separationDelay;
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	
	
	@Override
	public boolean allowsChildren() {
		return true;
	}
	
	/**
	 * Check whether the given type can be added to this component.  A Stage allows
	 * only BodyComponents to be added.
	 *
	 * @param type The RocketComponent class type to add.
	 *
	 * @return Whether such a component can be added.
	 */
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return BodyComponent.class.isAssignableFrom(type);
	}
}
