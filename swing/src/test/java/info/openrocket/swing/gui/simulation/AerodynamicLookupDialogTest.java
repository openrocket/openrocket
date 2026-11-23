package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.swing.util.BaseTestCase;

public class AerodynamicLookupDialogTest extends BaseTestCase {

	/**
	 * Regression test for #3488: loading a CSV with very long header comments must not cause
	 * the dialog's packed width to expand to the unwrapped line width instead of scrolling.
	 */
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void testWideCsvCommentsDoNotExpandDialogWidth(boolean drag) throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "A display is required to construct the dialog");

		String wideComment = "# " + "BlueRavenTelemetryHeaderColumn,".repeat(25);
		List<String> rows = drag
				? List.of(wideComment, "Mach,Cd", "0.0,0.40", "1.0,0.55")
				: List.of(wideComment, "Mach,Cn,Cm,Cp", "0.0,1.0,1.0,1.0", "1.0,1.1,1.0,1.1");

		SimulationOptions loadedOptions = new SimulationOptions();
		Path csvPath = Path.of("wide-comments-test.csv");
		if (drag) {
			loadedOptions.setDragLookup(csvPath, CsvMachAoALookup.parse(rows, List.of("cd"), ','), rows);
		} else {
			loadedOptions.setStabilityLookup(csvPath,
					CsvMachAoALookup.parse(rows, List.of("cn", "cm", "cp"), ','), rows);
		}

		AtomicInteger emptyDialogWidth = new AtomicInteger();
		AtomicInteger loadedDialogWidth = new AtomicInteger();
		AtomicInteger unwrappedTextWidth = new AtomicInteger();

		SwingUtilities.invokeAndWait(() -> {
			AerodynamicLookupDialog emptyDialog = new AerodynamicLookupDialog(null, new SimulationOptions());
			try {
				emptyDialogWidth.set(emptyDialog.getWidth());
			} finally {
				emptyDialog.dispose();
			}

			AerodynamicLookupDialog loadedDialog = new AerodynamicLookupDialog(null, loadedOptions);
			try {
				loadedDialogWidth.set(loadedDialog.getWidth());
				JTextArea area = (JTextArea) field(loadedDialog, drag ? "dragExampleArea" : "stabilityExampleArea");
				unwrappedTextWidth.set(area.getPreferredSize().width);
			} finally {
				loadedDialog.dispose();
			}
		});

		assertTrue(unwrappedTextWidth.get() > 2000,
				"Test input should produce a wide unwrapped CSV line (got " + unwrappedTextWidth.get() + " px)");
		assertTrue(loadedDialogWidth.get() < unwrappedTextWidth.get() / 2,
				"Dialog width (" + loadedDialogWidth.get() + " px) should stay bounded rather than matching "
						+ "unwrapped CSV width (" + unwrappedTextWidth.get() + " px)");
		assertTrue(loadedDialogWidth.get() <= emptyDialogWidth.get() + 200,
				"Dialog width with wide CSV (" + loadedDialogWidth.get() + " px) should stay close to "
						+ "empty dialog width (" + emptyDialogWidth.get() + " px)");
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
}
