package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;
import info.openrocket.core.file.cad.export.FlatPart.Arc;
import info.openrocket.core.file.cad.export.FlatPart.ArcLoop;
import info.openrocket.core.file.cad.export.FlatPart.Circle;
import info.openrocket.core.file.cad.export.FlatPart.Loop;
import info.openrocket.core.file.cad.export.FlatPart.Polygon;
import info.openrocket.core.file.cad.export.FlatPart.Region;
import info.openrocket.core.util.BuildProperties;

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
 * <p>
 * <b>Orientation rules.</b> The topology follows ISO 10303-42 (topology and geometry resources) as constrained by
 * ISO 10303-514 (advanced boundary representation); the entity definitions are public, e.g. in the STEP Tools AP214
 * reference at https://www.steptools.com/stds/stp_aim/html/, and the CAx-IF recommended practices
 * (https://www.mbx-if.org/cax/cax_recommPractice.php) describe how CAD programs read them. Three rules are used:
 * <ul>
 *     <li>An EDGE_CURVE runs from its first to its second vertex; its <code>same_sense</code> is .T. when that is the
 *     direction in which its curve is parametrized. LINEs are created in the edge direction (.T.); CIRCLEs are always
 *     counter-clockwise about +Z, so a clockwise edge on a circle has .F.</li>
 *     <li>An ADVANCED_FACE has the normal of its surface when its <code>same_sense</code> is .T., and the opposite
 *     normal when it is .F. Every face must end up with its normal pointing out of the material.</li>
 *     <li>The ORIENTED_EDGEs of a face bound run counter-clockwise seen from outside the solid (against the face
 *     normal), so the face lies on their left. In a closed shell every edge is then used exactly twice, once in
 *     each direction, which is what makes the shell closed and manifold.</li>
 * </ul>
 * Each loop of a region is extruded into a wall. For loop points P0, P1, ... (material on the left) the writer
 * creates a bottom vertex Bi at z = 0 and a top vertex Ti at z = thickness, a bottom edge bi from Bi to Bi+1, a top
 * edge ti from Ti to Ti+1 and a vertical edge vi from Bi to Ti. One side face, seen from outside:
 * <pre>
 *   Ti &lt;----ti---- Ti+1
 *   |               ^
 *   vi (reversed)   vi+1
 *   v               |
 *   Bi -----bi----&gt; Bi+1
 * </pre>
 * The side face uses bi forward, vi+1 forward, ti reversed and vi reversed. The top face uses every ti forward, the
 * bottom face every bi reversed, and the neighboring side face uses vi forward, so each edge appears once in each
 * direction.
 */
final class STEPPartsWriter {
	/** The program and version, written as the preprocessor and originating system of the file */
	static final String APPLICATION = "OpenRocket " + BuildProperties.getVersion();

	// AP214 is read by practically all CAD programs. Moving to AP242 changes these entities, not only the names.
	private static final String SCHEMA = "AUTOMOTIVE_DESIGN { 1 0 10303 214 1 1 1 1 }";
	private static final String PROTOCOL = "automotive_design";
	private static final int PROTOCOL_YEAR = 2000;
	private static final String APPLICATION_CONTEXT = "core data for automotive mechanical design processes";

	private final StringBuilder data = new StringBuilder();
	private int nextId = 1;

	private int zAxis;
	private int minusZAxis;
	private int xAxis;

	private STEPPartsWriter() {
	}

	static void write(List<ExportedPart> parts, String title, String author, OutputStream out) throws IOException {
		STEPPartsWriter writer = new STEPPartsWriter();
		writer.writeParts(parts);

		Writer w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.US_ASCII));
		w.write("ISO-10303-21;\n");
		w.write(header(title, author, LocalDateTime.now()));
		w.write("DATA;\n");
		w.write(writer.data.toString());
		w.write("ENDSEC;\n");
		w.write("END-ISO-10303-21;\n");
		w.flush();
	}

	/**
	 * Returns the HEADER section (ISO 10303-21, clause 8). FILE_NAME holds the name, time stamp, author, organization,
	 * preprocessor version, originating system and authorization; the organization and authorization are left empty.
	 */
	static String header(String title, String author, LocalDateTime time) {
		return "HEADER;\n" +
				"FILE_DESCRIPTION(('OpenRocket parts'),'2;1');\n" +
				"FILE_NAME(" + string(title == null ? "" : title) + ",'" + time.truncatedTo(ChronoUnit.SECONDS) +
				"',(" + string(author == null ? "" : author) + "),('')," + string(APPLICATION) + "," +
				string(APPLICATION) + ",'');\n" +
				"FILE_SCHEMA(('" + SCHEMA + "'));\n" +
				"ENDSEC;\n";
	}

	private void writeParts(List<ExportedPart> parts) {
		int applicationContext = add("APPLICATION_CONTEXT('" + APPLICATION_CONTEXT + "')");
		add("APPLICATION_PROTOCOL_DEFINITION('international standard','" + PROTOCOL + "'," + PROTOCOL_YEAR + ",#" +
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

	/**
	 * Creates one MANIFOLD_SOLID_BREP of a region: a CLOSED_SHELL of the top face, the bottom face and the side faces of
	 * every loop.
	 */
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

	/**
	 * Creates the top or bottom face. The first loop is the outer bound and the others are holes. The plane normal is
	 * already +Z for the top face and -Z for the bottom face, so the face has <code>same_sense</code> .T.; the bottom
	 * edges are reversed to run counter-clockwise seen from -Z.
	 */
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

	/**
	 * Creates the wall of a polygon as shown in the class description: one vertex pair, three LINE edges and one
	 * PLANE side face per corner. The polygon runs with the material on its left (counter-clockwise for the outline
	 * of a fin, clockwise for a hole), so the outward normal of each side face is its edge direction turned 90 degrees
	 * clockwise about +Z, and the PLANE is placed with that normal (face <code>same_sense</code> .T.).
	 */
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
			// The material is on the left of the edge, so the normal pointing out of the material is on its right
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
	 * Creates the wall of a hole made of clockwise arcs (merged overlapping holes), with the same edge and face
	 * pattern as {@link #polygonLoop}: one vertex pair per arc start, a bottom and a top edge on a CIRCLE and a
	 * vertical LINE edge, and one CYLINDRICAL_SURFACE side face per arc.
	 * <p>
	 * The CIRCLEs run counter-clockwise about +Z but the arc edges clockwise, so the arc edges have
	 * <code>same_sense</code> .F. The material is outside every circle, so the surface normal, which points away
	 * from the cylinder axis, points into the material; the faces therefore have <code>same_sense</code> .F. and
	 * their normal points into the hole, out of the material.
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

	/**
	 * Creates the wall of a full circle: a single vertex pair on a seam at (cx + r, cy), one closed bottom and one
	 * closed top edge (CIRCLE, counter-clockwise, <code>same_sense</code> .T.), a vertical seam LINE and one
	 * CYLINDRICAL_SURFACE face. The seam is used twice in the face bound, once in each direction, the usual way to
	 * bound a closed periodic face.
	 * <p>
	 * For the outer circle of a ring the material is inside the cylinder: the top edge is used forward
	 * (counter-clockwise from +Z), and the surface normal already points out of the material (face .T.). For a hole the
	 * material is outside: the top edge is used reversed (clockwise from +Z), and the face has .F. so its normal points
	 * into the hole. The side face loop is counter-clockwise seen from outside the material in both cases.
	 */
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

	/**
	 * Formats a length in mm with 9 decimals, finer than the declared uncertainty of 1E-7 mm, so that a rounded vertex
	 * still lies on its circle within that uncertainty. A value that rounds to zero is written without a minus sign.
	 *
	 * @throws IllegalArgumentException if the value is NaN or infinite, which STEP cannot represent
	 */
	static String number(double value) {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("Non-finite coordinate: " + value);
		}
		String s = String.format(Locale.ROOT, "%.9f", value);
		return "-0.000000000".equals(s) ? "0.000000000" : s;
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
