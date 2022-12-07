package net.sf.openrocket.simulation.extension.impl;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class CSVSaveProvider extends AbstractSimulationExtensionProvider {
	
	public CSVSaveProvider() {
		super(CSVSave.class, "Reports", "CSV Save");
	}
	
}
