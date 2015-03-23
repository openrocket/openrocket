package net.sf.openrocket.simulation.extension.impl;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class JavaCodeProvider extends AbstractSimulationExtensionProvider {
	
	public JavaCodeProvider() {
		super(JavaCode.class, "User code", "Java code");
	}
	
}
