package net.sf.openrocket.plugin.example;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class ExampleSingletonPlugin implements ExamplePluginInterface {
	
	@Override
	public void print() {
		System.out.println("ExampleSingletonPlugin");
	}
	
}
