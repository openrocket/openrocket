package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class AirStartProvider extends AbstractSimulationExtensionProvider {

	public AirStartProvider() {
		super(AirStart.class, "Launch conditions", "Air-start");
	}

}
