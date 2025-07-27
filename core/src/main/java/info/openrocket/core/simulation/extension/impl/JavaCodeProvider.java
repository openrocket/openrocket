package info.openrocket.core.simulation.extension.impl;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class JavaCodeProvider extends AbstractSimulationExtensionProvider {

	public JavaCodeProvider() {
		super(JavaCode.class, "Scripts", "Java listeners");
	}

}
