package net.sf.openrocket.plugin;

import java.util.List;

import net.xeoh.plugins.base.Plugin;

public interface PluginFactory {
	
	public <E extends Plugin> List<E> getPlugins(Class<E> e, Object... args);
	
}
