package info.openrocket.core.file.svg.export;

import info.openrocket.core.util.Coordinate;
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
import java.awt.Color;
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
	private double maxX = Double.MIN_VALUE;
	private double maxY = Double.MIN_VALUE;

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
	public void addPath(Coordinate[] coordinates, double xPos, double yPos, Color fill, Color stroke, double strokeWidth,
						LineCap lineCap) {
		final Element path = this.doc.createElement("path");
		final StringBuilder dAttribute = new StringBuilder();

		for (int i = 0; i < coordinates.length; i++) {
			final Coordinate coord = coordinates[i];
			double x = (coord.x + xPos) * OR_UNIT_TO_SVG_UNIT;
			double y = (coord.y+ yPos) * OR_UNIT_TO_SVG_UNIT;
			updateCanvasSize(x, y);
			final String command = (i == 0) ? "M" : "L";
			dAttribute.append(String.format(Locale.ENGLISH, "%s%.1f,%.1f ", command, x, y));		// Coordinates are in meters, SVG is in mm
		}

		path.setAttribute("d", dAttribute.toString());
		path.setAttribute("fill", colorToString(fill));
		path.setAttribute("stroke", colorToString(stroke));
		path.setAttribute("stroke-width", String.format(Locale.ENGLISH, "%.001f", strokeWidth));
		path.setAttribute("stroke-linecap", lineCap.getValue());
		svgRoot.appendChild(path);
	}

	public void addPath(Coordinate[] coordinates, double xPos, double yPos, Color fill, Color stroke, double strokeWidth) {
		addPath(coordinates, xPos, yPos, fill, stroke, strokeWidth, LineCap.SQUARE);
	}

	public void addPath(Coordinate[] coordinates, Color fill, Color stroke, double strokeWidth, LineCap lineCap) {
		addPath(coordinates, 0, 0, fill, stroke, strokeWidth, lineCap);
	}

	public void addPath(Coordinate[] coordinates, Color fill, Color stroke, double strokeWidth) {
		addPath(coordinates, fill, stroke, strokeWidth, LineCap.SQUARE);
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
	 */
	public void finalizeSVG() {
		svgRoot.setAttribute("width", (maxX - minX) + "mm");
		svgRoot.setAttribute("height", (maxY - minY) + "mm");
		svgRoot.setAttribute("viewBox", minX + " " + minY + " " + (maxX - minX) + " " + (maxY - minY));
	}

	/**
	 * Converts a color to an SVG string representation.
	 *
	 * @param color the color to convert
	 * @return the string representation of the color
	 */
	private String colorToString(Color color) {
		return color == null ?
				"none" :
				String.format("rgb(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue());
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

		Coordinate[] coordinates = {
				new Coordinate(0, 0),
				new Coordinate(0, 0.01),
				new Coordinate(0.02, 0.02),
				new Coordinate(0.01, 0),
				new Coordinate(0, 0)};

		svgBuilder.addPath(coordinates, null, Color.BLACK, 0.1);
		svgBuilder.writeToFile(new File("/Users/SiboVanGool/Downloads/shape.svg"));
	}
}
