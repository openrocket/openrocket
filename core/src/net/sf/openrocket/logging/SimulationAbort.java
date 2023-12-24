package net.sf.openrocket.logging;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

/**
 * Class for logging errors causing a simulation to fail
 */
public class SimulationAbort extends Message {
	
	private static final Translator trans = Application.getTranslator();
	
	private final String description;

	SimulationAbort(String _description) {
		description = _description;
	}
	
	@Override
	public String getMessageDescription() {
		return description;
	}

	@Override
    public boolean replaceBy(Message other) {
		return false;
	}

	/**
	 * Possible causes of sim aborts
	 */

	// No motors are defined in the sim configuration
	public static final SimulationAbort NOMOTORSDEFINED = new SimulationAbort(trans.get("SimulationAbort.noMotorsDefined"));

	// Motors are defined, but none are configured to fire at liftoff
	public static final SimulationAbort NOCONFIGUREDIGNITION = new SimulationAbort(trans.get("SimulationAbort.noConfiguredIgnition"));
	
	// No motors fired (can this really happen without getting a NoMotorsDefined?)
	public static final SimulationAbort NOMOTORSFIRED = new SimulationAbort(trans.get("SimulationAbort.noIgnition"));

	// Motors ignited, but rocket did not lift off
	public static final SimulationAbort NOLIFTOFF = new SimulationAbort(trans.get("SimulationAbort.noLiftOff"));
																		
	// It is impossible to calculate the active components' center of pressure
	public static final SimulationAbort NOCP = new SimulationAbort(trans.get("SimulationAbort.noCP"));

	// The currently active components have a total length of 0
	public static final SimulationAbort ACTIVELENGTHZERO = new SimulationAbort(trans.get("SimulationAbort.activeLengthZero"));

	// The currently active components have a total mass of 0
	public static final SimulationAbort ACTIVEMASSZERO = new SimulationAbort(trans.get("SimulationAbort.activeMassZero"));
}

		

		
