package info.openrocket.core.simulation;

/**
 * Which RK stepper to use: 4, or 6.
 */
public enum SimulationStepperMethod {

	/**
	 * Perform computations using an RK4 stepper.
	 */
	RK4 {
		@Override
		public String getName() {
			return "6-DOF Runge-Kutta 4";
		}
		@Override
		public String getShortName() {
			return "RK4";
		}
		@Override
		public String getDescription() {
			return getName();
		}
	},

	/**
	 * Perform computations using an RK6 stepper.
	 */
	RK6 {
		@Override
		public String getName() {
			return "6-DOF Runge-Kutta 6";
		}

		@Override
		public String getShortName() {
			return "RK6";
		}

		@Override
		public String getDescription() {
			return "6-DOF Runge-Kutta 6: Slower than RK4, but more accurate for some cases.";
		}
	};


	// TODO: use translator.
	//private static final Translator trans = Application.getTranslator();

	/**
	 * Return the name of this geodetic computation method.
	 * TODO: use translator.
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
	 * Return a description of the geodetic computation methods.
	 * TODO: use translator.
	 */
	public abstract String getDescription();

	@Override
	public String toString() {
		return getName();
	}

}
