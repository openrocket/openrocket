package net.sf.openrocket.plugin.example;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.plugin.Configurable;
import net.sf.openrocket.plugin.SwingConfigurator;
import net.sf.openrocket.plugin.framework.Service;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.util.BugException;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.annotations.Capabilities;

public abstract class OpenRocketSimulationListener extends AbstractSimulationListener
		implements Plugin, Service, SwingConfigurator<OpenRocketSimulationListener>, Configurable {
	
	private final String[] ids;
	private final String[] capabilities;
	
	public OpenRocketSimulationListener(String... ids) {
		if (ids.length == 0) {
			ids = new String[] { this.getClass().getCanonicalName() };
		}
		
		this.ids = ids.clone();
		this.capabilities = new String[ids.length * 2];
		for (int i = 0; i < ids.length; i++) {
			capabilities[i * 2] = ids[i] + ":service";
			capabilities[i * 2 + 1] = ids[i] + ":config";
		}
		
	}
	
	@Capabilities
	public String[] capabilities() {
		return capabilities.clone();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E> List<E> getPlugins(Class<E> e, Object... args) {
		if (e != this.getClass()) {
			throw new BugException("Attempting to get plugin of type " + e + " but I am of type " + this.getClass());
		}
		try {
			return (List<E>) Arrays.asList(this.getClass().newInstance());
		} catch (IllegalAccessException e1) {
			throw new BugException("Could not instantiate new object of type " + this.getClass(), e1);
		} catch (InstantiationException e2) {
			throw new BugException("Could not instantiate new object of type " + this.getClass(), e2);
		}
	}
	
	
	@Override
	public boolean isConfigurable(OpenRocketSimulationListener plugin) {
		return plugin.isConfigurable();
	}
	
	@Override
	public Component getConfigurationComponent(OpenRocketSimulationListener plugin) {
		return plugin.getConfigurationComponent();
	}
	
	public abstract boolean isConfigurable();
	
	public abstract Component getConfigurationComponent();
	
	
	
	@Override
	public String getPluginID() {
		return ids[0];
	}
	
	@Override
	public boolean isCompatible(String pluginID) {
		for (String id : ids) {
			if (pluginID.equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	
}
