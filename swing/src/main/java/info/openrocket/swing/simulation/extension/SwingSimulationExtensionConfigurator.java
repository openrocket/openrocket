package info.openrocket.swing.simulation.extension;

import java.awt.Window;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.simulation.extension.SimulationExtension;

@Plugin
public interface SwingSimulationExtensionConfigurator {
	
	/**
	 * Test whether this configurator supports configuring an extension.
	 * 
	 * @param extension		the extension to test
	 * @return				true if this configurator can configure the specified extension
	 */
	public boolean support(SimulationExtension extension);
	
	/**
	 * Open an application-modal dialog for configuring a simulation extension.
	 * Close the dialog when ready.
	 * 
	 * @param extension		the extension to configure
	 * @param simulation	the simulation the extension is attached to
	 * @param parent		the parent window for the dialog
	 */
	public void configure(SimulationExtension extension, Simulation simulation, Window parent);
	
}
