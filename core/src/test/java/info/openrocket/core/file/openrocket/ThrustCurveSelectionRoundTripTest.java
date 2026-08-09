package info.openrocket.core.file.openrocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.GeneralRocketSaver;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.DecalNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/**
 * Verifies that when a design is saved with a specific thrust curve selected for
 * a motor, the same curve (identified by its digest) is restored on load.
 *
 * Two synthetic Estes C6 motors with identical metadata but different thrust
 * data (and therefore different digests) are used.  Two flight configurations
 * each reference a different curve.  After a save/load round-trip the correct
 * digest must be present in each configuration.
 */
public class ThrustCurveSelectionRoundTripTest {

	@TempDir
	Path tempDir;

	private static final String DIGEST_CURVE_1 = "c6-curve-digest-1";
	private static final String DIGEST_CURVE_2 = "c6-curve-digest-2";

	// Two C6 motors: same metadata, different thrust profiles → different digests.
	private static ThrustCurveMotor c6Curve1;
	private static ThrustCurveMotor c6Curve2;

	@BeforeAll
	static void setup() {
		c6Curve1 = buildC6(DIGEST_CURVE_1,
				new double[] { 0, 0.1, 1.7, 2.0 },
				new double[] { 0, 14.0, 9.0, 0 });

		c6Curve2 = buildC6(DIGEST_CURVE_2,
				new double[] { 0, 0.15, 1.6, 1.9 },
				new double[] { 0, 11.0, 10.5, 0 });

		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();
		Module dbOverrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ComponentPresetDao.class).toProvider(new EmptyComponentDbProvider());
				bind(MotorDatabase.class).toProvider(new TwoCurveC6Provider());
				bind(Translator.class).toInstance(new DebugTranslator(null));
			}
		};

		Injector injector = Guice.createInjector(
				Modules.override(applicationModule).with(dbOverrides), pluginModule);
		Application.setInjector(injector);
	}

	// -------------------------------------------------------------------------
	// Tests
	// -------------------------------------------------------------------------

	/**
	 * A design with two flight configurations each using a different C6 thrust
	 * curve must restore the correct curve (by digest) after a save/load round-trip.
	 */
	@Test
	void testDifferentCurvesRestoredAfterRoundTrip() throws IOException, RocketLoadException, DecalNotFoundException {
		// Build rocket with two flight configurations
		Rocket rocket = buildRocketWithTwoConfigs();
		OpenRocketDocument doc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		StorageOptions options = new StorageOptions();
		options.setSaveSimulationData(false);

		// Save as zip (.ork)
		File orkFile = tempDir.resolve("c6_round_trip.ork").toFile();
		GeneralRocketSaver saver = new GeneralRocketSaver();
		saver.save(orkFile, doc, options);

		// Load back
		OpenRocketDocument loaded = new GeneralRocketLoader(orkFile).load();
		Rocket loadedRocket = loaded.getRocket();

		// Collect the two flight config IDs (in insertion order)
		List<FlightConfigurationId> fcids = new java.util.ArrayList<>();
		for (FlightConfiguration fc : loadedRocket.getFlightConfigurations()) {
			if (!fc.getFlightConfigurationID().isDefaultId()) {
				fcids.add(fc.getFlightConfigurationID());
			}
		}
		assertEquals(2, fcids.size(), "Expected 2 flight configurations after round-trip");

		// Find the motor mount
		MotorMount mount = findMotorMount(loadedRocket);
		assertNotNull(mount, "Motor mount not found in loaded rocket");

		// Verify each configuration restored the correct curve
		String digest1 = getDigest(mount, fcids.get(0));
		String digest2 = getDigest(mount, fcids.get(1));

		assertTrue(
				(DIGEST_CURVE_1.equals(digest1) && DIGEST_CURVE_2.equals(digest2))
						|| (DIGEST_CURVE_2.equals(digest1) && DIGEST_CURVE_1.equals(digest2)),
				"Loaded digests [" + digest1 + ", " + digest2 + "] do not match " +
						"saved digests [" + DIGEST_CURVE_1 + ", " + DIGEST_CURVE_2 + "]");
	}

	/**
	 * Saving with curve 1 selected and loading back must give exactly curve 1.
	 */
	@Test
	void testSingleCurveSelectionPreserved() throws IOException, RocketLoadException, DecalNotFoundException {
		Rocket rocket = buildMinimalRocket(c6Curve1);
		OpenRocketDocument doc = OpenRocketDocumentFactory.createDocumentFromRocket(rocket);
		StorageOptions options = new StorageOptions();
		options.setSaveSimulationData(false);

		File orkFile = tempDir.resolve("c6_single_curve.ork").toFile();
		new GeneralRocketSaver().save(orkFile, doc, options);

		OpenRocketDocument loaded = new GeneralRocketLoader(orkFile).load();
		Rocket loadedRocket = loaded.getRocket();

		MotorMount mount = findMotorMount(loadedRocket);
		assertNotNull(mount, "Motor mount not found in loaded rocket");

		FlightConfigurationId fcid = loadedRocket.getSelectedConfiguration().getFlightConfigurationID();
		String loadedDigest = getDigest(mount, fcid);

		assertEquals(DIGEST_CURVE_1, loadedDigest,
				"Expected curve 1 digest after round-trip, got: " + loadedDigest);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private static Rocket buildRocketWithTwoConfigs() {
		Rocket rocket = new Rocket();
		rocket.setName("C6RoundTripRocket");

		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		BodyTube bodyTube = new BodyTube(0.024, 0.05);
		stage.addChild(bodyTube);

		InnerTube motorTube = new InnerTube();
		motorTube.setOuterRadius(0.009);
		motorTube.setLength(0.07);
		bodyTube.addChild(motorTube);

		// First flight configuration → curve 1
		FlightConfigurationId fcid1 = new FlightConfigurationId();
		rocket.createFlightConfiguration(fcid1);
		MotorConfiguration mc1 = new MotorConfiguration(motorTube, fcid1);
		mc1.setMotor(c6Curve1);
		mc1.setEjectionDelay(5.0);
		motorTube.setMotorConfig(mc1, fcid1);

		// Second flight configuration → curve 2
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		rocket.createFlightConfiguration(fcid2);
		MotorConfiguration mc2 = new MotorConfiguration(motorTube, fcid2);
		mc2.setMotor(c6Curve2);
		mc2.setEjectionDelay(5.0);
		motorTube.setMotorConfig(mc2, fcid2);

		rocket.setSelectedConfiguration(fcid1);
		rocket.enableEvents();
		return rocket;
	}

	private static Rocket buildMinimalRocket(ThrustCurveMotor motor) {
		Rocket rocket = new Rocket();
		rocket.setName("C6SingleCurveRocket");

		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		BodyTube bodyTube = new BodyTube(0.024, 0.05);
		stage.addChild(bodyTube);

		InnerTube motorTube = new InnerTube();
		motorTube.setOuterRadius(0.009);
		motorTube.setLength(0.07);
		bodyTube.addChild(motorTube);

		FlightConfigurationId fcid = new FlightConfigurationId();
		rocket.createFlightConfiguration(fcid);
		MotorConfiguration mc = new MotorConfiguration(motorTube, fcid);
		mc.setMotor(motor);
		mc.setEjectionDelay(5.0);
		motorTube.setMotorConfig(mc, fcid);

		rocket.setSelectedConfiguration(fcid);
		rocket.enableEvents();
		return rocket;
	}

	private static ThrustCurveMotor buildC6(String digest, double[] time, double[] thrust) {
		int n = time.length;
		CoordinateIF[] cg = new CoordinateIF[n];
		for (int i = 0; i < n; i++) {
			cg[i] = new Coordinate(0.035, 0, 0, 0.025 - i * 0.005);
		}
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("Estes"))
				.setDesignation("C6")
				.setCommonName("C6")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] { 0, 3, 5, 7, Motor.PLUGGED_DELAY })
				.setDiameter(0.018)
				.setLength(0.070)
				.setTimePoints(time)
				.setThrustPoints(thrust)
				.setCGPoints(cg)
				.setDigest(digest)
				.build();
	}

	private static MotorMount findMotorMount(Rocket rocket) {
		for (java.util.Iterator<RocketComponent> it = rocket.iterator(true); it.hasNext(); ) {
			RocketComponent c = it.next();
			if (c instanceof MotorMount mount && mount.isMotorMount()) {
				return mount;
			}
		}
		return null;
	}

	private static String getDigest(MotorMount mount, FlightConfigurationId fcid) {
		MotorConfiguration mc = mount.getMotorConfig(fcid);
		if (mc == null) {
			fail("No motor configuration for fcid " + fcid);
		}
		Motor motor = mc.getMotor();
		assertNotNull(motor, "Motor is null for fcid " + fcid);
		assertTrue(motor instanceof ThrustCurveMotor, "Motor is not a ThrustCurveMotor");
		return ((ThrustCurveMotor) motor).getDigest();
	}

	// -------------------------------------------------------------------------
	// Guice providers
	// -------------------------------------------------------------------------

	public static class EmptyComponentDbProvider implements Provider<ComponentPresetDao> {
		@Override
		public ComponentPresetDao get() {
			return new ComponentPresetDatabase();
		}
	}

	public static class TwoCurveC6Provider implements Provider<ThrustCurveMotorSetDatabase> {
		private final ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();

		public TwoCurveC6Provider() {
			db.addMotor(c6Curve1);
			db.addMotor(c6Curve2);
		}

		@Override
		public ThrustCurveMotorSetDatabase get() {
			return db;
		}
	}
}
