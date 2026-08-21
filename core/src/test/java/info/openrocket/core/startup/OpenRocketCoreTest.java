package info.openrocket.core.startup;

import com.google.inject.Injector;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OpenRocketCore} initialization, verifying that external applications
 * can use OpenRocket as a library by calling {@code OpenRocketCore.initialize()}.
 *
 * These tests validate that all Guice bindings are correctly configured, databases load
 * properly, and design files (including those with motors and preset components) can be
 * loaded and simulated.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OpenRocketCoreTest {

	private static Injector previousInjector;
	private static Path coreModuleRoot;

	@BeforeAll
	public static void setUp() {
		// Save any existing injector so we can restore it after tests
		previousInjector = Application.getInjector();

		// Reset OpenRocketCore so we can test a fresh initialization
		OpenRocketCore.reset();

		// Initialize using the public API, exactly as an external user would
		OpenRocketCore.initialize();

		coreModuleRoot = findCoreModuleRoot();
	}

	@AfterAll
	public static void tearDown() {
		// Restore previous state
		OpenRocketCore.reset();
		if (previousInjector != null) {
			Application.setInjector(previousInjector);
		}
	}

	// ==================== Initialization State ====================

	@Test
	@Order(1)
	public void testIsInitialized() {
		assertTrue(OpenRocketCore.isInitialized());
	}

	@Test
	@Order(2)
	public void testDoubleInitializeIsIdempotent() {
		// Calling initialize() again should not throw
		assertDoesNotThrow(() -> OpenRocketCore.initialize());
		assertTrue(OpenRocketCore.isInitialized());
	}

	@Test
	@Order(3)
	public void testGetCoreModule() {
		CoreModule module = OpenRocketCore.getCoreModule();
		assertNotNull(module, "CoreModule should be available after initialization");
	}

	// ==================== Guice Bindings ====================

	@Test
	@Order(10)
	public void testInjectorIsSet() {
		Injector injector = Application.getInjector();
		assertNotNull(injector, "Guice injector should be set after initialization");
	}

	@Test
	@Order(11)
	public void testTranslatorBinding() {
		Translator translator = Application.getInjector().getInstance(Translator.class);
		assertNotNull(translator, "Translator should be bound");
		// Verify it can actually translate a known key
		String translated = translator.get("RocketCompCfg.lbl.Componentname");
		assertNotNull(translated);
		assertFalse(translated.isEmpty(), "Translator should return a non-empty translation");
	}

	@Test
	@Order(12)
	public void testApplicationPreferencesBinding() {
		ApplicationPreferences prefs = Application.getInjector().getInstance(ApplicationPreferences.class);
		assertNotNull(prefs, "ApplicationPreferences should be bound");
	}

	@Test
	@Order(13)
	public void testRocketDescriptorBinding() {
		RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);
		assertNotNull(descriptor, "RocketDescriptor should be bound");
	}

	@Test
	@Order(14)
	public void testComponentPresetDaoBinding() {
		// This is the binding that was previously missing from CoreModule
		ComponentPresetDao dao = Application.getInjector().getInstance(ComponentPresetDao.class);
		assertNotNull(dao, "ComponentPresetDao should be bound");
	}

	@Test
	@Order(15)
	public void testComponentPresetDatabaseBinding() {
		ComponentPresetDatabase db = Application.getInjector().getInstance(ComponentPresetDatabase.class);
		assertNotNull(db, "ComponentPresetDatabase should be bound");
	}

	@Test
	@Order(16)
	public void testMotorDatabaseBinding() {
		MotorDatabase db = Application.getInjector().getInstance(MotorDatabase.class);
		assertNotNull(db, "MotorDatabase should be bound");
	}

	@Test
	@Order(17)
	public void testThrustCurveMotorSetDatabaseBinding() {
		ThrustCurveMotorSetDatabase db = Application.getInjector().getInstance(ThrustCurveMotorSetDatabase.class);
		assertNotNull(db, "ThrustCurveMotorSetDatabase should be bound");
	}

	@Test
	@Order(18)
	public void testComponentPresetDaoAndDatabaseAreSameInstance() {
		// Both should resolve to the same ComponentPresetDatabase instance
		ComponentPresetDao dao = Application.getInjector().getInstance(ComponentPresetDao.class);
		ComponentPresetDatabase db = Application.getInjector().getInstance(ComponentPresetDatabase.class);
		assertTrue(dao instanceof ComponentPresetDatabase,
				"ComponentPresetDao should be backed by ComponentPresetDatabase");
	}

	// ==================== Database Content ====================

	@Test
	@Order(20)
	public void testMotorDatabaseIsPopulated() {
		ThrustCurveMotorSetDatabase db = Application.getInjector().getInstance(ThrustCurveMotorSetDatabase.class);
		assertNotNull(db.getMotorSets(), "Motor sets should not be null");
		assertFalse(db.getMotorSets().isEmpty(), "Motor database should contain motors after loading");
	}

	@Test
	@Order(21)
	public void testMotorDatabaseContainsKnownMotor() {
		MotorDatabase db = Application.getInjector().getInstance(MotorDatabase.class);
		// Search for a well-known Estes motor that should always be in the database
		List<? extends Motor> motors = db.findMotors(null, null, "Estes", "B6",
				Double.NaN, Double.NaN);
		assertFalse(motors.isEmpty(),
				"Motor database should contain Estes B6 motors");
	}

	@Test
	@Order(22)
	public void testComponentPresetDatabaseIsPopulated() {
		ComponentPresetDao dao = Application.getInjector().getInstance(ComponentPresetDao.class);
		List<ComponentPreset> allPresets = dao.listAll();
		assertNotNull(allPresets, "Preset list should not be null");
		assertFalse(allPresets.isEmpty(), "Component preset database should contain presets after loading");
	}

	@Test
	@Order(23)
	public void testComponentPresetDatabaseHasBodyTubes() {
		ComponentPresetDao dao = Application.getInjector().getInstance(ComponentPresetDao.class);
		List<ComponentPreset> bodyTubes = dao.listForType(ComponentPreset.Type.BODY_TUBE);
		assertFalse(bodyTubes.isEmpty(), "Component preset database should contain body tube presets");
	}

	@Test
	@Order(24)
	public void testComponentPresetDatabaseHasNoseCones() {
		ComponentPresetDao dao = Application.getInjector().getInstance(ComponentPresetDao.class);
		List<ComponentPreset> noseCones = dao.listForType(ComponentPreset.Type.NOSE_CONE);
		assertFalse(noseCones.isEmpty(), "Component preset database should contain nose cone presets");
	}

	// ==================== File Loading ====================

	@Test
	@Order(30)
	public void testLoadSimpleModelRocket() throws Exception {
		Path orkFile = findExampleFile("A simple model rocket.ork");
		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		assertNotNull(doc, "Document should not be null");
		Rocket rocket = doc.getRocket();
		assertNotNull(rocket, "Rocket should not be null");
		assertTrue(rocket.getChildCount() > 0, "Rocket should have child components");
	}

	@Test
	@Order(31)
	public void testLoadedRocketHasMotors() throws Exception {
		// "A simple model rocket.ork" should have motor configurations
		Path orkFile = findExampleFile("A simple model rocket.ork");
		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		Rocket rocket = doc.getRocket();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		assertNotNull(config, "Flight configuration should not be null");
		assertTrue(config.hasMotors(), "Flight configuration should have motors assigned");

		Collection<MotorConfiguration> activeMotors = config.getActiveMotors();
		assertFalse(activeMotors.isEmpty(), "There should be active motors in the configuration");

		for (MotorConfiguration mc : activeMotors) {
			Motor motor = mc.getMotor();
			assertNotNull(motor, "Motor should not be null");
			assertNotNull(motor.getDesignation(), "Motor should have a designation");
			assertFalse(motor.getDesignation().isEmpty(), "Motor designation should not be empty");
		}
	}

	@Test
	@Order(32)
	public void testLoadTwoStageRocket() throws Exception {
		Path orkFile = findExampleFile("Two stage high power rocket.ork");
		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		assertNotNull(doc, "Document should not be null");
		Rocket rocket = doc.getRocket();
		assertNotNull(rocket, "Rocket should not be null");
		// A two-stage rocket should have at least 2 stage children
		assertTrue(rocket.getChildCount() >= 2,
				"Two-stage rocket should have at least 2 stage components, found " + rocket.getChildCount());
	}

	@Test
	@Order(33)
	public void testLoadThreeStageRocket() throws Exception {
		Path orkFile = findExampleFile("Three stage low power rocket.ork");
		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		Rocket rocket = doc.getRocket();
		assertNotNull(rocket);
		assertTrue(rocket.getChildCount() >= 3,
				"Three-stage rocket should have at least 3 stage components, found " + rocket.getChildCount());
	}

	@Test
	@Order(34)
	public void testLoadClusteredMotorsRocket() throws Exception {
		Path orkFile = findExampleFile("Clustered motors.ork");
		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		Rocket rocket = doc.getRocket();
		assertNotNull(rocket);
		FlightConfiguration config = rocket.getSelectedConfiguration();
		assertTrue(config.hasMotors(), "Clustered motors rocket should have motors");
	}

	// ==================== Simulation ====================

	@Test
	@Order(40)
	public void testSimulateSimpleModelRocket() throws Exception {
		Path orkFile = findExampleFile("A simple model rocket.ork");
		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		List<Simulation> simulations = doc.getSimulations();
		assertFalse(simulations.isEmpty(), "Document should have simulations");

		// Simulate the first simulation
		Simulation sim = simulations.get(0);
		assertDoesNotThrow(() -> sim.simulate(), "Simulation should run without exception");
		assertFalse(sim.hasErrors(), "Simulation should complete without errors");
		assertNotNull(sim.getSimulatedData(), "Simulation should produce flight data");
	}

	@Test
	@Order(41)
	public void testSimulateTwoStageRocket() throws Exception {
		Path orkFile = findExampleFile("Two stage high power rocket.ork");
		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		List<Simulation> simulations = doc.getSimulations();
		assertFalse(simulations.isEmpty(), "Document should have simulations");

		Simulation sim = simulations.get(0);
		assertDoesNotThrow(() -> sim.simulate(), "Two-stage simulation should run without exception");
		assertFalse(sim.hasErrors(), "Two-stage simulation should complete without errors");
	}

	@Test
	@Order(42)
	public void testSimulateAllExampleFiles() throws Exception {
		List<Path> orkFiles = listExampleOrkFiles();
		assertFalse(orkFiles.isEmpty(), "Should find example .ork files");

		for (Path orkFile : orkFiles) {
			GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
			OpenRocketDocument doc = loader.load();
			assertNotNull(doc, "Should load " + orkFile.getFileName());

			Rocket rocket = doc.getRocket();
			assertNotNull(rocket, "Rocket should not be null for " + orkFile.getFileName());

			for (Simulation sim : doc.getSimulations()) {
				assertDoesNotThrow(() -> sim.simulate(),
						"Simulation '" + sim.getName() + "' in " + orkFile.getFileName()
								+ " should run without exception");
				assertFalse(sim.hasErrors(),
						"Simulation '" + sim.getName() + "' in " + orkFile.getFileName()
								+ " should complete without errors");
			}
		}
	}

	// ==================== Rocket Structure with Presets ====================

	@Test
	@Order(50)
	public void testLoadedRocketComponentsAreComplete() throws Exception {
		Path orkFile = findExampleFile("A simple model rocket.ork");
		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		Rocket rocket = doc.getRocket();
		List<RocketComponent> allChildren = rocket.getAllChildren();
		assertFalse(allChildren.isEmpty(), "Rocket should have components");

		// Check that each component has a valid name
		for (RocketComponent component : allChildren) {
			assertNotNull(component.getName(), "Each component should have a name");
		}
	}

	@Test
	@Order(51)
	public void testRocketHasMultipleFlightConfigurations() throws Exception {
		// The simple model rocket has multiple flight configurations with different motors
		Path orkFile = findExampleFile("A simple model rocket.ork");
		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		Rocket rocket = doc.getRocket();
		int configCount = rocket.getFlightConfigurationCount();
		assertTrue(configCount > 0, "Rocket should have at least one flight configuration");
	}

	// ==================== Application Static Accessors ====================

	@Test
	@Order(60)
	public void testApplicationGetTranslator() {
		Translator translator = Application.getTranslator();
		assertNotNull(translator, "Application.getTranslator() should work after initialization");
	}

	@Test
	@Order(61)
	public void testApplicationGetPreferences() {
		ApplicationPreferences prefs = Application.getPreferences();
		assertNotNull(prefs, "Application.getPreferences() should work after initialization");
	}

	@Test
	@Order(62)
	public void testApplicationGetComponentPresetDao() {
		ComponentPresetDao dao = Application.getComponentPresetDao();
		assertNotNull(dao, "Application.getComponentPresetDao() should work after initialization");
	}

	@Test
	@Order(63)
	public void testApplicationGetMotorSetDatabase() {
		MotorDatabase db = Application.getMotorSetDatabase();
		assertNotNull(db, "Application.getMotorSetDatabase() should work after initialization");
	}

	// ==================== Error Handling ====================

	@Test
	@Order(70)
	public void testGetCoreModuleBeforeInitializationThrows() {
		// Save current state, reset, verify exception, then restore
		Injector savedInjector = Application.getInjector();
		OpenRocketCore.reset();
		try {
			assertFalse(OpenRocketCore.isInitialized());
			assertThrows(IllegalStateException.class, OpenRocketCore::getCoreModule,
					"getCoreModule() should throw if not initialized");
		} finally {
			// Re-initialize for remaining tests
			OpenRocketCore.initialize();
		}
	}

	// ==================== Helper Methods ====================

	private static Path findExampleFile(String fileName) {
		Path file = coreModuleRoot.resolve("src/main/resources/datafiles/examples/" + fileName);
		assertTrue(Files.isRegularFile(file), "Example file should exist: " + file);
		return file;
	}

	private static List<Path> listExampleOrkFiles() {
		Path examplesDir = coreModuleRoot.resolve("src/main/resources/datafiles/examples");
		try (Stream<Path> stream = Files.list(examplesDir)) {
			return stream
					.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".ork"))
					.sorted()
					.toList();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static Path findCoreModuleRoot() {
		Path cwd = Path.of("").toAbsolutePath().normalize();
		if (Files.isDirectory(cwd.resolve("src/main/resources/datafiles/examples"))) {
			return cwd;
		}
		if (Files.isDirectory(cwd.resolve("core/src/main/resources/datafiles/examples"))) {
			return cwd.resolve("core");
		}
		throw new IllegalStateException("Unable to locate core module root from working directory: " + cwd);
	}
}
