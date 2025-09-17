package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.aerodynamics.lookup.MachAoALookup;
import info.openrocket.core.logging.WarningSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LookupTableDragCalculatorTest {

	@TempDir
	private Path tempDir;

	private static final double EPSILON = 1e-6;

	@Test
	public void usesTableCdValue() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		String data = String.join("\n",
				"mach,aoa,cd",
				"0,0,0.20",
				"1,0,0.40",
				"1,10,0.50",
				"");
		Files.writeString(csv, data);

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setMach(0.5);
		conditions.setAOA(0);

		AerodynamicForces total = new AerodynamicForces().zero();
		calculator.calculateDrag(null, conditions, null, null, total, new WarningSet());

		assertEquals(0.30, total.getCD(), EPSILON);
		assertEquals(0.30, total.getCDaxial(), EPSILON);
		assertEquals(0.30, total.getFrictionCD(), EPSILON);
		assertEquals(0, total.getPressureCD(), EPSILON);
		assertEquals(0, total.getBaseCD(), EPSILON);
	}
}
