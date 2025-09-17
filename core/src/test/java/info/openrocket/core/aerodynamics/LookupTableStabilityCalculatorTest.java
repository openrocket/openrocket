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

public class LookupTableStabilityCalculatorTest {

	@TempDir
	private Path tempDir;

	private static final double EPSILON = 1e-6;

	@Test
	public void interpolatesNonAxialCoefficients() throws IOException {
		Path csv = tempDir.resolve("stability.csv");
		String data = String.join("\n",
				"mach,aoa,cn,cm,cp",
				"0,0,0.10,0.01,0.50",
				"0,10,0.20,0.02,0.55",
				"1,0,0.30,0.03,0.60",
				"1,10,0.40,0.04,0.65",
				"");
		Files.writeString(csv, data);

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cn", "cm", "cp"));
		LookupTableStabilityCalculator calculator = new LookupTableStabilityCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setMach(0.5);
		conditions.setAOA(Math.toRadians(5));

		AerodynamicForces forces = calculator.calculateNonAxialForces(null, conditions, new WarningSet());

		assertEquals(0.25, forces.getCN(), EPSILON);
		assertEquals(0.025, forces.getCm(), EPSILON);
		assertEquals(0.575, forces.getCP().x, EPSILON);
		assertEquals(Math.toRadians(10) - Math.toRadians(5), calculator.getStallMargin(), EPSILON);
	}
}
