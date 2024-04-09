package info.openrocket.core.simulation.extension.impl;

import com.google.inject.ConfigurationException;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.extension.AbstractSimulationExtension;
import info.openrocket.core.simulation.listeners.SimulationListener;
import info.openrocket.core.util.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class JavaCode extends AbstractSimulationExtension {

	@Inject
	private Injector injector;

	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		String className = getClassName();
		try {
			if (!StringUtils.isEmpty(className)) {
				Class<?> clazz = Class.forName(className);
				if (!SimulationListener.class.isAssignableFrom(clazz)) {
					throw new SimulationException("Class " + className + " does not implement SimulationListener");
				}
				try {
					SimulationListener listener = (SimulationListener) injector.getInstance(clazz);
					conditions.getSimulationListenerList().add(listener);
				} catch (ConfigurationException e) {
					throw new SimulationException(
							String.format(trans.get("SimulationExtension.javacode.couldnotinstantiate"), className), e);
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
		if (!StringUtils.isEmpty(className)) {
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
