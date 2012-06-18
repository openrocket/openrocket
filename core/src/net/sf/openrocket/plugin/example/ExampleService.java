package net.sf.openrocket.plugin.example;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.plugin.framework.AbstractService;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class ExampleService extends AbstractService<ExamplePluginInterface> {
	
	protected ExampleService() {
		super(ExamplePluginInterface.class);
	}
	
	@Override
	protected List<ExamplePluginInterface> getPlugins(Object... args) {
		List<ExamplePluginInterface> plugins = new ArrayList<ExamplePluginInterface>();
		plugins.add(new ExamplePlugin("a"));
		plugins.add(new ExamplePlugin("b"));
		return plugins;
	}
	
}
