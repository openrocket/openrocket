package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.file.cad.export.FlatPart.Polygon;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static info.openrocket.core.file.cad.export.PartFixtures.FIN_VOLUME_MM3;
import static info.openrocket.core.file.cad.export.PartFixtures.createFinSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests of the flat part of a fin.
 */
class FlatPartFinTest extends BaseTestCase {
	private static double dimension(FlatPart part, Dimension dimension) {
		return part.getDimensions().stream().filter(d -> d.dimension() == dimension).findFirst().orElseThrow().value();
	}

	private static boolean hasDimension(FlatPart part, Dimension dimension) {
		return part.getDimensions().stream().anyMatch(d -> d.dimension() == dimension);
	}

	private static void assertHasCorner(Polygon polygon, double x, double y) {
		for (int i = 0; i < polygon.size(); i++) {
			if (Math.abs(polygon.xs()[i] - x) < 1e-6 && Math.abs(polygon.ys()[i] - y) < 1e-6) {
				return;
			}
		}
		throw new AssertionError(String.format(Locale.ROOT, "No corner at (%.6f, %.6f)", x, y));
	}

	/**
	 * Root 100 mm, tip 50 mm, height 60 mm, sweep 30 mm: the corners are (0,0), (30,60), (80,60) and (100,0).
	 */
	@Test
	void trapezoidOutlineIsCounterClockwiseInMillimeters() {
		FlatPart part = FlatPart.fromFinSet(createFinSet());

		assertEquals(1, part.getRegions().size());
		Polygon outline = assertInstanceOf(Polygon.class, part.getRegions().get(0).outer());
		assertEquals(4, outline.size());
		assertHasCorner(outline, 0, 0);
		assertHasCorner(outline, 30, 60);
		assertHasCorner(outline, 80, 60);
		assertHasCorner(outline, 100, 0);
		assertEquals(4500, outline.signedArea(), 1e-6);
		assertTrue(part.getRegions().get(0).holes().isEmpty());
	}

	@Test
	void dimensionsMatchTheDesign() {
		FlatPart part = FlatPart.fromFinSet(createFinSet());

		assertEquals("Fins", part.getName());
		assertEquals(3, part.getThicknessMm(), 1e-9);
		assertEquals(0.100, dimension(part, Dimension.OUTLINE_LENGTH), 1e-9);
		assertEquals(0.060, dimension(part, Dimension.OUTLINE_HEIGHT), 1e-9);
		assertEquals(0.100, dimension(part, Dimension.ROOT_CHORD), 1e-9);
		assertEquals(0.050, dimension(part, Dimension.TIP_CHORD), 1e-9);
		assertEquals(0.060, dimension(part, Dimension.FIN_HEIGHT), 1e-9);
		assertEquals(0.030, dimension(part, Dimension.SWEEP_LENGTH), 1e-9);
		assertEquals(0.003, dimension(part, Dimension.MATERIAL_THICKNESS), 1e-9);
		assertEquals(FIN_VOLUME_MM3 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
		assertFalse(hasDimension(part, Dimension.TAB_LENGTH));
	}

	/**
	 * A tab of 40 mm by 5 mm in the middle of the root is part of the outline, 5 mm below the root.
	 */
	@Test
	void tabIsPartOfTheOutline() {
		TrapezoidFinSet fins = createFinSet();
		fins.setTabHeight(0.005);
		fins.setTabLength(0.040);

		FlatPart part = FlatPart.fromFinSet(fins);

		assertEquals(1, part.getRegions().size());
		Polygon outline = assertInstanceOf(Polygon.class, part.getRegions().get(0).outer());
		assertHasCorner(outline, 30, -5);
		assertHasCorner(outline, 70, -5);
		assertEquals(0.065, dimension(part, Dimension.OUTLINE_HEIGHT), 1e-9);
		assertEquals(0.100, dimension(part, Dimension.OUTLINE_LENGTH), 1e-9);
		assertEquals(0.040, dimension(part, Dimension.TAB_LENGTH), 1e-9);
		assertEquals(0.005, dimension(part, Dimension.TAB_HEIGHT), 1e-9);
		assertEquals((4500 + 40 * 5) * 3 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
	}

	/**
	 * A tab that does not touch the fin is a second area of material.
	 */
	@Test
	void tabBeyondTheFinIsASeparateRegion() {
		TrapezoidFinSet fins = createFinSet();
		fins.setTabHeight(0.005);
		fins.setTabLength(0.040);
		fins.setTabOffsetMethod(AxialMethod.TOP);
		fins.setTabOffset(0.120);
		assertTrue(fins.isTabBeyondFin());

		FlatPart part = FlatPart.fromFinSet(fins);

		assertEquals(2, part.getRegions().size());
		Polygon tab = assertInstanceOf(Polygon.class, part.getRegions().get(1).outer());
		assertEquals(40 * 5, tab.signedArea(), 1e-6);
		assertEquals((4500 + 40 * 5) * 3 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
	}

	/**
	 * A freeform fin with a notch: (0,0), (40,50), (50,20), (70,60), (90,0), 2 mm thick. By the shoelace formula
	 * the area is 2750 mm^2.
	 */
	@Test
	void concaveFreeformFinHasTheExactArea() {
		FreeformFinSet fins = new FreeformFinSet();
		createFinSet().getParent().addChild(fins);
		fins.setThickness(0.002);
		fins.setPoints(new Coordinate[]{new Coordinate(0, 0), new Coordinate(0.04, 0.05),
				new Coordinate(0.05, 0.02), new Coordinate(0.07, 0.06), new Coordinate(0.09, 0)});

		FlatPart part = FlatPart.fromFinSet(fins);

		Polygon outline = assertInstanceOf(Polygon.class, part.getRegions().get(0).outer());
		assertEquals(2750, outline.signedArea(), 1e-6);
		assertEquals(2750 * 2 * 1e-9, dimension(part, Dimension.VOLUME), 1e-15);
		assertEquals(0.090, dimension(part, Dimension.OUTLINE_LENGTH), 1e-9);
		assertEquals(0.060, dimension(part, Dimension.OUTLINE_HEIGHT), 1e-9);
	}

	@Test
	void translatingMovesTheBounds() {
		FlatPart part = FlatPart.fromFinSet(createFinSet());
		double[] bounds = part.getBounds();
		assertEquals(0, bounds[0], 1e-9);
		assertEquals(0, bounds[1], 1e-9);
		assertEquals(100, bounds[2], 1e-9);
		assertEquals(60, bounds[3], 1e-9);

		double[] moved = part.translated(10, -5).getBounds();
		assertEquals(10, moved[0], 1e-9);
		assertEquals(-5, moved[1], 1e-9);
		assertEquals(110, moved[2], 1e-9);
		assertEquals(55, moved[3], 1e-9);
		// The original is not changed
		assertEquals(0, part.getBounds()[0], 1e-9);
	}

	@Test
	void nullFinSetIsRejected() {
		assertThrows(NullPointerException.class, () -> FlatPart.fromFinSet(null));
	}
}
