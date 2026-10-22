package info.openrocket.core.file.rasaero.importt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
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
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
				.setDesignation(designation)
				.setCode(designation)
				.setCommonName(designation)
				.setMotorType(Motor.Type.RELOAD)
				.setStandardDelays(new double[] { 0.0, 6.0 })
				.setDiameter(0.038)
				.setLength(0.3)
				.setTimePoints(new double[] { 0.0, 1.0, 2.0 })
				.setThrustPoints(new double[] { 0.0, 300.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.15, 0.0, 0.0, 0.6),
						new Coordinate(0.15, 0.0, 0.0, 0.45),
						new Coordinate(0.15, 0.0, 0.0, 0.3) })
				.setDigest("AeroTech-" + designation)
				.build();
	}
}
