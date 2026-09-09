package info.openrocket.core.file.svg.export;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import info.openrocket.core.util.ORColor;
import java.io.File;
import java.util.Locale;

/**
 * SVGBuilder is a class that allows you to build SVG (Scalable Vector Graphics) files.
 * The functionality is limited to the bare minimum needed to export shapes from OpenRocket.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class SVGBuilder {
	private final Document doc;
	private final Element svgRoot;

	private double minX = Double.MAX_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxX = -Double.MAX_VALUE;
	private double maxY = -Double.MAX_VALUE;
	private double maxStrokeWidth = 0.0; // Track maximum stroke width
	private double originX = 0.0;
	private double originY = 0.0;

	/**
	 * Different stroke cap styles.
	 */
	public enum LineCap {
		BUTT("butt"),		// Stroke does not extend beyond the end of the line
		ROUND("round"),		// Stroke extends beyond the end of the line by a half circle
		SQUARE("square");	// Stroke extends beyond the end of the line by half the stroke width

		private final String value;

		LineCap(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private static final double OR_UNIT_TO_SVG_UNIT = 1000;		// OpenRocket units are in meters, SVG units are in mm

	/**
	 * Creates a new SVGBuilder instance.
	 *
	 * @throws ParserConfigurationException if a DocumentBuilder cannot be created
	 */
	public SVGBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// Root element
		this.doc = docBuilder.newDocument();
		this.svgRoot = this.doc.createElement("svg");
		this.svgRoot.setAttribute("xmlns", "http://www.w3.org/2000/svg");
		this.svgRoot.setAttribute("version", "1.1");
		this.doc.appendChild(this.svgRoot);
	}

	/**
	 * Adds a path to the SVG document.
	 * The path is defined by a list of coordinates, where each coordinate represents a point on the path.
	 *
	 * @param coordinates  the array of coordinates defining the path (coordinates are in meters)
	 * @param xPos         the offset x-axis position of the path (coordinates are in meters)
	 * @param yPos         the offset y-axis position of the path (coordinates are in meters)
	 * @param fill         the color used to fill the path, or null if the path should not be filled
	 * @param stroke       the color used to stroke the path, or null if the path should not be stroked
	 * @param strokeWidth the width of the path stroke (in millimeters)
	 * @param lineCap      the line cap style of the path
	 */
	public void addPath(CoordinateIF[] coordinates, double xPos, double yPos, ORColor fill, ORColor stroke, double strokeWidth,
						LineCap lineCap) {
		final Element path = this.doc.createElement("path");
		final StringBuilder dAttribute = new StringBuilder();

		trackStrokeWidth(stroke, strokeWidth);

		for (int i = 0; i < coordinates.length; i++) {
			final CoordinateIF coord = coordinates[i];
			double x = toSvgUnits(coord.getX() + xPos + originX);
			double y = toSvgUnits(coord.getY() + yPos + originY);
			updateCanvasSize(x, y);
			final String command = (i == 0) ? "M" : "L";
			// Use higher precision for coordinates to avoid visible stepping on small parts
			dAttribute.append(String.format(Locale.ENGLISH, "%s%.3f,%.3f ", command, x, y));		// Coordinates are in meters, SVG is in mm
		}

		// Close the path if it's the same start and end point
		if (coordinates.length > 2 &&
				Math.abs(coordinates[0].getX() - coordinates[coordinates.length-1].getX()) < 1e-10 &&
				Math.abs(coordinates[0].getY() - coordinates[coordinates.length-1].getY()) < 1e-10) {
			dAttribute.append("Z");
		}

		path.setAttribute("d", dAttribute.toString());
		path.setAttribute("fill", colorToString(fill));
		path.setAttribute("stroke", colorToString(stroke));
		path.setAttribute("stroke-width", String.format(Locale.ENGLISH, "%.3f", strokeWidth));
		path.setAttribute("stroke-linecap", lineCap.getValue());
		svgRoot.appendChild(path);
	}

	public void addPath(CoordinateIF[] coordinates, double xPos, double yPos, ORColor fill, ORColor stroke, double strokeWidth) {
		addPath(coordinates, xPos, yPos, fill, stroke, strokeWidth, LineCap.SQUARE);
	}

	public void addPath(CoordinateIF[] coordinates, ORColor fill, ORColor stroke, double strokeWidth, LineCap lineCap) {
		addPath(coordinates, 0, 0, fill, stroke, strokeWidth, lineCap);
	}

	public void addPath(CoordinateIF[] coordinates, ORColor fill, ORColor stroke, double strokeWidth) {
		addPath(coordinates, fill, stroke, strokeWidth, LineCap.SQUARE);
	}

	/**
	 * Adds a circle element.
	 */
	public void addCircle(double centerX, double centerY, double radius, ORColor fill, ORColor stroke, double strokeWidth) {
		Element circle = doc.createElement("circle");

		double cx = toSvgUnits(centerX + originX);
		double cy = toSvgUnits(centerY + originY);
		double r = toSvgUnits(radius);

		trackStrokeWidth(stroke, strokeWidth);
		updateCanvasSize(cx - r, cy - r);
		updateCanvasSize(cx + r, cy + r);

		circle.setAttribute("cx", formatDouble(cx));
		circle.setAttribute("cy", formatDouble(cy));
		circle.setAttribute("r", formatDouble(r));
		circle.setAttribute("fill", colorToString(fill));
		circle.setAttribute("stroke", colorToString(stroke));
		circle.setAttribute("stroke-width", formatDouble(strokeWidth));
		svgRoot.appendChild(circle);
	}

	/**
	 * Adds a donut/annulus using the even-odd fill rule.
	 */
	public void addAnnulus(double centerX, double centerY, double outerRadius, double innerRadius,
						   ORColor fill, ORColor stroke, double strokeWidth) {
		if (innerRadius <= 0) {
			addCircle(centerX, centerY, outerRadius, fill, stroke, strokeWidth);
			return;
		}

		Element path = doc.createElement("path");
		double cx = toSvgUnits(centerX + originX);
		double cy = toSvgUnits(centerY + originY);
		double outer = toSvgUnits(outerRadius);
		double inner = toSvgUnits(innerRadius);

		StringBuilder builder = new StringBuilder();
		builder.append(createCirclePath(cx, cy, outer, false));
		builder.append(createCirclePath(cx, cy, inner, true));
		builder.append("Z");

		trackStrokeWidth(stroke, strokeWidth);
		updateCanvasSize(cx - outer, cy - outer);
		updateCanvasSize(cx + outer, cy + outer);

		path.setAttribute("d", builder.toString());
		path.setAttribute("fill-rule", "evenodd");
		path.setAttribute("fill", colorToString(fill));
		path.setAttribute("stroke", colorToString(stroke));
		path.setAttribute("stroke-width", formatDouble(strokeWidth));
		svgRoot.appendChild(path);
	}

	/**
	 * Adds a straight line guide.
	 */
	public void addLine(double startX, double startY, double endX, double endY, ORColor stroke, double strokeWidth,
						LineCap lineCap) {
		Element line = doc.createElement("line");

		double x1 = toSvgUnits(startX + originX);
		double y1 = toSvgUnits(startY + originY);
		double x2 = toSvgUnits(endX + originX);
		double y2 = toSvgUnits(endY + originY);

		trackStrokeWidth(stroke, strokeWidth);
		updateCanvasSize(x1, y1);
		updateCanvasSize(x2, y2);

		line.setAttribute("x1", formatDouble(x1));
		line.setAttribute("y1", formatDouble(y1));
		line.setAttribute("x2", formatDouble(x2));
		line.setAttribute("y2", formatDouble(y2));
		line.setAttribute("stroke", colorToString(stroke));
		line.setAttribute("stroke-width", formatDouble(strokeWidth));
		line.setAttribute("stroke-linecap", lineCap.getValue());
		svgRoot.appendChild(line);
	}

	public void addLine(double startX, double startY, double endX, double endY, ORColor stroke, double strokeWidth) {
		addLine(startX, startY, endX, endY, stroke, strokeWidth, LineCap.BUTT);
	}

	/**
	 * Convenience helper to draw a crosshair centered at {@code (centerX, centerY)}.
	 */
	public void addCrosshair(double centerX, double centerY, double armHalfWidth, double armHalfHeight,
							 ORColor stroke, double strokeWidth) {
		addLine(centerX - armHalfWidth, centerY, centerX + armHalfWidth, centerY, stroke, strokeWidth, LineCap.SQUARE);
		addLine(centerX, centerY - armHalfHeight, centerX, centerY + armHalfHeight, stroke, strokeWidth, LineCap.SQUARE);
	}

	/**
	 * Adds a text element to the SVG document.
	 *
	 * @param x the x-coordinate of the text anchor (in meters)
	 * @param y the y-coordinate of the text anchor (in meters)
	 * @param text the text content to display
	 * @param fontSize the font size in millimeters
	 * @param fill the text color, or null for black
	 * @param anchor the text anchor position ("start", "middle", "end")
	 */
	public void addText(double x, double y, String text, double fontSize, ORColor fill, String anchor) {
		Element textElement = doc.createElement("text");
		
		double svgX = toSvgUnits(x + originX);
		double svgY = toSvgUnits(y + originY);
		
		// Estimate text width: average character width is about 0.6 * font size for most fonts
		double estimatedTextWidth = text.length() * fontSize * 0.6;
		
		// Update canvas size to account for text bounds
		// Text extends above baseline (y - fontSize) and below baseline (y + fontSize * 0.3 for descenders)
		// For horizontal extent, account for anchor position
		if (anchor == null || anchor.isEmpty() || "start".equals(anchor)) {
			// Text starts at x and extends right
			updateCanvasSize(svgX, svgY - fontSize);
			updateCanvasSize(svgX + estimatedTextWidth, svgY + fontSize * 0.3);
		} else if ("middle".equals(anchor)) {
			// Text is centered at x, extends left and right
			double halfWidth = estimatedTextWidth / 2.0;
			updateCanvasSize(svgX - halfWidth, svgY - fontSize);
			updateCanvasSize(svgX + halfWidth, svgY + fontSize * 0.3);
		} else if ("end".equals(anchor)) {
			// Text ends at x, extends left
			updateCanvasSize(svgX - estimatedTextWidth, svgY - fontSize);
			updateCanvasSize(svgX, svgY + fontSize * 0.3);
		} else {
			// Default: treat as start
			updateCanvasSize(svgX, svgY - fontSize);
			updateCanvasSize(svgX + estimatedTextWidth, svgY + fontSize * 0.3);
		}
		
		textElement.setAttribute("x", formatDouble(svgX));
		textElement.setAttribute("y", formatDouble(svgY));
		textElement.setAttribute("font-size", formatDouble(fontSize));
		textElement.setAttribute("fill", colorToString(fill != null ? fill : ORColor.BLACK));
		if (anchor != null && !anchor.isEmpty()) {
			textElement.setAttribute("text-anchor", anchor);
		}
		textElement.setTextContent(text);
		svgRoot.appendChild(textElement);
	}

	/**
	 * Adds a text element with default anchor "middle" (centered horizontally).
	 */
	public void addText(double x, double y, String text, double fontSize, ORColor fill) {
		addText(x, y, text, fontSize, fill, "middle");
	}

	/**
	 * Repositions the drawing origin so subsequent calls are offset.
	 */
	public void setOrigin(double originX, double originY) {
		this.originX = originX;
		this.originY = originY;
	}

	public void translate(double deltaX, double deltaY) {
		this.originX += deltaX;
		this.originY += deltaY;
	}

	public double getOriginX() {
		return originX;
	}

	public double getOriginY() {
		return originY;
	}

	/**
	 * Updates the canvas size based on the given coordinates.
	 *
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 */
	private void updateCanvasSize(double x, double y) {
		if (x < minX) minX = x;
		if (y < minY) minY = y;
		if (x > maxX) maxX = x;
		if (y > maxY) maxY = y;
	}

	/**
	 * Finalizes the SVG document by setting the width, height and viewBox attributes.
	 * Accounts for stroke width to ensure strokes are not clipped.
	 */
	public void finalizeSVG() {
		if (minX == Double.MAX_VALUE || minY == Double.MAX_VALUE) {
			minX = 0;
			minY = 0;
			maxX = 0;
			maxY = 0;
		}
		// Expand bounds by half the maximum stroke width to account for stroke rendering
		double strokeOffset = maxStrokeWidth / 2.0;

		double finalMinX = minX - strokeOffset;
		double finalMinY = minY - strokeOffset;
		double finalWidth = (maxX - minX) + (2 * strokeOffset);
		double finalHeight = (maxY - minY) + (2 * strokeOffset);

		svgRoot.setAttribute("width", String.format(Locale.ENGLISH, "%.3fmm", finalWidth));
		svgRoot.setAttribute("height", String.format(Locale.ENGLISH, "%.3fmm", finalHeight));
		svgRoot.setAttribute("viewBox", String.format(Locale.ENGLISH, "%.3f %.3f %.3f %.3f",
				finalMinX, finalMinY, finalWidth, finalHeight));
	}

	/**
	 * Converts a color to an SVG string representation.
	 *
	 * @param color the color to convert
	 * @return the string representation of the color
	 */
	private String colorToString(ORColor color) {
		return color == null ?
				"none" :
				String.format("rgb(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue());
	}

	private double toSvgUnits(double meters) {
		return meters * OR_UNIT_TO_SVG_UNIT;
	}

	private void trackStrokeWidth(ORColor stroke, double strokeWidth) {
		if (stroke != null && strokeWidth > maxStrokeWidth) {
			maxStrokeWidth = strokeWidth;
		}
	}

	private String formatDouble(double value) {
		return String.format(Locale.ENGLISH, "%.3f", value);
	}

	private String createCirclePath(double cx, double cy, double radius, boolean reverse) {
		double startX = cx + radius;
		double startY = cy;
		double endX = cx - radius;
		double endY = cy;
		int sweep = reverse ? 0 : 1;
		return String.format(Locale.ENGLISH,
				"M%.3f,%.3f " +
						"A%.3f,%.3f 0 1,%d %.3f,%.3f " +
						"A%.3f,%.3f 0 1,%d %.3f,%.3f ",
				startX, startY,
				radius, radius, sweep, endX, endY,
				radius, radius, sweep, startX, startY);
	}

	/**
	 * Writes the SVG document to a file.
	 * @param file the file to write to
	 * @throws TransformerException if an error occurs while writing the file
	 */
	public void writeToFile(File file) throws TransformerException {
		finalizeSVG();
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}

	public static void main(String[] args) throws ParserConfigurationException, TransformerException {
		SVGBuilder svgBuilder = new SVGBuilder();

		CoordinateIF[] coordinates = {
				new Coordinate(0, 0),
				new Coordinate(0, 0.01),
				new Coordinate(0.02, 0.02),
				new Coordinate(0.01, 0),
				new Coordinate(0, 0)};

		svgBuilder.addPath(coordinates, null, ORColor.BLACK, 0.1);
		svgBuilder.writeToFile(new File("<your_path_here>/test.svg"));
	}
}