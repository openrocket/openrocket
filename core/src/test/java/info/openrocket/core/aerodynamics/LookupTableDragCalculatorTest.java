package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.aerodynamics.lookup.MachAoALookup;
import info.openrocket.core.logging.WarningSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import info.openrocket.core.rocketcomponent.RocketComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	@Test
	public void interpolatesWithAoA() throws IOException {
		Path csv = tempDir.resolve("drag_aoa.csv");
		String data = String.join("\n",
				"mach,aoa,cd",
				"0,0,0.20",
				"0,10,0.40",
				"1,0,0.30",
				"1,10,0.50",
				"");
		Files.writeString(csv, data);

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setMach(0.5);
		conditions.setAOA(Math.toRadians(5));

		AerodynamicForces total = new AerodynamicForces().zero();
		calculator.calculateDrag(null, conditions, null, null, total, new WarningSet());

		// Should interpolate to 0.35 at mach=0.5, aoa=5
		assertEquals(0.35, total.getCD(), EPSILON);
	}

	@Test
	public void toAxialDragWithZeroAOA() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setAOA(0);

		// At zero AOA, axial drag should equal total drag
		double cd = 0.5;
		double axial = calculator.toAxialDrag(conditions, cd);
		assertEquals(cd, axial, EPSILON);
	}

	@Test
	public void toAxialDragWithSmallAOA() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setAOA(Math.toRadians(10)); // 10 degrees

		double cd = 0.5;
		double axial = calculator.toAxialDrag(conditions, cd);
		// At 10 degrees AOA (< 17°), multiplier is between 1.0 and 1.3, so axial >= cd
		// The multiplier increases from 1.0 at 0° to 1.3 at 17°
		assertTrue(axial >= cd * 1.0);
		assertTrue(axial <= cd * 1.3);
		assertTrue(axial > 0);
	}

	@Test
	public void toAxialDragWithLargeAOA() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setAOA(Math.toRadians(45)); // 45 degrees

		double cd = 0.5;
		double axial = calculator.toAxialDrag(conditions, cd);
		// At large AOA, axial drag should be significantly less than total drag
		assertTrue(axial < cd);
		assertTrue(axial > 0);
	}

	@Test
	public void toAxialDragWithNegativeAOA() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setAOA(-Math.toRadians(10)); // -10 degrees

		double cd = 0.5;
		double axial = calculator.toAxialDrag(conditions, cd);
		// Should handle negative AOA (clamped to 0)
		assertTrue(Math.abs(axial) <= cd);
	}

	@Test
	public void toAxialDragWithAOAOver90Degrees() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setAOA(Math.toRadians(120)); // 120 degrees

		double cd = 0.5;
		double axial = calculator.toAxialDrag(conditions, cd);
		// At AOA > 90, should return negative axial drag
		assertTrue(axial < 0);
	}

	@Test
	public void zerosComponentForces() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setMach(0.5);
		conditions.setAOA(0);

		Map<RocketComponent, AerodynamicForces> componentForces = new HashMap<>();
		AerodynamicForces componentForce = new AerodynamicForces().zero();
		componentForce.setCD(1.0);
		componentForce.setFrictionCD(0.5);
		componentForces.put(null, componentForce);

		AerodynamicForces total = new AerodynamicForces().zero();
		calculator.calculateDrag(null, conditions, componentForces, null, total, new WarningSet());

		// Component forces should be zeroed
		assertEquals(0, componentForce.getCD(), EPSILON);
		assertEquals(0, componentForce.getFrictionCD(), EPSILON);
		assertEquals(0, componentForce.getCDaxial(), EPSILON);
	}

	@Test
	public void zerosAssemblyForces() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setMach(0.5);
		conditions.setAOA(0);

		Map<RocketComponent, AerodynamicForces> assemblyForces = new HashMap<>();
		AerodynamicForces assemblyForce = new AerodynamicForces().zero();
		assemblyForce.setCD(1.0);
		assemblyForce.setFrictionCD(0.5);
		assemblyForces.put(null, assemblyForce);

		AerodynamicForces total = new AerodynamicForces().zero();
		calculator.calculateDrag(null, conditions, null, assemblyForces, total, new WarningSet());

		// Assembly forces should be zeroed
		assertEquals(0, assemblyForce.getCD(), EPSILON);
		assertEquals(0, assemblyForce.getFrictionCD(), EPSILON);
		assertEquals(0, assemblyForce.getCDaxial(), EPSILON);
	}

	@Test
	public void handlesNullComponentForces() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setMach(0.5);
		conditions.setAOA(0);

		AerodynamicForces total = new AerodynamicForces().zero();
		calculator.calculateDrag(null, conditions, null, null, total, new WarningSet());

		assertEquals(0.50, total.getCD(), EPSILON);
	}

	@Test
	public void newInstance() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		DragCalculator newInstance = calculator.newInstance();
		assertNotNull(newInstance);
		assertNotSame(calculator, newInstance);
		assertEquals(LookupTableDragCalculator.class, newInstance.getClass());
	}

	@Test
	public void voidAerodynamicCache() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.50\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));
		LookupTableDragCalculator calculator = new LookupTableDragCalculator(table);

		// Should not throw
		calculator.voidAerodynamicCache();
	}
}
