package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class DampingMomentProvider extends AbstractSimulationExtensionProvider {

	public DampingMomentProvider() {
		super(DampingMoment.class, "Post-step flight conditions", "Damping Moment Coefficient (Cdm)");
	}

}
