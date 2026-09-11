package info.openrocket.core.simulation;

import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.preferences.ApplicationPreferences;

import com.google.inject.Inject;

/**
 * Provides the launch conditions that new simulations start from, and stores the
 * conditions of an existing simulation as the new defaults.
 * <p>
 * Both directions are the same copy between two {@link SimulationOptionsInterface}
 * instances: {@link #getDefault()} copies the launch preferences into a fresh
 * {@link SimulationOptions}, {@link #saveDefault(SimulationOptions)} copies the
 * conditions of a simulation back into the launch preferences. Keeping a single
 * copy implementation guarantees that both directions cover the same properties.
 */
public class DefaultSimulationOptionFactory {

	private final ApplicationPreferences prefs;

	@Inject
	public DefaultSimulationOptionFactory(ApplicationPreferences prefs) {
		this.prefs = prefs;
	}

	/**
	 * Returns the launch conditions that a new simulation should start from, as stored
	 * in the launch preferences.
	 *
	 * @return the default simulation options
	 */
	public SimulationOptions getDefault() {
		SimulationOptions defaults = new SimulationOptions();
		copyLaunchConditions(prefs, defaults);
		if (prefs.isRandomSeedFixed()) {
			defaults.setRandomSeed(prefs.getRandomSeed());
			defaults.setRandomSeedFixed(true);
		}
		return defaults;
	}

	/**
	 * Stores the supplied launch conditions as the application launch preferences.
	 * These are the same preferences shown on the Launch preferences panel and used
	 * by {@link #getDefault()}.
	 *
	 * @param newDefaults the simulation conditions to store
	 */
	public void saveDefault(SimulationOptions newDefaults) {
		copyLaunchConditions(newDefaults, prefs);
	}

	/**
	 * Copies all launch conditions shown on the launch conditions panel from one set of
	 * simulation options to another. Both the launch preferences and a simulation's
	 * options implement {@link SimulationOptionsInterface}, so this serves loading and
	 * saving the defaults alike.
	 *
	 * @param source      the options to read the launch conditions from
	 * @param destination the options to write the launch conditions to
	 */
	private static void copyLaunchConditions(SimulationOptionsInterface source,
			SimulationOptionsInterface destination) {
		// The wind is copied first, because the launch rod direction follows the wind
		// direction while launching into wind.
		PinkNoiseWindModel sourceWind = source.getAverageWindModel();
		PinkNoiseWindModel destinationWind = destination.getAverageWindModel();
		// Setting the average rescales the standard deviation, so copy the deviation after it.
		destinationWind.setAverage(sourceWind.getAverage());
		destinationWind.setStandardDeviation(sourceWind.getStandardDeviation());
		destinationWind.setDirection(sourceWind.getDirection());

		destination.setLaunchLatitude(source.getLaunchLatitude());
		destination.setLaunchLongitude(source.getLaunchLongitude());

		// While the ISA model is in use, the atmospheric conditions are derived from the
		// altitude, so the explicit values are applied after the ISA flag and the altitude.
		destination.setISAAtmosphere(source.isISAAtmosphere());
		destination.setLaunchAltitude(source.getLaunchAltitude());
		destination.setLaunchTemperature(source.getLaunchTemperature());
		destination.setLaunchPressure(source.getLaunchPressure());
		destination.setLaunchRelativeHumidity(source.getLaunchRelativeHumidity());

		destination.setLaunchIntoWind(source.getLaunchIntoWind());
		destination.setLaunchRodLength(source.getLaunchRodLength());
		destination.setLaunchRodAngle(source.getLaunchRodAngle());
		destination.setLaunchRodDirection(source.getLaunchRodDirection());
	}
}
