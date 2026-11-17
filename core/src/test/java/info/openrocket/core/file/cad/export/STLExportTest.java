package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADDimensionVerifier.Status;
import info.openrocket.core.file.cad.export.CADPartsExporter.Format;
import info.openrocket.core.file.cad.export.ExportTestSupport.Written;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.file.svg.export.RingSvgExporter;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static info.openrocket.core.file.cad.export.PartFixtures.FIN_VOLUME_MM3;
import static info.openrocket.core.file.cad.export.PartFixtures.RING_VOLUME_MM3;
import static info.openrocket.core.file.cad.export.PartFixtures.createFinSet;
import static info.openrocket.core.file.cad.export.PartFixtures.createRing;
import static info.openrocket.core.file.cad.export.ExportTestSupport.createAllParts;
import static info.openrocket.core.file.cad.export.ExportTestSupport.write;
import static info.openrocket.core.file.cad.export.ExportTestSupport.assertMeasured;
import static info.openrocket.core.file.cad.export.ExportTestSupport.find;
import static info.openrocket.core.file.cad.export.ExportTestSupport.describe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the STL export and of the mesh generation that it shares with 3MF.
 */
class STLExportTest extends BaseTestCase {
	@Test
	void meshIsClosedAndHasDesignVolume() throws IOException {
		Written written = write(createAllParts(), Format.STL);
		List<double[]> triangles = stlTriangles(written.bytes());

		assertClosedMesh(triangles);
		double expected = FIN_VOLUME_MM3 + RING_VOLUME_MM3 - Math.PI * 3 * 3 * 5 + Math.PI * 37.5 * 37.5 * 6;
		assertEquals(expected, meshVolume(triangles), expected * 1e-3);
	}

	@Test
	void finHasExactExtents() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(createFinSet());
		Written written = write(exporter, Format.STL);

		double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
		double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
		for (double[] t : stlTriangles(written.bytes())) {
			for (int i = 0; i < 9; i++) {
				min[i % 3] = Math.min(min[i % 3], t[i]);
				max[i % 3] = Math.max(max[i % 3], t[i]);
			}
		}
		assertEquals(100, max[0] - min[0], 1e-4);
		assertEquals(60, max[1] - min[1], 1e-4);
		assertEquals(3, max[2] - min[2], 1e-6);
		assertEquals(FIN_VOLUME_MM3, meshVolume(stlTriangles(written.bytes())), 1e-3);
	}

	@Test
	void verifierMatchesExportedParts() throws IOException {
		CADDimensionVerifier.Result result = write(createAllParts(), Format.STL).verify(Format.STL);
		assertTrue(result.isAllMatching(), describe(result));
		assertEquals(CADDimensionVerifier.Units.NONE, result.units());

		assertMeasured(result, "Fins", Dimension.OUTLINE_LENGTH, 0.100);
		assertMeasured(result, "Fins", Dimension.MATERIAL_THICKNESS, 0.003);
		assertEquals(FIN_VOLUME_MM3 * 1e-9, find(result, "Fins", Dimension.VOLUME).measured(), 1e-12);
		assertMeasured(result, "Ring", Dimension.OUTER_DIAMETER, 0.050);
		assertMeasured(result, "Ring", Dimension.MATERIAL_THICKNESS, 0.005);
		assertMeasured(result, "Disc", Dimension.OUTER_DIAMETER, 0.075);
		// Holes are not measured from a mesh
		assertEquals(Status.REFERENCE, find(result, "Ring", Dimension.HOLE_DIAMETER).status());
	}

	@Test
	void circleSegmentsKeepChordDeviationSmall() {
		for (double r : new double[]{1, 10, 50, 150}) {
			int n = PartMesher.circleSegments(r);
			assertEquals(0, n % 4);
			double deviation = r * (1 - Math.cos(Math.PI / n));
			assertTrue(deviation <= PartMesher.MAX_CHORD_DEVIATION_MM + 1e-12, "r=" + r + " n=" + n);
		}
	}

	/**
	 * Two overlapping holes of radius 10 mm, 10 mm apart, in a ring of radius 30 mm: the mesh must stay closed and
	 * have the exact volume of the ring with the merged hole.
	 */
	@Test
	void overlappingHolesGiveClosedMeshWithExactVolume() throws IOException {
		double lens = 2 * 100 * Math.acos(0.5) - 5 * Math.sqrt(300);
		double area = Math.PI * 900 - (2 * Math.PI * 100 - lens);
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.030, 0), List.of(
				new RingSvgExporter.Hole(-0.005, 0, 0.010),
				new RingSvgExporter.Hole(0.005, 0, 0.010)));

		List<double[]> triangles = stlTriangles(write(exporter, Format.STL).bytes());
		assertClosedMesh(triangles);
		assertEquals(area * 5, meshVolume(triangles), area * 5 * 1e-3);
	}

	@Test
	void partWithoutThicknessIsRejected() {
		TrapezoidFinSet fins = createFinSet();
		fins.setThickness(0);
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(fins);
		assertThrows(IllegalStateException.class, () -> write(exporter, Format.STL));
	}

	@Test
	void holeThroughOuterEdgeIsRejected() {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.020, 0), List.of(new RingSvgExporter.Hole(0.015, 0, 0.010)));
		assertThrows(IllegalStateException.class, () -> write(exporter, Format.STL));
	}

	static List<double[]> stlTriangles(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		assertFalse(new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("solid"));
		buffer.position(80);
		int count = buffer.getInt();
		assertEquals(84 + 50L * count, bytes.length);
		List<double[]> triangles = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			buffer.position(buffer.position() + 12);
			double[] t = new double[9];
			for (int j = 0; j < 9; j++) {
				t[j] = buffer.getFloat();
			}
			buffer.getShort();
			triangles.add(t);
		}
		return triangles;
	}

	/**
	 * Asserts that every directed edge has exactly one opposite edge, i.e. the mesh is closed and consistently
	 * oriented.
	 */
	static void assertClosedMesh(List<double[]> triangles) {
		Map<String, Integer> edges = new HashMap<>();
		for (double[] t : triangles) {
			for (int i = 0; i < 3; i++) {
				int j = (i + 1) % 3;
				String key = String.format(Locale.ROOT, "%.4f,%.4f,%.4f>%.4f,%.4f,%.4f",
						t[3 * i], t[3 * i + 1], t[3 * i + 2], t[3 * j], t[3 * j + 1], t[3 * j + 2]);
				edges.merge(key, 1, Integer::sum);
			}
		}
		for (Map.Entry<String, Integer> edge : edges.entrySet()) {
			assertEquals(1, edge.getValue(), "Duplicate directed edge " + edge.getKey());
			String[] ends = edge.getKey().split(">");
			assertTrue(edges.containsKey(ends[1] + ">" + ends[0]), "Open edge " + edge.getKey());
		}
	}

	static double meshVolume(List<double[]> triangles) {
		double volume = 0;
		for (double[] t : triangles) {
			volume += (t[0] * (t[4] * t[8] - t[5] * t[7])
					- t[1] * (t[3] * t[8] - t[5] * t[6])
					+ t[2] * (t[3] * t[7] - t[4] * t[6])) / 6;
		}
		return volume;
	}
}
