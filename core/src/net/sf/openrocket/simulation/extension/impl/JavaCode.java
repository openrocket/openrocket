package net.sf.openrocket.simulation.extension.impl;

import com.google.inject.ConfigurationException;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.util.StringUtil;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class JavaCode extends AbstractSimulationExtension {
	
	@Inject
	private Injector injector;
	
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		String className = getClassName();
		try {
			if (!StringUtil.isEmpty(className)) {
				Class<?> clazz = Class.forName(className);
				if (!SimulationListener.class.isAssignableFrom(clazz)) {
					throw new SimulationException("Class " + className + " does not implement SimulationListener");
				}
				try {
					SimulationListener listener = (SimulationListener) injector.getInstance(clazz);
					conditions.getSimulationListenerList().add(listener);
				} catch (ConfigurationException e) {
					throw new SimulationException(String.format(trans.get("SimulationExtension.javacode.couldnotinstantiate"), className), e);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new SimulationException(trans.get("SimulationExtension.javacode.classnotfound") + " " + className);
		}
	}
	
	@Override
	public String getName() {
		String name = trans.get("SimulationExtension.javacode.name") + ": ";
		String className = getClassName();
		if (!StringUtil.isEmpty(className)) {
			name = name + className;
		} else {
			name = name + trans.get("SimulationExtension.javacode.name.none");
		}
		return name;
	}
	
	public String getClassName() {
		return config.getString("className", "");
	}
	
	public void setClassName(String className) {
		config.put("className", className);
		fireChangeEvent();
	}
	
}
