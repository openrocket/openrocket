package net.sf.openrocket.simulation.extension.example;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class StopSimulationProvider extends AbstractSimulationExtensionProvider {
	
	public StopSimulationProvider() {
		super(StopSimulation.class, "Simulation Conditions", "Stop Simulation");
	}
	
}
