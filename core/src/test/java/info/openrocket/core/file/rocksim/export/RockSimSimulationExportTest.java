package info.openrocket.core.file.rocksim.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rocksim.importt.RockSimTestBase;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModel.AltitudeReference;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ClusterConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;

/**
 * Tests RockSim simulation, motor-configuration, and launch-condition export.
 */
public class RockSimSimulationExportTest extends RockSimTestBase {

	private static final double EPSILON = 1.0e-9;

	@Test
	public void exportsPhysicalMotorMountsAndLaunchConditionsForEachSimulation() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		AxialStage sustainer = rocket.getStage(0);

		BodyTube sustainerBody = makeBodyTube("Sustainer body");
		sustainer.addChild(sustainerBody);
		InnerTube clusteredMount = makeMotorMount("Cluster mount", 0.012);
		clusteredMount.setClusterConfiguration(ClusterConfiguration.CONFIGURATIONS[1]);
		sustainerBody.addChild(clusteredMount);
		InnerTube auxiliaryMount = makeMotorMount("Auxiliary mount", 0.004);
		sustainerBody.addChild(auxiliaryMount);
		Parachute main = new Parachute();
		main.setName("Main");
		sustainerBody.addChild(main);
		Streamer drogue = new Streamer();
		drogue.setName("Drogue");
		sustainerBody.addChild(drogue);

		AxialStage booster = new AxialStage();
		booster.setName("Booster");
		rocket.addChild(booster);
		BodyTube boosterBody = makeBodyTube("Booster body");
		booster.addChild(boosterBody);
		InnerTube boosterMount = makeMotorMount("Booster mount", 0.008);
		boosterBody.addChild(boosterMount);

		FlightConfigurationId firstConfiguration = new FlightConfigurationId();
		rocket.createFlightConfiguration(firstConfiguration).setAllStages();
		configureMotor(clusteredMount, firstConfiguration, makeMotor("AeroTech", "H148R"),
				Motor.PLUGGED_DELAY, 1.25, IgnitionEvent.BURNOUT);
		configureMotor(auxiliaryMount, firstConfiguration, makeMotor("Estes", "C6"),
				5.0, 0.0, IgnitionEvent.LAUNCH);
		configureMotor(boosterMount, firstConfiguration, makeMotor("Estes", "D12"),
				7.0, 0.3, IgnitionEvent.AUTOMATIC);
		configureSeparation(booster, firstConfiguration, SeparationEvent.BURNOUT, 0.5);
		configureDeployment(main, firstConfiguration, DeployEvent.ALTITUDE, 0.0, 250.0);
		configureDeployment(drogue, firstConfiguration, DeployEvent.APOGEE, 2.0, 0.0);
		rocket.getFlightConfiguration(firstConfiguration).setAllStages();

		FlightConfigurationId secondConfiguration = new FlightConfigurationId();
		rocket.createFlightConfiguration(secondConfiguration).setAllStages();
		configureMotor(clusteredMount, secondConfiguration, makeMotor("AeroTech", "I59WN"),
				Motor.PLUGGED_DELAY, 0.4, IgnitionEvent.BURNOUT);
		configureMotor(boosterMount, secondConfiguration, makeMotor("AeroTech", "I357T"),
				14.0, 0.0, IgnitionEvent.AUTOMATIC);
		configureSeparation(booster, secondConfiguration, SeparationEvent.UPPER_IGNITION, 0.0);
		configureDeployment(main, secondConfiguration, DeployEvent.LOWER_STAGE_SEPARATION, 0.0, 0.0);
		configureDeployment(drogue, secondConfiguration, DeployEvent.LAUNCH, 1.5, 0.0);
		rocket.getFlightConfiguration(secondConfiguration).setAllStages();

		Simulation averageWindSimulation = new Simulation(document, rocket);
		averageWindSimulation.setName("Average wind configuration");
		averageWindSimulation.setFlightConfigurationId(firstConfiguration);
		setAverageWindLaunchConditions(averageWindSimulation.getOptions());
		document.addSimulation(averageWindSimulation);

		Simulation multiLevelWindSimulation = new Simulation(document, rocket);
		multiLevelWindSimulation.setName("Multi-level wind configuration");
		multiLevelWindSimulation.setFlightConfigurationId(secondConfiguration);
		setMultiLevelWindLaunchConditions(multiLevelWindSimulation.getOptions());
		document.addSimulation(multiLevelWindSimulation);

		// Export twice because save dialogs commonly estimate size before writing the
		// file.  The second document must retain valid, repeatable component references.
		RockSimDocumentDTO firstExport = marshalAndRead(document);
		RockSimDocumentDTO exported = marshalAndRead(document);
		List<SimulationResultsDTO> simulations = exported.getSimulationResultsList().getSimulations();
		assertEquals(2, simulations.size());
		int firstComponentSerial = firstExport.getDesign().getDesign().getStage3().getExternalPart().get(0)
				.getSerialNumber();
		int repeatedComponentSerial = exported.getDesign().getDesign().getStage3().getExternalPart().get(0)
				.getSerialNumber();
		assertEquals(firstComponentSerial, repeatedComponentSerial,
				"Repeated exports must assign the same component serials.");
		assertTrue(repeatedComponentSerial > 0,
				"RockSim reserves component serial zero for an absent component reference.");

		Set<Integer> clusteredMountSerials = findMountSerials(exported, "Cluster mount");
		Set<Integer> auxiliaryMountSerials = findMountSerials(exported, "Auxiliary mount");
		Set<Integer> boosterMountSerials = findMountSerials(exported, "Booster mount");
		assertEquals(2, clusteredMountSerials.size(), "A two-motor cluster must export two physical mounts.");
		assertEquals(1, auxiliaryMountSerials.size());
		assertEquals(1, boosterMountSerials.size());

		SimulationResultsDTO first = simulations.get(0);
		assertEquals("[D12-0.5-0.30] [H148R-Plugged-0.75, H148R-Plugged-0.75, C6-5] ",
				first.getSimulationName());
		assertEquals("Average wind configuration", first.getComments());
		assertEquals(3, first.getStage3Engines().size());
		assertEquals(1, first.getStage2Engines().size());
		assertEquals(clusteredMountSerials, serialsForMotor(first.getStage3Engines(), "H148R"));
		assertEquals(auxiliaryMountSerials, serialsForMotor(first.getStage3Engines(), "C6"));
		assertEquals(boosterMountSerials, serialsForMotor(first.getStage2Engines(), "D12"));
		assertEngine(first.getStage3Engines(), "H148R", "AeroTech", 0.75, -2.0, 12.0);
		assertEngine(first.getStage2Engines(), "D12", "Estes", 0.3, 0.5, 8.0);
		assertRecoveryEvent(exported, first, "Main", 5, 250.0, 0.0);
		assertRecoveryEvent(exported, first, "Drogue", 3, 0.0, 2.0);
		assertAverageWindLaunchConditions(first);

		SimulationResultsDTO second = simulations.get(1);
		assertEquals("[I357T-0] [I59WN-Plugged-0.40, I59WN-Plugged-0.40] ", second.getSimulationName());
		assertEquals("Multi-level wind configuration", second.getComments());
		assertEquals(2, second.getStage3Engines().size());
		assertEquals(1, second.getStage2Engines().size());
		assertEquals(clusteredMountSerials, serialsForMotor(second.getStage3Engines(), "I59WN"));
		assertEquals(boosterMountSerials, serialsForMotor(second.getStage2Engines(), "I357T"));
		assertEngine(second.getStage2Engines(), "I357T", "AeroTech", 0.0, 0.0, 8.0);
		assertRecoveryEvent(exported, second, "Main", 0, 0.0, 0.0);
		assertRecoveryEvent(exported, second, "Drogue", 2, 0.0, 1.5);
		assertMultiLevelWindLaunchConditions(second);
	}

	private RockSimDocumentDTO marshalAndRead(OpenRocketDocument document) throws Exception {
		String result = new RockSimSaver().marshalToRockSim(document);
		assertNotNull(result);
		assertTrue(result.contains("<SimulationResultsList>"));

		JAXBContext binder = JAXBContext.newInstance(RockSimDocumentDTO.class);
		Unmarshaller unmarshaller = binder.createUnmarshaller();
		return (RockSimDocumentDTO) unmarshaller.unmarshal(new StringReader(result));
	}

	private BodyTube makeBodyTube(String name) {
		BodyTube tube = new BodyTube();
		tube.setName(name);
		tube.setLength(0.5);
		tube.setOuterRadius(0.04);
		tube.setThickness(0.002);
		return tube;
	}

	private InnerTube makeMotorMount(String name, double overhang) {
		InnerTube mount = new InnerTube();
		mount.setName(name);
		mount.setLength(0.15);
		mount.setOuterRadius(0.015);
		mount.setThickness(0.001);
		mount.setMotorMount(true);
		mount.setMotorOverhang(overhang);
		return mount;
	}

	private void configureMotor(InnerTube mount, FlightConfigurationId configurationId, ThrustCurveMotor motor,
			double ejectionDelay, double ignitionDelay, IgnitionEvent ignitionEvent) {
		MotorConfiguration configuration = new MotorConfiguration(mount, configurationId);
		configuration.setMotor(motor);
		configuration.setEjectionDelay(ejectionDelay);
		configuration.setIgnitionDelay(ignitionDelay);
		configuration.setIgnitionEvent(ignitionEvent);
		mount.setMotorConfig(configuration, configurationId);
	}

	private void configureSeparation(AxialStage stage, FlightConfigurationId configurationId,
			SeparationEvent event, double delay) {
		StageSeparationConfiguration configuration = new StageSeparationConfiguration();
		configuration.setSeparationEvent(event);
		configuration.setSeparationDelay(delay);
		stage.getSeparationConfigurations().set(configurationId, configuration);
	}

	private void configureDeployment(RecoveryDevice device, FlightConfigurationId configurationId,
			DeployEvent event, double delay, double altitude) {
		DeploymentConfiguration configuration = new DeploymentConfiguration();
		configuration.setDeployEvent(event);
		configuration.setDeployDelay(delay);
		configuration.setDeployAltitude(altitude);
		device.getDeploymentConfigurations().set(configurationId, configuration);
	}

	private ThrustCurveMotor makeMotor(String manufacturer, String designation) {
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer(manufacturer))
				.setDesignation(designation)
				.setCommonName(designation)
				.setMotorType(Motor.Type.RELOAD)
				.setStandardDelays(new double[] { 0.0, 5.0, Motor.PLUGGED_DELAY })
				.setDiameter(0.029)
				.setLength(0.12)
				.setTimePoints(new double[] { 0.0, 0.5, 1.0 })
				.setThrustPoints(new double[] { 0.0, 100.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.06, 0.0, 0.0, 0.1),
						new Coordinate(0.06, 0.0, 0.0, 0.08),
						new Coordinate(0.06, 0.0, 0.0, 0.06) })
				.setDigest(manufacturer + "-" + designation)
				.build();
	}

	private void setAverageWindLaunchConditions(SimulationOptions options) {
		options.setLaunchRodLength(3.048);
		options.setLaunchRodAngle(Math.toRadians(5.0));
		options.setLaunchIntoWind(false);
		options.setLaunchRodDirection(Math.toRadians(123.0));
		options.setLaunchAltitude(350.0);
		options.setISAAtmosphere(false);
		options.setLaunchTemperature(285.15);
		options.setLaunchPressure(95000.0);
		options.setLaunchRelativeHumidity(0.65);
		options.setLaunchLatitude(50.9);
		options.setLaunchLongitude(4.4);
		options.getAverageWindModel().setAverage(7.5);
		options.getAverageWindModel().setStandardDeviation(1.2);
		options.getAverageWindModel().setDirection(Math.toRadians(210.0));
		options.setMaxSimulationTime(600.0);
	}

	private void setMultiLevelWindLaunchConditions(SimulationOptions options) {
		options.setLaunchAltitude(100.0);
		options.setLaunchIntoWind(false);
		options.setLaunchRodDirection(Math.toRadians(10.0));
		options.setWindModelType(WindModelType.MULTI_LEVEL);
		MultiLevelPinkNoiseWindModel wind = options.getMultiLevelWindModel();
		wind.clearLevels();
		wind.setAltitudeReference(AltitudeReference.AGL);
		wind.addWindLevel(0.0, 3.0, Math.toRadians(45.0), 0.3);
		wind.addWindLevel(1000.0, 9.0, Math.toRadians(270.0), 0.9);
	}

	private Set<Integer> findMountSerials(RockSimDocumentDTO document, String namePrefix) {
		Set<Integer> serialNumbers = new HashSet<>();
		RocketDesignDTO design = document.getDesign().getDesign();
		collectMountSerials(design.getStage3().getExternalPart(), namePrefix, serialNumbers);
		collectMountSerials(design.getStage2().getExternalPart(), namePrefix, serialNumbers);
		collectMountSerials(design.getStage1().getExternalPart(), namePrefix, serialNumbers);
		return serialNumbers;
	}

	private void collectMountSerials(List<BasePartDTO> parts, String namePrefix, Set<Integer> serialNumbers) {
		for (BasePartDTO part : parts) {
			if (part instanceof BodyTubeDTO) {
				BodyTubeDTO tube = (BodyTubeDTO) part;
				if (tube.getMotorMount() == 1 && tube.getName().startsWith(namePrefix)) {
					serialNumbers.add(tube.getSerialNumber());
				}
				collectMountSerials(tube.getAttachedParts(), namePrefix, serialNumbers);
			}
		}
	}

	private Set<Integer> serialsForMotor(List<EngineSetDTO> engines, String designation) {
		Set<Integer> serialNumbers = new HashSet<>();
		for (EngineSetDTO engine : engines) {
			if (designation.equals(engine.getEngineCode())) {
				serialNumbers.add(engine.getMountSerialNumber());
			}
		}
		return serialNumbers;
	}

	private void assertRecoveryEvent(RockSimDocumentDTO document, SimulationResultsDTO simulation,
			String deviceName, int type,
			double altitude, double time) {
		Set<Integer> serials = findRecoverySerials(document, deviceName);
		List<SimulationEventDTO> matching = new ArrayList<>();
		for (SimulationEventDTO event : simulation.getSimulationEvents()) {
			if (serials.contains(event.getPartSerialNumber())) {
				matching.add(event);
			}
		}
		assertEquals(1, matching.size());
		SimulationEventDTO event = matching.get(0);
		assertEquals(type, event.getType());
		assertEquals(altitude, event.getDeployAltitude(), EPSILON);
		assertEquals(time, event.getDeployTime(), EPSILON);
	}

	private Set<Integer> findRecoverySerials(RockSimDocumentDTO document, String name) {
		Set<Integer> serialNumbers = new HashSet<>();
		RocketDesignDTO design = document.getDesign().getDesign();
		collectRecoverySerials(design.getStage3().getExternalPart(), name, serialNumbers);
		collectRecoverySerials(design.getStage2().getExternalPart(), name, serialNumbers);
		collectRecoverySerials(design.getStage1().getExternalPart(), name, serialNumbers);
		return serialNumbers;
	}

	private void collectRecoverySerials(List<BasePartDTO> parts, String name, Set<Integer> serialNumbers) {
		for (BasePartDTO part : parts) {
			if ((part instanceof ParachuteDTO || part instanceof StreamerDTO) && name.equals(part.getName())) {
				serialNumbers.add(part.getSerialNumber());
			}
			if (part instanceof BodyTubeDTO) {
				collectRecoverySerials(((BodyTubeDTO) part).getAttachedParts(), name, serialNumbers);
			}
		}
	}

	private void assertEngine(List<EngineSetDTO> engines, String designation, String manufacturer,
			double ignitionDelay, double ejectionDelay, double overhang) {
		List<EngineSetDTO> matching = new ArrayList<>();
		for (EngineSetDTO engine : engines) {
			if (designation.equals(engine.getEngineCode())) {
				matching.add(engine);
			}
		}
		assertNotEquals(0, matching.size());
		for (EngineSetDTO engine : matching) {
			assertEquals(1, engine.getEngineCount());
			assertEquals(manufacturer, engine.getEngineManufacturer());
			assertEquals(ignitionDelay, engine.getIgnitionDelay(), EPSILON);
			assertEquals(ejectionDelay, engine.getEjectionDelay(), EPSILON);
			assertEquals(overhang, engine.getEngineOverhang(), EPSILON);
		}
	}

	private void assertAverageWindLaunchConditions(SimulationResultsDTO simulation) {
		assertEquals(3048.0, simulation.getLaunchGuideLength(), EPSILON);
		assertEquals(Math.toRadians(5.0), simulation.getLaunchAngle(), EPSILON);
		assertEquals(123.0, simulation.getLaunchGuideAzimuth(), EPSILON);
		assertEquals(95000.0 / (101325.0 / 760.0), simulation.getLaunchBarometer(), EPSILON);
		assertEquals(50.9, simulation.getLaunchLatitude(), EPSILON);
		assertEquals(4.4, simulation.getLaunchLongitude(), EPSILON);
		assertEquals(65.0, simulation.getLaunchHumidity(), EPSILON);
		assertEquals(12.0, simulation.getLaunchTemperature(), EPSILON);
		assertEquals(350.0, simulation.getLaunchAltitude(), EPSILON);
		assertEquals(7.5, simulation.getLaunchWindLowSpeed(), EPSILON);
		assertEquals(7.5, simulation.getLaunchWindHighSpeed(), EPSILON);
		assertEquals(0, simulation.getLaunchWindTableSize());
	}

	private void assertMultiLevelWindLaunchConditions(SimulationResultsDTO simulation) {
		assertEquals(10.0, simulation.getLaunchGuideAzimuth(), EPSILON);
		assertEquals(2, simulation.getLaunchWindTableSize());
		assertTableStartsWith(simulation.getLaunchWindAltitudeTable(), 100.0, 1100.0);
		assertTableStartsWith(simulation.getLaunchWindSpeedTable(), 3.0, 9.0);
		assertTableStartsWith(simulation.getLaunchWindDirectionTable(), 45.0, 270.0);
	}

	private void assertTableStartsWith(String table, double first, double second) {
		String[] values = table.split(",");
		assertEquals(25, values.length);
		assertEquals(first, Double.parseDouble(values[0]), EPSILON);
		assertEquals(second, Double.parseDouble(values[1]), EPSILON);
	}
}
