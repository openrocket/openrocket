package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;
import info.openrocket.core.file.cad.export.CADPartsExporter.Format;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.file.cad.export.FlatPart.DimensionValue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads a file written by {@link CADPartsExporter} back in and measures the parts from the file geometry,
 * so the exported dimensions can be compared with the design values.
 * <ul>
 *     <li>DXF: outlines and circles on the cut layer</li>
 *     <li>STEP: vertices and circles of the solids</li>
 *     <li>STL and 3MF: the triangle mesh (extents, thickness and volume)</li>
 * </ul>
 */
public class CADDimensionVerifier {
	/** Maximum allowed difference between design and measured lengths, in meters (0.001 mm). */
	public static final double TOLERANCE = 1e-6;
	/** Maximum allowed relative difference in volume, which for STL includes the approximation of circles. */
	public static final double VOLUME_TOLERANCE = 1e-3;

	private static final double MM_TO_M = 0.001;
	private static final double REGION_MARGIN_MM = 1e-3;

	/**
	 * The outcome of comparing one design dimension with the value measured from the file.
	 */
	public enum Status {
		/** The value measured from the file matches the design */
		MATCH,
		/** The value measured from the file differs from the design */
		MISMATCH,
		/** The geometry for this dimension was not found in the file */
		NOT_FOUND,
		/** The dimension cannot be measured from this file format and is listed for reference */
		REFERENCE
	}

	/**
	 * The length unit that a file declares.
	 */
	public enum Units {
		MILLIMETERS,
		/** The file declares units other than millimeters */
		OTHER,
		/** The format has no units (STL); millimeters are assumed by convention */
		NONE
	}

	/**
	 * @param measured the measured value in meters (cubic meters for volume), or NaN if not measured
	 */
	public record Check(ExportedPart part, DimensionValue dimension, double measured, Status status) { }

	/**
	 * The result of checking a whole file.
	 *
	 * @param format the format of the file
	 * @param units  the unit that the file declares
	 * @param checks one check for every design dimension of every part
	 */
	public record Result(Format format, Units units, List<Check> checks) {
		/**
		 * @return true if the file units are correct and all measurable dimensions match the design
		 */
		public boolean isAllMatching() {
			if (units == Units.OTHER) {
				return false;
			}
			for (Check check : checks) {
				if (check.status() == Status.MISMATCH || check.status() == Status.NOT_FOUND) {
					return false;
				}
			}
			return true;
		}
	}

	private record Circle(double x, double y, double z, double r) { }

	/**
	 * Geometry read from a file, in mm.
	 */
	private static final class Geometry {
		Units units = Units.OTHER;
		boolean threeD;
		boolean hasCircles;
		final List<double[]> points = new ArrayList<>();
		final List<Circle> circles = new ArrayList<>();
		/** Triangles as 9 coordinates, STL only */
		final List<double[]> triangles = new ArrayList<>();
	}

	private CADDimensionVerifier() {
	}

	/**
	 * Reads an exported file and compares what is in it with the design dimensions of the parts.
	 *
	 * @param format the format of the file
	 * @param in     the contents of the file
	 * @param parts  the parts that were written to the file, as returned by
	 *               {@link CADPartsExporter#write(Format, String, java.io.OutputStream)}
	 * @return the comparison, which also tells whether everything matches
	 * @throws IOException if the file cannot be read or is not a valid file of this format
	 */
	public static Result verify(Format format, InputStream in, List<ExportedPart> parts) throws IOException {
		Geometry geometry = switch (format) {
			case DXF -> readDXF(in);
			case STEP -> readSTEP(in);
			case STL -> readSTL(in);
			case THREE_MF -> read3MF(in);
		};

		List<Check> checks = new ArrayList<>();
		for (ExportedPart part : parts) {
			measure(part, geometry, checks);
		}
		return new Result(format, geometry.units, checks);
	}

	private static void measure(ExportedPart part, Geometry geometry, List<Check> checks) {
		// Extents of all points in the part's region
		double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
		double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
		for (double[] p : geometry.points) {
			if (inRegion(p[0], p[1], part)) {
				for (int i = 0; i < 3; i++) {
					min[i] = Math.min(min[i], p[i]);
					max[i] = Math.max(max[i], p[i]);
				}
			}
		}

		// Circles in the region, only those at the bottom of 3D solids
		List<Circle> circles = new ArrayList<>();
		Circle outer = null;
		for (Circle c : geometry.circles) {
			if (inRegion(c.x, c.y, part) && (!geometry.threeD || Math.abs(c.z - min[2]) < REGION_MARGIN_MM)) {
				circles.add(c);
				if (outer == null || c.r > outer.r) {
					outer = c;
				}
			}
		}
		boolean found = min[0] != Double.MAX_VALUE || outer != null;

		for (DimensionValue dim : part.part().getDimensions()) {
			Dimension type = dim.dimension();
			double measured;
			boolean measurable = true;
			switch (type) {
				case OUTLINE_LENGTH -> measured = (max[0] - min[0]) * MM_TO_M;
				case OUTLINE_HEIGHT -> measured = (max[1] - min[1]) * MM_TO_M;
				case MATERIAL_THICKNESS -> {
					measurable = geometry.threeD;
					measured = (max[2] - min[2]) * MM_TO_M;
				}
				case VOLUME -> {
					measurable = !geometry.triangles.isEmpty();
					measured = volume(geometry.triangles, part) * 1e-9;
				}
				case OUTER_DIAMETER -> {
					if (geometry.hasCircles) {
						measured = outer == null ? Double.NaN : 2 * outer.r * MM_TO_M;
					} else {
						measured = (max[0] - min[0]) * MM_TO_M;
					}
				}
				case INNER_DIAMETER -> {
					measurable = geometry.hasCircles;
					measured = Double.NaN;
					if (outer != null) {
						Circle bore = null;
						for (Circle c : circles) {
							if (c != outer && Math.hypot(c.x - outer.x, c.y - outer.y) * MM_TO_M < TOLERANCE &&
									(bore == null || c.r > bore.r)) {
								bore = c;
							}
						}
						measured = bore == null ? Double.NaN : 2 * bore.r * MM_TO_M;
					}
				}
				case HOLE_DIAMETER, HOLE_OFFSET -> {
					measurable = geometry.hasCircles;
					measured = Double.NaN;
					Circle hole = outer == null ? null : findHole(part, dim.index(), circles, outer);
					if (hole != null) {
						measured = (type == Dimension.HOLE_DIAMETER) ? 2 * hole.r * MM_TO_M :
								Math.hypot(hole.x - outer.x, hole.y - outer.y) * MM_TO_M;
					}
				}
				default -> {
					measurable = false;
					measured = Double.NaN;
				}
			}

			Status status;
			if (!measurable) {
				status = Status.REFERENCE;
				measured = Double.NaN;
			} else if (!found || Double.isNaN(measured)) {
				status = Status.NOT_FOUND;
			} else {
				double tolerance = (type == Dimension.VOLUME) ? VOLUME_TOLERANCE * dim.value() : TOLERANCE;
				status = Math.abs(measured - dim.value()) <= tolerance ? Status.MATCH : Status.MISMATCH;
			}
			checks.add(new Check(part, dim, measured, status));
		}
	}

	/**
	 * Finds the circle that best matches the expected diameter and center offset of the hole with the given index.
	 */
	private static Circle findHole(ExportedPart part, int index, List<Circle> circles, Circle outer) {
		double diameter = Double.NaN;
		double offset = Double.NaN;
		for (DimensionValue dim : part.part().getDimensions()) {
			if (dim.index() != index) {
				continue;
			}
			if (dim.dimension() == Dimension.HOLE_DIAMETER) {
				diameter = dim.value();
			} else if (dim.dimension() == Dimension.HOLE_OFFSET) {
				offset = dim.value();
			}
		}

		Circle best = null;
		double bestError = Double.MAX_VALUE;
		for (Circle c : circles) {
			if (c == outer) {
				continue;
			}
			double error = Math.abs(2 * c.r * MM_TO_M - diameter) +
					Math.abs(Math.hypot(c.x - outer.x, c.y - outer.y) * MM_TO_M - offset);
			if (error < bestError) {
				bestError = error;
				best = c;
			}
		}
		return best;
	}

	/**
	 * Volume (mm^3) enclosed by the triangles whose centroid is in the part's region.
	 */
	private static double volume(List<double[]> triangles, ExportedPart part) {
		double volume = 0;
		for (double[] t : triangles) {
			double cx = (t[0] + t[3] + t[6]) / 3;
			double cy = (t[1] + t[4] + t[7]) / 3;
			if (!inRegion(cx, cy, part)) {
				continue;
			}
			// Signed volume of the tetrahedron with the origin
			volume += (t[0] * (t[4] * t[8] - t[5] * t[7])
					- t[1] * (t[3] * t[8] - t[5] * t[6])
					+ t[2] * (t[3] * t[7] - t[4] * t[6])) / 6;
		}
		return volume;
	}

	private static boolean inRegion(double x, double y, ExportedPart part) {
		return x >= part.minXmm() - REGION_MARGIN_MM && x <= part.maxXmm() + REGION_MARGIN_MM &&
				y >= part.minYmm() - REGION_MARGIN_MM && y <= part.maxYmm() + REGION_MARGIN_MM;
	}

	/**
	 * Reads the header units and the polylines and circles on the cut layer.
	 */
	private static Geometry readDXF(InputStream in) throws IOException {
		Geometry geometry = new Geometry();
		geometry.hasCircles = true;
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, DXFWriter.CHARSET));

		String headerVariable = null;
		String entity = null;
		String layer = null;
		double x = 0, y = 0, r = 0;
		String polylineLayer = null;

		String codeLine;
		while ((codeLine = reader.readLine()) != null) {
			String value = reader.readLine();
			if (value == null) {
				break;
			}
			int code = Integer.parseInt(codeLine.trim());
			value = value.trim();

			if (code == 9) {
				headerVariable = value;
				continue;
			}
			if (code == 70 && "$INSUNITS".equals(headerVariable)) {
				geometry.units = Integer.parseInt(value) == 4 ? Units.MILLIMETERS : Units.OTHER;
				headerVariable = null;
				continue;
			}

			if (code == 0) {
				// Finish the previous entity
				boolean cut = DXFPartsWriter.LAYER_CUT.equals(layer);
				if (("CIRCLE".equals(entity) || "ARC".equals(entity)) && cut) {
					geometry.circles.add(new Circle(x, y, 0, r));
					geometry.points.add(new double[]{x - r, y - r, 0});
					geometry.points.add(new double[]{x + r, y + r, 0});
				} else if ("POLYLINE".equals(entity)) {
					polylineLayer = layer;
				} else if ("VERTEX".equals(entity) && DXFPartsWriter.LAYER_CUT.equals(polylineLayer)) {
					geometry.points.add(new double[]{x, y, 0});
				}

				entity = value;
				layer = null;
				x = y = r = 0;
				continue;
			}

			switch (code) {
				case 8 -> layer = value;
				case 10 -> x = Double.parseDouble(value);
				case 20 -> y = Double.parseDouble(value);
				case 40 -> r = Double.parseDouble(value);
				default -> {
				}
			}
		}
		return geometry;
	}

	private static final Pattern STEP_ENTITY = Pattern.compile("#(\\d+)\\s*=\\s*([A-Z_0-9]+)\\s*\\((.*)\\)");
	private static final Pattern STEP_REF = Pattern.compile("#(\\d+)");

	/**
	 * Reads the units, vertices and circles of a STEP file.
	 */
	private static Geometry readSTEP(InputStream in) throws IOException {
		Geometry geometry = new Geometry();
		geometry.threeD = true;
		geometry.hasCircles = true;

		String text = new String(in.readAllBytes(), StandardCharsets.US_ASCII);
		String compact = text.replaceAll("\\s+", " ");
		boolean millimeters = compact.contains("LENGTH_UNIT() NAMED_UNIT(*) SI_UNIT(.MILLI.,.METRE.)");
		geometry.units = millimeters ? Units.MILLIMETERS : Units.OTHER;

		Map<Integer, double[]> points = new HashMap<>();
		Map<Integer, Integer> placements = new HashMap<>();
		List<int[]> vertexRefs = new ArrayList<>();
		List<String[]> circleArgs = new ArrayList<>();
		for (String statement : text.split(";")) {
			Matcher m = STEP_ENTITY.matcher(statement.trim());
			if (!m.matches()) {
				continue;
			}
			int id = Integer.parseInt(m.group(1));
			String type = m.group(2);
			String args = m.group(3);
			switch (type) {
				case "CARTESIAN_POINT" -> {
					String coords = args.substring(args.indexOf('(') + 1, args.lastIndexOf(')'));
					String[] parts = coords.split(",");
					double[] p = new double[3];
					for (int i = 0; i < Math.min(3, parts.length); i++) {
						p[i] = Double.parseDouble(parts[i].trim());
					}
					points.put(id, p);
				}
				case "VERTEX_POINT" -> vertexRefs.add(new int[]{firstRef(args)});
				case "AXIS2_PLACEMENT_3D" -> placements.put(id, firstRef(args));
				case "CIRCLE" -> circleArgs.add(args.split(","));
				default -> {
				}
			}
		}

		for (int[] ref : vertexRefs) {
			double[] p = points.get(ref[0]);
			if (p != null) {
				geometry.points.add(p);
			}
		}
		for (String[] args : circleArgs) {
			Integer location = placements.get(firstRef(args[1]));
			double[] center = location == null ? null : points.get(location);
			if (center != null) {
				double r = Double.parseDouble(args[2].trim());
				geometry.circles.add(new Circle(center[0], center[1], center[2], r));
				geometry.points.add(new double[]{center[0] - r, center[1] - r, center[2]});
				geometry.points.add(new double[]{center[0] + r, center[1] + r, center[2]});
			}
		}
		return geometry;
	}

	private static int firstRef(String text) {
		Matcher m = STEP_REF.matcher(text);
		return m.find() ? Integer.parseInt(m.group(1)) : -1;
	}

	/**
	 * Reads the triangles of all build items of a 3MF file, placed by their transforms, and the model unit.
	 */
	private static Geometry read3MF(InputStream in) throws IOException {
		Geometry geometry = new Geometry();
		geometry.threeD = true;

		Document doc = null;
		ZipInputStream zip = new ZipInputStream(in);
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			if (entry.getName().equalsIgnoreCase(ThreeMFPartsWriter.MODEL_PATH)) {
				doc = parseXML(zip.readAllBytes());
				break;
			}
		}
		if (doc == null) {
			throw new IOException("No 3D model found in the 3MF file");
		}

		Element model = doc.getDocumentElement();
		String unit = model.getAttribute("unit");
		geometry.units = unit.isEmpty() || unit.equals("millimeter") ? Units.MILLIMETERS : Units.OTHER;

		// Object meshes, by id
		Map<String, List<double[]>> objects = new HashMap<>();
		NodeList objectNodes = model.getElementsByTagNameNS("*", "object");
		for (int i = 0; i < objectNodes.getLength(); i++) {
			Element object = (Element) objectNodes.item(i);
			NodeList vertexNodes = object.getElementsByTagNameNS("*", "vertex");
			double[][] vertices = new double[vertexNodes.getLength()][];
			for (int v = 0; v < vertices.length; v++) {
				Element vertex = (Element) vertexNodes.item(v);
				vertices[v] = new double[]{Double.parseDouble(vertex.getAttribute("x")),
						Double.parseDouble(vertex.getAttribute("y")), Double.parseDouble(vertex.getAttribute("z"))};
			}
			List<double[]> triangles = new ArrayList<>();
			NodeList triangleNodes = object.getElementsByTagNameNS("*", "triangle");
			for (int t = 0; t < triangleNodes.getLength(); t++) {
				Element triangle = (Element) triangleNodes.item(t);
				double[] a = vertices[Integer.parseInt(triangle.getAttribute("v1"))];
				double[] b = vertices[Integer.parseInt(triangle.getAttribute("v2"))];
				double[] c = vertices[Integer.parseInt(triangle.getAttribute("v3"))];
				triangles.add(new double[]{a[0], a[1], a[2], b[0], b[1], b[2], c[0], c[1], c[2]});
			}
			objects.put(object.getAttribute("id"), triangles);
		}

		// Build items place the objects with a 3x4 affine transform: m00 m01 m02 m10 m11 m12 m20 m21 m22 m30 m31 m32
		NodeList items = model.getElementsByTagNameNS("*", "item");
		for (int i = 0; i < items.getLength(); i++) {
			Element item = (Element) items.item(i);
			List<double[]> triangles = objects.get(item.getAttribute("objectid"));
			if (triangles == null) {
				continue;
			}
			double[] m = {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0};
			String transform = item.getAttribute("transform").trim();
			if (!transform.isEmpty()) {
				String[] values = transform.split("\\s+");
				for (int k = 0; k < 12; k++) {
					m[k] = Double.parseDouble(values[k]);
				}
			}
			for (double[] t : triangles) {
				double[] placed = new double[9];
				for (int j = 0; j < 3; j++) {
					double x = t[3 * j], y = t[3 * j + 1], z = t[3 * j + 2];
					placed[3 * j] = x * m[0] + y * m[3] + z * m[6] + m[9];
					placed[3 * j + 1] = x * m[1] + y * m[4] + z * m[7] + m[10];
					placed[3 * j + 2] = x * m[2] + y * m[5] + z * m[8] + m[11];
					geometry.points.add(new double[]{placed[3 * j], placed[3 * j + 1], placed[3 * j + 2]});
				}
				geometry.triangles.add(placed);
			}
		}
		return geometry;
	}

	private static Document parseXML(byte[] bytes) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
		} catch (ParserConfigurationException | SAXException e) {
			throw new IOException("Invalid 3MF model: " + e.getMessage(), e);
		}
	}

	/**
	 * Reads the triangles of a binary STL file.
	 */
	private static Geometry readSTL(InputStream in) throws IOException {
		Geometry geometry = new Geometry();
		geometry.threeD = true;
		geometry.units = Units.NONE;

		ByteBuffer buffer = ByteBuffer.wrap(in.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN);
		if (buffer.remaining() < 84) {
			return geometry;
		}
		buffer.position(80);
		long count = Integer.toUnsignedLong(buffer.getInt());
		if (buffer.remaining() < count * 50) {
			throw new IOException("Truncated STL file");
		}
		for (long i = 0; i < count; i++) {
			buffer.position(buffer.position() + 12);		// Normal
			double[] t = new double[9];
			for (int j = 0; j < 9; j++) {
				t[j] = buffer.getFloat();
			}
			buffer.getShort();
			geometry.triangles.add(t);
			for (int j = 0; j < 3; j++) {
				geometry.points.add(new double[]{t[3 * j], t[3 * j + 1], t[3 * j + 2]});
			}
		}
		return geometry;
	}
}
