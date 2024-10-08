package info.openrocket.core.simulation;

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

			defaults.getAverageWindModel().setAverage(prefs.getAverageWindModel().getAverage());
			defaults.getAverageWindModel().setStandardDeviation(prefs.getAverageWindModel().getStandardDeviation());
			defaults.getAverageWindModel().setTurbulenceIntensity(prefs.getAverageWindModel().getTurbulenceIntensity());

			defaults.setLaunchLatitude(prefs.getDouble(SIMCONDITION_SITE_LAT, defaults.getLaunchLatitude()));
			defaults.setLaunchLongitude(prefs.getDouble(SIMCONDITION_SITE_LON, defaults.getLaunchLongitude()));
			defaults.setLaunchAltitude(prefs.getDouble(SIMCONDITION_SITE_ALT, defaults.getLaunchAltitude()));

			defaults.setISAAtmosphere(prefs.getBoolean(SIMCONDITION_ATMOS_STD, defaults.isISAAtmosphere()));
			defaults.setLaunchTemperature(prefs.getDouble(SIMCONDITION_ATMOS_TEMP, defaults.getLaunchTemperature()));
			defaults.setLaunchPressure(prefs.getDouble(SIMCONDITION_ATMOS_PRESSURE, defaults.getLaunchPressure()));

			defaults.setLaunchIntoWind(prefs.getBoolean(SIMCONDITION_ROD_INTO_WIND, defaults.getLaunchIntoWind()));
			defaults.setLaunchRodLength(prefs.getDouble(SIMCONDITION_ROD_LENGTH, defaults.getLaunchRodLength()));
			defaults.setLaunchRodAngle(prefs.getDouble(SIMCONDITION_ROD_ANGLE, defaults.getLaunchRodAngle()));
			defaults.setLaunchRodDirection(
					prefs.getDouble(SIMCONDITION_ROD_DIRECTION, defaults.getLaunchRodDirection()));
		}
		return defaults;
	}

	public void saveDefault(SimulationOptions newDefaults) {

		prefs.putDouble(SIMCONDITION_WIND_SPEED, newDefaults.getAverageWindModel().getAverage());
		prefs.putDouble(SIMCONDITION_WIND_STDDEV, newDefaults.getAverageWindModel().getStandardDeviation());
		prefs.putDouble(SIMCONDITION_WIND_TURB, newDefaults.getAverageWindModel().getTurbulenceIntensity());

		prefs.putDouble(SIMCONDITION_SITE_LAT, newDefaults.getLaunchLatitude());
		prefs.putDouble(SIMCONDITION_SITE_LON, newDefaults.getLaunchLongitude());
		prefs.putDouble(SIMCONDITION_SITE_ALT, newDefaults.getLaunchAltitude());
		prefs.putBoolean(SIMCONDITION_ATMOS_STD, newDefaults.isISAAtmosphere());
		prefs.putDouble(SIMCONDITION_ATMOS_TEMP, newDefaults.getLaunchTemperature());
		prefs.putDouble(SIMCONDITION_ATMOS_PRESSURE, newDefaults.getLaunchPressure());

		prefs.putBoolean(SIMCONDITION_ROD_INTO_WIND, newDefaults.getLaunchIntoWind());
		prefs.putDouble(SIMCONDITION_ROD_LENGTH, newDefaults.getLaunchRodLength());
		prefs.putDouble(SIMCONDITION_ROD_ANGLE, newDefaults.getLaunchRodAngle());
		prefs.putDouble(SIMCONDITION_ROD_DIRECTION, newDefaults.getLaunchRodDirection());
	}
}
