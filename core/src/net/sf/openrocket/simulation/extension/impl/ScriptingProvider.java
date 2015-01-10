package net.sf.openrocket.simulation.extension.impl;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class ScriptingProvider extends AbstractSimulationExtensionProvider {
	
	public ScriptingProvider() {
		super(ScriptingExtension.class, "User code", "Scripts");
	}
	
}
