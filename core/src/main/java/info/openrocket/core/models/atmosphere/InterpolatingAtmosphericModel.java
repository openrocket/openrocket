package info.openrocket.core.models.atmosphere;

/**
 * An abstract implementation of AtmosphericModel that uses pre-computed layers
 * for efficient altitude lookups. This class implements a performance optimization
 * strategy where atmospheric conditions are pre-calculated at fixed intervals
 * (layers) and then interpolated between these layers when needed.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class InterpolatingAtmosphericModel implements AtmosphericModel {
	/**
	 * Layer thickness for interpolation in meters.
	 * Set to 500m as a balance between accuracy and performance.
	 * - Small enough to maintain good accuracy for typical rocket flights
	 * - Large enough to keep memory usage and computation time reasonable
	 */
	private static final double DELTA = 500;

	/** Cache of pre-computed atmospheric conditions at each layer. */
	private AtmosphericConditions[] levels = null;

	/**
	 * Returns atmospheric conditions at the specified altitude using linear interpolation
	 * between pre-computed layers. The first time this method is called, it triggers
	 * the computation of all layers up to the maximum altitude.
	 *
	 * Process:
	 * 1. If layers haven't been computed, compute them
	 * 2. For altitudes below 0, return conditions at lowest layer
	 * 3. For altitudes above max, return conditions at highest layer
	 * 4. Otherwise, interpolate between nearest layers
	 *
	 * @param altitude The altitude in meters
	 * @return Interpolated atmospheric conditions at the specified altitude
	 */
	@Override
	public AtmosphericConditions getConditions(double altitude) {
		if (levels == null)
			computeLayers();

		if (altitude <= 0) {
			// TODO: LOW: levels[0] returned null in some cases, see GitHub issue #952 for
			// more information
			if (levels[0] == null) {
				computeLayers();
			}
			return levels[0];
		}
		if (altitude >= DELTA * (levels.length - 1)) {
			// TODO: LOW: levels[levels.length - 1] returned null in some cases, see GitHub
			// issue #952 for more information
			if (levels[levels.length - 1] == null) {
				computeLayers();
			}
			return levels[levels.length - 1];
		}

		int n = (int) (altitude / DELTA);
		double d = (altitude - n * DELTA) / DELTA;
		AtmosphericConditions c = new AtmosphericConditions();
		// TODO: LOW: levels[n] returned null in some cases, see GitHub issue #2180 for
		// more information
		if (levels[n] == null) {
			computeLayers();
		}
		c.setTemperature(levels[n].getTemperature() * (1 - d) + levels[n + 1].getTemperature() * d);
		c.setPressure(levels[n].getPressure() * (1 - d) + levels[n + 1].getPressure() * d);

		return c;
	}

	/**
	 * Compute atmospheric conditions at fixed intervals from 0 m up to the maximum altitude.
	 * @return Array of atmospheric conditions at each layer in DELTA intervals
	 */
	private AtmosphericConditions[] computeLayers() {
		double max = getMaxAltitude();
		int n = (int) (max / DELTA) + 1;
		levels = new AtmosphericConditions[n];
		for (int i = 0; i < n; i++) {
			levels[i] = getExactConditions(i * DELTA);
		}
	}

	protected abstract double getMaxAltitude();

	protected abstract AtmosphericConditions getExactConditions(double altitude);
}
