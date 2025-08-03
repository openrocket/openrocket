package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class AdvancedRollControlProvider extends AbstractSimulationExtensionProvider {

	public AdvancedRollControlProvider() {
		super(AdvancedRollControl.class, "Control Enhancements", "Advanced Roll Control");
	}

}
