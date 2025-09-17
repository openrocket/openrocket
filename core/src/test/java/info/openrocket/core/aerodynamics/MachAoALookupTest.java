package info.openrocket.core.aerodynamics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.aerodynamics.lookup.MachAoALookup;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MachAoALookupTest {

	@TempDir
	private Path tempDir;

	private static final double EPSILON = 1e-6;

	@Test
	public void interpolatesSingleAxis() throws IOException {
		Path csv = tempDir.resolve("drag.csv");
		Files.writeString(csv, "mach,cd\n0,0.30\n1,0.50\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));

		double cd = table.interpolate(0.5, 0, "cd");
		assertEquals(0.40, cd, 1e-9);
	}

	@Test
	public void interpolatesMachAndAoA() throws IOException {
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

		double cd = table.interpolate(0.5, 5, "cd");
		assertEquals(0.35, cd, EPSILON);
	}

	@Test
	public void clampsOutsideRange() throws IOException {
		Path csv = tempDir.resolve("drag_bounds.csv");
		Files.writeString(csv, "mach,cd\n0.2,0.20\n0.4,0.40\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"));

		double low = table.interpolate(0.0, 0, "cd");
		double high = table.interpolate(0.8, 0, "cd");

		assertEquals(0.20, low, EPSILON);
		assertEquals(0.40, high, EPSILON);
		assertFalse(table.hasAoA());
	}

	@Test
	public void buildsFromBuilder() {
		MachAoALookup table = MachAoALookup.builder(List.of("cd"))
				.addData(0.0, Map.of("cd", 0.2))
				.addData(1.0, Map.of("cd", 0.6))
				.build();

		assertEquals(0.4, table.interpolate(0.5, 0, "cd"), EPSILON);
	}

	@Test
	public void supportsCustomSeparator() throws IOException {
		Path csv = tempDir.resolve("drag_semicolon.csv");
		Files.writeString(csv, "mach;cd\n0;0.20\n1;0.60\n");

		MachAoALookup table = CsvMachAoALookup.fromCsv(csv, List.of("cd"), ';');

		assertEquals(0.40, table.interpolate(0.5, 0, "cd"), EPSILON);
	}
}
