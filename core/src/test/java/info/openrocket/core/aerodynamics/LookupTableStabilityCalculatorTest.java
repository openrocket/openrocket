package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.Injector;
import com.google.inject.Module;
import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.aerodynamics.lookup.MachAoALookup;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.util.Coordinate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LookupTableStabilityCalculatorTest {

	@TempDir
	private Path tempDir;

	private static final double EPSILON = 1e-6;

	@BeforeAll
	public static void setup() {
		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();
		Injector injector = com.google.inject.Guice.createInjector(applicationModule, pluginModule);
		Application.setInjector(injector);
	}

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
		assertEquals(0.575, forces.getCP().getX(), EPSILON);
		assertEquals(Math.toRadians(10) - Math.toRadians(5), calculator.getStallMargin(), EPSILON);
	}

	@Test
	public void getCP() throws IOException {
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

		FlightConfiguration config = new FlightConfiguration(new Rocket());
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(0.5);
		conditions.setAOA(Math.toRadians(5));

		info.openrocket.core.util.CoordinateIF cp = calculator.getCP(config, conditions, new WarningSet());
		assertNotNull(cp);
		assertEquals(0.575, cp.getX(), EPSILON);
		assertEquals(0, cp.getY(), EPSILON);
		assertEquals(0, cp.getZ(), EPSILON);
	}

	@Test
	public void getStallMarginWithAoA() throws IOException {
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
		conditions.setAOA(Math.toRadians(3));

		calculator.calculateNonAxialForces(null, conditions, new WarningSet());
		double margin = calculator.getStallMargin();
		// Max AoA is 10 degrees, current is 3 degrees, so margin should be 7 degrees
		assertEquals(Math.toRadians(7), margin, EPSILON);
	}

	@Test
	public void getStallMarginWithoutAoA() throws IOException {
		Path csv = tempDir.resolve("stability_no_aoa.csv");
		String data = String.join("\n",
				"mach,cn,cm,cp",
				"0,0.10,0.01,0.50",
				"1,0.30,0.03,0.60",
				"");
		Files.writeString(csv, data);

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cn", "cm", "cp"));
		LookupTableStabilityCalculator calculator = new LookupTableStabilityCalculator(table);
		assertFalse(table.hasAoA());

		FlightConditions conditions = new FlightConditions(null);
		conditions.setMach(0.5);
		conditions.setAOA(Math.toRadians(5));

		calculator.calculateNonAxialForces(null, conditions, new WarningSet());
		double margin = calculator.getStallMargin();
		// Without AoA data, stall margin should be infinity
		assertEquals(Double.POSITIVE_INFINITY, margin, EPSILON);
	}

	@Test
	public void getForceAnalysis() throws IOException {
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

		Rocket rocket = new Rocket();
		FlightConfiguration config = new FlightConfiguration(rocket);
		FlightConditions conditions = new FlightConditions(config);
		conditions.setMach(0.5);
		conditions.setAOA(Math.toRadians(5));

		StabilityForceBreakdown breakdown = calculator.getForceAnalysis(config, conditions, new WarningSet());

		assertNotNull(breakdown);
		assertNotNull(breakdown.getComponentForces());
		assertNotNull(breakdown.getAssemblyForces());

		// Total forces should be in assembly map for the rocket
		AerodynamicForces total = breakdown.getAssemblyForces().get(rocket);
		assertNotNull(total);
		assertEquals(0.25, total.getCN(), EPSILON);
		assertEquals(0.025, total.getCm(), EPSILON);
		assertEquals(0.575, total.getCP().getX(), EPSILON);
	}

	@Test
	public void calculateDampingMoments() throws IOException {
		Path csv = tempDir.resolve("stability.csv");
		String data = String.join("\n",
				"mach,aoa,cn,cm,cp",
				"0,0,0.10,0.01,0.50",
				"1,0,0.30,0.03,0.60",
				"");
		Files.writeString(csv, data);

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cn", "cm", "cp"));
		LookupTableStabilityCalculator calculator = new LookupTableStabilityCalculator(table);

		FlightConfiguration config = null; // Can be null for this test
		FlightConditions conditions = new FlightConditions(null);
		conditions.setMach(0.5);
		conditions.setAOA(0);

		AerodynamicForces total = new AerodynamicForces().zero();
		total.setPitchDampingMoment(1.0);
		total.setYawDampingMoment(2.0);

		calculator.calculateDampingMoments(config, conditions, total);

		// Damping moments should be zeroed
		assertEquals(0, total.getPitchDampingMoment(), EPSILON);
		assertEquals(0, total.getYawDampingMoment(), EPSILON);
	}

	@Test
	public void checkGeometry() throws IOException {
		Path csv = tempDir.resolve("stability.csv");
		String data = String.join("\n",
				"mach,aoa,cn,cm,cp",
				"0,0,0.10,0.01,0.50",
				"1,0,0.30,0.03,0.60",
				"");
		Files.writeString(csv, data);

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cn", "cm", "cp"));
		LookupTableStabilityCalculator calculator = new LookupTableStabilityCalculator(table);

		Rocket rocket = new Rocket();
		FlightConfiguration config = new FlightConfiguration(rocket);
		WarningSet warnings = new WarningSet();

		// Should not throw or add warnings
		calculator.checkGeometry(config, rocket, warnings);
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void newInstance() throws IOException {
		Path csv = tempDir.resolve("stability.csv");
		String data = String.join("\n",
				"mach,aoa,cn,cm,cp",
				"0,0,0.10,0.01,0.50",
				"1,0,0.30,0.03,0.60",
				"");
		Files.writeString(csv, data);

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cn", "cm", "cp"));
		LookupTableStabilityCalculator calculator = new LookupTableStabilityCalculator(table);

		StabilityCalculator newInstance = calculator.newInstance();
		assertNotNull(newInstance);
		assertNotSame(calculator, newInstance);
		assertEquals(LookupTableStabilityCalculator.class, newInstance.getClass());
	}

	@Test
	public void voidAerodynamicCache() throws IOException {
		Path csv = tempDir.resolve("stability.csv");
		String data = String.join("\n",
				"mach,aoa,cn,cm,cp",
				"0,0,0.10,0.01,0.50",
				"1,0,0.30,0.03,0.60",
				"");
		Files.writeString(csv, data);

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cn", "cm", "cp"));
		LookupTableStabilityCalculator calculator = new LookupTableStabilityCalculator(table);

		// Should not throw
		calculator.voidAerodynamicCache();
	}

	@Test
	public void setsAllForceCoefficientsToZero() throws IOException {
		Path csv = tempDir.resolve("stability.csv");
		String data = String.join("\n",
				"mach,aoa,cn,cm,cp",
				"0,0,0.10,0.01,0.50",
				"1,0,0.30,0.03,0.60",
				"");
		Files.writeString(csv, data);

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cn", "cm", "cp"));
		LookupTableStabilityCalculator calculator = new LookupTableStabilityCalculator(table);

		FlightConditions conditions = new FlightConditions(null);
		conditions.setMach(0.5);
		conditions.setAOA(0);

		AerodynamicForces forces = calculator.calculateNonAxialForces(null, conditions, new WarningSet());

		// Side forces should be zero
		assertEquals(0, forces.getCside(), EPSILON);
		assertEquals(0, forces.getCyaw(), EPSILON);
		assertEquals(0, forces.getCroll(), EPSILON);
		assertEquals(0, forces.getCrollDamp(), EPSILON);
		assertEquals(0, forces.getCrollForce(), EPSILON);
	}
}
