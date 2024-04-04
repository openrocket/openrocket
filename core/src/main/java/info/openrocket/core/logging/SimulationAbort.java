package info.openrocket.core.logging;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

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
		// No active stages in simulation
		NO_ACTIVE_STAGES(trans.get("SimulationAbort.noActiveStages")),
		
		// No motors are defined in the sim configuration
		NO_MOTORS_DEFINED(trans.get("SimulationAbort.noMotorsDefined")),

		// Motors are defined, but none are configured to fire at liftoff
		NO_CONFIGURED_IGNITION(trans.get("SimulationAbort.noConfiguredIgnition")),
	
		// No motors fired (can this really happen without getting a NoMotorsDefined?)
		NO_MOTORS_FIRED(trans.get("SimulationAbort.noIgnition")),

		// Motors ignited, but rocket did not lift off
		NO_LIFTOFF(trans.get("SimulationAbort.noLiftOff")),
																		
		// It is impossible to calculate the active components' center of pressure
		NO_CP(trans.get("SimulationAbort.noCP")),

		// The currently active components have a total length of 0
		ACTIVE_LENGTH_ZERO(trans.get("SimulationAbort.activeLengthZero")),

		// The currently active components have a total mass of 0
		ACTIVE_MASS_ZERO(trans.get("SimulationAbort.activeMassZero")),

		// Stage is tumbling under thrust
		TUMBLE_UNDER_THRUST(trans.get("SimulationAbort.tumbleUnderThrust")),

		// Recovery system deployed while motor is still burning
		DEPLOY_UNDER_THRUST(trans.get("SimulationAbort.deployUnderThrust"));

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
