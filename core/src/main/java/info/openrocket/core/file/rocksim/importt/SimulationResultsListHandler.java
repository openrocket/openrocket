package info.openrocket.core.file.rocksim.importt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModel.AltitudeReference;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.SimulationOptions;

import org.xml.sax.SAXException;

/**
 * A SAX handler for RockSim's list of simulations. Each RockSim simulation is imported as a flight configuration with
 * its motors and recovery events, and an OpenRocket simulation with its launch conditions.
 */
class SimulationResultsListHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	private final Rocket rocket;

	SimulationResultsListHandler(DocumentLoadingContext context, Rocket rocket) {
		this.context = context;
		this.rocket = rocket;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		if (RockSimCommonConstants.SIMULATION_RESULTS.equals(element)) {
			return new SimulationResultsHandler(context, rocket);
		}
		return null;
	}
}

/**
 * A SAX handler for a single RockSim simulation.
 */
class SimulationResultsHandler extends AbstractElementHandler {
	private static final double PASCALS_PER_MMHG = 101325.0 / 760.0;
	private static final double CELSIUS_TO_KELVIN = 273.15;
	/**
	 * RockSim's plugged (no ejection charge) ejection delay.
	 */
	private static final double ROCKSIM_NO_EJECTION_CHARGE = -2.0;
	/**
	 * Launch temperatures above this (°C) are taken to be in °F. Many RockSim files store 59 (°F, the standard 15 °C).
	 */
	private static final double MAX_PLAUSIBLE_CELSIUS = 50.0;

	private static final int NO_EVENT = 0;
	private static final int AT_MAX_EJECTION = 1;
	private static final int AT_TIME_AFTER_IGNITION = 2;
	private static final int AT_TIME_AFTER_APOGEE = 3;
	private static final int AT_APOGEE = 4;
	private static final int AT_ALTITUDE = 5;

	/**
	 * The simulation values that are imported; all other elements are ignored.
	 */
	private static final Set<String> IMPORTED_VALUES = Set.of(RockSimCommonConstants.SIMULATION_NAME,
			RockSimCommonConstants.COMMENTS, RockSimCommonConstants.LAUNCH_GUIDE_LEN,
			RockSimCommonConstants.LAUNCH_ANGLE, RockSimCommonConstants.LAUNCH_GUIDE_AZIMUTH,
			RockSimCommonConstants.LAUNCH_BAROMETER, RockSimCommonConstants.LAUNCH_LATITUDE,
			RockSimCommonConstants.LAUNCH_LONGITUDE, RockSimCommonConstants.LAUNCH_HUMIDITY,
			RockSimCommonConstants.LAUNCH_TEMPERATURE, RockSimCommonConstants.LAUNCH_ALTITUDE,
			RockSimCommonConstants.LAUNCH_WIND_LOW_SPEED, RockSimCommonConstants.LAUNCH_WIND_HIGH_SPEED,
			RockSimCommonConstants.LAUNCH_WIND_TABLE_SIZE, RockSimCommonConstants.LAUNCH_WIND_ALT_TABLE,
			RockSimCommonConstants.LAUNCH_WIND_SPEED_TABLE, RockSimCommonConstants.LAUNCH_WIND_DIRECTION_TABLE);

	private final DocumentLoadingContext context;
	private final Rocket rocket;
	private final Map<String, String> values = new HashMap<>();
	/**
	 * The engine sets of the RockSim stages 3 (sustainer), 2 and 1.
	 */
	private final List<List<Map<String, String>>> engineSets = List.of(new ArrayList<>(), new ArrayList<>(),
			new ArrayList<>());
	private final List<Map<String, String>> events = new ArrayList<>();

	SimulationResultsHandler(DocumentLoadingContext context, Rocket rocket) {
		this.context = context;
		this.rocket = rocket;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		if (RockSimCommonConstants.STAGE_3_ENGINES.equals(element)) {
			return new ListHandler(RockSimCommonConstants.ENGINE_SET, engineSets.get(0));
		}
		if (RockSimCommonConstants.STAGE_2_ENGINES.equals(element)) {
			return new ListHandler(RockSimCommonConstants.ENGINE_SET, engineSets.get(1));
		}
		if (RockSimCommonConstants.STAGE_1_ENGINES.equals(element)) {
			return new ListHandler(RockSimCommonConstants.ENGINE_SET, engineSets.get(2));
		}
		if (RockSimCommonConstants.SIMULATION_EVENTS.equals(element)) {
			return new ListHandler(RockSimCommonConstants.SIMULATION_EVENT, events);
		}
		return IMPORTED_VALUES.contains(element) ? PlainTextHandler.INSTANCE : null;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content,
			WarningSet warnings) {
		values.put(element, content.trim());
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		OpenRocketDocument document = context == null ? null : context.getOpenRocketDocument();
		if (document == null) {
			return;
		}
		String name = values.getOrDefault(RockSimCommonConstants.SIMULATION_NAME, "");
		if (name.isEmpty()) {
			name = values.getOrDefault(RockSimCommonConstants.COMMENTS, "");
		}

		FlightConfigurationId configurationId = new FlightConfigurationId();
		rocket.createFlightConfiguration(configurationId);
		int motorCount = 0;
		for (List<Map<String, String>> stageEngineSets : engineSets) {
			for (Map<String, String> engineSet : stageEngineSets) {
				if (addMotor(engineSet, configurationId, warnings)) {
					motorCount++;
				}
			}
		}
		if (motorCount == 0) {
			rocket.removeFlightConfiguration(configurationId);
			warnings.add("RockSim simulation '" + name + "' has no motors that could be imported; it was ignored.");
			return;
		}
		for (Map<String, String> event : events) {
			addRecoveryEvent(event, configurationId);
		}

		Simulation simulation = new Simulation(document, rocket);
		simulation.setFlightConfigurationId(configurationId);
		if (!name.isEmpty()) {
			simulation.setName(name);
		}
		setLaunchConditions(simulation.getOptions(), name, warnings);
		document.addSimulation(simulation);
	}

	private boolean addMotor(Map<String, String> engineSet, FlightConfigurationId configurationId,
			WarningSet warnings) {
		String code = engineSet.getOrDefault(RockSimCommonConstants.ENGINE_CODE, "");
		Object mount = context.getComponentByFileId(engineSet.getOrDefault(RockSimCommonConstants.MOUNT_SERIAL_NUMBER, ""));
		if (!(mount instanceof MotorMount) || !((MotorMount) mount).isMotorMount()) {
			warnings.add("The motor mount of RockSim motor '" + code + "' was not found; the motor was ignored.");
			return false;
		}
		String manufacturer = engineSet.getOrDefault(RockSimCommonConstants.ENGINE_MFG, "");
		Motor motor = context.getMotorFinder().findMotor(null, manufacturer.isEmpty() ? null : manufacturer, code,
				Double.NaN, Double.NaN, null, warnings);
		if (motor == null) {
			return false;
		}

		MotorMount motorMount = (MotorMount) mount;
		MotorConfiguration configuration = new MotorConfiguration(motorMount, configurationId);
		configuration.setMotor(motor);
		double ejectionDelay = parse(engineSet.get(RockSimCommonConstants.EJECTION_DELAY), 0);
		if (ejectionDelay == ROCKSIM_NO_EJECTION_CHARGE) {
			configuration.setEjectionDelay(Motor.PLUGGED_DELAY);
		} else if (ejectionDelay >= 0) {
			configuration.setEjectionDelay(ejectionDelay);
		}
		configuration.setIgnitionDelay(Math.max(0, parse(engineSet.get(RockSimCommonConstants.IGNITION_DELAY), 0)));
		motorMount.setMotorConfig(configuration, configurationId);
		return true;
	}

	private void addRecoveryEvent(Map<String, String> event, FlightConfigurationId configurationId) {
		Object device = context.getComponentByFileId(event.getOrDefault(RockSimCommonConstants.PART_SERIAL_NUMBER, ""));
		if (!(device instanceof RecoveryDevice)) {
			return;
		}
		double time = parse(event.get(RockSimCommonConstants.DEPLY_TIME), 0);
		DeploymentConfiguration deployment = new DeploymentConfiguration();
		switch ((int) parse(event.get(RockSimCommonConstants.EVENT_TYPE), NO_EVENT)) {
			case AT_MAX_EJECTION:
				deployment.setDeployEvent(DeployEvent.EJECTION);
				deployment.setDeployDelay(time);
				break;
			case AT_TIME_AFTER_IGNITION:
				deployment.setDeployEvent(DeployEvent.LAUNCH);
				deployment.setDeployDelay(time);
				break;
			case AT_TIME_AFTER_APOGEE:
				deployment.setDeployEvent(DeployEvent.APOGEE);
				deployment.setDeployDelay(time);
				break;
			case AT_APOGEE:
				deployment.setDeployEvent(DeployEvent.APOGEE);
				break;
			case AT_ALTITUDE:
				deployment.setDeployEvent(DeployEvent.ALTITUDE);
				deployment.setDeployAltitude(parse(event.get(RockSimCommonConstants.DEPLOY_ALTITUDE), 0));
				break;
			default:
				deployment.setDeployEvent(DeployEvent.NEVER);
				break;
		}
		((RecoveryDevice) device).getDeploymentConfigurations().set(configurationId, deployment);
	}

	private void setLaunchConditions(SimulationOptions options, String name, WarningSet warnings) {
		if (values.containsKey(RockSimCommonConstants.LAUNCH_GUIDE_LEN)) {
			options.setLaunchRodLength(parse(values.get(RockSimCommonConstants.LAUNCH_GUIDE_LEN), 0)
					/ RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
		}
		// RockSim-written files store the launch angle in radians and the guide azimuth in degrees
		double angle = parse(values.get(RockSimCommonConstants.LAUNCH_ANGLE), 0);
		double azimuth = Math.toRadians(parse(values.get(RockSimCommonConstants.LAUNCH_GUIDE_AZIMUTH), 0));
		if (angle < 0) {
			angle = -angle;
			azimuth += Math.PI;
		}
		options.setLaunchIntoWind(false);
		options.setLaunchRodAngle(angle);
		options.setLaunchRodDirection(azimuth);

		if (values.containsKey(RockSimCommonConstants.LAUNCH_ALTITUDE)) {
			options.setLaunchAltitude(parse(values.get(RockSimCommonConstants.LAUNCH_ALTITUDE), 0));
		}
		if (values.containsKey(RockSimCommonConstants.LAUNCH_LATITUDE)) {
			options.setLaunchLatitude(parse(values.get(RockSimCommonConstants.LAUNCH_LATITUDE), 0));
		}
		if (values.containsKey(RockSimCommonConstants.LAUNCH_LONGITUDE)) {
			options.setLaunchLongitude(parse(values.get(RockSimCommonConstants.LAUNCH_LONGITUDE), 0));
		}
		if (values.containsKey(RockSimCommonConstants.LAUNCH_HUMIDITY)) {
			options.setLaunchRelativeHumidity(parse(values.get(RockSimCommonConstants.LAUNCH_HUMIDITY), 0) / 100.0);
		}

		double pressure = parse(values.get(RockSimCommonConstants.LAUNCH_BAROMETER), 0) * PASCALS_PER_MMHG;
		if (pressure > 0 && values.containsKey(RockSimCommonConstants.LAUNCH_TEMPERATURE)) {
			double temperature = parse(values.get(RockSimCommonConstants.LAUNCH_TEMPERATURE), 15);
			if (temperature > MAX_PLAUSIBLE_CELSIUS) {
				warnings.add(String.format("The launch temperature %s of RockSim simulation '%s' was taken to be in "
						+ "°F.", values.get(RockSimCommonConstants.LAUNCH_TEMPERATURE), name));
				temperature = (temperature - 32) * 5 / 9;
			}
			options.setISAAtmosphere(false);
			options.setLaunchTemperature(temperature + CELSIUS_TO_KELVIN);
			options.setLaunchPressure(pressure);
		}

		setWind(options);
	}

	private void setWind(SimulationOptions options) {
		int tableSize = (int) parse(values.get(RockSimCommonConstants.LAUNCH_WIND_TABLE_SIZE), 0);
		if (tableSize > 0) {
			double[] altitudes = parseTable(values.get(RockSimCommonConstants.LAUNCH_WIND_ALT_TABLE));
			double[] speeds = parseTable(values.get(RockSimCommonConstants.LAUNCH_WIND_SPEED_TABLE));
			double[] directions = parseTable(values.get(RockSimCommonConstants.LAUNCH_WIND_DIRECTION_TABLE));
			int size = Math.min(tableSize, Math.min(altitudes.length, Math.min(speeds.length, directions.length)));
			if (size > 0) {
				// RockSim-written wind tables use altitudes above sea level and compass directions in degrees
				MultiLevelPinkNoiseWindModel windModel = options.getMultiLevelWindModel();
				windModel.clearLevels();
				windModel.setAltitudeReference(AltitudeReference.MSL);
				for (int i = 0; i < size; i++) {
					windModel.addWindLevel(altitudes[i], speeds[i], Math.toRadians(directions[i]));
				}
				options.setWindModelType(WindModelType.MULTI_LEVEL);
				return;
			}
		}

		if (values.containsKey(RockSimCommonConstants.LAUNCH_WIND_LOW_SPEED)) {
			// RockSim varies the wind sinusoidally between its low and high speed
			double low = parse(values.get(RockSimCommonConstants.LAUNCH_WIND_LOW_SPEED), 0);
			double high = Math.max(low, parse(values.get(RockSimCommonConstants.LAUNCH_WIND_HIGH_SPEED), low));
			options.setWindModelType(WindModelType.AVERAGE);
			options.getAverageWindModel().setAverage((low + high) / 2);
			options.getAverageWindModel().setStandardDeviation((high - low) / (2 * Math.sqrt(2)));
		}
	}

	private static double parse(String value, double defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private static double[] parseTable(String table) {
		if (table == null || table.isBlank()) {
			return new double[0];
		}
		String[] entries = table.split(",");
		double[] result = new double[entries.length];
		for (int i = 0; i < entries.length; i++) {
			result[i] = parse(entries[i], 0);
		}
		return result;
	}

	/**
	 * Collects the elements of a list (such as the engine sets of a stage) as maps from child element to text.
	 */
	private static class ListHandler extends AbstractElementHandler {
		private final String itemElement;
		private final List<Map<String, String>> items;

		ListHandler(String itemElement, List<Map<String, String>> items) {
			this.itemElement = itemElement;
			this.items = items;
		}

		@Override
		public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
			if (itemElement.equals(element)) {
				Map<String, String> item = new HashMap<>();
				items.add(item);
				return new ValuesHandler(item);
			}
			return null;
		}
	}

	private static class ValuesHandler extends AbstractElementHandler {
		private final Map<String, String> values;

		ValuesHandler(Map<String, String> values) {
			this.values = values;
		}

		@Override
		public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
			return PlainTextHandler.INSTANCE;
		}

		@Override
		public void closeElement(String element, HashMap<String, String> attributes, String content,
				WarningSet warnings) {
			values.put(element, content.trim());
		}
	}
}
