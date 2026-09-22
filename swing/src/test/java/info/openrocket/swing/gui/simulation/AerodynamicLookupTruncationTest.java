package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.openrocket.OpenRocketSaver;
import info.openrocket.core.file.openrocket.importt.OpenRocketLoader;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.swing.util.BaseTestCase;

public class AerodynamicLookupTruncationTest extends BaseTestCase {

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void testEditingLongTablePreservesFinalRows(boolean drag) throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "A display is required to construct the editor");
		SimulationOptions options = new SimulationOptions();
		List<String> rows = new ArrayList<>();
		rows.add(drag ? "Mach,Cd" : "Mach,Cn,Cm,Cp");
		for (int i = 0; i < 40; i++) {
			rows.add(drag ? i + "," + (i + 1) : i + ",1,1," + (i + 1));
		}
		Path path = Path.of("missing-beta-test-lookup.csv");
		if (drag) {
			options.setDragLookup(path, CsvMachAoALookup.parse(rows, List.of("cd"), ','), rows);
		} else {
			options.setStabilityLookup(path, CsvMachAoALookup.parse(rows, List.of("cn", "cm", "cp"), ','), rows);
		}

		SwingUtilities.invokeAndWait(() -> {
			AerodynamicLookupDialog dialog = new AerodynamicLookupDialog(null, options);
			try {
				JTextArea area = (JTextArea) field(dialog, drag ? "dragExampleArea" : "stabilityExampleArea");
				// A harmless comment edit must not replace the table with just its preview.
				area.append("\n# edited");
				apply(dialog);
			} finally {
				dialog.dispose();
			}
		});

		SimulationOptions loaded = roundTrip(options);
		var table = drag ? loaded.getDragLookupTable() : loaded.getStabilityLookupTable();
		assertNotNull(table);
		assertEquals(39, table.getMaxMach());
		assertEquals(40, table.interpolate(39, 0, drag ? "cd" : "cp"));
		assertEquals(42, (drag ? loaded.getDragLookupCsvRows() : loaded.getStabilityLookupCsvRows()).size());
	}

	private static Object field(AerodynamicLookupDialog dialog, String name) {
		try {
			var field = AerodynamicLookupDialog.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(dialog);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static void apply(AerodynamicLookupDialog dialog) {
		try {
			var method = AerodynamicLookupDialog.class.getDeclaredMethod("applyAndClose");
			method.setAccessible(true);
			method.invoke(dialog);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError(e);
		}
	}

	private static SimulationOptions roundTrip(SimulationOptions options) throws Exception {
		var document = OpenRocketDocumentFactory.createEmptyRocket();
		document.addSimulation(new Simulation(document, document.getRocket(), Simulation.Status.NOT_SIMULATED,
				"Lookup test", options, List.of(), null));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		new OpenRocketSaver().save(output, document, new StorageOptions(), new WarningSet(), new ErrorSet());
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(OpenRocketDocumentFactory.createEmptyRocket());
		context.setMotorFinder((type, manufacturer, designation, diameter, length, digest, warnings) -> null);
		new OpenRocketLoader().loadFromStream(context, new ByteArrayInputStream(output.toByteArray()), "test.ork");
		return context.getOpenRocketDocument().getSimulations().get(0).getOptions();
	}
}
