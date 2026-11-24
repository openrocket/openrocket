package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADPartsExporter.Format;
import info.openrocket.core.file.cad.export.ExportTestSupport.Written;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.file.svg.export.RingSvgExporter;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.BuildProperties;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static info.openrocket.core.file.cad.export.PartFixtures.createFinSet;
import static info.openrocket.core.file.cad.export.PartFixtures.createRing;
import static info.openrocket.core.file.cad.export.ExportTestSupport.createAllParts;
import static info.openrocket.core.file.cad.export.ExportTestSupport.write;
import static info.openrocket.core.file.cad.export.ExportTestSupport.assertMeasured;
import static info.openrocket.core.file.cad.export.ExportTestSupport.describe;
import static info.openrocket.core.file.cad.export.ExportTestSupport.count;
import static info.openrocket.core.file.cad.export.ExportTestSupport.assertHasPoint3D;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the STEP export.
 */
class STEPExportTest extends BaseTestCase {
	@Test
	void solidsAreClosedAndInMillimeters() throws IOException {
		Written written = write(createAllParts(), Format.STEP);
		String step = written.text();

		assertTrue(step.startsWith("ISO-10303-21;"));
		assertTrue(step.contains("SI_UNIT(.MILLI.,.METRE.)"));
		assertEquals(3, count(step, "=PRODUCT('"));
		assertEquals(3, count(step, "=MANIFOLD_SOLID_BREP("));
		assertTrue(step.contains("PRODUCT('Fins','Fins'"));

		// In a closed shell every edge is used exactly twice, in opposite directions
		Map<String, int[]> uses = new HashMap<>();
		Matcher m = Pattern.compile("ORIENTED_EDGE\\('',\\*,\\*,#(\\d+),\\.(T|F)\\.\\)").matcher(step);
		while (m.find()) {
			int[] counts = uses.computeIfAbsent(m.group(1), k -> new int[2]);
			counts[m.group(2).equals("T") ? 0 : 1]++;
		}
		assertEquals(count(step, "=EDGE_CURVE("), uses.size());
		for (Map.Entry<String, int[]> entry : uses.entrySet()) {
			assertEquals(1, entry.getValue()[0], "edge #" + entry.getKey());
			assertEquals(1, entry.getValue()[1], "edge #" + entry.getKey());
		}
	}

	@Test
	void finHasDesignVertices() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(createFinSet());
		Written written = write(exporter, Format.STEP);
		ExportedPart part = written.parts().get(0);

		List<double[]> points = new ArrayList<>();
		Matcher m = Pattern.compile("CARTESIAN_POINT\\('',\\(([-0-9.]+),([-0-9.]+),([-0-9.]+)\\)\\)")
				.matcher(written.text());
		while (m.find()) {
			points.add(new double[]{Double.parseDouble(m.group(1)) - part.minXmm(),
					Double.parseDouble(m.group(2)) - part.minYmm(), Double.parseDouble(m.group(3))});
		}
		for (double z : new double[]{0, 3}) {
			assertHasPoint3D(points, 0, 0, z);
			assertHasPoint3D(points, 30, 60, z);
			assertHasPoint3D(points, 80, 60, z);
			assertHasPoint3D(points, 100, 0, z);
		}
	}

	@Test
	void verifierMatchesExportedParts() throws IOException {
		CADDimensionVerifier.Result result = write(createAllParts(), Format.STEP).verify(Format.STEP);
		assertTrue(result.isAllMatching(), describe(result));
		assertEquals(CADDimensionVerifier.Units.MILLIMETERS, result.units());

		assertMeasured(result, "Fins", Dimension.OUTLINE_HEIGHT, 0.060);
		assertMeasured(result, "Fins", Dimension.MATERIAL_THICKNESS, 0.003);
		assertMeasured(result, "Ring", Dimension.INNER_DIAMETER, 0.018);
		assertMeasured(result, "Ring", Dimension.HOLE_OFFSET, 0.013);
		assertMeasured(result, "Disc", Dimension.MATERIAL_THICKNESS, 0.006);
	}

	@Test
	void verifierDetectsWrongUnits() throws IOException {
		Written written = write(createAllParts(), Format.STEP);
		String inches = written.text().replace("SI_UNIT(.MILLI.,.METRE.)", "SI_UNIT($,.METRE.)");
		CADDimensionVerifier.Result result = CADDimensionVerifier.verify(Format.STEP,
				new ByteArrayInputStream(inches.getBytes(StandardCharsets.US_ASCII)), written.parts());
		assertFalse(result.isAllMatching());
	}

	@Test
	void numbersHaveNineDecimalsAndNoNegativeZero() {
		assertEquals("12.345678912", STEPPartsWriter.number(12.3456789123));
		assertEquals("0.000000000", STEPPartsWriter.number(-1e-12));
		assertEquals("-0.000000001", STEPPartsWriter.number(-1e-9));
	}

	@Test
	void nonFiniteNumbersAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> STEPPartsWriter.number(Double.NaN));
		assertThrows(IllegalArgumentException.class, () -> STEPPartsWriter.number(Double.POSITIVE_INFINITY));
	}

	@Test
	void stringsAreEscaped() {
		assertEquals("'Bob''s fin \\X2\\00E9\\X0\\'", STEPPartsWriter.string("Bob's fin é"));
	}

	/**
	 * FILE_NAME holds, in this order (ISO 10303-21, 8.2.2): name, time stamp, author, organization, preprocessor
	 * version, originating system and authorization.
	 */
	@Test
	void headerNamesTheAuthorAndTheProgramVersion() {
		String program = "'OpenRocket " + BuildProperties.getVersion() + "'";
		String header = STEPPartsWriter.header("Rocket", "Jane O'Neil", LocalDateTime.of(2026, 11, 8, 12, 30, 15, 500));

		assertTrue(header.contains("FILE_NAME('Rocket','2026-11-08T12:30:15',('Jane O''Neil'),(''),"
				+ program + "," + program + ",'');"), header);
		assertTrue(header.contains("FILE_SCHEMA(('AUTOMOTIVE_DESIGN { 1 0 10303 214 1 1 1 1 }'));"), header);
	}

	@Test
	void overlappingHolesAreMeasuredAsOneHole() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.030, 0), List.of(
				new RingSvgExporter.Hole(-0.005, 0, 0.010),
				new RingSvgExporter.Hole(0.005, 0, 0.010)));

		CADDimensionVerifier.Result result = write(exporter, Format.STEP).verify(Format.STEP);
		assertTrue(result.isAllMatching(), describe(result));
		assertMeasured(result, "Ring", Dimension.HOLE_DIAMETER, 0.020);
		assertMeasured(result, "Ring", Dimension.HOLE_OFFSET, 0.005);
	}

	/**
	 * Every face is named after its place on the part, so that it can be found in the file when debugging.
	 */
	@Test
	void facesAreNamed() throws IOException {
		String step = write(createAllParts(), Format.STEP).text();
		assertEquals(0, count(step, "=ADVANCED_FACE('',"));
		assertTrue(step.contains("=ADVANCED_FACE('top',"));
		assertTrue(step.contains("=ADVANCED_FACE('bottom',"));
		assertTrue(step.contains("=ADVANCED_FACE('outline side 1',"));
		assertTrue(step.contains("=PLANE('outline side 1',"));
		assertTrue(step.contains("=ADVANCED_FACE('hole 1',"));
		assertTrue(step.contains("=CYLINDRICAL_SURFACE('hole 1',"));

		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.030, 0), List.of(
				new RingSvgExporter.Hole(-0.005, 0, 0.010),
				new RingSvgExporter.Hole(0.005, 0, 0.010)));
		step = write(exporter, Format.STEP).text();
		assertTrue(step.contains("=ADVANCED_FACE('hole 1 side 2',"));
		assertTrue(step.contains("=FACE_OUTER_BOUND('hole 1 side 2',"));
	}

	@Test
	void partWithoutThicknessIsRejected() {
		TrapezoidFinSet fins = createFinSet();
		fins.setThickness(0);
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(fins);
		assertThrows(IllegalStateException.class, () -> write(exporter, Format.STEP));
	}

	/**
	 * Two holes of radius 5 mm with centers 10 mm apart touch at one point, which is not a manifold solid.
	 */
	@Test
	void touchingHolesAreRejected() {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.030, 0), List.of(
				new RingSvgExporter.Hole(-0.005, 0, 0.005),
				new RingSvgExporter.Hole(0.005, 0, 0.005)));
		assertThrows(IllegalStateException.class, () -> write(exporter, Format.STEP));
		assertDoesNotThrow(() -> write(exporter, Format.DXF));
	}

	@Test
	void holeThroughOuterEdgeIsRejected() {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.020, 0), List.of(new RingSvgExporter.Hole(0.015, 0, 0.010)));
		assertThrows(IllegalStateException.class, () -> write(exporter, Format.STEP));
	}
}
