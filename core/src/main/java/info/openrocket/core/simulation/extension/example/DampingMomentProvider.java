package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

/**
 * Deprecated provider kept for backwards compatibility with saved simulation extension IDs.
 */
@Plugin
@Deprecated
public class DampingMomentProvider extends AbstractSimulationExtensionProvider {

	public DampingMomentProvider() {
		super(DampingMoment.class, "Deprecated", "Damping moment coefficient (Cdm) (built-in)");
	}
}

