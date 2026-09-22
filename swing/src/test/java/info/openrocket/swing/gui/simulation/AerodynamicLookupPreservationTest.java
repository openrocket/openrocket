package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
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

public class AerodynamicLookupPreservationTest extends BaseTestCase {
	private static final Path CSV = Path.of("missing-beta-test-lookup.csv");

	@Test
	public void testAcceptingUnchangedEditorPreservesEmbeddedData() throws Exception {
		SimulationOptions options = options();
		edit(options, dialog -> apply(dialog));

		assertValues(roundTrip(options), 1, 1);
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void testEditingOneTablePreservesTheOther(boolean drag) throws Exception {
		SimulationOptions options = options();
		edit(options, dialog -> {
			((JTextArea) field(dialog, drag ? "dragExampleArea" : "stabilityExampleArea"))
					.setText(drag ? "Mach,Cd\n0,2" : "Mach,Cn,Cm,Cp\n0,1,1,2");
			apply(dialog);
		});

		assertValues(roundTrip(options), drag ? 2 : 1, drag ? 1 : 2);
	}

	@Test
	public void testRetryAfterInvalidStabilityTableRetainsDragEdit() throws Exception {
		SimulationOptions options = options();
		edit(options, dialog -> {
			((JTextArea) field(dialog, "dragExampleArea")).setText("Mach,Cd\n0,2");
			((JTextArea) field(dialog, "stabilityExampleArea")).setText("invalid");
			AtomicBoolean errorShown = new AtomicBoolean();
			Timer dismissError = new Timer(100, event -> {
				for (var window : dialog.getOwnedWindows()) {
					if (window.isVisible()) {
						errorShown.set(true);
						window.dispose();
					}
				}
			});
			dismissError.start();
			try {
				apply(dialog);
			} finally {
				dismissError.stop();
			}
			assertTrue(errorShown.get());
			assertValues(options, 1, 1);
			((JTextArea) field(dialog, "stabilityExampleArea")).setText("Mach,Cn,Cm,Cp\n0,1,1,3");
			apply(dialog);
		});

		assertValues(roundTrip(options), 2, 3);
	}

	private static SimulationOptions options() {
		SimulationOptions options = new SimulationOptions();
		List<String> drag = List.of("Mach,Cd", "0,1");
		List<String> stability = List.of("Mach,Cn,Cm,Cp", "0,1,1,1");
		options.setDragLookup(CSV, CsvMachAoALookup.parse(drag, List.of("cd"), ','), drag);
		options.setStabilityLookup(CSV, CsvMachAoALookup.parse(stability, List.of("cn", "cm", "cp"), ','), stability);
		return options;
	}

	private static void assertValues(SimulationOptions options, double drag, double cp) {
		assertNotNull(options.getDragLookupTable());
		assertNotNull(options.getStabilityLookupTable());
		assertEquals(drag, options.getDragLookupTable().interpolate(0, 0, "cd"));
		assertEquals(cp, options.getStabilityLookupTable().interpolate(0, 0, "cp"));
	}

	private static void edit(SimulationOptions options, java.util.function.Consumer<AerodynamicLookupDialog> action)
			throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "A display is required to construct the editor");
		SwingUtilities.invokeAndWait(() -> {
			AerodynamicLookupDialog dialog = new AerodynamicLookupDialog(null, options);
			try {
				action.accept(dialog);
			} finally {
				dialog.dispose();
			}
		});
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
