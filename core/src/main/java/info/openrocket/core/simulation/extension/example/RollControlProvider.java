package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class RollControlProvider extends AbstractSimulationExtensionProvider {

	public RollControlProvider() {
		super(RollControl.class, "Control Enhancements", "Roll Control");
	}

}
