package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;
import info.openrocket.core.file.cad.export.CADPartsExporter.Format;
import info.openrocket.core.file.cad.export.ExportTestSupport.Written;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static info.openrocket.core.file.cad.export.PartFixtures.FIN_VOLUME_MM3;
import static info.openrocket.core.file.cad.export.PartFixtures.createFinSet;
import static info.openrocket.core.file.cad.export.ExportTestSupport.createAllParts;
import static info.openrocket.core.file.cad.export.ExportTestSupport.write;
import static info.openrocket.core.file.cad.export.ExportTestSupport.assertMeasured;
import static info.openrocket.core.file.cad.export.ExportTestSupport.find;
import static info.openrocket.core.file.cad.export.ExportTestSupport.describe;
import static info.openrocket.core.file.cad.export.ExportTestSupport.count;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the 3MF export.
 */
class ThreeMFExportTest extends BaseTestCase {
	@Test
	void hasOneClosedObjectPerPart() throws IOException {
		Written written = write(createAllParts(), Format.THREE_MF);
		Map<String, String> files = unzip(written.bytes());
		assertTrue(files.containsKey("[Content_Types].xml"));
		assertTrue(files.containsKey("_rels/.rels"));
		String model = files.get("3D/3dmodel.model");
		assertTrue(model.contains("unit=\"millimeter\""));

		// Fin, ring and disc, in that order
		List<String> objects = new ArrayList<>();
		Matcher m = Pattern.compile("<object id=\"(\\d+)\" type=\"model\" name=\"([^\"]*)\">(.*?)</object>",
				Pattern.DOTALL).matcher(model);
		while (m.find()) {
			objects.add(m.group(2));
			assertClosedIndexedMesh(m.group(3));
		}
		assertEquals(List.of("Fins", "Ring", "Disc"), objects);
		assertEquals(3, count(model, "<item objectid="));
	}

	@Test
	void finHasDesignVolumeAndPositiveCoordinates() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(createFinSet());
		Written written = write(exporter, Format.THREE_MF);

		CADDimensionVerifier.Result result = written.verify(Format.THREE_MF);
		assertTrue(result.isAllMatching(), describe(result));
		assertEquals(CADDimensionVerifier.Units.MILLIMETERS, result.units());
		assertEquals(FIN_VOLUME_MM3 * 1e-9, find(result, "Fins", Dimension.VOLUME).measured(), 1e-12);
		// The part is placed at the origin, so slicers put it on the plate
		ExportedPart part = written.parts().get(0);
		assertEquals(0, part.minXmm(), 1e-9);
		assertEquals(0, part.minYmm(), 1e-9);
	}

	@Test
	void verifierMatchesExportedParts() throws IOException {
		CADDimensionVerifier.Result result = write(createAllParts(), Format.THREE_MF).verify(Format.THREE_MF);
		assertTrue(result.isAllMatching(), describe(result));
		assertMeasured(result, "Fins", Dimension.OUTLINE_HEIGHT, 0.060);
		assertMeasured(result, "Ring", Dimension.OUTER_DIAMETER, 0.050);
		assertMeasured(result, "Disc", Dimension.MATERIAL_THICKNESS, 0.006);
	}

	@Test
	void verifierRejectsOtherUnits() throws IOException {
		Written written = write(createAllParts(), Format.THREE_MF);
		Map<String, String> files = unzip(written.bytes());
		String inches = files.get("3D/3dmodel.model").replace("unit=\"millimeter\"", "unit=\"inch\"");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(out)) {
			for (Map.Entry<String, String> file : files.entrySet()) {
				zip.putNextEntry(new ZipEntry(file.getKey()));
				String content = file.getKey().equals("3D/3dmodel.model") ? inches : file.getValue();
				zip.write(content.getBytes(StandardCharsets.UTF_8));
				zip.closeEntry();
			}
		}
		CADDimensionVerifier.Result result = CADDimensionVerifier.verify(Format.THREE_MF,
				new ByteArrayInputStream(out.toByteArray()), written.parts());
		assertFalse(result.isAllMatching());
	}

	static Map<String, String> unzip(byte[] bytes) throws IOException {
		Map<String, String> files = new HashMap<>();
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
				files.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
			}
		}
		return files;
	}

	/**
	 * Asserts that every edge of an indexed 3MF mesh is used once in each direction.
	 */
	static void assertClosedIndexedMesh(String mesh) {
		Map<String, Integer> edges = new HashMap<>();
		Matcher m = Pattern.compile("<triangle v1=\"(\\d+)\" v2=\"(\\d+)\" v3=\"(\\d+)\"/>").matcher(mesh);
		int triangles = 0;
		while (m.find()) {
			triangles++;
			String[] v = {m.group(1), m.group(2), m.group(3)};
			for (int i = 0; i < 3; i++) {
				edges.merge(v[i] + ">" + v[(i + 1) % 3], 1, Integer::sum);
			}
		}
		assertTrue(triangles > 0);
		for (Map.Entry<String, Integer> edge : edges.entrySet()) {
			assertEquals(1, edge.getValue(), "Duplicate directed edge " + edge.getKey());
			String[] ends = edge.getKey().split(">");
			assertTrue(edges.containsKey(ends[1] + ">" + ends[0]), "Open edge " + edge.getKey());
		}
	}

	@Test
	void partWithoutThicknessIsRejected() {
		TrapezoidFinSet fins = createFinSet();
		fins.setThickness(0);
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(fins);
		assertThrows(IllegalStateException.class, () -> write(exporter, Format.THREE_MF));
	}
}
