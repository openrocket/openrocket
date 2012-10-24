package net.sf.openrocket.rocketcomponent;

import java.util.HashMap;
import java.util.Map;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

public class Stage extends ComponentAssembly implements FlightConfigurable<StageSeparationConfiguration> {
	
	static final Translator trans = Application.getTranslator();
	
	private StageSeparationConfiguration defaultConfiguration = new StageSeparationConfiguration();
	private final Map<String,StageSeparationConfiguration> configuration = new HashMap<String,StageSeparationConfiguration>();
	
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


	@Override
	public StageSeparationConfiguration getFlightConfiguration(String configId) {
		return configuration.get(configId);
	}


	@Override
	public void setFlightConfiguration(String configId,	StageSeparationConfiguration config) {
		if ( config == null ) {
			configuration.remove(configId);
		} else {
			configuration.put(configId,config);
		}
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
	}


	@Override
	public void cloneFlightConfiguration(String oldConfigId, String newConfigId) {
		StageSeparationConfiguration oldConfig = configuration.get(oldConfigId);
		if ( oldConfig == null ) {
			configuration.remove(newConfigId);
		} else {
			StageSeparationConfiguration newConfig = oldConfig.clone();
			configuration.put(newConfigId, newConfig);
		}
		
	}


	@Override
	public StageSeparationConfiguration getDefaultFlightConfiguration() {
		return defaultConfiguration;
	}


	@Override
	public void setDefaultFlightConfiguration(StageSeparationConfiguration config) {
		this.defaultConfiguration = config;
		fireComponentChangeEvent(ComponentChangeEvent.EVENT_CHANGE);
		
	}


	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
	
}
