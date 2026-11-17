package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.FlatPart.Arc;
import info.openrocket.core.file.cad.export.FlatPart.ArcLoop;
import info.openrocket.core.file.cad.export.FlatPart.Circle;
import info.openrocket.core.file.cad.export.FlatPart.Loop;
import info.openrocket.core.file.cad.export.FlatPart.Polygon;
import info.openrocket.core.file.cad.export.FlatPart.Region;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;
import org.locationtech.jts.triangulate.tri.Tri;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a flat part into a closed triangle mesh, extruded from z = 0 to the part thickness, in mm.
 * <p>
 * Circles are approximated by polygons whose vertices lie on the circle, with a maximum deviation of
 * {@link #MAX_CHORD_DEVIATION_MM}. The number of segments is a multiple of four, so the extents of a circle
 * are exact. Neighboring triangles use the exact same vertex coordinates, so the mesh is watertight.
 */
final class PartMesher {
	static final double MAX_CHORD_DEVIATION_MM = 0.005;
	private static final int MIN_CIRCLE_SEGMENTS = 32;
	private static final int MAX_CIRCLE_SEGMENTS = 1440;

	private PartMesher() {
	}

	/**
	 * @return the triangles of the part, each as x1,y1,z1, x2,y2,z2, x3,y3,z3, counter-clockwise seen from outside
	 */
	static List<double[]> triangulate(FlatPart part) {
		List<double[]> triangles = new ArrayList<>();
		for (Region region : part.getRegions()) {
			addRegion(triangles, region, part.getThicknessMm());
		}
		return triangles;
	}

	private static void addRegion(List<double[]> triangles, Region region, double thickness) {
		// Outer loop counter-clockwise, holes clockwise: the material is on the left of each loop
		List<double[][]> loops = new ArrayList<>();
		double[][] outer = points(region.outer(), true);
		loops.add(outer);
		for (Loop hole : region.holes()) {
			loops.add(points(hole, false));
		}

		// Top and bottom caps
		GeometryFactory factory = new GeometryFactory();
		LinearRing shell = factory.createLinearRing(coordinates(outer));
		LinearRing[] holes = new LinearRing[loops.size() - 1];
		for (int i = 1; i < loops.size(); i++) {
			holes[i - 1] = factory.createLinearRing(coordinates(loops.get(i)));
		}
		List<Tri> tris = new ConstrainedDelaunayTriangulator(factory.createPolygon(shell, holes)).getTriangles();
		for (Tri tri : tris) {
			Coordinate a = tri.getCoordinate(0);
			Coordinate b = tri.getCoordinate(1);
			Coordinate c = tri.getCoordinate(2);
			if ((b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y) < 0) {
				Coordinate tmp = b;
				b = c;
				c = tmp;
			}
			triangles.add(new double[]{a.x, a.y, thickness, b.x, b.y, thickness, c.x, c.y, thickness});
			triangles.add(new double[]{a.x, a.y, 0, c.x, c.y, 0, b.x, b.y, 0});
		}

		// Side walls, facing to the right of the loop direction
		for (double[][] loop : loops) {
			int n = loop[0].length;
			for (int i = 0; i < n; i++) {
				int j = (i + 1) % n;
				double x0 = loop[0][i], y0 = loop[1][i];
				double x1 = loop[0][j], y1 = loop[1][j];
				triangles.add(new double[]{x0, y0, 0, x1, y1, 0, x1, y1, thickness});
				triangles.add(new double[]{x0, y0, 0, x1, y1, thickness, x0, y0, thickness});
			}
		}
	}

	/**
	 * @return the loop points as {xs, ys}, counter-clockwise if ccw is true, otherwise clockwise
	 */
	private static double[][] points(Loop loop, boolean ccw) {
		if (loop instanceof Circle c) {
			int n = circleSegments(c.r());
			double[] xs = new double[n];
			double[] ys = new double[n];
			int quarter = n / 4;
			for (int i = 0; i < n; i++) {
				// Index around the circle counter-clockwise from the +X axis
				int k = ccw ? i : (n - i) % n;
				double cos = Math.cos(2 * Math.PI * k / n);
				double sin = Math.sin(2 * Math.PI * k / n);
				if (k % quarter == 0) {
					// Exact extreme points, so the extents of the circle are exact
					int q = k / quarter;
					cos = (q == 0) ? 1 : (q == 2) ? -1 : 0;
					sin = (q == 1) ? 1 : (q == 3) ? -1 : 0;
				}
				xs[i] = c.cx() + c.r() * cos;
				ys[i] = c.cy() + c.r() * sin;
			}
			return new double[][]{xs, ys};
		}
		if (loop instanceof ArcLoop arcs) {
			if (ccw) {
				throw new IllegalArgumentException("Arc loops are only used for holes");
			}
			// Each arc contributes its start point and the points along it; its end is the next arc's start
			List<double[]> pts = new ArrayList<>();
			for (Arc arc : arcs.arcs()) {
				pts.add(new double[]{arc.x0(), arc.y0()});
				int segments = (int) Math.ceil(arc.sweep() / (2 * Math.PI / circleSegments(arc.r())));
				double start = arc.startAngle();
				for (int k = 1; k < segments; k++) {
					double angle = start - arc.sweep() * k / segments;
					pts.add(new double[]{arc.cx() + arc.r() * Math.cos(angle), arc.cy() + arc.r() * Math.sin(angle)});
				}
			}
			double[] xs = new double[pts.size()];
			double[] ys = new double[pts.size()];
			for (int i = 0; i < pts.size(); i++) {
				xs[i] = pts.get(i)[0];
				ys[i] = pts.get(i)[1];
			}
			return new double[][]{xs, ys};
		}
		Polygon p = (Polygon) loop;
		if ((p.signedArea() > 0) != ccw) {
			p = p.reversed();
		}
		return new double[][]{p.xs(), p.ys()};
	}

	static int circleSegments(double radius) {
		double n = Math.PI / Math.acos(Math.max(-1, 1 - MAX_CHORD_DEVIATION_MM / radius));
		int segments = (int) Math.ceil(n / 4) * 4;
		return Math.max(MIN_CIRCLE_SEGMENTS, Math.min(MAX_CIRCLE_SEGMENTS, segments));
	}

	private static Coordinate[] coordinates(double[][] loop) {
		int n = loop[0].length;
		Coordinate[] coords = new Coordinate[n + 1];
		for (int i = 0; i < n; i++) {
			coords[i] = new Coordinate(loop[0][i], loop[1][i]);
		}
		coords[n] = coords[0];
		return coords;
	}
}
