package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADDimensionVerifier.Check;
import info.openrocket.core.file.cad.export.CADDimensionVerifier.Status;
import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;
import info.openrocket.core.file.cad.export.CADPartsExporter.Format;
import info.openrocket.core.file.cad.export.ExportTestSupport.Written;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.file.svg.export.RingSvgExporter;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static info.openrocket.core.file.cad.export.PartFixtures.createFinSet;
import static info.openrocket.core.file.cad.export.PartFixtures.createRing;
import static info.openrocket.core.file.cad.export.ExportTestSupport.createAllParts;
import static info.openrocket.core.file.cad.export.ExportTestSupport.write;
import static info.openrocket.core.file.cad.export.ExportTestSupport.assertMeasured;
import static info.openrocket.core.file.cad.export.ExportTestSupport.find;
import static info.openrocket.core.file.cad.export.ExportTestSupport.describe;
import static info.openrocket.core.file.cad.export.ExportTestSupport.assertHasPoint;
import static info.openrocket.core.file.cad.export.ExportTestSupport.assertHasCircle;
import static info.openrocket.core.file.cad.export.ExportTestSupport.dxfEntities;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the DXF export.
 */
class DXFExportTest extends BaseTestCase {
	@Test
	void finOutlineIsWrittenInMillimeters() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(createFinSet());
		Written written = write(exporter, Format.DXF);

		String dxf = written.text();
		assertTrue(dxf.contains("$INSUNITS\n70\n4\n"), "Drawing units must be millimeters");
		List<double[]> vertices = dxfEntities(dxf, "VERTEX");
		ExportedPart part = written.parts().get(0);
		double x0 = part.minXmm();
		double y0 = part.minYmm();
		assertEquals(4, vertices.size());
		assertHasPoint(vertices, x0, y0);
		assertHasPoint(vertices, x0 + 30, y0 + 60);
		assertHasPoint(vertices, x0 + 80, y0 + 60);
		assertHasPoint(vertices, x0 + 100, y0);
	}

	@Test
	void finTabIsIncludedInOutline() throws IOException {
		TrapezoidFinSet fins = createFinSet();
		fins.setTabHeight(0.005);
		fins.setTabLength(0.040);
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(fins);
		Written written = write(exporter, Format.DXF);

		// Tab is 5 mm deep below the root, 40 mm long, centered on the 100 mm root chord
		ExportedPart part = written.parts().get(0);
		assertEquals(65, part.maxYmm() - part.minYmm(), 1e-6);
		List<double[]> vertices = dxfEntities(written.text(), "VERTEX");
		assertHasPoint(vertices, part.minXmm() + 30, part.minYmm());
		assertHasPoint(vertices, part.minXmm() + 70, part.minYmm());
		assertTrue(written.verify(Format.DXF).isAllMatching());
	}

	@Test
	void ringWithClusterHolesWritesAllCircles() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.025, 0), List.of(
				new RingSvgExporter.Hole(0.012, 0, 0.006),
				new RingSvgExporter.Hole(-0.012, 0, 0.006)));
		Written written = write(exporter, Format.DXF);

		List<double[]> circles = dxfEntities(written.text(), "CIRCLE");
		assertEquals(3, circles.size());
		ExportedPart part = written.parts().get(0);
		double cx = (part.minXmm() + part.maxXmm()) / 2;
		double cy = (part.minYmm() + part.maxYmm()) / 2;
		assertHasCircle(circles, cx, cy, 25);
		assertHasCircle(circles, cx + 12, cy, 6);
		assertHasCircle(circles, cx - 12, cy, 6);
	}

	@Test
	void verifierMatchesExportedParts() throws IOException {
		CADDimensionVerifier.Result result = write(createAllParts(), Format.DXF).verify(Format.DXF);
		assertTrue(result.isAllMatching(), describe(result));

		assertMeasured(result, "Fins", Dimension.OUTLINE_LENGTH, 0.100);
		assertMeasured(result, "Fins", Dimension.OUTLINE_HEIGHT, 0.060);
		assertMeasured(result, "Ring", Dimension.OUTER_DIAMETER, 0.050);
		assertMeasured(result, "Ring", Dimension.INNER_DIAMETER, 0.018);
		assertMeasured(result, "Ring", Dimension.HOLE_DIAMETER, 0.006);
		assertMeasured(result, "Ring", Dimension.HOLE_OFFSET, 0.013);
		assertMeasured(result, "Disc", Dimension.OUTER_DIAMETER, 0.075);
		// A 2D drawing has no thickness
		assertEquals(Status.REFERENCE, find(result, "Fins", Dimension.MATERIAL_THICKNESS).status());
	}

	@Test
	void verifierDetectsWrongDimension() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.025, 0.010), List.of());
		Written written = write(exporter, Format.DXF);

		// Enlarge the outer circle by 0.1 mm
		String tampered = written.text().replace("40\n25.000000\n", "40\n25.050000\n");
		CADDimensionVerifier.Result result = CADDimensionVerifier.verify(Format.DXF,
				new ByteArrayInputStream(tampered.getBytes(StandardCharsets.ISO_8859_1)), written.parts());

		assertFalse(result.isAllMatching());
		Check od = find(result, "Ring", Dimension.OUTER_DIAMETER);
		assertEquals(Status.MISMATCH, od.status());
		assertEquals(0.0501, od.measured(), 1e-9);
	}

	@Test
	void verifierRejectsNonMillimeterUnits() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(createFinSet());
		Written written = write(exporter, Format.DXF);

		// 1 = inches
		String inches = written.text().replace("$INSUNITS\n70\n4\n", "$INSUNITS\n70\n1\n");
		CADDimensionVerifier.Result result = CADDimensionVerifier.verify(Format.DXF,
				new ByteArrayInputStream(inches.getBytes(StandardCharsets.ISO_8859_1)), written.parts());
		assertEquals(CADDimensionVerifier.Units.OTHER, result.units());
		assertFalse(result.isAllMatching());
	}

	/**
	 * Two overlapping holes of radius 10 mm are cut as one contour of two arcs, not as two intersecting circles.
	 */
	@Test
	void overlappingHolesAreWrittenAsArcs() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.030, 0), List.of(
				new RingSvgExporter.Hole(-0.005, 0, 0.010),
				new RingSvgExporter.Hole(0.005, 0, 0.010)));
		Written written = write(exporter, Format.DXF);

		assertEquals(1, dxfEntities(written.text(), "CIRCLE").size());
		assertEquals(2, dxfEntities(written.text(), "ARC").size());
	}
}
