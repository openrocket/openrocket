package info.openrocket.core.simulation;

import info.openrocket.core.util.MathUtil;

/**
 * Decides whether the rocket is tumbling, from how long its angle of attack has
 * persisted rather than from its instantaneous value.
 * <p>
 * The previous criterion combined an instantaneous angle of attack above the
 * stall angle with a CP/CG stability comparison. Both halves are problematic.
 * The angle of attack is measured against the apparent airflow, which includes
 * the modelled wind, and a turbulence sample is a discontinuous function of
 * time: the angle of attack can therefore step across the stall threshold in a
 * single integration step without the rocket's state having changed. And the CP
 * the stability test consumes is produced by an aerodynamic model that is
 * outside its validity envelope above the stall angle -- that is, in exactly the
 * regime where the test is evaluated -- so the test draws its conclusion from a
 * quantity it has itself declared unreliable.
 * <p>
 * What distinguishes a tumbling rocket from a gust is not the size of the angle
 * of attack at one instant but whether the rocket recovers. A statically stable
 * rocket returns to alignment within about a pitch period; a tumbling rocket
 * does not. This detector therefore low-pass filters the angle of attack with a
 * time constant taken from the rocket's own pitch natural frequency, and
 * declares tumbling only once the filtered value is sustained. No aerodynamic
 * coefficient enters the decision, and no flight-phase gating is needed in
 * either direction: the launch guide departure transient does not survive the
 * filter, and descent tumbling does.
 * <p>
 * The angle is measured against the air-relative velocity rather than the
 * ground-relative trajectory. A stable rocket aligns itself with the air, not
 * with the ground, so in a crosswind its axis is offset from its ground track by
 * atan(wind / airspeed) purely as a matter of geometry -- an offset that reaches
 * tens of degrees at the low speeds just after launch guide departure, which is
 * precisely where the detector must not fire.
 */
public class TumbleDetector {

	/**
	 * Filtered angle of attack above which the rocket is considered to be tumbling.
	 * Normal flight sits below the stall angle, while a rocket whose axis is
	 * uncorrelated with the airflow averages 90 degrees; the threshold sits in the
	 * gap between the two populations rather than close to either.
	 */
	private static final double TUMBLE_THRESHOLD = 60 * Math.PI / 180;

	/**
	 * Dynamic pressure below which the airflow direction carries no usable
	 * information. At sea level this corresponds to roughly 1.3 m/s.
	 */
	private static final double MIN_DYNAMIC_PRESSURE = 1.0;

	/** Filter time constant, expressed in pitch natural periods. */
	private static final double DWELL_PERIODS = 2.0;

	private static final double MIN_TIME_CONSTANT = 0.05;
	private static final double MAX_TIME_CONSTANT = 2.0;

	private double filteredAOA = 0.0;
	private double lastTime = Double.NaN;

	public TumbleDetector() {
	}

	/**
	 * Copy constructor, so that a branch created at stage separation inherits the
	 * filter state accumulated by its parent.
	 *
	 * @param orig the detector to copy
	 */
	public TumbleDetector(TumbleDetector orig) {
		this.filteredAOA = orig.filteredAOA;
		this.lastTime = orig.lastTime;
	}

	/**
	 * Advance the detector by one simulation step and report whether the rocket
	 * should be considered to be tumbling.
	 *
	 * @param time             current simulation time, s
	 * @param guideCleared     whether the rocket has left the launch guide, and so is
	 *                         free to rotate at all
	 * @param aoa              angle of attack, radians
	 * @param airSpeed         speed relative to the air, m/s
	 * @param airDensity       ambient air density, kg/m^3
	 * @param naturalFrequency pitch natural frequency in rad/s, or NaN when the
	 *                         rocket is statically unstable and so has no restoring
	 *                         dynamics to wait out
	 * @return true if the rocket is tumbling
	 */
	public boolean update(double time, boolean guideCleared, double aoa, double airSpeed,
			double airDensity, double naturalFrequency) {
		final double dt = Double.isNaN(lastTime) ? 0.0 : time - lastTime;
		lastTime = time;

		final double dynamicPressure = 0.5 * airDensity * airSpeed * airSpeed;

		// Observability gate.  A rocket still on the launch guide is held on course and
		// cannot tumble, yet a crosswind across a stationary rocket puts the angle of
		// attack near 90 degrees, so the angle carries no information until the rocket
		// is free.  Below the dynamic pressure floor the airflow direction is likewise
		// meaningless.  Hold rather than reset, so that a spell of negligible airflow --
		// passing through apogee, for instance -- does not discard evidence accumulated
		// before it.
		if (dt <= 0 || !guideCleared || Double.isNaN(aoa) || Double.isNaN(dynamicPressure)
				|| dynamicPressure < MIN_DYNAMIC_PRESSURE) {
			return isTumbling();
		}

		final double alpha = 1.0 - Math.exp(-dt / timeConstant(naturalFrequency));
		filteredAOA += alpha * (aoa - filteredAOA);

		return isTumbling();
	}

	/**
	 * @return true if the filtered angle of attack currently exceeds the tumbling
	 *         threshold
	 */
	public boolean isTumbling() {
		return filteredAOA > TUMBLE_THRESHOLD;
	}

	/**
	 * @return the current filtered angle of attack, in radians
	 */
	public double getFilteredAOA() {
		return filteredAOA;
	}

	/**
	 * Filter time constant, expressed in the rocket's own pitch periods so that the
	 * detector waits proportionally longer for a slowly responding airframe than for
	 * a stiff one.
	 *
	 * @param naturalFrequency pitch natural frequency in rad/s
	 * @return the time constant to use, in seconds
	 */
	private static double timeConstant(double naturalFrequency) {
		if (Double.isNaN(naturalFrequency) || naturalFrequency <= 0) {
			// Statically unstable, or not yet computable: there is no restoring
			// oscillation to wait out, so respond at the shortest sensible scale.
			return MIN_TIME_CONSTANT;
		}
		return MathUtil.clamp(DWELL_PERIODS * 2 * Math.PI / naturalFrequency,
				MIN_TIME_CONSTANT, MAX_TIME_CONSTANT);
	}
}
