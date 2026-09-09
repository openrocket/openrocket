package info.openrocket.core.simulation.montecarlo;

/**
 * Independent uncertainty inputs currently supported by landing-dispersion analysis.
 * Values use OpenRocket's internal SI units; angular values are radians and relative
 * multipliers are represented as fractions.
 */
public enum MonteCarloParameter {
	WIND_SPEED(false),
	WIND_DIRECTION(false),
	AIR_DENSITY(true),
	LAUNCH_GUIDE_ANGLE(false),
	LAUNCH_GUIDE_DIRECTION(false),
	TOTAL_MASS(true),
	CG_AXIAL(false),
	AXIAL_DRAG(true),
	NORMAL_FORCE(true),
	THRUST(true),
	IGNITION_DELAY(false),
	RECOVERY_DRAG(true),
	DEPLOYMENT_DELAY(false);

	private final boolean relative;

	MonteCarloParameter(boolean relative) {
		this.relative = relative;
	}

	/**
	 * Whether a sampled value is a fraction applied as a multiplier around the nominal
	 * value rather than an offset added to it. Only relative parameters can be sampled
	 * from a strictly positive distribution such as {@link MonteCarloDistribution#LOG_NORMAL}.
	 *
	 * @return {@code true} if the sampled variation is a relative fraction
	 */
	public boolean isRelative() {
		return relative;
	}
}
