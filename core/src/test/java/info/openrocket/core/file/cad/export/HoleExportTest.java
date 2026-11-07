package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADPartsExporter.Format;
import info.openrocket.core.file.cad.export.ExportTestSupport.Written;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.file.svg.export.RingSvgExporter;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static info.openrocket.core.file.cad.export.PartFixtures.createRing;
import static info.openrocket.core.file.cad.export.ExportTestSupport.write;
import static info.openrocket.core.file.cad.export.ExportTestSupport.find;
import static info.openrocket.core.file.cad.export.ExportTestSupport.describe;
import static info.openrocket.core.file.cad.export.ExportTestSupport.dxfEntities;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of how holes in centering rings are exported, for all formats.
 */
class HoleExportTest extends BaseTestCase {
	/**
	 * A motor tube that fits inside a slightly larger bore cuts nothing and must not create a nested hole.
	 */
	@Test
	void holeInsideBoreIsDropped() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.049, 0.0394), List.of(new RingSvgExporter.Hole(0, 0, 0.03937)));

		Written dxf = write(exporter, Format.DXF);
		assertEquals(2, dxfEntities(dxf.text(), "CIRCLE").size());
		assertTrue(dxf.parts().get(0).part().getDimensions().stream()
				.noneMatch(d -> d.dimension() == Dimension.HOLE_DIAMETER));
		for (Format format : Format.values()) {
			CADDimensionVerifier.Result result = write(exporter, format).verify(format);
			assertTrue(result.isAllMatching(), format + ": " + describe(result));
		}
	}

	/**
	 * A hole with the same size and position as the inner bore must not be cut twice.
	 */
	@Test
	void holeMatchingInnerBoreIsNotCutTwice() throws IOException {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(createRing(0.025, 0.010), List.of(new RingSvgExporter.Hole(0, 0, 0.010)));
		Written written = write(exporter, Format.DXF);

		assertEquals(2, dxfEntities(written.text(), "CIRCLE").size());
		for (Format format : Format.values()) {
			CADDimensionVerifier.Result result = write(exporter, format).verify(format);
			assertTrue(result.isAllMatching(), format + ": " + describe(result));
		}
	}

	/**
	 * Two overlapping holes of radius 10 mm, 10 mm apart, in a ring of radius 30 mm. The union of two circles of
	 * radius r at distance d is 2*pi*r^2 minus the lens 2*r^2*acos(d/2r) - (d/2)*sqrt(4r^2 - d^2).
	 */
	@Test
	void overlappingHolesAreMergedIntoOneOutline() throws IOException {
		double lens = 2 * 100 * Math.acos(0.5) - 5 * Math.sqrt(300);
		double area = Math.PI * 900 - (2 * Math.PI * 100 - lens);
		CenteringRing ring = createRing(0.030, 0);
		List<RingSvgExporter.Hole> holes = List.of(
				new RingSvgExporter.Hole(-0.005, 0, 0.010),
				new RingSvgExporter.Hole(0.005, 0, 0.010));
		assertEquals(area * 5e-9, find(FlatPart.fromCenteringRing(ring, holes), Dimension.VOLUME), 1e-15);

		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addCenteringRing(ring, holes);
		for (Format format : Format.values()) {
			CADDimensionVerifier.Result result = write(exporter, format).verify(format);
			assertTrue(result.isAllMatching(), format + ": " + describe(result));
		}
	}
}
