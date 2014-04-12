package net.sf.openrocket.simulation.extension.impl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.StringUtil;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class JavaCode extends AbstractSimulationExtension {
	
	@Inject
	private Injector injector;
	
	public JavaCode() {
		config.put("my_string", "foobar");
		config.put("my_int", 123);
		config.put("my_long", 123456789012345L);
		config.put("my_float", 12.345f);
		config.put("my_double", 12.345e99);
		config.put("my_bigint", new BigInteger("12345678901234567890", 10));
		config.put("my_bool", true);
		List<Object> list = new ArrayList<Object>();
		list.add(true);
		list.add(123);
		list.add(123.456);
		list.add(Arrays.asList(1, 2, 3));
		list.add("foo");
		config.put("my_list", list);
	}
	
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		String className = getClassName();
		try {
			if (!StringUtil.isEmpty(className)) {
				Class<?> clazz = Class.forName(className);
				if (!SimulationListener.class.isAssignableFrom(clazz)) {
					throw new SimulationException("Class " + className + " does not implement SimulationListener");
				}
				SimulationListener listener = (SimulationListener) injector.getInstance(clazz);
				conditions.getSimulationListenerList().add(listener);
			}
		} catch (ClassNotFoundException e) {
			throw new SimulationException("Could not find class " + className);
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
