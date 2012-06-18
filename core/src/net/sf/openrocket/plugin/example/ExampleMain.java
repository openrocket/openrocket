package net.sf.openrocket.plugin.example;

import net.sf.openrocket.plugin.framework.JSPFPluginFactory;
import net.sf.openrocket.plugin.framework.PluginFactory;

public class ExampleMain {
	
	
	public static void main(String[] args) {
		PluginFactory factory = new JSPFPluginFactory();
		
		System.out.println("Plugins:");
		System.out.println("---------");
		for (ExamplePluginInterface plugin : factory.getPlugins(ExamplePluginInterface.class)) {
			plugin.print();
		}
		System.out.println("---------");
	}
	
	
}
