package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.FlatPart.Arc;
import info.openrocket.core.file.cad.export.FlatPart.ArcLoop;
import info.openrocket.core.file.cad.export.FlatPart.Circle;
import info.openrocket.core.file.cad.export.FlatPart.Loop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Combines circular holes into the outlines that are actually cut: holes that lie inside another hole are dropped,
 * and overlapping holes are merged into one loop of circular arcs.
 */
final class HoleUnion {
	/** Distances below this (mm) are treated as equal. */
	static final double TOLERANCE_MM = 1e-6;

	private HoleUnion() {
	}

	/**
	 * @return the indices of the circles that are not inside another circle; of identical circles the first is kept
	 */
	static List<Integer> visible(List<Circle> circles) {
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < circles.size(); i++) {
			if (!isContained(circles, i)) {
				result.add(i);
			}
		}
		return result;
	}

	private static boolean isContained(List<Circle> circles, int i) {
		Circle c = circles.get(i);
		for (int j = 0; j < circles.size(); j++) {
			if (j == i) {
				continue;
			}
			Circle other = circles.get(j);
			double d = Math.hypot(c.cx() - other.cx(), c.cy() - other.cy());
			boolean identical = d < TOLERANCE_MM && Math.abs(c.r() - other.r()) < TOLERANCE_MM;
			if (identical ? j < i : d + c.r() <= other.r() + TOLERANCE_MM) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Merges circles, none of which is inside another, into hole loops. Circles that do not overlap any other are
	 * returned as they are; overlapping circles become {@link ArcLoop}s running clockwise.
	 */
	static List<Loop> union(List<Circle> circles) {
		int n = circles.size();
		List<List<double[]>> cuts = new ArrayList<>();		// Per circle: {angle, x, y} of intersection points
		for (int i = 0; i < n; i++) {
			cuts.add(new ArrayList<>());
		}
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				addIntersections(circles, i, j, cuts);
			}
		}

		List<Loop> loops = new ArrayList<>();
		List<Arc> arcs = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			Circle c = circles.get(i);
			List<double[]> points = cuts.get(i);
			if (points.isEmpty()) {
				loops.add(c);
				continue;
			}
			points.sort(Comparator.comparingDouble(p -> p[0]));
			for (int k = 0; k < points.size(); k++) {
				double[] from = points.get(k);
				double[] to = points.get((k + 1) % points.size());
				double sweep = to[0] - from[0];
				if (sweep <= 0) {
					sweep += 2 * Math.PI;
				}
				// Keep the counter-clockwise piece from 'from' to 'to' if it is not inside another hole
				double mid = from[0] + sweep / 2;
				double mx = c.cx() + c.r() * Math.cos(mid);
				double my = c.cy() + c.r() * Math.sin(mid);
				if (!insideOther(circles, i, mx, my)) {
					// Traversed clockwise, from 'to' back to 'from', so the hole is on the right
					arcs.add(new Arc(c.cx(), c.cy(), c.r(), to[1], to[2], from[1], from[2]));
				}
			}
		}

		// Chain the arcs into closed loops: each arc ends where the next one starts
		while (!arcs.isEmpty()) {
			List<Arc> loop = new ArrayList<>();
			Arc current = arcs.remove(0);
			loop.add(current);
			while (true) {
				Arc next = null;
				for (Arc arc : arcs) {
					if (Math.hypot(arc.x0() - current.x1(), arc.y0() - current.y1()) < TOLERANCE_MM) {
						next = arc;
						break;
					}
				}
				if (next == null) {
					break;
				}
				arcs.remove(next);
				// Use the exact same point for both ends of the joint
				next = new Arc(next.cx(), next.cy(), next.r(), current.x1(), current.y1(), next.x1(), next.y1());
				loop.add(next);
				current = next;
			}
			Arc first = loop.get(0);
			if (Math.hypot(first.x0() - current.x1(), first.y0() - current.y1()) >= TOLERANCE_MM) {
				throw new IllegalStateException("Could not merge overlapping holes");
			}
			loops.add(new ArcLoop(loop));
		}
		return loops;
	}

	private static void addIntersections(List<Circle> circles, int i, int j, List<List<double[]>> cuts) {
		Circle a = circles.get(i);
		Circle b = circles.get(j);
		double dx = b.cx() - a.cx();
		double dy = b.cy() - a.cy();
		double d = Math.hypot(dx, dy);
		if (d >= a.r() + b.r() - TOLERANCE_MM || d <= Math.abs(a.r() - b.r()) + TOLERANCE_MM) {
			return;		// Separate, touching or nested
		}
		double along = (d * d + a.r() * a.r() - b.r() * b.r()) / (2 * d);
		double h = Math.sqrt(Math.max(0, a.r() * a.r() - along * along));
		double px = a.cx() + along * dx / d;
		double py = a.cy() + along * dy / d;
		for (int sign = -1; sign <= 1; sign += 2) {
			double x = px - sign * h * dy / d;
			double y = py + sign * h * dx / d;
			cuts.get(i).add(new double[]{angle(a, x, y), x, y});
			cuts.get(j).add(new double[]{angle(b, x, y), x, y});
		}
	}

	private static boolean insideOther(List<Circle> circles, int i, double x, double y) {
		for (int j = 0; j < circles.size(); j++) {
			Circle c = circles.get(j);
			if (j != i && Math.hypot(x - c.cx(), y - c.cy()) < c.r() - TOLERANCE_MM) {
				return true;
			}
		}
		return false;
	}

	private static double angle(Circle c, double x, double y) {
		double angle = Math.atan2(y - c.cy(), x - c.cx());
		return angle < 0 ? angle + 2 * Math.PI : angle;
	}
}
