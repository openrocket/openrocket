package info.openrocket.core.simulation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.openrocket.OpenRocketSaver;
import info.openrocket.core.file.openrocket.importt.OpenRocketLoader;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.util.BaseTestCase;

public class SimulationLookupCopyTest extends BaseTestCase {
	private static final Path CSV = Path.of("missing-beta-test-lookup.csv");

	@Test
	public void testCopiedLookupsSurviveSaveAndReloadWithoutExternalFiles() throws Exception {
		SimulationOptions source = options(2);
		SimulationOptions target = new SimulationOptions();

		target.copyConditionsFrom(source);

		assertLookups(roundTrip(target), 2);
		assertEquals(source.getDragLookupCsvRows(), target.getDragLookupCsvRows());
		assertEquals(source.getStabilityLookupCsvRows(), target.getStabilityLookupCsvRows());
	}

	@Test
	public void testCopiedLookupsReplaceStaleDestinationRows() throws Exception {
		SimulationOptions target = options(1);

		target.copyConditionsFrom(options(2));

		assertLookups(roundTrip(target), 2);
	}

	@Test
	public void testAbsentSourceRowsClearDestinationRows() {
		SimulationOptions source = options(2);
		source.setDragLookup(CSV, source.getDragLookupTable(), null);
		source.setStabilityLookup(CSV, source.getStabilityLookupTable(), null);
		SimulationOptions target = options(1);

		target.copyConditionsFrom(source);

		assertNull(target.getDragLookupCsvRows());
		assertNull(target.getStabilityLookupCsvRows());
		assertSame(source.getDragLookupTable(), target.getDragLookupTable());
		assertSame(source.getStabilityLookupTable(), target.getStabilityLookupTable());
	}

	@Test
	public void testChangedRowsAreCopiedAndNotifyEvenWhenTablesAreShared() {
		SimulationOptions source = options(2);
		SimulationOptions target = source.clone();
		List<String> rows = List.of("# edited comment", "Mach,Cd", "0,2", "1,2");
		source.setDragLookup(CSV, source.getDragLookupTable(), rows);
		AtomicInteger events = new AtomicInteger();
		target.addChangeListener(event -> events.incrementAndGet());

		target.copyConditionsFrom(source);

		assertEquals(rows, target.getDragLookupCsvRows());
		assertEquals(1, events.get());
		target.copyConditionsFrom(source);
		assertEquals(1, events.get(), "Copying unchanged conditions should not notify again");
		source.clearDragLookup();
		assertEquals(rows, target.getDragLookupCsvRows());
	}

	private static SimulationOptions options(double value) {
		SimulationOptions options = new SimulationOptions();
		List<String> drag = List.of("Mach,Cd", "0," + value, "1," + value);
		List<String> stability = List.of("Mach,Cn,Cm,Cp", "0,1,1," + value, "1,1,1," + value);
		options.setDragLookup(CSV, CsvMachAoALookup.parse(drag, List.of("cd"), ','), drag);
		options.setStabilityLookup(CSV, CsvMachAoALookup.parse(stability, List.of("cn", "cm", "cp"), ','), stability);
		return options;
	}

	private static void assertLookups(SimulationOptions options, double value) {
		assertTrue(options.hasDragLookup());
		assertTrue(options.hasStabilityLookup());
		assertEquals(value, options.getDragLookupTable().interpolate(0.5, 0, "cd"));
		assertEquals(value, options.getStabilityLookupTable().interpolate(0.5, 0, "cp"));
	}

	private static SimulationOptions roundTrip(SimulationOptions options) throws Exception {
		var document = OpenRocketDocumentFactory.createEmptyRocket();
		Simulation simulation = new Simulation(document, document.getRocket());
		simulation.getOptions().copyConditionsFrom(options);
		document.addSimulation(simulation);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		new OpenRocketSaver().save(output, document, new StorageOptions(), new WarningSet(), new ErrorSet());
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setOpenRocketDocument(OpenRocketDocumentFactory.createEmptyRocket());
		context.setMotorFinder((type, manufacturer, designation, diameter, length, digest, warnings) -> null);
		new OpenRocketLoader().loadFromStream(context, new ByteArrayInputStream(output.toByteArray()), "test.ork");
		return context.getOpenRocketDocument().getSimulations().get(0).getOptions();
	}
}
