package info.openrocket.core.file.rocksim.export;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel.LevelWindModel;
import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModel.AltitudeReference;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import info.openrocket.core.simulation.SimulationOptions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

/**
 * RockSim's combined simulation-input and simulation-results record.
 * <p>
 * OpenRocket exports the inputs that RockSim can recalculate: motor selections,
 * launch conditions, and basic simulation controls.  Result fields are left at
 * their unevaluated values because results produced by a different simulator
 * should not be presented as RockSim results.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulationResultsDTO {

	private static final int ROCKSIM_CUSTOM_PRESET = 99;
	private static final int ROCKSIM_NOT_SIMULATED = 5;
	private static final int ROCKSIM_RUNGE_KUTTA = 1;
	private static final int MAX_WIND_LEVELS = 25;
	private static final double STANDARD_TEMPERATURE_OFFSET = 273.15;
	private static final double PASCALS_PER_MMHG = 101325.0 / 760.0;

	@XmlElement(name = RockSimCommonConstants.FINAL_STATE)
	private final int finalState = ROCKSIM_NOT_SIMULATED;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_GUIDE_TYPE)
	private final int launchGuideType = 0;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_GUIDE_LEN)
	private double launchGuideLength;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_DIRECTION)
	private final int launchWindDirection = 0;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_SPEED)
	private final double launchWindSpeed = 0.0;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_DIRECTION)
	private final int launchDirection = 0;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_ANGLE)
	private double launchAngle;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_GUIDE_AZIMUTH)
	private double launchGuideAzimuth;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_BAROMETER)
	private double launchBarometer;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_LATITUDE)
	private double launchLatitude;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_LONGITUDE)
	private double launchLongitude;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_HUMIDITY)
	private double launchHumidity;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_TEMPERATURE)
	private double launchTemperature;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_ALTITUDE)
	private double launchAltitude;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_LANDING_ALTITUDE)
	private double launchLandingAltitude;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_PRESET)
	private final int launchWindPreset = ROCKSIM_CUSTOM_PRESET;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_LOW_SPEED)
	private double launchWindLowSpeed;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_HIGH_SPEED)
	private double launchWindHighSpeed;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_TURBULENCE_PRESET)
	private final int launchWindTurbulencePreset = ROCKSIM_CUSTOM_PRESET;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_USE_RANDOM_CONDITIONS)
	private final int launchUseRandomConditions = 0;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_TABLE_SIZE)
	private int launchWindTableSize;
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_ALT_TABLE)
	private String launchWindAltitudeTable = zeroWindTable();
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_SPEED_TABLE)
	private String launchWindSpeedTable = zeroWindTable();
	@XmlElement(name = RockSimCommonConstants.LAUNCH_WIND_DIRECTION_TABLE)
	private String launchWindDirectionTable = zeroWindTable();
	@XmlElement(name = RockSimCommonConstants.CNA_MULTIPLIER)
	private final double cnaMultiplier = 1.0;
	@XmlElement(name = RockSimCommonConstants.CD_MULTIPLIER)
	private final double cdMultiplier = 1.0;
	@XmlElement(name = RockSimCommonConstants.CP_OFFSET)
	private final double cpOffset = 0.0;
	@XmlElement(name = RockSimCommonConstants.MAX_SIM_TIME)
	private double maxSimulationTime;
	@XmlElement(name = RockSimCommonConstants.SIMULATION_NAME)
	private String simulationName = "";
	@XmlElement(name = RockSimCommonConstants.COMMENTS)
	private String comments = "";
	@XmlElement(name = RockSimCommonConstants.CALC_RESOLUTION)
	private final int calculateResolution = 1;
	@XmlElement(name = RockSimCommonConstants.SIMULATION_TYPE)
	private final int simulationType = ROCKSIM_RUNGE_KUTTA;
	@XmlElement(name = RockSimCommonConstants.CALCULATION_FLAGS)
	private final int calculationFlags = 1;
	@XmlElementWrapper(name = RockSimCommonConstants.SIMULATION_EVENTS)
	@XmlElement(name = RockSimCommonConstants.SIMULATION_EVENT)
	private final List<SimulationEventDTO> simulationEvents = new ArrayList<>();

	@XmlElementWrapper(name = RockSimCommonConstants.STAGE_1_ENGINES)
	@XmlElement(name = RockSimCommonConstants.ENGINE_SET)
	private final List<EngineSetDTO> stage1Engines = new ArrayList<>();
	@XmlElementWrapper(name = RockSimCommonConstants.STAGE_2_ENGINES)
	@XmlElement(name = RockSimCommonConstants.ENGINE_SET)
	private final List<EngineSetDTO> stage2Engines = new ArrayList<>();
	@XmlElementWrapper(name = RockSimCommonConstants.STAGE_3_ENGINES)
	@XmlElement(name = RockSimCommonConstants.ENGINE_SET)
	private final List<EngineSetDTO> stage3Engines = new ArrayList<>();

	/**
	 * Constructor required by JAXB.
	 */
	public SimulationResultsDTO() {
	}

	/**
	 * Convert an OpenRocket simulation into inputs RockSim can recalculate.
	 *
	 * @param simulation the OpenRocket simulation
	 * @param context    the component-to-mount-serial mapping for this export
	 */
	public SimulationResultsDTO(Simulation simulation, RockSimExportContext context) {
		comments = simulation.getName();
		copyLaunchConditions(simulation.getOptions());
		copyMotors(simulation, context);
		copyRecoveryEvents(simulation, context);
		simulationName = formatMotorSummary();
	}

	private void copyLaunchConditions(SimulationOptions options) {
		launchGuideLength = options.getLaunchRodLength() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
		launchAngle = options.getLaunchRodAngle();
		// RockSim-written RKT files store compass azimuths in degrees.
		launchGuideAzimuth = Math.toDegrees(options.getLaunchRodDirection());
		launchBarometer = options.getLaunchPressure() / PASCALS_PER_MMHG;
		launchLatitude = options.getLaunchLatitude();
		launchLongitude = options.getLaunchLongitude();
		launchHumidity = options.getLaunchRelativeHumidity() * 100.0;
		launchTemperature = options.getLaunchTemperature() - STANDARD_TEMPERATURE_OFFSET;
		launchAltitude = options.getLaunchAltitude();
		launchLandingAltitude = options.getLaunchAltitude();
		maxSimulationTime = options.getMaxSimulationTime();

		if (options.getWindModelType() == WindModelType.MULTI_LEVEL) {
			copyMultiLevelWind(options);
		} else {
			copyAverageWind(options.getAverageWindModel());
		}
	}

	private void copyAverageWind(PinkNoiseWindModel windModel) {
		// RockSim's fixed custom-wind range preserves the OpenRocket mean.  The two
		// applications do not expose equivalent pink-noise turbulence parameters.
		launchWindLowSpeed = windModel.getAverage();
		launchWindHighSpeed = windModel.getAverage();
	}

	private void copyMultiLevelWind(SimulationOptions options) {
		MultiLevelPinkNoiseWindModel windModel = options.getMultiLevelWindModel();
		List<LevelWindModel> levels = windModel.getLevels();
		launchWindTableSize = Math.min(levels.size(), MAX_WIND_LEVELS);

		if (!levels.isEmpty()) {
			launchWindLowSpeed = levels.get(0).getSpeed();
			launchWindHighSpeed = levels.get(0).getSpeed();
		}

		double[] altitudes = new double[MAX_WIND_LEVELS];
		double[] speeds = new double[MAX_WIND_LEVELS];
		double[] directions = new double[MAX_WIND_LEVELS];
		for (int i = 0; i < launchWindTableSize; i++) {
			LevelWindModel level = levels.get(i);
			double altitude = level.getAltitude();
			if (windModel.getAltitudeReference() == AltitudeReference.AGL) {
				// RockSim wind-table altitudes are above sea level (ASL).
				altitude += options.getLaunchAltitude();
			}
			altitudes[i] = altitude;
			speeds[i] = level.getSpeed();
			// RockSim-written wind tables use compass degrees, despite older format
			// documentation describing this value as radians.
			directions[i] = Math.toDegrees(level.getDirection());
		}

		launchWindAltitudeTable = formatWindTable(altitudes);
		launchWindSpeedTable = formatWindTable(speeds);
		launchWindDirectionTable = formatWindTable(directions);
	}

	private void copyMotors(Simulation simulation, RockSimExportContext context) {
		FlightConfiguration flightConfiguration = simulation.getActiveConfiguration();
		for (MotorConfiguration motorConfiguration : flightConfiguration.getActiveMotors()) {
			if (motorConfiguration.getIgnitionEvent() == IgnitionEvent.NEVER) {
				// RockSim has no never-ignite flag.  Omitting the motor is safer than
				// turning a deliberately disabled motor into an automatically staged one.
				continue;
			}
			RocketComponent mount = (RocketComponent) motorConfiguration.getMount();
			double ignitionDelay = getRockSimIgnitionDelay(motorConfiguration, simulation);
			double ejectionDelay = getRockSimEjectionDelay(motorConfiguration, simulation);
			for (int serialNumber : context.getMotorMountSerialNumbers(motorConfiguration.getMount())) {
				EngineSetDTO engine = new EngineSetDTO(motorConfiguration, serialNumber, ignitionDelay, ejectionDelay);
				switch (mount.getStage().getStageNumber()) {
					case 0:
						stage3Engines.add(engine);
						break;
					case 1:
						stage2Engines.add(engine);
						break;
					case 2:
						stage1Engines.add(engine);
						break;
					default:
						// RockSim supports at most three axial stages.
						break;
				}
			}
		}

		Comparator<EngineSetDTO> byMountSerial = Comparator.comparingInt(EngineSetDTO::getMountSerialNumber);
		stage1Engines.sort(byMountSerial);
		stage2Engines.sort(byMountSerial);
		stage3Engines.sort(byMountSerial);
	}

	private double getRockSimEjectionDelay(MotorConfiguration motorConfiguration, Simulation simulation) {
		RocketComponent mount = (RocketComponent) motorConfiguration.getMount();
		AxialStage stage = mount.getStage();
		if (stage.getUpperStage() == null) {
			return motorConfiguration.getEjectionDelay();
		}

		StageSeparationConfiguration separation = stage.getSeparationConfigurations()
				.get(simulation.getFlightConfigurationId());
		SeparationEvent event = separation.getSeparationEvent();
		if (event == SeparationEvent.NEVER) {
			return Motor.PLUGGED_DELAY;
		}
		if (event == SeparationEvent.EJECTION) {
			if (motorConfiguration.getEjectionDelay() == Motor.PLUGGED_DELAY) {
				return Motor.PLUGGED_DELAY;
			}
			return motorConfiguration.getEjectionDelay() + separation.getSeparationDelay();
		}
		if (event == SeparationEvent.BURNOUT) {
			// RockSim stages a booster when its ejection delay expires.  A zero
			// ejection delay therefore reproduces burnout staging.
			return separation.getSeparationDelay();
		}
		if (event == SeparationEvent.UPPER_IGNITION) {
			// RockSim couples upper-stage ignition and separation.  The exported
			// upper-stage ignition delay therefore also supplies the separation time.
			return separation.getSeparationDelay();
		}

		// RockSim Standard cannot stage on altitude, apogee, launch, or ignition.
		// Keep those stages attached instead of inventing a potentially unsafe time.
		return Motor.PLUGGED_DELAY;
	}

	private double getRockSimIgnitionDelay(MotorConfiguration motorConfiguration, Simulation simulation) {
		RocketComponent mount = (RocketComponent) motorConfiguration.getMount();
		AxialStage stage = mount.getStage();
		AxialStage lowerStage = getLowerActiveStage(stage, simulation.getActiveConfiguration());
		if (lowerStage == null) {
			return motorConfiguration.getIgnitionDelay();
		}

		StageSeparationConfiguration separation = lowerStage.getSeparationConfigurations()
				.get(simulation.getFlightConfigurationId());
		double separationDelay = separation.getSeparationDelay();
		IgnitionEvent ignitionEvent = motorConfiguration.getIgnitionEvent();
		if (separation.getSeparationEvent() == SeparationEvent.UPPER_IGNITION) {
			return motorConfiguration.getIgnitionDelay();
		}
		if (ignitionEvent == IgnitionEvent.BURNOUT && separation.getSeparationEvent() == SeparationEvent.BURNOUT) {
			return Math.max(0.0, motorConfiguration.getIgnitionDelay() - separationDelay);
		}
		if ((ignitionEvent == IgnitionEvent.EJECTION_CHARGE || ignitionEvent == IgnitionEvent.AUTOMATIC)
				&& separation.getSeparationEvent() == SeparationEvent.EJECTION) {
			return Math.max(0.0, motorConfiguration.getIgnitionDelay() - separationDelay);
		}

		// RockSim cannot ignite an upper-stage motor before the stage separates.
		return 0.0;
	}

	private AxialStage getLowerActiveStage(AxialStage stage, FlightConfiguration configuration) {
		AxialStage lowerStage = null;
		for (AxialStage candidate : configuration.getActiveStages()) {
			if (candidate.getStageNumber() > stage.getStageNumber()
					&& (lowerStage == null || candidate.getStageNumber() < lowerStage.getStageNumber())) {
				lowerStage = candidate;
			}
		}
		return lowerStage;
	}

	private void copyRecoveryEvents(Simulation simulation, RockSimExportContext context) {
		FlightConfigurationId configurationId = simulation.getFlightConfigurationId();
		for (RocketComponent component : simulation.getRocket()) {
			if (!(component instanceof RecoveryDevice)) {
				continue;
			}
			RecoveryDevice device = (RecoveryDevice) component;
			if (!simulation.getActiveConfiguration().isComponentActive(device)) {
				continue;
			}
			DeploymentConfiguration configuration = device.getDeploymentConfigurations().get(configurationId);
			for (int serialNumber : context.getRecoveryDeviceSerialNumbers(device)) {
				simulationEvents.add(new SimulationEventDTO(device, configuration, serialNumber));
			}
		}
	}

	private String formatMotorSummary() {
		StringBuilder result = new StringBuilder();
		appendMotorGroup(result, stage1Engines);
		appendMotorGroup(result, stage2Engines);
		appendMotorGroup(result, stage3Engines);
		return result.toString();
	}

	private void appendMotorGroup(StringBuilder result, List<EngineSetDTO> engines) {
		if (engines.isEmpty()) {
			return;
		}

		StringJoiner group = new StringJoiner(", ", "[", "] ");
		for (EngineSetDTO engine : engines) {
			group.add(engine.getDisplayName());
		}
		result.append(group);
	}

	private static String zeroWindTable() {
		return formatWindTable(new double[MAX_WIND_LEVELS]);
	}

	private static String formatWindTable(double[] values) {
		StringJoiner result = new StringJoiner(",");
		for (double value : values) {
			result.add(Double.toString(value));
		}
		return result.toString();
	}

	public String getSimulationName() {
		return simulationName;
	}

	public String getComments() {
		return comments;
	}

	public List<SimulationEventDTO> getSimulationEvents() {
		return simulationEvents;
	}

	public double getLaunchGuideLength() {
		return launchGuideLength;
	}

	public double getLaunchAngle() {
		return launchAngle;
	}

	public double getLaunchGuideAzimuth() {
		return launchGuideAzimuth;
	}

	public double getLaunchBarometer() {
		return launchBarometer;
	}

	public double getLaunchLatitude() {
		return launchLatitude;
	}

	public double getLaunchLongitude() {
		return launchLongitude;
	}

	public double getLaunchHumidity() {
		return launchHumidity;
	}

	public double getLaunchTemperature() {
		return launchTemperature;
	}

	public double getLaunchAltitude() {
		return launchAltitude;
	}

	public double getLaunchWindLowSpeed() {
		return launchWindLowSpeed;
	}

	public double getLaunchWindHighSpeed() {
		return launchWindHighSpeed;
	}

	public int getLaunchWindTableSize() {
		return launchWindTableSize;
	}

	public String getLaunchWindAltitudeTable() {
		return launchWindAltitudeTable;
	}

	public String getLaunchWindSpeedTable() {
		return launchWindSpeedTable;
	}

	public String getLaunchWindDirectionTable() {
		return launchWindDirectionTable;
	}

	public List<EngineSetDTO> getStage1Engines() {
		return stage1Engines;
	}

	public List<EngineSetDTO> getStage2Engines() {
		return stage2Engines;
	}

	public List<EngineSetDTO> getStage3Engines() {
		return stage3Engines;
	}
}
