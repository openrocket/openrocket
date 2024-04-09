package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class PrintSimulationProvider extends AbstractSimulationExtensionProvider {

	public PrintSimulationProvider() {
		super(PrintSimulation.class, "Reports", "Print Simulation");
	}

}
