package info.openrocket.core.file.cad.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Minimal ASCII DXF (AutoCAD R12 / AC1009) writer. R12 is the DXF version that is read by practically every
 * CAD, CAM and laser cutter program. Only the entities needed for flat part outlines are supported.
 * <p>
 * All coordinates passed to this class are in millimeters, and the drawing units of the file are declared as
 * millimeters so that the parts import at 1:1 scale.
 */
public class DXFWriter {
	/** R12 files are read as Windows-1252 text by most programs. */
	public static final Charset CHARSET = Charset.forName("windows-1252");

	/** ACI (AutoCAD Color Index) number of red */
	public static final int COLOR_RED = 1;
	/** ACI number of blue */
	public static final int COLOR_BLUE = 5;
	/** ACI number of white, which CAD programs show as black on a white background */
	public static final int COLOR_WHITE = 7;

	private static final int INSUNITS_MILLIMETERS = 4;

	private final Map<String, Integer> layers = new LinkedHashMap<>();
	private final StringBuilder entities = new StringBuilder();

	private double minX = Double.MAX_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxX = -Double.MAX_VALUE;
	private double maxY = -Double.MAX_VALUE;

	/**
	 * Creates a writer with only the default layer "0".
	 */
	public DXFWriter() {
		layers.put("0", COLOR_WHITE);
	}

	/**
	 * Defines a layer. Entities can only be added to layers that have been defined.
	 */
	public void addLayer(String name, int color) {
		layers.put(name, color);
	}

	/**
	 * Adds a closed polyline. If the last point equals the first point it is dropped, as the polyline
	 * is closed by its flag.
	 *
	 * @param layer the layer of the entity
	 * @param xs    the x coordinates (mm)
	 * @param ys    the y coordinates (mm)
	 */
	public void addClosedPolyline(String layer, double[] xs, double[] ys) {
		checkLayer(layer);
		if (xs.length != ys.length) {
			throw new IllegalArgumentException("xs and ys must have the same length");
		}
		int count = xs.length;
		if (count > 1 && xs[0] == xs[count - 1] && ys[0] == ys[count - 1]) {
			count--;
		}
		if (count < 2) {
			return;
		}

		group(0, "POLYLINE");
		group(8, layer);
		group(66, 1);			// Vertices follow
		group(10, 0.0);
		group(20, 0.0);
		group(30, 0.0);
		group(70, 1);			// Closed
		for (int i = 0; i < count; i++) {
			group(0, "VERTEX");
			group(8, layer);
			point(xs[i], ys[i]);
		}
		group(0, "SEQEND");
		group(8, layer);
	}

	/**
	 * Adds a full circle.
	 */
	public void addCircle(String layer, double centerX, double centerY, double radius) {
		checkLayer(layer);
		group(0, "CIRCLE");
		group(8, layer);
		point(centerX, centerY);
		group(40, radius);
		include(centerX - radius, centerY - radius);
		include(centerX + radius, centerY + radius);
	}

	/**
	 * Adds a circular arc, running counter-clockwise from startAngle to endAngle (degrees).
	 */
	public void addArc(String layer, double centerX, double centerY, double radius, double startAngle,
					   double endAngle) {
		checkLayer(layer);
		group(0, "ARC");
		group(8, layer);
		point(centerX, centerY);
		group(40, radius);
		group(50, startAngle);
		group(51, endAngle);
		include(centerX - radius, centerY - radius);
		include(centerX + radius, centerY + radius);
	}

	/**
	 * Adds a straight line from (x1, y1) to (x2, y2).
	 */
	public void addLine(String layer, double x1, double y1, double x2, double y2) {
		checkLayer(layer);
		group(0, "LINE");
		group(8, layer);
		point(x1, y1);
		group(11, x2);
		group(21, y2);
		group(31, 0.0);
		include(x2, y2);
	}

	/**
	 * Adds a single line of text, horizontally centered on {@code centerX} with its baseline at {@code baselineY}.
	 */
	public void addCenteredText(String layer, double centerX, double baselineY, double height, String text) {
		checkLayer(layer);
		group(0, "TEXT");
		group(8, layer);
		point(centerX, baselineY);
		group(40, height);
		group(1, text.replace('\n', ' ').replace('\r', ' '));
		group(72, 1);			// Horizontal justification: center
		group(11, centerX);		// Alignment point, used when justification is not left
		group(21, baselineY);
		group(31, 0.0);
	}

	/**
	 * Writes the complete DXF file to the output stream. The stream is not closed.
	 */
	public void write(OutputStream out) throws IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(out, CHARSET));
		boolean empty = (minX == Double.MAX_VALUE);

		StringBuilder sb = new StringBuilder();
		appendGroup(sb, 0, "SECTION");
		appendGroup(sb, 2, "HEADER");
		appendGroup(sb, 9, "$ACADVER");
		appendGroup(sb, 1, "AC1009");
		appendGroup(sb, 9, "$DWGCODEPAGE");
		appendGroup(sb, 3, "ANSI_1252");
		appendGroup(sb, 9, "$INSUNITS");
		appendGroup(sb, 70, String.valueOf(INSUNITS_MILLIMETERS));
		appendGroup(sb, 9, "$MEASUREMENT");
		appendGroup(sb, 70, "1");		// Metric
		appendGroup(sb, 9, "$EXTMIN");
		appendGroup(sb, 10, format(empty ? 0 : minX));
		appendGroup(sb, 20, format(empty ? 0 : minY));
		appendGroup(sb, 30, format(0));
		appendGroup(sb, 9, "$EXTMAX");
		appendGroup(sb, 10, format(empty ? 0 : maxX));
		appendGroup(sb, 20, format(empty ? 0 : maxY));
		appendGroup(sb, 30, format(0));
		appendGroup(sb, 0, "ENDSEC");

		appendGroup(sb, 0, "SECTION");
		appendGroup(sb, 2, "TABLES");
		appendGroup(sb, 0, "TABLE");
		appendGroup(sb, 2, "LTYPE");
		appendGroup(sb, 70, "1");
		appendGroup(sb, 0, "LTYPE");
		appendGroup(sb, 2, "CONTINUOUS");
		appendGroup(sb, 70, "0");
		appendGroup(sb, 3, "Solid line");
		appendGroup(sb, 72, "65");
		appendGroup(sb, 73, "0");
		appendGroup(sb, 40, format(0));
		appendGroup(sb, 0, "ENDTAB");
		appendGroup(sb, 0, "TABLE");
		appendGroup(sb, 2, "LAYER");
		appendGroup(sb, 70, String.valueOf(layers.size()));
		for (Map.Entry<String, Integer> layer : layers.entrySet()) {
			appendGroup(sb, 0, "LAYER");
			appendGroup(sb, 2, layer.getKey());
			appendGroup(sb, 70, "0");
			appendGroup(sb, 62, String.valueOf(layer.getValue()));
			appendGroup(sb, 6, "CONTINUOUS");
		}
		appendGroup(sb, 0, "ENDTAB");
		appendGroup(sb, 0, "ENDSEC");

		appendGroup(sb, 0, "SECTION");
		appendGroup(sb, 2, "ENTITIES");
		sb.append(entities);
		appendGroup(sb, 0, "ENDSEC");
		appendGroup(sb, 0, "EOF");

		writer.write(sb.toString());
		writer.flush();
	}

	private void checkLayer(String layer) {
		if (!layers.containsKey(layer)) {
			throw new IllegalArgumentException("Undefined layer: " + layer);
		}
	}

	private void point(double x, double y) {
		group(10, x);
		group(20, y);
		group(30, 0.0);
		include(x, y);
	}

	private void include(double x, double y) {
		minX = Math.min(minX, x);
		minY = Math.min(minY, y);
		maxX = Math.max(maxX, x);
		maxY = Math.max(maxY, y);
	}

	private void group(int code, String value) {
		appendGroup(entities, code, value);
	}

	private void group(int code, int value) {
		appendGroup(entities, code, String.valueOf(value));
	}

	private void group(int code, double value) {
		appendGroup(entities, code, format(value));
	}

	private static void appendGroup(StringBuilder sb, int code, String value) {
		sb.append(code).append('\n').append(value).append('\n');
	}

	private static String format(double value) {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("Non-finite coordinate: " + value);
		}
		String s = String.format(Locale.ROOT, "%.6f", value);
		return "-0.000000".equals(s) ? "0.000000" : s;
	}
}
