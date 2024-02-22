package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class StopSimulationProvider extends AbstractSimulationExtensionProvider {

	public StopSimulationProvider() {
		super(StopSimulation.class, "Simulation Conditions", "Stop Simulation");
	}

}
