package info.openrocket.core.models.atmosphere;

import static info.openrocket.core.util.MathUtil.interpolate;

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
	private final Object lock = new Object();

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
		if (levels == null) {
			synchronized (lock) {
				levels = computeLayers();
			}
		}

		if (altitude <= 0) {
			return levels[0];
		}

		int maxIndex = levels.length - 1;
		if (altitude >= DELTA * maxIndex) {
			return levels[maxIndex];
		}

		int lowerIndex = (int) Math.floor(altitude / DELTA);
		double fraction = (altitude - lowerIndex * DELTA) / DELTA;

		AtmosphericConditions lower = levels[lowerIndex];
		AtmosphericConditions upper = levels[lowerIndex + 1];

		return new AtmosphericConditions(
				interpolate(lower.getTemperature(), upper.getTemperature(), fraction),
				interpolate(lower.getPressure(), upper.getPressure(), fraction)
		);
	}

	/**
	 * Compute atmospheric conditions at fixed intervals from 0 m up to the maximum altitude.
	 * @return Array of atmospheric conditions at each layer in DELTA intervals
	 */
	private AtmosphericConditions[] computeLayers() {
		double max = getMaxAltitude();
		int size = (int) Math.ceil(max / DELTA);
		AtmosphericConditions[] newLevels = new AtmosphericConditions[size];

		for (int i = 0; i < size; i++) {
			newLevels[i] = getExactConditions(i * DELTA);
		}
		return newLevels;
	}

	protected abstract double getMaxAltitude();

	protected abstract AtmosphericConditions getExactConditions(double altitude);
}
