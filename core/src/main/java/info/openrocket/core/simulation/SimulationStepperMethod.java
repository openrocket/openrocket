package info.openrocket.core.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

/**
 * Which simulation stepper to use.
 */
public enum SimulationStepperMethod {

	/**
	 * Perform computations using an RK4 stepper.
	 */
	RK4 {
		@Override
		public String getName() {
			return trans.get("SimulationStepperMethod.RK4.name");
		}

		@Override
		public String getShortName() {
			return trans.get("SimulationStepperMethod.RK4.shortName");
		}

		@Override
		public String getDescription() {
			return trans.get("SimulationStepperMethod.RK4.desc");
		}
	},

	/**
	 * Perform computations using an RK6 stepper.
	 */
	RK6 {
		@Override
		public String getName() {
			return trans.get("SimulationStepperMethod.RK6.name");
		}

		@Override
		public String getShortName() {
			return trans.get("SimulationStepperMethod.RK6.shortName");
		}

		@Override
		public String getDescription() {
			return trans.get("SimulationStepperMethod.RK6.desc");
		}
	};

	private static final Translator trans = Application.getTranslator();

	/**
	 * Return the name of this simulation stepper method.
	 */
	public abstract String getName();

	/**
	 * Returns a short name or abbreviation for this RK stepper choice.
	 * This is intended to provide a concise identifier for the stepper.
	 *
	 * @return the short name of the RK stepper choice
	 */
	public abstract String getShortName();

	/**
	 * Return a description of the simulation stepper method.
	 */
	public abstract String getDescription();

	@Override
	public String toString() {
		return getName();
	}

}
