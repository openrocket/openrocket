package info.openrocket.swing.gui.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;

import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.utils.CoreServicesModule;

/**
 * Tests for {@link DesignReportCSVExport}. The export is pure/headless (no Swing
 * components), so it can be exercised directly.
 */
public class DesignReportCSVExportTest {

	@BeforeAll
	public static void setUp() {
		Module applicationModule = new CoreServicesModule();
		Module pluginModule = new PluginModule();
		Module debugTranslator = new AbstractModule() {
			@Override
			protected void configure() {
				bind(Translator.class).toInstance(new DebugTranslator(null));
			}
		};
		Module dbOverrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ComponentPresetDao.class).toProvider(new Provider<>() {
					final ComponentPresetDao db = new ComponentPresetDatabase();
					@Override public ComponentPresetDao get() { return db; }
				});
				bind(MotorDatabase.class).toProvider(new Provider<>() {
					final ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();
					@Override public ThrustCurveMotorSetDatabase get() { return db; }
				});
			}
		};
		Injector injector = Guice.createInjector(
				Modules.override(applicationModule).with(debugTranslator), pluginModule, dbOverrides);
		Application.setInjector(injector);
	}

	private OpenRocketDocument load(String path) throws Exception {
		return new GeneralRocketLoader(new File(path)).load();
	}

	private String export(OpenRocketDocument doc) throws Exception {
		StringWriter writer = new StringWriter();
		DesignReportCSVExport.export(doc, writer);
		return writer.toString();
	}

	/** Split a CSV line into fields (the test data contains no quoted commas). */
	private String[] fields(String line) {
		return line.split(",", -1);
	}

	private String rowFor(String csv, String scope, String field) {
		for (String line : csv.split("\r\n")) {
			String[] f = fields(line);
			if (f.length >= 2 && f[0].equals(scope) && f[1].equals(field)) {
				return line;
			}
		}
		return null;
	}

	@Test
	public void headerAndMetadata() throws Exception {
		OpenRocketDocument doc = load("src/test/resources/simplerocket.ork");
		Rocket rocket = doc.getRocket();
		rocket.setDesigner("Jane Doe");
		rocket.setKitName("Estes Alpha III");
		rocket.setRevision("Rev C");
		rocket.setComment("Some comment");

		String csv = export(doc);
		List<String> lines = Arrays.asList(csv.split("\r\n"));

		// Four-column header
		assertEquals("Scope,Field,Value,Unit", lines.get(0));

		// Metadata rows present with empty unit
		assertEquals("Design,Designer,Jane Doe,", rowFor(csv, "Design", "Designer"));
		assertEquals("Design,Kit,Estes Alpha III,", rowFor(csv, "Design", "Kit"));
		assertEquals("Design,Stages,1,", rowFor(csv, "Design", "Stages"));

		// Revision and comment are intentionally excluded from the CSV
		assertFalse(csv.contains(",Revision,"), "CSV should not contain Revision:\n" + csv);
		assertFalse(csv.contains(",Comment,"), "CSV should not contain Comment:\n" + csv);
	}

	@Test
	public void valueAndUnitAreSeparateColumns() throws Exception {
		OpenRocketDocument doc = load("src/test/resources/simplerocket.ork");
		String csv = export(doc);

		// Length: numeric value in column 3, unit in column 4 (not "42.5 cm" in one cell)
		String lengthRow = rowFor(csv, "Rocket", "Length");
		String[] f = fields(lengthRow);
		assertEquals(4, f.length, "row: " + lengthRow);
		assertEquals("cm", f[3]);
		assertFalse(f[2].contains(" "), "value column must not contain the unit: " + lengthRow);
		Double.parseDouble(f[2]);   // value column is numeric

		// Percentage unit lands in the unit column
		assertEquals("%", fields(rowFor(csv, "Rocket", "Stability (%)"))[3]);

		// Dimensionless statistic has an empty unit
		assertEquals("", fields(rowFor(csv, "Rocket", "Fineness (L/D)"))[3]);
	}

	@Test
	public void perStageScopesForMultiStage() throws Exception {
		OpenRocketDocument doc = load("../core/src/test/resources/file/rasaero/export/02.Two-stage.ork");
		String csv = export(doc);

		Set<String> scopes = new LinkedHashSet<>();
		for (String line : csv.split("\r\n")) {
			String[] f = fields(line);
			if (f.length >= 1 && !f[0].equals("Scope")) {
				scopes.add(f[0]);
			}
		}
		// Design + Rocket + one scope per stage
		assertTrue(scopes.contains("Design"), scopes.toString());
		assertTrue(scopes.contains("Rocket"), scopes.toString());
		assertTrue(scopes.size() >= 4, "expected per-stage scopes too: " + scopes);

		// Stages metadata reflects the two-stage design
		assertEquals("Design,Stages,2,", rowFor(csv, "Design", "Stages"));
	}

	@Test
	public void finRootMeasurementsSingleStage() throws Exception {
		OpenRocketDocument doc = load("src/test/resources/simplerocket.ork");
		String csv = export(doc);

		// Single stage -> "Rocket" scope, consistent with the statistics
		String top = rowFor(csv, "Rocket", "Trapezoidal fin set: Nose to top of fin root");
		String bottom = rowFor(csv, "Rocket", "Trapezoidal fin set: Nose to bottom of fin root");
		assertNotNull(top, "missing fin root top row:\n" + csv);
		assertNotNull(bottom, "missing fin root bottom row:\n" + csv);

		String[] topFields = fields(top);
		String[] bottomFields = fields(bottom);
		assertEquals("cm", topFields[3]);
		assertEquals("cm", bottomFields[3]);
		// The bottom (aft) of the root is further from the nose than the top (fore)
		assertTrue(Double.parseDouble(bottomFields[2]) > Double.parseDouble(topFields[2]),
				"bottom should be aft of top: " + top + " / " + bottom);
	}

	@Test
	public void finRootMeasurementsMultiStageDisambiguated() throws Exception {
		OpenRocketDocument doc = load("../core/src/test/resources/file/rasaero/export/02.Two-stage.ork");
		String csv = export(doc);

		boolean anyFinRow = false;
		boolean sawSuffix1 = false;
		boolean sawSuffix2 = false;
		for (String line : csv.split("\r\n")) {
			if (!line.contains("fin root")) {
				continue;
			}
			anyFinRow = true;
			// Multi-stage fin rows are scoped by stage name, not the whole-rocket "Rocket"
			assertNotEquals("Rocket", fields(line)[0], "fin row should be stage-scoped: " + line);
			if (line.contains(" #1: Nose to top of fin root")) {
				sawSuffix1 = true;
			}
			if (line.contains(" #2: Nose to top of fin root")) {
				sawSuffix2 = true;
			}
		}
		assertTrue(anyFinRow, "expected fin root rows:\n" + csv);
		// Two same-named fin sets within a stage are disambiguated with #1 / #2
		assertTrue(sawSuffix1 && sawSuffix2, "expected disambiguated duplicate fin-set names:\n" + csv);
	}
}
