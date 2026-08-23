package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Line2D;

import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link Geo2D} reproduces the semantics of the {@code java.awt.geom}
 * routines it replaced.  The AWT classes are used here purely as the reference oracle;
 * they are no longer referenced by the headless core itself.
 */
public class Geo2DTest {

	private static final double EPS = 1e-12;

	@Test
	public void testDistanceMatchesHypot() {
		assertEquals(5.0, Geo2D.distance(0, 0, 3, 4), EPS);
		assertEquals(0.0, Geo2D.distance(2, 2, 2, 2), EPS);
		assertEquals(Math.hypot(-1.5, 2.5), Geo2D.distance(1, 1, -0.5, 3.5), EPS);
	}

	@Test
	public void testRelativeCCWMatchesLine2D() {
		double[][] cases = {
				// x1,y1,x2,y2, px,py
				{ 0, 0, 10, 0, 5, 5 },     // left of the line
				{ 0, 0, 10, 0, 5, -5 },    // right of the line
				{ 0, 0, 10, 0, 5, 0 },     // on the segment
				{ 0, 0, 10, 0, -3, 0 },    // collinear, before the start
				{ 0, 0, 10, 0, 13, 0 },    // collinear, beyond the end
				{ 0, 0, 10, 0, 0, 0 },     // exactly on the start point
				{ 0, 0, 10, 0, 10, 0 },    // exactly on the end point
				{ 1, 1, 4, 5, 2, 2 },      // arbitrary diagonal
		};
		for (double[] c : cases) {
			int expected = Line2D.relativeCCW(c[0], c[1], c[2], c[3], c[4], c[5]);
			int actual = Geo2D.relativeCCW(c[0], c[1], c[2], c[3], c[4], c[5]);
			assertEquals(expected, actual,
					"relativeCCW mismatch for " + java.util.Arrays.toString(c));
		}
	}

	@Test
	public void testSegmentsIntersectMatchesLine2D() {
		double[][] cases = {
				// x1,y1,x2,y2, x3,y3,x4,y4
				{ 0, 0, 10, 10, 0, 10, 10, 0 },   // clean cross
				{ 0, 0, 10, 0, 5, 1, 5, 10 },     // disjoint (no touch)
				{ 0, 0, 10, 0, 10, 0, 20, 0 },    // collinear, touch at endpoint
				{ 0, 0, 10, 0, 5, 0, 15, 0 },     // collinear, overlapping
				{ 0, 0, 10, 0, 12, 0, 20, 0 },    // collinear, disjoint
				{ 0, 0, 10, 0, 0, 5, 10, 5 },     // parallel, non-collinear
				{ 0, 0, 10, 10, 5, 5, 5, 20 },    // T-junction (endpoint on segment)
				{ 0, 0, 10, 0, 5, -5, 5, 5 },     // perpendicular cross
				{ 0, 0, 1, 1, 2, 2, 3, 3 },       // collinear, disjoint diagonal
				{ 0, 0, 3, 3, 1, 1, 2, 2 },       // collinear, one inside the other
		};
		for (double[] c : cases) {
			boolean expected = Line2D.linesIntersect(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]);
			boolean actual = Geo2D.segmentsIntersect(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]);
			assertEquals(expected, actual,
					"segmentsIntersect mismatch for " + java.util.Arrays.toString(c));
		}
	}
}
