package info.openrocket.core.simulation.extension.impl;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class ScriptingProvider extends AbstractSimulationExtensionProvider {

	public ScriptingProvider() {
		super(ScriptingExtension.class, "Scripts", "JavaScript");
	}

}
