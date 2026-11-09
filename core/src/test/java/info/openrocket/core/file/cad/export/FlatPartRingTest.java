package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.FlatPart.Arc;
import info.openrocket.core.file.cad.export.FlatPart.ArcLoop;
import info.openrocket.core.file.cad.export.FlatPart.Circle;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.file.cad.export.FlatPart.Region;
import info.openrocket.core.file.svg.export.RingSvgExporter;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static info.openrocket.core.file.cad.export.PartFixtures.createDisc;
import static info.openrocket.core.file.cad.export.PartFixtures.createRing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the flat part of a centering ring, in particular how its holes are combined.
 */
class FlatPartRingTest extends BaseTestCase {
	/** Area of the union of two circles of radius 10 mm that are 10 mm apart: 2 circles minus their lens. */
	private static final double MERGED_HOLES_AREA_MM2 =
			2 * Math.PI * 100 - (2 * 100 * Math.acos(0.5) - 5 * Math.sqrt(300));

	private static double dimension(FlatPart part, Dimension dimension, int index) {
		return part.getDimensions().stream()
				.filter(d -> d.dimension() == dimension && d.index() == index).findFirst().orElseThrow().value();
	}

	private static double dimension(FlatPart part, Dimension dimension) {
		return dimension(part, dimension, 0);
	}

	private static boolean hasDimension(FlatPart part, Dimension dimension) {
		return part.getDimensions().stream().anyMatch(d -> d.dimension() == dimension);
	}

	private static List<RingSvgExporter.Hole> twoOverlappingHoles() {
		return List.of(new RingSvgExporter.Hole(-0.005, 0, 0.010), new RingSvgExporter.Hole(0.005, 0, 0.010));
	}

	/**
	 * Ring with an outer diameter of 50 mm, an inner diameter of 18 mm and 5 mm thick, with one hole of 6 mm at 12 mm
	 * and 5 mm from the center, so 13 mm from the center. It does not touch the bore.
	 */
	@Test
	void dimensionsMatchTheDesign() {
		FlatPart part = FlatPart.fromCenteringRing(createRing(0.025, 0.009),
				List.of(new RingSvgExporter.Hole(0.012, 0.005, 0.003)));

		assertEquals("Ring", part.getName());
		assertEquals(5, part.getThicknessMm(), 1e-9);
		assertEquals(0.050, dimension(part, Dimension.OUTER_DIAMETER), 1e-9);
		assertEquals(0.018, dimension(part, Dimension.INNER_DIAMETER), 1e-9);
		assertEquals(0.006, dimension(part, Dimension.HOLE_DIAMETER, 1), 1e-9);
		assertEquals(0.013, dimension(part, Dimension.HOLE_OFFSET, 1), 1e-9);
		assertEquals(0.005, dimension(part, Dimension.MATERIAL_THICKNESS), 1e-9);
		assertEquals(Math.PI * (25 * 25 - 9 * 9 - 3 * 3) * 5 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
	}

	@Test
	void ringIsCenteredOnTheOrigin() {
		FlatPart part = FlatPart.fromCenteringRing(createRing(0.025, 0.009), List.of());

		Region region = part.getRegions().get(0);
		Circle outer = assertInstanceOf(Circle.class, region.outer());
		assertEquals(0, outer.cx(), 1e-9);
		assertEquals(0, outer.cy(), 1e-9);
		assertEquals(25, outer.r(), 1e-9);
		assertEquals(1, region.holes().size());
		assertEquals(9, assertInstanceOf(Circle.class, region.holes().get(0)).r(), 1e-9);

		double[] bounds = part.translated(100, 50).getBounds();
		assertEquals(75, bounds[0], 1e-9);
		assertEquals(25, bounds[1], 1e-9);
		assertEquals(125, bounds[2], 1e-9);
		assertEquals(75, bounds[3], 1e-9);
	}

	@Test
	void solidDiscHasNoInnerDiameter() {
		FlatPart part = FlatPart.fromCenteringRing(createDisc(), List.of());

		assertTrue(part.getRegions().get(0).holes().isEmpty());
		assertFalse(hasDimension(part, Dimension.INNER_DIAMETER));
		assertEquals(0.075, dimension(part, Dimension.OUTER_DIAMETER), 1e-9);
		assertEquals(Math.PI * 37.5 * 37.5 * 6 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
	}

	/**
	 * Holes use the same orientation as the SVG export: the Z offset of a hole is downwards in the drawing.
	 */
	@Test
	void holePositionFollowsTheOrientationOfTheSvgExport() {
		FlatPart part = FlatPart.fromCenteringRing(createRing(0.025, 0),
				List.of(new RingSvgExporter.Hole(0.012, 0.005, 0.003)));

		Circle hole = assertInstanceOf(Circle.class, part.getRegions().get(0).holes().get(0));
		assertEquals(12, hole.cx(), 1e-9);
		assertEquals(-5, hole.cy(), 1e-9);
		assertEquals(3, hole.r(), 1e-9);
	}

	/**
	 * A motor tube of 78.74 mm in an automatic bore of 78.82 mm cuts nothing: the hole is not exported or reported.
	 */
	@Test
	void holeInsideTheBoreIsDropped() {
		FlatPart part = FlatPart.fromCenteringRing(createRing(0.049, 0.0394),
				List.of(new RingSvgExporter.Hole(0, 0, 0.03937)));

		assertEquals(1, part.getRegions().get(0).holes().size());
		assertFalse(hasDimension(part, Dimension.HOLE_DIAMETER));
		assertEquals(0.0788, dimension(part, Dimension.INNER_DIAMETER), 1e-9);
	}

	/**
	 * A manual inner diameter of 24 mm with a 36 mm motor tube through the ring: the 36 mm hole is cut, but the
	 * inner diameter of the ring is still reported, and the user is warned.
	 */
	@Test
	void manualBoreSmallerThanAMotorTubeIsReportedAndWarned() {
		FlatPart part = FlatPart.fromCenteringRing(createRing(0.025, 0.012),
				List.of(new RingSvgExporter.Hole(0, 0, 0.018)));

		List<FlatPart.Loop> holes = part.getRegions().get(0).holes();
		assertEquals(1, holes.size());
		assertEquals(18, assertInstanceOf(Circle.class, holes.get(0)).r(), 1e-9);
		assertEquals(0.024, dimension(part, Dimension.INNER_DIAMETER), 1e-9);
		assertEquals(0.036, dimension(part, Dimension.HOLE_DIAMETER, 1), 1e-9);
		assertEquals(Math.PI * (25 * 25 - 18 * 18) * 5 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
		assertEquals(1, part.getWarnings().size());
		assertTrue(part.getWarnings().get(0).contains("'Ring'"), part.getWarnings().get(0));
	}

	/**
	 * A ring with automatic diameters in a body tube with an inner diameter of 48 mm, around a motor tube with an
	 * outer diameter of 24 mm. The bore follows the motor tube, also after the motor tube is changed to 30 mm.
	 */
	@Test
	void automaticBoreFollowsTheMotorTube() {
		BodyTube body = new BodyTube();
		body.setOuterRadius(0.025);
		body.setThickness(0.001);
		body.setLength(0.3);
		InnerTube motorTube = new InnerTube();
		motorTube.setOuterRadius(0.012);
		motorTube.setLength(0.1);
		body.addChild(motorTube);
		CenteringRing ring = new CenteringRing();
		ring.setLength(0.005);
		ring.setName("Ring");
		body.addChild(ring);
		ring.setOuterRadiusAutomatic(true);
		ring.setInnerRadiusAutomatic(true);

		FlatPart part = FlatPart.fromCenteringRing(ring, RingSvgExporter.holesFromMotorMounts(List.of(motorTube)));
		assertEquals(0.048, dimension(part, Dimension.OUTER_DIAMETER), 1e-9);
		assertEquals(0.024, dimension(part, Dimension.INNER_DIAMETER), 1e-9);
		assertEquals(12, assertInstanceOf(Circle.class, part.getRegions().get(0).holes().get(0)).r(), 1e-9);
		assertTrue(part.getWarnings().isEmpty());

		motorTube.setOuterRadius(0.015);
		part = FlatPart.fromCenteringRing(ring, RingSvgExporter.holesFromMotorMounts(List.of(motorTube)));
		assertEquals(0.030, dimension(part, Dimension.INNER_DIAMETER), 1e-9);
		assertEquals(1, part.getRegions().get(0).holes().size());
		assertEquals(15, assertInstanceOf(Circle.class, part.getRegions().get(0).holes().get(0)).r(), 1e-9);
		assertEquals(Math.PI * (24 * 24 - 15 * 15) * 5 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
		assertTrue(part.getWarnings().isEmpty());
	}

	/**
	 * Changing the outer diameter to 40 mm, the thickness to 4 mm and the inner diameter to 20 mm changes the part.
	 */
	@Test
	void changedRingDimensionsAreExported() {
		CenteringRing ring = createRing(0.025, 0.009);
		ring.setOuterRadius(0.020);
		ring.setLength(0.004);
		ring.setInnerRadius(0.010);

		FlatPart part = FlatPart.fromCenteringRing(ring, List.of());
		assertEquals(4, part.getThicknessMm(), 1e-9);
		assertEquals(20, assertInstanceOf(Circle.class, part.getRegions().get(0).outer()).r(), 1e-9);
		assertEquals(10, assertInstanceOf(Circle.class, part.getRegions().get(0).holes().get(0)).r(), 1e-9);
		assertEquals(0.040, dimension(part, Dimension.OUTER_DIAMETER), 1e-9);
		assertEquals(0.020, dimension(part, Dimension.INNER_DIAMETER), 1e-9);
		assertEquals(0.004, dimension(part, Dimension.MATERIAL_THICKNESS), 1e-9);
		assertEquals(Math.PI * (20 * 20 - 10 * 10) * 4 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
	}

	@Test
	void holeWithTheSizeOfTheBoreIsNotCutTwice() {
		FlatPart part = FlatPart.fromCenteringRing(createRing(0.025, 0.010),
				List.of(new RingSvgExporter.Hole(0, 0, 0.010)));

		assertEquals(1, part.getRegions().get(0).holes().size());
		assertFalse(hasDimension(part, Dimension.HOLE_DIAMETER));
	}

	/**
	 * Two holes of radius 10 mm, 10 mm apart, become one outline of two arcs, with the area of the union.
	 */
	@Test
	void overlappingHolesAreMergedIntoOneOutline() {
		FlatPart part = FlatPart.fromCenteringRing(createRing(0.030, 0), twoOverlappingHoles());

		Region region = part.getRegions().get(0);
		assertEquals(1, region.holes().size());
		ArcLoop loop = assertInstanceOf(ArcLoop.class, region.holes().get(0));
		assertEquals(2, loop.arcs().size());
		// The loop runs clockwise, so its signed area is negative
		assertEquals(-MERGED_HOLES_AREA_MM2, loop.signedArea(), 1e-6);
		assertEquals((Math.PI * 900 - MERGED_HOLES_AREA_MM2) * 5 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
		// Both holes are still reported with their design dimensions
		assertEquals(0.020, dimension(part, Dimension.HOLE_DIAMETER, 1), 1e-9);
		assertEquals(0.020, dimension(part, Dimension.HOLE_DIAMETER, 2), 1e-9);
		assertEquals(0.005, dimension(part, Dimension.HOLE_OFFSET, 1), 1e-9);
	}

	/**
	 * Each arc of a merged hole ends where the next one starts, and the last one ends where the first one starts.
	 */
	@Test
	void arcsOfAMergedHoleFormAClosedLoop() {
		FlatPart part = FlatPart.fromCenteringRing(createRing(0.030, 0), twoOverlappingHoles());

		List<Arc> arcs = assertInstanceOf(ArcLoop.class, part.getRegions().get(0).holes().get(0)).arcs();
		for (int i = 0; i < arcs.size(); i++) {
			Arc arc = arcs.get(i);
			Arc next = arcs.get((i + 1) % arcs.size());
			assertEquals(next.x0(), arc.x1(), 1e-9);
			assertEquals(next.y0(), arc.y1(), 1e-9);
			assertTrue(arc.sweep() > 0 && arc.sweep() <= 2 * Math.PI);
		}
	}

	/**
	 * A cluster of four 19 mm motors in a ring of 53 mm, like the "Clustered motors" example: the four holes merge
	 * with each other and with the bore in the middle, to one outline.
	 */
	@Test
	void clusterOfFourMotorsGivesOneOutline() {
		double offset = 0.01045;
		FlatPart part = FlatPart.fromCenteringRing(createRing(0.0265, 0.0095), List.of(
				new RingSvgExporter.Hole(-offset, offset, 0.0095), new RingSvgExporter.Hole(offset, offset, 0.0095),
				new RingSvgExporter.Hole(offset, -offset, 0.0095), new RingSvgExporter.Hole(-offset, -offset, 0.0095)));

		assertEquals(1, part.getRegions().get(0).holes().size());
		assertInstanceOf(ArcLoop.class, part.getRegions().get(0).holes().get(0));
		// The area was checked against an independent geometry library (shapely): 926.2684 mm^2 of material
		assertEquals(926.2684 * 5 * 1e-9, dimension(part, Dimension.VOLUME), 1e-12);
	}

	@Test
	void nullRingIsRejected() {
		assertThrows(NullPointerException.class, () -> FlatPart.fromCenteringRing(null, List.of()));
	}
}
