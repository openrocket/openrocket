package net.sf.openrocket.plugin;

import java.util.List;

import net.xeoh.plugins.base.Plugin;

public interface Service extends Plugin {
	
	
	public <E> List<E> getPlugins(Class<E> e, Object... args);
	
	
}
