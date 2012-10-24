package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

public class Stage extends ComponentAssembly {
	
	static final Translator trans = Application.getTranslator();
	
	private final StageSeparationConfiguration defaultConfiguration = new StageSeparationConfiguration();
	
	@Override
	public String getComponentName() {
		//// Stage
		return trans.get("Stage.Stage");
	}
	
	
	public StageSeparationConfiguration.SeparationEvent getDefaultSeparationEvent() {
		return defaultConfiguration.getSeparationEvent();
	}
	
	
	public void setDefaultSeparationEvent(StageSeparationConfiguration.SeparationEvent separationEvent) {
		if (separationEvent == defaultConfiguration.getSeparationEvent())
			return;
		defaultConfiguration.setSeparationEvent(separationEvent);
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}
	
	
	public double getDefaultSeparationDelay() {
		return defaultConfiguration.getSeparationDelay();
	}
	
	
	public void setDefaultSeparationDelay(double separationDelay) {
		if (MathUtil.equals(separationDelay, defaultConfiguration.getSeparationDelay()))
			return;
		defaultConfiguration.setSeparationDelay(separationDelay);
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
