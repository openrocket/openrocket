package info.openrocket.core.simulation;

import info.openrocket.core.simulation.listeners.AbstractSimulationListener;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

/**
 * Supplies a fully specified wind field, overriding the simulation's own wind
 * model.
 * <p>
 * The bundled {@code PinkNoiseWindModel} is seeded from
 * {@code SimulationOptions.randomSeed} as it stood when the options object was
 * constructed, and {@code setRandomSeed} does not re-seed it. Turbulent runs are
 * therefore not reproducible from the seed alone, which makes them unusable as
 * test fixtures. Overriding {@code preWindModel} sidesteps that entirely: the
 * gust is written down rather than sampled, so the scenario is identical on
 * every run and on every machine.
 */
public class DeterministicWind extends AbstractSimulationListener {

	private final double baseSpeed;
	private final double gustSpeed;
	private final double gustStart;
	private final double gustEnd;

	/**
	 * @param baseSpeed steady crosswind, m/s, blowing along +X
	 * @param gustSpeed additional crosswind during the gust, m/s
	 * @param gustStart time at which the gust starts, s
	 * @param gustEnd   time at which the gust ends, s
	 */
	public DeterministicWind(double baseSpeed, double gustSpeed, double gustStart, double gustEnd) {
		this.baseSpeed = baseSpeed;
		this.gustSpeed = gustSpeed;
		this.gustStart = gustStart;
		this.gustEnd = gustEnd;
	}

	/** A steady crosswind with no gust. */
	public static DeterministicWind steady(double speed) {
		return new DeterministicWind(speed, 0, 0, 0);
	}

	@Override
	public CoordinateIF preWindModel(SimulationStatus status) {
		final double time = status.getSimulationTime();
		final boolean gusting = time >= gustStart && time < gustEnd;
		return new Coordinate(baseSpeed + (gusting ? gustSpeed : 0), 0, 0);
	}

	@Override
	public boolean isSystemListener() {
		// Run as a system listener so that nested simulations keep the same wind.
		return true;
	}
}
