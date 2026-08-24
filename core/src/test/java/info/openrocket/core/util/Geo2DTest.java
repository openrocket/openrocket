package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Verifies {@link Geo2D}'s geometry helpers.
 * <p>
 * The expected values for {@code relativeCCW} and {@code segmentsIntersect} are those
 * produced by {@code java.awt.geom.Line2D} (the routines {@code Geo2D} replaced); they are
 * hard-coded here so the test itself stays free of any {@code java.desktop} dependency.
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
	public void testRelativeCCW() {
		// { x1,y1,x2,y2, px,py, expected }
		double[][] cases = {
				{ 0, 0, 10, 0, 5, 5, -1 },     // left of the line
				{ 0, 0, 10, 0, 5, -5, 1 },     // right of the line
				{ 0, 0, 10, 0, 5, 0, 0 },      // on the segment
				{ 0, 0, 10, 0, -3, 0, -1 },    // collinear, before the start
				{ 0, 0, 10, 0, 13, 0, 1 },     // collinear, beyond the end
				{ 0, 0, 10, 0, 0, 0, 0 },      // exactly on the start point
				{ 0, 0, 10, 0, 10, 0, 0 },     // exactly on the end point
				{ 1, 1, 4, 5, 2, 2, 1 },       // arbitrary diagonal
		};
		for (double[] c : cases) {
			int actual = Geo2D.relativeCCW(c[0], c[1], c[2], c[3], c[4], c[5]);
			assertEquals((int) c[6], actual,
					"relativeCCW mismatch for " + java.util.Arrays.toString(c));
		}
	}

	@Test
	public void testSegmentsIntersect() {
		// { x1,y1,x2,y2, x3,y3,x4,y4, expected(1=true,0=false) }
		double[][] cases = {
				{ 0, 0, 10, 10, 0, 10, 10, 0, 1 },   // clean cross
				{ 0, 0, 10, 0, 5, 1, 5, 10, 0 },     // disjoint (no touch)
				{ 0, 0, 10, 0, 10, 0, 20, 0, 1 },    // collinear, touch at endpoint
				{ 0, 0, 10, 0, 5, 0, 15, 0, 1 },     // collinear, overlapping
				{ 0, 0, 10, 0, 12, 0, 20, 0, 0 },    // collinear, disjoint
				{ 0, 0, 10, 0, 0, 5, 10, 5, 0 },     // parallel, non-collinear
				{ 0, 0, 10, 10, 5, 5, 5, 20, 1 },    // T-junction (endpoint on segment)
				{ 0, 0, 10, 0, 5, -5, 5, 5, 1 },     // perpendicular cross
				{ 0, 0, 1, 1, 2, 2, 3, 3, 0 },       // collinear, disjoint diagonal
				{ 0, 0, 3, 3, 1, 1, 2, 2, 1 },       // collinear, one inside the other
		};
		for (double[] c : cases) {
			boolean actual = Geo2D.segmentsIntersect(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]);
			assertEquals(c[8] != 0, actual,
					"segmentsIntersect mismatch for " + java.util.Arrays.toString(c));
		}
	}
}
