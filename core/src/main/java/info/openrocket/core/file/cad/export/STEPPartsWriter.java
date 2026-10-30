package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;
import info.openrocket.core.file.cad.export.FlatPart.Arc;
import info.openrocket.core.file.cad.export.FlatPart.ArcLoop;
import info.openrocket.core.file.cad.export.FlatPart.Circle;
import info.openrocket.core.file.cad.export.FlatPart.Loop;
import info.openrocket.core.file.cad.export.FlatPart.Polygon;
import info.openrocket.core.file.cad.export.FlatPart.Region;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Writes flat parts as exact 3D solids in a STEP file (ISO 10303-21, AP214 "automotive design" schema), which is
 * read by practically all CAD programs. Each part becomes a separate product with one boundary representation
 * solid per region, extruded from z = 0 to the part thickness. Units are millimeters.
 * <p>
 * Straight edges become planar side faces and circles become cylindrical faces, so the geometry is exact.
 */
final class STEPPartsWriter {
	private final StringBuilder data = new StringBuilder();
	private int nextId = 1;

	private int zAxis;
	private int minusZAxis;
	private int xAxis;

	private STEPPartsWriter() {
	}

	static void write(List<ExportedPart> parts, String title, OutputStream out) throws IOException {
		STEPPartsWriter writer = new STEPPartsWriter();
		writer.writeParts(parts);

		Writer w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.US_ASCII));
		w.write("ISO-10303-21;\n");
		w.write("HEADER;\n");
		w.write("FILE_DESCRIPTION(('OpenRocket parts'),'2;1');\n");
		w.write("FILE_NAME(" + string(title == null ? "" : title) + ",'" +
				LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS) + "',(''),(''),'OpenRocket','OpenRocket','');\n");
		w.write("FILE_SCHEMA(('AUTOMOTIVE_DESIGN { 1 0 10303 214 1 1 1 1 }'));\n");
		w.write("ENDSEC;\n");
		w.write("DATA;\n");
		w.write(writer.data.toString());
		w.write("ENDSEC;\n");
		w.write("END-ISO-10303-21;\n");
		w.flush();
	}

	private void writeParts(List<ExportedPart> parts) {
		int applicationContext = add("APPLICATION_CONTEXT('core data for automotive mechanical design processes')");
		add("APPLICATION_PROTOCOL_DEFINITION('international standard','automotive_design',2000,#" +
				applicationContext + ")");
		int productContext = add("PRODUCT_CONTEXT('',#" + applicationContext + ",'mechanical')");
		int definitionContext = add("PRODUCT_DEFINITION_CONTEXT('part definition',#" + applicationContext +
				",'design')");

		int mm = add("( LENGTH_UNIT() NAMED_UNIT(*) SI_UNIT(.MILLI.,.METRE.) )");
		int radian = add("( NAMED_UNIT(*) PLANE_ANGLE_UNIT() SI_UNIT($,.RADIAN.) )");
		int steradian = add("( NAMED_UNIT(*) SI_UNIT($,.STERADIAN.) SOLID_ANGLE_UNIT() )");
		int uncertainty = add("UNCERTAINTY_MEASURE_WITH_UNIT(LENGTH_MEASURE(1.E-07),#" + mm +
				",'distance_accuracy_value','confusion accuracy')");
		int geometricContext = add("( GEOMETRIC_REPRESENTATION_CONTEXT(3) GLOBAL_UNCERTAINTY_ASSIGNED_CONTEXT((#" +
				uncertainty + ")) GLOBAL_UNIT_ASSIGNED_CONTEXT((#" + mm + ",#" + radian + ",#" + steradian +
				")) REPRESENTATION_CONTEXT('','3D') )");

		zAxis = direction(0, 0, 1);
		minusZAxis = direction(0, 0, -1);
		xAxis = direction(1, 0, 0);
		int origin = placement(point(0, 0, 0), zAxis, xAxis);

		for (ExportedPart placed : parts) {
			String name = string(placed.name());
			List<Integer> items = new ArrayList<>();
			items.add(origin);
			for (Region region : placed.part().getRegions()) {
				items.add(solid(placed.name(), region, placed.part().getThicknessMm()));
			}

			int product = add("PRODUCT(" + name + "," + name + ",'',(#" + productContext + "))");
			add("PRODUCT_RELATED_PRODUCT_CATEGORY('part',$,(#" + product + "))");
			int formation = add("PRODUCT_DEFINITION_FORMATION('','',#" + product + ")");
			int definition = add("PRODUCT_DEFINITION('design','',#" + formation + ",#" + definitionContext + ")");
			int definitionShape = add("PRODUCT_DEFINITION_SHAPE('','',#" + definition + ")");
			int representation = add("ADVANCED_BREP_SHAPE_REPRESENTATION(" + name + "," + refs(items) + ",#" +
					geometricContext + ")");
			add("SHAPE_DEFINITION_REPRESENTATION(#" + definitionShape + ",#" + representation + ")");
		}
	}

	/**
	 * Topology of one loop of a region, extruded into a wall.
	 */
	private static final class LoopTopology {
		/** Oriented edges that bound the top face (counter-clockwise from +Z for outer loops) */
		final List<Integer> top = new ArrayList<>();
		/** Oriented edges that bound the bottom face */
		final List<Integer> bottom = new ArrayList<>();
		/** Side faces */
		final List<Integer> sides = new ArrayList<>();
	}

	private int solid(String name, Region region, double thickness) {
		List<Integer> faces = new ArrayList<>();

		List<LoopTopology> loops = new ArrayList<>();
		loops.add(loop(region.outer(), true, thickness));
		for (Loop hole : region.holes()) {
			loops.add(loop(hole, false, thickness));
		}

		// Top face, normal +Z
		int topPlane = add("PLANE('',#" + placement(point(0, 0, thickness), zAxis, xAxis) + ")");
		faces.add(planarFace(topPlane, loops, true));
		// Bottom face, normal -Z
		int bottomPlane = add("PLANE('',#" + placement(point(0, 0, 0), minusZAxis, xAxis) + ")");
		faces.add(planarFace(bottomPlane, loops, false));
		for (LoopTopology loop : loops) {
			faces.addAll(loop.sides);
		}

		int shell = add("CLOSED_SHELL(''," + refs(faces) + ")");
		return add("MANIFOLD_SOLID_BREP(" + string(name) + ",#" + shell + ")");
	}

	private int planarFace(int plane, List<LoopTopology> loops, boolean top) {
		List<Integer> bounds = new ArrayList<>();
		for (int i = 0; i < loops.size(); i++) {
			List<Integer> edges = top ? loops.get(i).top : loops.get(i).bottom;
			int edgeLoop = add("EDGE_LOOP(''," + refs(edges) + ")");
			bounds.add(add((i == 0 ? "FACE_OUTER_BOUND" : "FACE_BOUND") + "('',#" + edgeLoop + ",.T.)"));
		}
		return add("ADVANCED_FACE(''," + refs(bounds) + ",#" + plane + ",.T.)");
	}

	/**
	 * Creates the edges and side faces of a loop. The material is on the left of the loop direction, which is
	 * counter-clockwise (seen from +Z) for the outer loop and clockwise for holes.
	 */
	private LoopTopology loop(Loop loop, boolean outer, double thickness) {
		if (loop instanceof Circle circle) {
			return circleLoop(circle, outer, thickness);
		} else if (loop instanceof ArcLoop arcs) {
			if (outer) {
				throw new IllegalArgumentException("Arc loops are only used for holes");
			}
			return arcLoop(arcs, thickness);
		}
		Polygon polygon = (Polygon) loop;
		if ((polygon.signedArea() > 0) != outer) {
			polygon = polygon.reversed();
		}
		return polygonLoop(polygon, thickness);
	}

	private LoopTopology polygonLoop(Polygon polygon, double thickness) {
		int n = polygon.size();
		double[] xs = polygon.xs();
		double[] ys = polygon.ys();

		int[] bottomPoints = new int[n];
		int[] topPoints = new int[n];
		int[] bottomVertices = new int[n];
		int[] topVertices = new int[n];
		for (int i = 0; i < n; i++) {
			bottomPoints[i] = point(xs[i], ys[i], 0);
			topPoints[i] = point(xs[i], ys[i], thickness);
			bottomVertices[i] = add("VERTEX_POINT('',#" + bottomPoints[i] + ")");
			topVertices[i] = add("VERTEX_POINT('',#" + topPoints[i] + ")");
		}

		int up = zAxis;
		int[] bottomEdges = new int[n];
		int[] topEdges = new int[n];
		int[] verticalEdges = new int[n];
		int[] edgeDirections = new int[n];
		for (int i = 0; i < n; i++) {
			int j = (i + 1) % n;
			double dx = xs[j] - xs[i];
			double dy = ys[j] - ys[i];
			double length = Math.hypot(dx, dy);
			edgeDirections[i] = direction(dx / length, dy / length, 0);
			bottomEdges[i] = lineEdge(bottomVertices[i], bottomVertices[j], bottomPoints[i], edgeDirections[i], length);
			topEdges[i] = lineEdge(topVertices[i], topVertices[j], topPoints[i], edgeDirections[i], length);
			verticalEdges[i] = lineEdge(bottomVertices[i], topVertices[i], bottomPoints[i], up, thickness);
		}

		LoopTopology topology = new LoopTopology();
		for (int i = 0; i < n; i++) {
			topology.top.add(oriented(topEdges[i], true));
		}
		for (int i = n - 1; i >= 0; i--) {
			topology.bottom.add(oriented(bottomEdges[i], false));
		}
		for (int i = 0; i < n; i++) {
			int j = (i + 1) % n;
			double dx = xs[j] - xs[i];
			double dy = ys[j] - ys[i];
			double length = Math.hypot(dx, dy);
			// Outward normal is to the right of the loop direction
			int normal = direction(dy / length, -dx / length, 0);
			int plane = add("PLANE('',#" + placement(bottomPoints[i], normal, edgeDirections[i]) + ")");
			List<Integer> edges = List.of(
					oriented(bottomEdges[i], true),
					oriented(verticalEdges[j], true),
					oriented(topEdges[i], false),
					oriented(verticalEdges[i], false));
			int edgeLoop = add("EDGE_LOOP(''," + refs(edges) + ")");
			int bound = add("FACE_OUTER_BOUND('',#" + edgeLoop + ",.T.)");
			topology.sides.add(add("ADVANCED_FACE('',(#" + bound + "),#" + plane + ",.T.)"));
		}
		return topology;
	}

	/**
	 * Creates the walls of a hole made of clockwise arcs. Each arc becomes a cylindrical face whose normal points
	 * into the hole.
	 */
	private LoopTopology arcLoop(ArcLoop loop, double thickness) {
		List<Arc> arcs = loop.arcs();
		int n = arcs.size();

		int[] bottomPoints = new int[n];
		int[] bottomVertices = new int[n];
		int[] topVertices = new int[n];
		for (int i = 0; i < n; i++) {
			Arc arc = arcs.get(i);
			bottomPoints[i] = point(arc.x0(), arc.y0(), 0);
			bottomVertices[i] = add("VERTEX_POINT('',#" + bottomPoints[i] + ")");
			topVertices[i] = add("VERTEX_POINT('',#" + point(arc.x0(), arc.y0(), thickness) + ")");
		}

		int[] bottomEdges = new int[n];
		int[] topEdges = new int[n];
		int[] verticalEdges = new int[n];
		int[] surfaces = new int[n];
		for (int i = 0; i < n; i++) {
			Arc arc = arcs.get(i);
			int j = (i + 1) % n;
			// The circles run counter-clockwise, the edges clockwise
			int bottomCircle = add("CIRCLE('',#" + placement(point(arc.cx(), arc.cy(), 0), zAxis, xAxis) + "," +
					number(arc.r()) + ")");
			int topCircle = add("CIRCLE('',#" + placement(point(arc.cx(), arc.cy(), thickness), zAxis, xAxis) +
					"," + number(arc.r()) + ")");
			bottomEdges[i] = add("EDGE_CURVE('',#" + bottomVertices[i] + ",#" + bottomVertices[j] + ",#" +
					bottomCircle + ",.F.)");
			topEdges[i] = add("EDGE_CURVE('',#" + topVertices[i] + ",#" + topVertices[j] + ",#" + topCircle +
					",.F.)");
			verticalEdges[i] = lineEdge(bottomVertices[i], topVertices[i], bottomPoints[i], zAxis, thickness);
			surfaces[i] = add("CYLINDRICAL_SURFACE('',#" + placement(point(arc.cx(), arc.cy(), 0), zAxis, xAxis) +
					"," + number(arc.r()) + ")");
		}

		LoopTopology topology = new LoopTopology();
		for (int i = 0; i < n; i++) {
			topology.top.add(oriented(topEdges[i], true));
		}
		for (int i = n - 1; i >= 0; i--) {
			topology.bottom.add(oriented(bottomEdges[i], false));
		}
		for (int i = 0; i < n; i++) {
			int j = (i + 1) % n;
			List<Integer> edges = List.of(
					oriented(bottomEdges[i], true),
					oriented(verticalEdges[j], true),
					oriented(topEdges[i], false),
					oriented(verticalEdges[i], false));
			int edgeLoop = add("EDGE_LOOP(''," + refs(edges) + ")");
			int bound = add("FACE_OUTER_BOUND('',#" + edgeLoop + ",.T.)");
			topology.sides.add(add("ADVANCED_FACE('',(#" + bound + "),#" + surfaces[i] + ",.F.)"));
		}
		return topology;
	}

	private LoopTopology circleLoop(Circle circle, boolean outer, double thickness) {
		double cx = circle.cx();
		double cy = circle.cy();
		double r = circle.r();

		int bottomPoint = point(cx + r, cy, 0);
		int topPoint = point(cx + r, cy, thickness);
		int bottomVertex = add("VERTEX_POINT('',#" + bottomPoint + ")");
		int topVertex = add("VERTEX_POINT('',#" + topPoint + ")");

		// Both circles run counter-clockwise seen from +Z, starting and ending at the seam
		int bottomCircle = add("CIRCLE('',#" + placement(point(cx, cy, 0), zAxis, xAxis) + "," + number(r) + ")");
		int topCircle = add("CIRCLE('',#" + placement(point(cx, cy, thickness), zAxis, xAxis) + "," +
				number(r) + ")");
		int bottomEdge = add("EDGE_CURVE('',#" + bottomVertex + ",#" + bottomVertex + ",#" + bottomCircle + ",.T.)");
		int topEdge = add("EDGE_CURVE('',#" + topVertex + ",#" + topVertex + ",#" + topCircle + ",.T.)");
		int seam = lineEdge(bottomVertex, topVertex, bottomPoint, zAxis, thickness);

		LoopTopology topology = new LoopTopology();
		topology.top.add(oriented(topEdge, outer));
		topology.bottom.add(oriented(bottomEdge, !outer));

		// The cylinder normal points away from its axis: outwards for the outer loop, inwards for a hole
		int surface = add("CYLINDRICAL_SURFACE('',#" + placement(point(cx, cy, 0), zAxis, xAxis) + "," +
				number(r) + ")");
		List<Integer> edges;
		if (outer) {
			edges = List.of(oriented(bottomEdge, true), oriented(seam, true),
					oriented(topEdge, false), oriented(seam, false));
		} else {
			edges = List.of(oriented(seam, true), oriented(topEdge, true),
					oriented(seam, false), oriented(bottomEdge, false));
		}
		int edgeLoop = add("EDGE_LOOP(''," + refs(edges) + ")");
		int bound = add("FACE_OUTER_BOUND('',#" + edgeLoop + ",.T.)");
		topology.sides.add(add("ADVANCED_FACE('',(#" + bound + "),#" + surface + "," + bool(outer) + ")"));
		return topology;
	}

	private int lineEdge(int startVertex, int endVertex, int startPoint, int direction, double length) {
		int vector = add("VECTOR('',#" + direction + "," + number(length) + ")");
		int line = add("LINE('',#" + startPoint + ",#" + vector + ")");
		return add("EDGE_CURVE('',#" + startVertex + ",#" + endVertex + ",#" + line + ",.T.)");
	}

	private int oriented(int edge, boolean sameSense) {
		return add("ORIENTED_EDGE('',*,*,#" + edge + "," + bool(sameSense) + ")");
	}

	private int point(double x, double y, double z) {
		return add("CARTESIAN_POINT('',(" + number(x) + "," + number(y) + "," + number(z) + "))");
	}

	private int direction(double x, double y, double z) {
		return add(String.format(Locale.ROOT, "DIRECTION('',(%s,%s,%s))", unit(x), unit(y), unit(z)));
	}

	private int placement(int location, int axis, int refDirection) {
		return add("AXIS2_PLACEMENT_3D('',#" + location + ",#" + axis + ",#" + refDirection + ")");
	}

	private int add(String entity) {
		int id = nextId++;
		data.append('#').append(id).append('=').append(entity).append(";\n");
		return id;
	}

	private static String refs(List<Integer> ids) {
		StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append('#').append(ids.get(i));
		}
		return sb.append(')').toString();
	}

	private static String bool(boolean value) {
		return value ? ".T." : ".F.";
	}

	private static String number(double value) {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("Non-finite coordinate: " + value);
		}
		String s = String.format(Locale.ROOT, "%.6f", value);
		return "-0.000000".equals(s) ? "0.000000" : s;
	}

	private static String unit(double value) {
		String s = String.format(Locale.ROOT, "%.12f", value);
		return "-0.000000000000".equals(s) ? "0.000000000000" : s;
	}

	/**
	 * Formats a STEP string literal, escaping quotes, backslashes and non-ASCII characters.
	 */
	static String string(String text) {
		StringBuilder sb = new StringBuilder("'");
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'') {
				sb.append("''");
			} else if (c == '\\') {
				sb.append("\\\\");
			} else if (c >= 32 && c < 127) {
				sb.append(c);
			} else {
				sb.append(String.format(Locale.ROOT, "\\X2\\%04X\\X0\\", (int) c));
			}
		}
		return sb.append('\'').toString();
	}
}
