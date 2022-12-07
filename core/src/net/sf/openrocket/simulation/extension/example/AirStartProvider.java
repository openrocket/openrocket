package net.sf.openrocket.simulation.extension.impl;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class AirStartProvider extends AbstractSimulationExtensionProvider {
	
	public AirStartProvider() {
		super(AirStart.class, "Launch conditions", "Air-start");
	}
	
}
