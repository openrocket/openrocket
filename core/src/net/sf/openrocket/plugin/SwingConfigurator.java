package net.sf.openrocket.plugin;

import java.awt.Component;

import net.xeoh.plugins.base.Plugin;

/**
 * Interface that defined a Swing configurator for a plugin.
 * The implemeting class should be a plugin that provides the
 * capability "<pluginID>:config" where <pluginID> is the
 * plugin ID of the plugin to configure.
 * <p>
 * 
 * @param <P> The plugin class that is being configured
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface SwingConfigurator<P> extends Plugin {
	
	/**
	 * Return whether this plugin is configurable or not.
	 * 
	 * @param plugin	the plugin to test.
	 * @return			whether the plugin has a configuration component.
	 */
	public boolean isConfigurable(P plugin);
	
	/**
	 * Return the configuration component for configuring the
	 * provided plugin.
	 * 
	 * @param plugin	the plugin to configure.
	 * @return			a Swing component for configuring the plugin.
	 */
	public Component getConfigurationComponent(P plugin);
	
	
}
