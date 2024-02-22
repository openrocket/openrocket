package info.openrocket.core.simulation.extension.example;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.AbstractSimulationExtensionProvider;

@Plugin
public class CSVSaveProvider extends AbstractSimulationExtensionProvider {

	public CSVSaveProvider() {
		super(CSVSave.class, "Reports", "CSV Save");
	}

}
