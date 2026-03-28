package info.openrocket.core.file.openrocket;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSQLiteDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.logging.MessagePriority;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.xml.OpenRocketComponentLoader;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ExampleFilesTest extends BaseTestCase {

	private static volatile boolean initialized = false;
	private static Path coreModuleRoot;
	private static Injector previousInjector;

	private static final Map<String, ExpectedWarnings> EXPECTATIONS = new HashMap<>();
	static {
		EXPECTATIONS.put("A simple model rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3 - too short delay", 0, 1, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Two stage high power rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 1, 0, 0)
				.simulationWarnings("Simulation 2", 1, 0, 0)
				.build());

		EXPECTATIONS.put("Three stage low power rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3", 0, 0, 0)
				.build());

		EXPECTATIONS.put("ARC payload rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Tube fin rocket.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.build());

		// ----------------------------

		EXPECTATIONS.put("Deployable payload.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3 - too short delay", 0, 2, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Airstart timing.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 1, 0)
				.simulationWarnings("Simulation 2", 0, 1, 0)
				.simulationWarnings("Simulation 3", 0, 1, 0)
				.simulationWarnings("Simulation 4", 0, 1, 0)
				.simulationWarnings("Simulation 5", 0, 1, 0)
				.build());

		EXPECTATIONS.put("Base drag hack (short-wide).ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 1, 0, 0)
				.simulationWarnings("Simulation 2", 1, 0, 0)
				.simulationWarnings("Simulation 3", 1, 0, 0)
				.build());

		EXPECTATIONS.put("Chute release.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3", 0, 1, 0)
				.build());

		EXPECTATIONS.put("Dual parachute deployment.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 1, 0)
				.simulationWarnings("Simulation 2", 0, 1, 0)
				.simulationWarnings("Simulation 3", 0, 2, 0)
				.simulationWarnings("Simulation 4", 0, 1, 0)
				.simulationWarnings("Simulation 5", 0, 1, 0)
				.simulationWarnings("Simulation 6", 0, 1, 0)
				.build());

		EXPECTATIONS.put("Clustered motors.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3 - too short delay", 0, 1, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Parallel booster staging.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Pods--airframes and winglets.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3", 0, 0, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

		EXPECTATIONS.put("Pods--powered with recovery deployment.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("BoosterOnly [Turn off Sustainer]", 0, 0, 0)
				.simulationWarnings("Powered Pods with Recovery Devices", 0, 0, 0)
				.simulationWarnings("Sustainer Only [Turn Off Booster]", 0, 0, 0)
				.simulationWarnings("Unpowered Pods", 0, 0, 0)
				.build());

		// ----------------------------

		EXPECTATIONS.put("Simulation extensions.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Active roll control", 0, 0, 0)
				.simulationWarnings("No controlling", 0, 0, 0)
				.simulationWarnings("Roll control + air-start", 1, 0, 0)
				.build());

		EXPECTATIONS.put("Simulation scripting.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Active roll control", 0, 0, 0)
				.simulationWarnings("No controlling", 0, 0, 0)
				.simulationWarnings("Roll control + air-start", 0, 0, 0)
				.build());

		// ----------------------------

		EXPECTATIONS.put("3D printable nose cone and fins.ork", ExpectedWarnings.builder()
				.openWarnings(0, 0, 0)
				.simulationWarnings("Simulation 1", 0, 0, 0)
				.simulationWarnings("Simulation 2", 0, 0, 0)
				.simulationWarnings("Simulation 3 - too short delay", 0, 1, 0)
				.simulationWarnings("Simulation 4", 0, 0, 0)
				.simulationWarnings("Simulation 5", 0, 0, 0)
				.build());

	}

	@BeforeAll
	public static void setUp() throws Exception {
		if (initialized) {
			return;
		}

		if (Application.getInjector() == null) {
			BaseTestCase.setUp();
		}
		previousInjector = Application.getInjector();

		coreModuleRoot = findCoreModuleRoot();

		ComponentPresetDatabase componentPresetDatabase = loadComponentPresetDatabase(coreModuleRoot);
		ThrustCurveMotorSetDatabase motorDatabase = loadMotorDatabase(coreModuleRoot);

		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();
		Module dbOverrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ComponentPresetDao.class).toInstance(componentPresetDatabase);
				bind(ThrustCurveMotorSetDatabase.class).toInstance(motorDatabase);
				bind(MotorDatabase.class).to(ThrustCurveMotorSetDatabase.class);
			}
		};
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(dbOverrides), pluginModule);

		Application.setInjector(injector);
		initialized = true;
	}

	@AfterAll
	public static void tearDown() {
		if (previousInjector != null) {
			Application.setInjector(previousInjector);
		}
	}

	static Stream<Path> exampleOrkFiles() {
		Path root = coreModuleRoot != null ? coreModuleRoot : findCoreModuleRoot();
		Path examplesDir = root.resolve("src/main/resources/datafiles/examples");
		try (Stream<Path> stream = Files.list(examplesDir)) {
			return stream
					.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".ork"))
					.sorted(Comparator.comparing(p -> p.getFileName().toString()))
					.toList()
					.stream();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("exampleOrkFiles")
	public void openAndSimulateExampleFileHasNoWarnings(Path orkFile) throws RocketLoadException {
		String fileName = orkFile.getFileName().toString();
		ExpectedWarnings expected = EXPECTATIONS.get(fileName);

		GeneralRocketLoader loader = new GeneralRocketLoader(orkFile.toFile());
		OpenRocketDocument doc = loader.load();

		WarningCounts openWarnings = countRelevantWarnings(loader.getWarnings());
		if (expected != null) {
			assertEquals(expected.openWarnings, openWarnings, () -> "Warnings when opening " + orkFile + " (expected=" + expected.openWarnings +
					", actual=" + openWarnings + "):\n" + formatWarnings(loader.getWarnings()));
		}

		Map<String, WarningCounts> actualSimWarnings = new HashMap<>();
		for (Simulation simulation : doc.getSimulations()) {
			try {
				simulation.simulate();
			} catch (Exception e) {
				fail("Simulation failed for " + orkFile + " (" + simulation.getName() + "): " + e.getMessage(), e);
			}

			WarningSet warnings = simulation.getSimulatedWarnings();
			WarningCounts simulationWarnings = warnings == null ? WarningCounts.MISSING : countRelevantWarnings(warnings);
			actualSimWarnings.put(simulation.getName(), simulationWarnings);

			if (expected != null) {
				WarningCounts expectedSimWarnings = expected.simulationWarnings.get(simulation.getName());
				if (expectedSimWarnings == null) {
					fail("Missing expected warnings configuration for " + fileName + " simulation '" +
							simulation.getName() + "'.\n\n" + expectationSnippet(fileName, openWarnings, actualSimWarnings));
				}

				assertEquals(expectedSimWarnings, simulationWarnings, () -> "Warnings when simulating " + orkFile + " (" + simulation.getName() + ") " +
						"(expected=" + expectedSimWarnings + ", actual=" + simulationWarnings + "):\n" +
						(warnings == null ? "<null WarningSet>" : formatWarnings(warnings)));
			}

			assertFalse(simulation.hasErrors(),
					() -> "Simulation aborted for " + orkFile + " (" + simulation.getName() + ")");
		}

		if (expected == null) {
			fail("Missing expected warnings configuration for example file: " + fileName +
					".\n\n" + expectationSnippet(fileName, openWarnings, actualSimWarnings));
		}
	}

	private static WarningCounts countRelevantWarnings(WarningSet warnings) {
		int informative = 0;
		int normal = 0;
		int critical = 0;

		for (Warning w : warnings) {
			MessagePriority priority = w.getPriority();
			if (priority == null) {
				normal++;
				continue;
			}

			switch (priority) {
				case LOW -> informative++;
				case NORMAL -> normal++;
				case HIGH -> critical++;
			}
		}
		return new WarningCounts(informative, normal, critical);
	}

	private static String formatWarnings(WarningSet warnings) {
		StringBuilder sb = new StringBuilder();
		for (Warning w : warnings) {
			sb.append("- ").append(w.getPriority()).append(": ").append(w).append("\n");
		}
		return sb.toString();
	}

	private static ComponentPresetDatabase loadComponentPresetDatabase(Path coreRoot) throws IOException {
		Path presetsDir = coreRoot.resolve("src/main/resources/datafiles/components");
		if (!Files.isDirectory(presetsDir)) {
			throw new IOException("Component preset directory not found: " + presetsDir);
		}

		ComponentPresetDatabase dao = new ComponentPresetDatabase();
		OpenRocketComponentLoader loader = new OpenRocketComponentLoader();

		try (Stream<Path> files = Files.walk(presetsDir)) {
			List<Path> presetFiles = files
					.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".orc"))
					.sorted()
					.toList();

			for (Path file : presetFiles) {
				try (InputStream is = Files.newInputStream(file)) {
					Collection<ComponentPreset> presets = loader.load(is, file.getFileName().toString());
					dao.addAll(presets);
				}
			}
		}

		return dao;
	}

	private static ThrustCurveMotorSetDatabase loadMotorDatabase(Path coreRoot) throws IOException {
		Path bundledDb = coreRoot.resolve("src/main/resources/datafiles/thrustcurves/initial_motors.db");
		if (!Files.isRegularFile(bundledDb)) {
			throw new IOException("Bundled motor database not found: " + bundledDb);
		}

		Path tmpDir = coreRoot.resolve("build/tmp/example-ork-tests");
		Files.createDirectories(tmpDir);
		Path dbCopy = tmpDir.resolve("initial_motors.db");
		Files.copy(bundledDb, dbCopy, StandardCopyOption.REPLACE_EXISTING);

		List<ThrustCurveMotor> motors;
		try {
			motors = ThrustCurveMotorSQLiteDatabase.readDatabase(dbCopy.toFile());
		} catch (Exception e) {
			throw new IOException("Failed to read motor database: " + dbCopy, e);
		}

		ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();
		for (ThrustCurveMotor motor : motors) {
			db.addMotor(motor);
		}
		return db;
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

	private static String expectationSnippet(String fileName, WarningCounts openWarnings, Map<String, WarningCounts> simWarnings) {
		StringBuilder sb = new StringBuilder();
		sb.append("EXPECTATIONS.put(\"").append(fileName).append("\", ExpectedWarnings.builder()\n");
		sb.append("\t\t.openWarnings(").append(openWarnings.informative).append(", ").append(openWarnings.normal)
				.append(", ").append(openWarnings.critical).append(")\n");
		simWarnings.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(e -> sb.append("\t\t.simulationWarnings(\"").append(e.getKey()).append("\", ")
						.append(e.getValue().informative).append(", ").append(e.getValue().normal).append(", ")
						.append(e.getValue().critical).append(")\n"));
		sb.append("\t\t.build());\n");
		return sb.toString();
	}

	private static final class WarningCounts {
		private static final WarningCounts MISSING = new WarningCounts(-1, -1, -1);

		private final int informative;
		private final int normal;
		private final int critical;

		private WarningCounts(int informative, int normal, int critical) {
			this.informative = informative;
			this.normal = normal;
			this.critical = critical;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof WarningCounts other)) {
				return false;
			}
			return this.informative == other.informative &&
					this.normal == other.normal &&
					this.critical == other.critical;
		}

		@Override
		public int hashCode() {
			int result = Integer.hashCode(informative);
			result = 31 * result + Integer.hashCode(normal);
			result = 31 * result + Integer.hashCode(critical);
			return result;
		}

		@Override
		public String toString() {
			return "{informative=" + informative + ", normal=" + normal + ", critical=" + critical + "}";
		}
	}

	private static final class ExpectedWarnings {
		private final WarningCounts openWarnings;
		private final Map<String, WarningCounts> simulationWarnings;

		private ExpectedWarnings(WarningCounts openWarnings, Map<String, WarningCounts> simulationWarnings) {
			this.openWarnings = openWarnings;
			this.simulationWarnings = simulationWarnings;
		}

		private static Builder builder() {
			return new Builder();
		}

		private static final class Builder {
			private WarningCounts openWarnings;
			private final Map<String, WarningCounts> simulationWarnings = new HashMap<>();

			private Builder openWarnings(int informative, int normal, int critical) {
				this.openWarnings = new WarningCounts(informative, normal, critical);
				return this;
			}

			private Builder simulationWarnings(String simulationName, int informative, int normal, int critical) {
				this.simulationWarnings.put(simulationName, new WarningCounts(informative, normal, critical));
				return this;
			}

			private ExpectedWarnings build() {
				return new ExpectedWarnings(openWarnings, Map.copyOf(simulationWarnings));
			}
		}
	}
}
