package info.openrocket.swing.gui.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.montecarlo.MonteCarloDistribution;
import info.openrocket.core.simulation.montecarlo.MonteCarloMetric;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.startup.Application;

/** Shared localized labels used by Monte Carlo screens and reports. */
public final class MonteCarloLabels {
	private static final Translator trans = Application.getTranslator();

	private MonteCarloLabels() {
	}

	public static String metric(MonteCarloMetric metric) {
		return trans.get(metricKey(metric));
	}

	public static String metricKey(MonteCarloMetric metric) {
		return switch (metric) {
			case APOGEE_ALTITUDE -> "simpanel.col.Apogee";
			case MAXIMUM_VELOCITY -> "MaximumVelocityParameter.name";
			case MAXIMUM_ACCELERATION -> "MaximumAccelerationParameter.name";
			case TIME_TO_APOGEE -> "simpanel.col.Timetoapogee";
			case FLIGHT_TIME -> "simpanel.col.Flighttime";
			case MAXIMUM_MACH, LANDING_VELOCITY -> "LandingDispersionResultsDlg.metric."
					+ metric.name().toLowerCase(java.util.Locale.ROOT);
		};
	}

	public static String parameter(MonteCarloParameter parameter) {
		return trans.get("LandingDispersionDlg.parameter." + parameterKey(parameter));
	}

	public static String distribution(MonteCarloDistribution distribution) {
		return trans.get("LandingDispersionDlg.distribution." + switch (distribution) {
			case NORMAL -> "normal";
			case UNIFORM -> "uniform";
			case LOG_NORMAL -> "lognormal";
		});
	}

	private static String parameterKey(MonteCarloParameter parameter) {
		return switch (parameter) {
			case WIND_SPEED -> "windSpeed";
			case WIND_DIRECTION -> "windDirection";
			case AIR_DENSITY -> "airDensity";
			case LAUNCH_GUIDE_ANGLE -> "guideAngle";
			case LAUNCH_GUIDE_DIRECTION -> "guideDirection";
			case TOTAL_MASS -> "mass";
			case CG_AXIAL -> "axialCg";
			case AXIAL_DRAG -> "axialDrag";
			case NORMAL_FORCE -> "normalForce";
			case THRUST -> "thrust";
			case IGNITION_DELAY -> "ignitionDelay";
			case RECOVERY_DRAG -> "recoveryDrag";
			case DEPLOYMENT_DELAY -> "deploymentDelay";
		};
	}
}
