package net.sf.openrocket.plugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.util.BugException;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.JSPFProperties;
import net.xeoh.plugins.base.util.PluginManagerUtil;

public class JSPFPluginFactory implements PluginFactory {
	
	private final PluginManager pluginManager;
	
	public JSPFPluginFactory() {
		
		final JSPFProperties props = new JSPFProperties();
		
		//		props.setProperty(PluginManager.class, "cache.enabled", "true");
		//		props.setProperty(PluginManager.class, "cache.mode", "weak"); //optional
		//		props.setProperty(PluginManager.class, "cache.file", "jspf.cache");
		
		try {
			pluginManager = PluginManagerFactory.createPluginManager(props);
			pluginManager.addPluginsFrom(new URI("classpath://*"));
		} catch (URISyntaxException e) {
			throw new BugException(e);
		}
	}
	
	@Override
	public <E extends Plugin> List<E> getPlugins(Class<E> e, Object... args) {
		
		List<E> plugins = new ArrayList<E>();
		
		PluginManagerUtil pluginManagerUtil = new PluginManagerUtil(pluginManager);
		plugins.addAll(pluginManagerUtil.getPlugins(e));
		
		for (Service s : pluginManagerUtil.getPlugins(Service.class)) {
			plugins.addAll(s.getPlugins(e, args));
		}
		
		return plugins;
		
	}
}
