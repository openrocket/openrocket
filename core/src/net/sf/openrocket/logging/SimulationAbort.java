package net.sf.openrocket.logging;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

/**
 * Class for logging errors causing a simulation to fail
 */
public class SimulationAbort extends Message {
	
	private static final Translator trans = Application.getTranslator();
	
	@Override
	public String getMessageDescription() {
		return cause.toString();
	}

	@Override
    public boolean replaceBy(Message other) {
		return false;
	}

	/**
	 * Possible causes of sim aborts
	 */
	public enum Cause {
		// No motors are defined in the sim configuration
		NOMOTORSDEFINED(trans.get("SimulationAbort.noMotorsDefined")),

		// Motors are defined, but none are configured to fire at liftoff
		NOCONFIGUREDIGNITION(trans.get("SimulationAbort.noConfiguredIgnition")),
	
		// No motors fired (can this really happen without getting a NoMotorsDefined?)
		NOMOTORSFIRED(trans.get("SimulationAbort.noIgnition")),

		// Motors ignited, but rocket did not lift off
		NOLIFTOFF(trans.get("SimulationAbort.noLiftOff")),
																		
		// It is impossible to calculate the active components' center of pressure
		NOCP(trans.get("SimulationAbort.noCP")),

		// The currently active components have a total length of 0
		ACTIVELENGTHZERO(trans.get("SimulationAbort.activeLengthZero")),

		// The currently active components have a total mass of 0
		ACTIVEMASSZERO(trans.get("SimulationAbort.activeMassZero")),

		// Stage is tumbling under thrust
		TUMBLE_UNDER_THRUST(trans.get("SimulationAbort.tumbleUnderThrust"));

		private final String name;

		private Cause(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private final Cause cause;
	
	public SimulationAbort(Cause cause) {
		this.cause = cause;
	}

	public Cause getCause() {
		return cause;
	}
	
}
