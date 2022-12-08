package net.sf.openrocket.simulation.extension.example;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class RollControlProvider extends AbstractSimulationExtensionProvider {
	
	public RollControlProvider() {
		super(RollControl.class, "Control Enhancements", "Roll Control");
	}
	
}
