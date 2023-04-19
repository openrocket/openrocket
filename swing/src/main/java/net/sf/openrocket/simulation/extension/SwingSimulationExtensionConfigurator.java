package net.sf.openrocket.simulation.extension;

import java.awt.Window;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.plugin.Plugin;

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
