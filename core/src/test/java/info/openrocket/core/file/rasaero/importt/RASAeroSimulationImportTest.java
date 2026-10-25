package info.openrocket.core.file.rasaero.importt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.rasaero.RASAeroMotorsLoader;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;

/**
 * Tests the import of RASAero simulations, with the motors of the test design available in the motor database.
 */
public class RASAeroSimulationImportTest {

	private static Injector previousInjector;

	@BeforeAll
	public static void setUp() {
		previousInjector = Application.getInjector();
		ThrustCurveMotorSetDatabase motorDatabase = new ThrustCurveMotorSetDatabase();
		motorDatabase.addMotor(makeMotor("J90W"));
		motorDatabase.addMotor(makeMotor("I170G"));
		motorDatabase.addMotor(makeMotor("J180T"));
		motorDatabase.addMotor(makeMotor("I215R"));
		motorDatabase.addMotor(makeMotor("Contrail", "K707-BG", 0.075, 1.0414));
		motorDatabase.addMotor(makeMotor("Gorilla", "J465GT", 0.054, 0.326));

		Module dbOverrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ComponentPresetDao.class).toInstance(new ComponentPresetDatabase());
				bind(ThrustCurveMotorSetDatabase.class).toInstance(motorDatabase);
				bind(MotorDatabase.class).to(ThrustCurveMotorSetDatabase.class);
			}
		};
		Application.setInjector(Guice.createInjector(Modules.override(new ServicesForTesting()).with(dbOverrides),
				new PluginModule()));
		RASAeroMotorsLoader.clearAllMotors();
	}

	@AfterAll
	public static void tearDown() {
		RASAeroMotorsLoader.clearAllMotors();
		Application.setInjector(previousInjector);
	}

	/**
	 * The nozzle exit diameters of a RASAero simulation are imported into the motor configurations.
	 */
	@Test
	public void importsNozzleExitDiameters() throws Exception {
		OpenRocketDocument document = load(design()
				.replaceFirst("<SustainerNozzleDiameter>0</SustainerNozzleDiameter>",
						"<SustainerNozzleDiameter>0.8</SustainerNozzleDiameter>")
				.replaceFirst("<Booster1NozzleDiameter>0</Booster1NozzleDiameter>",
						"<Booster1NozzleDiameter>0.6</Booster1NozzleDiameter>"));
		FlightConfigurationId configurationId = document.getSimulation(0).getFlightConfigurationId();
		Rocket rocket = document.getRocket();

		assertEquals(0.8 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH,
				findMotorMount(rocket, 0).getMotorConfig(configurationId).getNozzleExitDiameter(), 1e-9);
		assertEquals(0.6 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH,
				findMotorMount(rocket, 1).getMotorConfig(configurationId).getNozzleExitDiameter(), 1e-9);
	}

	/**
	 * In RASAero, the sustainer ignites after the booster burns out, the stages coast together for the booster's
	 * separation delay, and the sustainer coasts for its ignition delay.
	 */
	@Test
	public void importsSustainerIgnitionAfterSeparation() throws Exception {
		OpenRocketDocument document = load(design().replaceFirst("<SustainerIgnitionDelay>0</SustainerIgnitionDelay>",
				"<SustainerIgnitionDelay>1.5</SustainerIgnitionDelay>"));
		FlightConfigurationId configurationId = document.getSimulation(0).getFlightConfigurationId();
		MotorConfiguration sustainerMotor = findMotorMount(document.getRocket(), 0).getMotorConfig(configurationId);

		assertEquals(IgnitionEvent.BURNOUT, sustainerMotor.getIgnitionEvent());
		// Booster separation delay (2 s) + sustainer ignition delay (1.5 s)
		assertEquals(3.5, sustainerMotor.getIgnitionDelay(), 1e-9);
	}

	/**
	 * A booster that is not included in one RASAero simulation is only disabled in that simulation's flight
	 * configuration, and the sustainer then ignites at launch.
	 */
	@Test
	public void excludesBoosterOnlyFromItsSimulation() throws Exception {
		OpenRocketDocument document = load(design().replaceFirst("<IncludeBooster1>True</IncludeBooster1>",
				"<IncludeBooster1>False</IncludeBooster1>"));
		Rocket rocket = document.getRocket();
		int boosterStage = rocket.getChild(1).getStageNumber();
		FlightConfigurationId first = document.getSimulation(0).getFlightConfigurationId();
		FlightConfigurationId second = document.getSimulation(1).getFlightConfigurationId();

		assertFalse(rocket.getFlightConfiguration(first).isStageActive(boosterStage));
		assertTrue(rocket.getFlightConfiguration(second).isStageActive(boosterStage));
		assertTrue(findMotorMount(rocket, 1).isMotorMount());
		assertEquals(IgnitionEvent.AUTOMATIC, findMotorMount(rocket, 0).getMotorConfig(first).getIgnitionEvent());
	}

	/**
	 * A simulation without IncludeBooster elements is imported without its boosters instead of failing.
	 */
	@Test
	public void importsSimulationsWithoutIncludeBoosterElements() throws Exception {
		OpenRocketDocument document = load(design().replaceAll("\\s*<IncludeBooster[12]>\\w+</IncludeBooster[12]>", ""));
		Rocket rocket = document.getRocket();
		FlightConfigurationId first = document.getSimulation(0).getFlightConfigurationId();

		assertEquals(2, document.getSimulations().size());
		assertFalse(rocket.getFlightConfiguration(first).isStageActive(rocket.getChild(1).getStageNumber()));
	}

	/**
	 * RASAero motor names that differ from OpenRocket's designations (Contrail's K707 is K707-BG in OpenRocket) or
	 * use RASAero's manufacturer abbreviations (GM for Gorilla) resolve to the OpenRocket motor.
	 */
	@Test
	public void resolvesRASAeroMotorNames() {
		WarningSet warnings = new WarningSet();
		ThrustCurveMotor contrail = RASAeroMotorsLoader.getMotorFromRASAero("K707  (Contrail)", warnings);
		ThrustCurveMotor gorilla = RASAeroMotorsLoader.getMotorFromRASAero("J465GT  (GM)", warnings);

		assertNotNull(contrail);
		assertEquals("K707-BG", contrail.getDesignation());
		assertNotNull(gorilla);
		assertEquals("J465GT", gorilla.getDesignation());
		assertEquals(0, warnings.size());
	}

	/**
	 * RASAero uses the standard pressure at the launch site elevation when no barometric pressure is entered
	 * (Pressure 0), and always uses the entered temperature. A negative wind speed is a tailwind.
	 */
	@Test
	public void importsLaunchSiteAtmosphereAndWind() throws Exception {
		OpenRocketDocument document = load(design().replaceFirst("<WindSpeed>0</WindSpeed>",
				"<WindSpeed>-10</WindSpeed>"));
		SimulationOptions options = document.getSimulation(0).getOptions();

		// RASAero II's standard atmosphere at 3750 ft, in Pa
		double standardPressure = 14.6958 * Math.pow(1 - 6.87535e-6 * 3750, 5.2561) * 6894.757;
		assertFalse(options.isISAAtmosphere());
		assertEquals(standardPressure, options.getLaunchPressure(), 1);
		assertEquals((80 - 32) / 1.8 + 273.15, options.getLaunchTemperature(), 0.01);
		assertEquals(10 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SPEED,
				options.getAverageWindModel().getAverage(), 1e-6);
		// A tailwind blows from behind the rocket: the rod points away from the wind
		assertEquals(-1, Math.cos(options.getAverageWindModel().getDirection() - options.getLaunchRodDirection()),
				1e-9);
	}

	private static String design() throws Exception {
		try (InputStream stream = RASAeroSimulationImportTest.class
				.getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1")) {
			assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static OpenRocketDocument load(String design) throws Exception {
		OpenRocketDocument document = OpenRocketDocumentFactory.createEmptyRocket();
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(document);
		context.setMotorFinder(new DatabaseMotorFinder());
		new RASAeroLoader().loadFromStream(context,
				new ByteArrayInputStream(design.getBytes(StandardCharsets.UTF_8)), null);
		return document;
	}

	private static MotorMount findMotorMount(Rocket rocket, int stage) {
		for (RocketComponent component : rocket.getChild(stage).getAllChildren()) {
			if (component instanceof MotorMount && ((MotorMount) component).hasMotor()) {
				return (MotorMount) component;
			}
		}
		throw new AssertionError("No motor mount with a motor in stage " + stage);
	}

	private static ThrustCurveMotor makeMotor(String designation) {
		return makeMotor("AeroTech", designation, 0.038, 0.3);
	}

	private static ThrustCurveMotor makeMotor(String manufacturer, String designation, double diameter,
			double length) {
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer(manufacturer))
				.setDesignation(designation)
				.setCode(designation)
				.setCommonName(designation)
				.setMotorType(Motor.Type.RELOAD)
				.setStandardDelays(new double[] { 0.0, 6.0 })
				.setDiameter(diameter)
				.setLength(length)
				.setTimePoints(new double[] { 0.0, 1.0, 2.0 })
				.setThrustPoints(new double[] { 0.0, 300.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.15, 0.0, 0.0, 0.6),
						new Coordinate(0.15, 0.0, 0.0, 0.45),
						new Coordinate(0.15, 0.0, 0.0, 0.3) })
				.setDigest(manufacturer + "-" + designation)
				.build();
	}
}
