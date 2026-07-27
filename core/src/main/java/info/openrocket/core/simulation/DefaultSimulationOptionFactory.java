package info.openrocket.core.simulation;

import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.preferences.ApplicationPreferences;

import com.google.inject.Inject;

public class DefaultSimulationOptionFactory {

	@Inject
	private final ApplicationPreferences prefs;

	public static final String SIMCONDITION_WIND_SPEED = "SimConditionWindSpeed";
	public static final String SIMCONDITION_WIND_STDDEV = "SimConditionWindStdDev";
	public static final String SIMCONDITION_WIND_TURB = "SimConditionWindTurb";
	public static final String SIMCONDITION_SITE_LAT = "SimConditionSiteLat";
	public static final String SIMCONDITION_SITE_LON = "SimConditionSiteLon";
	public static final String SIMCONDITION_SITE_ALT = "SimConditionSiteAlt";
	public static final String SIMCONDITION_ATMOS_STD = "SimConditionsAtmosStd";
	public static final String SIMCONDITION_ATMOS_TEMP = "SimConditionsAtmosTemp";
	public static final String SIMCONDITION_ATMOS_PRESSURE = "SimConditionsAtmosPres";
	public static final String SIMCONDITION_ATMOS_RELATIVE_HUMIDITY = "SimConditionsAtmoHumi";
	public static final String SIMCONDITION_ROD_INTO_WIND = "SimConditionsRodIntoWind";
	public static final String SIMCONDITION_ROD_LENGTH = "SimConditionsRodLength";
	public static final String SIMCONDITION_ROD_ANGLE = "SimConditionsRodAngle";
	public static final String SIMCONDITION_ROD_DIRECTION = "SimConditionsRodDirection";

	public DefaultSimulationOptionFactory(ApplicationPreferences prefs) {
		this.prefs = prefs;
	}

	public DefaultSimulationOptionFactory() {
		prefs = null;
	}

	public SimulationOptions getDefault() {
		SimulationOptions defaults = new SimulationOptions();
		if (prefs != null) {
			PinkNoiseWindModel preferredWind = prefs.getAverageWindModel();
			PinkNoiseWindModel defaultWind = defaults.getAverageWindModel();
			defaultWind.setAverage(preferredWind.getAverage());
			defaultWind.setStandardDeviation(preferredWind.getStandardDeviation());
			defaultWind.setDirection(preferredWind.getDirection());

			defaults.setLaunchLatitude(prefs.getLaunchLatitude());
			defaults.setLaunchLongitude(prefs.getLaunchLongitude());
			defaults.setLaunchAltitude(prefs.getLaunchAltitude());

			defaults.setISAAtmosphere(prefs.isISAAtmosphere());
			defaults.setLaunchTemperature(prefs.getLaunchTemperature());
			defaults.setLaunchPressure(prefs.getLaunchPressure());
			defaults.setLaunchRelativeHumidity(prefs.getLaunchRelativeHumidity());

			defaults.setLaunchIntoWind(prefs.getLaunchIntoWind());
			defaults.setLaunchRodLength(prefs.getLaunchRodLength());
			defaults.setLaunchRodAngle(prefs.getLaunchRodAngle());
			defaults.setLaunchRodDirection(prefs.getLaunchRodDirection());
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
		PinkNoiseWindModel newWind = newDefaults.getAverageWindModel();
		PinkNoiseWindModel preferredWind = prefs.getAverageWindModel();
		preferredWind.setAverage(newWind.getAverage());
		preferredWind.setStandardDeviation(newWind.getStandardDeviation());
		preferredWind.setDirection(newWind.getDirection());

		// Write the values directly so saving a default does not trigger dependent
		// atmosphere updates or apply the narrower launch-preference angle clamp.
		prefs.putDouble(ApplicationPreferences.LAUNCH_LATITUDE, newDefaults.getLaunchLatitude());
		prefs.putDouble(ApplicationPreferences.LAUNCH_LONGITUDE, newDefaults.getLaunchLongitude());
		prefs.putDouble(ApplicationPreferences.LAUNCH_ALTITUDE, newDefaults.getLaunchAltitude());
		prefs.putBoolean(ApplicationPreferences.LAUNCH_USE_ISA, newDefaults.isISAAtmosphere());
		prefs.putDouble(ApplicationPreferences.LAUNCH_TEMPERATURE, newDefaults.getLaunchTemperature());
		prefs.putDouble(ApplicationPreferences.LAUNCH_PRESSURE, newDefaults.getLaunchPressure());
		prefs.putDouble(ApplicationPreferences.LAUNCH_RELATIVE_HUMIDITY,
				newDefaults.getLaunchRelativeHumidity());

		prefs.putBoolean(ApplicationPreferences.LAUNCH_INTO_WIND, newDefaults.getLaunchIntoWind());
		prefs.putDouble(ApplicationPreferences.LAUNCH_ROD_LENGTH, newDefaults.getLaunchRodLength());
		prefs.putDouble(ApplicationPreferences.LAUNCH_ROD_ANGLE, newDefaults.getLaunchRodAngle());
		prefs.putDouble(ApplicationPreferences.LAUNCH_ROD_DIRECTION, newDefaults.getLaunchRodDirection());
	}
}
