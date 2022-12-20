package net.sf.openrocket.simulation.extension.example;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class DampingMomentProvider extends AbstractSimulationExtensionProvider {
	
	public DampingMomentProvider() {
		super(DampingMoment.class, "Post-step flight conditions", "Damping Moment Coefficient (Cdm)");
	}
	
}
