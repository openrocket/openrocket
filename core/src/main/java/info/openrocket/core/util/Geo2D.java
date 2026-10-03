package info.openrocket.core.util;

/**
 * Minimal 2D geometry helpers used by the headless core.
 * <p>
 * These replace the handful of {@code java.awt.geom.Line2D}/{@code java.awt.geom.Point2D}
 * calls that previously tied {@code core} to the {@code java.desktop} module even though
 * they are pure coordinate arithmetic with no rendering involved.  The segment-intersection
 * routine reproduces the semantics of {@code Line2D.linesIntersect} — including the
 * treatment of collinear, overlapping segments — so that fin-outline validation behaves
 * exactly as it did before.
 */
public final class Geo2D {

	private Geo2D() {
	}

	/**
	 * Euclidean distance between two points.
	 */
	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.hypot(x2 - x1, y2 - y1);
	}

	/**
	 * Determine where the point <code>(px, py)</code> lies relative to the directed line
	 * through <code>(x1, y1) -&gt; (x2, y2)</code>.
	 * <p>
	 * Returns +1 or -1 for a point to either side of the line, and 0 when the point lies
	 * on the segment itself.  For a point that is collinear with the line but outside the
	 * segment, a non-zero value is returned whose sign distinguishes the "before the start"
	 * and "beyond the end" cases.  This matches the contract of
	 * {@code java.awt.geom.Line2D.relativeCCW}, which the segment-intersection test relies on
	 * to handle collinear overlaps correctly.
	 */
	public static int relativeCCW(double x1, double y1, double x2, double y2,
								   double px, double py) {
		// Translate so the segment starts at the origin.
		x2 -= x1;
		y2 -= y1;
		px -= x1;
		py -= y1;

		// Cross product of the segment vector with the point vector.
		double ccw = px * y2 - py * x2;
		if (ccw == 0.0) {
			// Point is collinear with the line: classify it by projecting onto the segment.
			ccw = px * x2 + py * y2;
			if (ccw > 0.0) {
				// Projection lies beyond the start point; re-measure from the far endpoint
				// so that a point strictly past the end reports a distinct (negative) sign
				// while a point within the segment collapses to zero.
				px -= x2;
				py -= y2;
				ccw = px * x2 + py * y2;
				if (ccw < 0.0) {
					ccw = 0.0;
				}
			}
		}
		return (ccw < 0.0) ? -1 : (ccw > 0.0 ? 1 : 0);
	}

	/**
	 * Test whether the closed segment <code>(x1,y1)-(x2,y2)</code> intersects the closed
	 * segment <code>(x3,y3)-(x4,y4)</code>.  Endpoint touches and collinear overlaps count
	 * as intersections, matching {@code java.awt.geom.Line2D.linesIntersect}.
	 */
	public static boolean segmentsIntersect(double x1, double y1, double x2, double y2,
											double x3, double y3, double x4, double y4) {
		return ((relativeCCW(x1, y1, x2, y2, x3, y3)
				* relativeCCW(x1, y1, x2, y2, x4, y4) <= 0)
				&& (relativeCCW(x3, y3, x4, y4, x1, y1)
				* relativeCCW(x3, y3, x4, y4, x2, y2) <= 0));
	}
}
