package net.sf.openrocket.simulation.extension.example;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class PrintSimulationProvider extends AbstractSimulationExtensionProvider {
	
	public PrintSimulationProvider() {
		super(PrintSimulation.class, "Reports", "Print Simulation");
	}
	
}
