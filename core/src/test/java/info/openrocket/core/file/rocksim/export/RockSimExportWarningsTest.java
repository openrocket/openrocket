package info.openrocket.core.file.rocksim.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.rocksim.importt.RockSimTestBase;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.models.wind.MultiLevelPinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration;
import info.openrocket.core.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

import jakarta.xml.bind.JAXBContext;

/**
 * Tests the warnings for design and simulation features that the RockSim export cannot represent.
 */
public class RockSimExportWarningsTest extends RockSimTestBase {

	@Test
	public void warnsAboutComponentsRockSimCannotRepresent() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		BodyTube body = makeBodyTube("Body");
		rocket.getStage(0).addChild(body);
		RailButton button = new RailButton();
		button.setName("Rail button");
		body.addChild(button);

		WarningSet warnings = new WarningSet();
		new RockSimSaver().marshalToRockSim(document, warnings);

		assertWarning(warnings, "Rail button");
		assertEquals(1, warnings.size());
	}

	@Test
	public void exportsAtMostThreeStages() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		rocket.getStage(0).addChild(makeBodyTube("Body"));
		for (int i = 1; i <= 3; i++) {
			AxialStage stage = new AxialStage();
			stage.setName("Booster " + i);
			rocket.addChild(stage);
			stage.addChild(makeBodyTube("Booster body " + i));
		}

		WarningSet warnings = new WarningSet();
		String result = new RockSimSaver().marshalToRockSim(document, warnings);
		RocketDesignDTO design = ((RockSimDocumentDTO) JAXBContext.newInstance(RockSimDocumentDTO.class)
				.createUnmarshaller().unmarshal(new StringReader(result))).getDesign().getDesign();

		assertEquals(3, design.getStageCount());
		assertWarning(warnings, "Booster 3");
		assertEquals(1, warnings.size());
	}

	@Test
	public void warnsAboutSimulationFeaturesRockSimCannotRepresent() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		BodyTube sustainerBody = makeBodyTube("Sustainer body");
		rocket.getStage(0).addChild(sustainerBody);
		InnerTube sustainerMount = makeMotorMount("Sustainer mount");
		sustainerBody.addChild(sustainerMount);
		AxialStage booster = new AxialStage();
		booster.setName("Booster");
		rocket.addChild(booster);
		BodyTube boosterBody = makeBodyTube("Booster body");
		booster.addChild(boosterBody);
		InnerTube boosterMount = makeMotorMount("Booster mount");
		boosterBody.addChild(boosterMount);

		FlightConfigurationId id = new FlightConfigurationId();
		rocket.createFlightConfiguration(id).setAllStages();
		configureMotor(sustainerMount, id, makeMotor("Ellis Mountain", "H50"), IgnitionEvent.NEVER);
		configureMotor(boosterMount, id, makeMotor("Ellis Mountain", "G37"), IgnitionEvent.AUTOMATIC);
		StageSeparationConfiguration separation = new StageSeparationConfiguration();
		separation.setSeparationEvent(SeparationEvent.APOGEE);
		booster.getSeparationConfigurations().set(id, separation);
		rocket.getFlightConfiguration(id).setAllStages();

		Simulation simulation = new Simulation(document, rocket);
		simulation.setName("Flight");
		simulation.setFlightConfigurationId(id);
		document.addSimulation(simulation);

		WarningSet warnings = new WarningSet();
		new RockSimSaver().marshalToRockSim(document, warnings);

		assertWarning(warnings, "H50");		// never ignites
		assertWarning(warnings, "Booster");	// separates at apogee
		assertWarning(warnings, "G37");		// no RockSim engine file for the manufacturer
		assertEquals(3, warnings.size());
	}

	/**
	 * RockSim's standard simulation only uses the lowest wind level of a multi-level wind.
	 */
	@Test
	public void warnsAboutMultiLevelWind() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		document.getRocket().getStage(0).addChild(makeBodyTube("Body"));
		Simulation simulation = new Simulation(document, document.getRocket());
		simulation.setName("Windy");
		simulation.getOptions().setWindModelType(WindModelType.MULTI_LEVEL);
		MultiLevelPinkNoiseWindModel wind = simulation.getOptions().getMultiLevelWindModel();
		wind.clearLevels();
		wind.addWindLevel(0.0, 3.0, 0.0, 0.3);
		wind.addWindLevel(1000.0, 9.0, 0.0, 0.9);
		document.addSimulation(simulation);

		WarningSet warnings = new WarningSet();
		new RockSimSaver().marshalToRockSim(document, warnings);

		assertWarning(warnings, "Windy");
		assertEquals(1, warnings.size());
	}

	/**
	 * RockSim cannot delay a deployment after the ejection charge, nor deploy a device when the lower stage
	 * separates.
	 */
	@Test
	public void warnsAboutRecoveryEventsRockSimCannotRepresent() throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		BodyTube body = makeBodyTube("Body");
		rocket.getStage(0).addChild(body);
		Parachute main = new Parachute();
		main.setName("Main");
		body.addChild(main);
		Streamer streamer = new Streamer();
		streamer.setName("Streamer");
		body.addChild(streamer);

		DeploymentConfiguration ejection = main.getDeploymentConfigurations().getDefault();
		ejection.setDeployEvent(DeploymentConfiguration.DeployEvent.EJECTION);
		ejection.setDeployDelay(2.0);
		streamer.getDeploymentConfigurations().getDefault()
				.setDeployEvent(DeploymentConfiguration.DeployEvent.LOWER_STAGE_SEPARATION);

		Simulation simulation = new Simulation(document, rocket);
		document.addSimulation(simulation);

		WarningSet warnings = new WarningSet();
		new RockSimSaver().marshalToRockSim(document, warnings);

		assertWarning(warnings, "Main");
		assertWarning(warnings, "Streamer");
		assertEquals(2, warnings.size());
	}

	private void assertWarning(WarningSet warnings, String text) {
		for (Warning warning : warnings) {
			if (warning.toString().contains("'" + text + "'")) {
				return;
			}
		}
		assertTrue(false, "No warning about '" + text + "' in " + warnings);
	}

	private BodyTube makeBodyTube(String name) {
		BodyTube tube = new BodyTube();
		tube.setName(name);
		tube.setLength(0.5);
		tube.setOuterRadius(0.04);
		tube.setThickness(0.002);
		return tube;
	}

	private InnerTube makeMotorMount(String name) {
		InnerTube mount = new InnerTube();
		mount.setName(name);
		mount.setLength(0.15);
		mount.setOuterRadius(0.015);
		mount.setThickness(0.001);
		mount.setMotorMount(true);
		return mount;
	}

	private void configureMotor(InnerTube mount, FlightConfigurationId id, ThrustCurveMotor motor,
			IgnitionEvent ignitionEvent) {
		MotorConfiguration configuration = new MotorConfiguration(mount, id);
		configuration.setMotor(motor);
		configuration.setIgnitionEvent(ignitionEvent);
		mount.setMotorConfig(configuration, id);
	}

	private ThrustCurveMotor makeMotor(String manufacturer, String designation) {
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer(manufacturer))
				.setDesignation(designation)
				.setCommonName(designation)
				.setMotorType(Motor.Type.RELOAD)
				.setStandardDelays(new double[] { 0.0 })
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
}
