package info.openrocket.swing.gui.export;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.cad.export.CADDimensionVerifier;
import info.openrocket.core.file.cad.export.CADDimensionVerifier.Check;
import info.openrocket.core.file.cad.export.CADDimensionVerifier.Status;
import info.openrocket.core.file.cad.export.CADPartsExporter.Format;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.ServicesForTesting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartExportHelperTest {
	@TempDir
	Path tempDir;

	@BeforeAll
	static void setUp() {
		// The examples reference motors, which are looked up in an empty database
		AbstractModule overrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(Translator.class).toInstance(new DebugTranslator(null));
				bind(MotorDatabase.class).toInstance(new ThrustCurveMotorSetDatabase());
				bind(ComponentPresetDao.class).toInstance(new ComponentPresetDatabase());
			}
		};
		Application.setInjector(Guice.createInjector(
				Modules.override(new ServicesForTesting()).with(overrides), new PluginModule()));
	}

	private static OpenRocketDocument loadExample(String name) throws Exception {
		// Loaded the same way as the examples in the File menu
		String path = "datafiles/examples/" + name + ".ork";
		try (InputStream in = ClassLoader.getSystemResourceAsStream(path)) {
			assertNotNull(in, "Example not found: " + path);
			return new GeneralRocketLoader(new File(path)).load(in, path);
		}
	}

	/**
	 * Every fin, centering ring and bulkhead of the bundled examples must export with dimensions that match the design.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"A simple model rocket",
			"3D printable nose cone and fins",
			"Clustered motors",
			"Two stage high power rocket",
			"Three stage low power rocket",
			"Pods--airframes and winglets",
			"Parallel booster staging",
			"Dual parachute deployment"})
	void examplePartsExportWithMatchingDimensions(String example) throws Exception {
		OpenRocketDocument document = loadExample(example);
		List<RocketComponent> parts = PartExportHelper.collectParts(allComponents(document));
		assertFalse(parts.isEmpty(), example + " has no parts to export");

		for (Format format : Format.values()) {
			File file = tempDir.resolve(example + "." + format.getExtension()).toFile();
			CADDimensionVerifier.Result result = PartExportHelper.exportToFile(parts, format, file, example);
			assertTrue(file.length() > 0);
			assertTrue(result.isAllMatching(), example + " " + format + ": " + describe(result));
		}
	}

	/**
	 * The cluster motor mount of the example must become holes in the centering rings it passes through.
	 */
	@Test
	void clusterMotorMountHolesAreExported() throws Exception {
		OpenRocketDocument document = loadExample("Clustered motors");
		List<RocketComponent> parts = PartExportHelper.collectParts(allComponents(document));

		for (Format format : new Format[]{Format.DXF, Format.STEP}) {
			File file = tempDir.resolve("cluster." + format.getExtension()).toFile();
			CADDimensionVerifier.Result result = PartExportHelper.exportToFile(parts, format, file, "cluster");
			long holes = result.checks().stream()
					.filter(c -> c.dimension().dimension() == Dimension.HOLE_DIAMETER)
					.peek(c -> assertEquals(Status.MATCH, c.status(), describe(result)))
					.count();
			assertTrue(holes >= 2, format + ": expected cluster holes, got " + holes);
		}
	}

	/**
	 * Only fins and centering rings are exported. Selecting a body tube or motor tube exports nothing, not even
	 * the fins and rings inside it.
	 */
	@Test
	void bodyTubesAndMotorTubesAreNotExported() throws Exception {
		OpenRocketDocument document = loadExample("A simple model rocket");
		List<RocketComponent> tubes = new ArrayList<>();
		List<RocketComponent> partsOnly = new ArrayList<>();
		for (RocketComponent component : document.getRocket()) {
			if (component instanceof BodyTube || component instanceof InnerTube) {
				tubes.add(component);
			} else if (component instanceof FinSet || component instanceof CenteringRing) {
				partsOnly.add(component);
			}
		}
		assertFalse(tubes.isEmpty());
		assertFalse(partsOnly.isEmpty());

		assertTrue(PartExportHelper.collectParts(tubes).isEmpty());
		assertEquals(partsOnly, PartExportHelper.collectParts(allComponents(document)));
	}

	/**
	 * The suggested file name must not contain characters that Windows does not allow in file names.
	 */
	@Test
	void defaultFileNameHasNoCharactersThatAreInvalidOnWindows() throws Exception {
		OpenRocketDocument document = loadExample("A simple model rocket");
		FinSet fins = null;
		for (RocketComponent component : document.getRocket()) {
			if (component instanceof FinSet finSet) {
				fins = finSet;
			}
		}
		fins.setName("Fins: 3\\4 <a|b>?*\"/");

		String name = PartExportHelper.defaultFileName(List.of(fins), document, Format.DXF);
		assertEquals("Fins_ 3_4 _a_b_____.dxf", name);
	}

	private static List<RocketComponent> allComponents(OpenRocketDocument document) {
		List<RocketComponent> components = new ArrayList<>();
		for (RocketComponent component : document.getRocket()) {
			components.add(component);
		}
		return components;
	}

	private static String describe(CADDimensionVerifier.Result result) {
		StringBuilder sb = new StringBuilder("units=" + result.units());
		for (Check check : result.checks()) {
			sb.append(String.format(Locale.ROOT, "%n  %s %s#%d design=%.9g measured=%.9g %s", check.part().name(),
					check.dimension().dimension(), check.dimension().index(), check.dimension().value(),
					check.measured(), check.status()));
		}
		return sb.toString();
	}
}
