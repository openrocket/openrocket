package net.sf.openrocket.simulation.extension;

import java.util.List;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.util.Config;

public interface SimulationExtension {
	
	/**
	 * Return the simulation extension ID that is used when storing this
	 * extension to a file.
	 * 
	 * @return		the extension ID
	 */
	public String getId();
	
	/**
	 * Return a short description of this extension.  The name may contain
	 * elements from the extension's configuration, for example
	 * "Air start (150m)".
	 * 
	 * @return		a short name / description of this extension to be shown in the UI (must not be null)
	 */
	public String getName();
	
	/**
	 * Return a longer description text for this extension, if available.
	 * This description may be shown in the UI as extra information about
	 * the extension.
	 * 
	 * @return		a longer description about this extension, or null if not available
	 */
	public String getDescription();
	
	/**
	 * Called once for each simulation this extension is attached to when loading a document.
	 * This may perform necessary changes to the document at load time.
	 * 
	 * @param document		the loaded document
	 * @param simulation	the simulation this extension is attached to
	 * @param warnings		the document loading warnings
	 */
	public void documentLoaded(OpenRocketDocument document, Simulation simulation, WarningSet warnings);
	
	/**
	 * Initialize this simulation extension for running within a simulation.
	 * This method is called before running a simulation.  It can either modify
	 * the simulation conditions or add simulation listeners to it.
	 * 
	 * @param conditions		the simulation conditions to be run
	 * @param configuration		the extension configuration
	 */
	public void initialize(SimulationConditions conditions) throws SimulationException;
	
	/**
	 * Return a list of any flight data types this simulation extension creates.
	 * This should only contain new types created by this extension, not existing
	 * types that the extension adds to the flight data.
	 */
	public List<FlightDataType> getFlightDataTypes();
	
	
	/**
	 * Return a copy of this simulation extension, with all configuration deep-copied.
	 * 
	 * @return		a new copy of this simulation extension
	 */
	public SimulationExtension clone();
	
	
	/**
	 * Return a Config object describing the current configuration of this simulation
	 * extension.  The extension may keep its configuration in a Config object, or create
	 * it when requested.
	 * 
	 * @return			the simulation extension configuration.
	 */
	public Config getConfig();
	
	/**
	 * Set this simulation extension's configuration.  The extension should load all its
	 * configuration from the provided Config object.
	 * 
	 * @param config	the configuration to set
	 */
	public void setConfig(Config config);
	
}
