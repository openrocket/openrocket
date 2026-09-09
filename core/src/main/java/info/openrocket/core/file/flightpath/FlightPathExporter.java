package info.openrocket.core.file.flightpath;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Escapers;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.FlightData;

/**
 * Renders a simulation's flight path to a chosen output format by binding a
 * {@link FlightPathModel} to a Mustache {@link FlightPathTemplate}. This is the single
 * entry point callers use; the format (KML, waypoint CSV, GPX, custom) is entirely
 * determined by the selected template.
 */
public class FlightPathExporter {

	/** Escapes text for XML/KML/GPX output. Ampersand must be replaced first. */
	private static final Mustache.Escaper XML_ESCAPER = Escapers.simple(new String[][] {
			{ "&", "&amp;" },
			{ "<", "&lt;" },
			{ ">", "&gt;" },
			{ "\"", "&quot;" },
			{ "'", "&apos;" },
	});

	/** Escapes text for CSV output by doubling quotes; templates quote each field. */
	private static final Mustache.Escaper CSV_ESCAPER = raw -> raw.replace("\"", "\"\"");

	private final Simulation simulation;
	private final FlightData data;
	private final FlightPathExportOptions options;

	/**
	 * @param simulation the simulation being exported
	 * @param data       the flight data to render
	 * @param options    the export options controlling the model contents
	 */
	public FlightPathExporter(Simulation simulation, FlightData data, FlightPathExportOptions options) {
		this.simulation = simulation;
		this.data = data;
		this.options = options;
	}

	/**
	 * Returns whether the simulation has a launch latitude/longitude set. When both are
	 * zero the exported coordinates fall on Null Island; callers may warn about this.
	 */
	public boolean hasLaunchPosition() {
		return simulation.getOptions().getLaunchLatitude() != 0
				|| simulation.getOptions().getLaunchLongitude() != 0;
	}

	/**
	 * Render the flight path using the given template and write it to the stream. The
	 * stream is not closed.
	 */
	public void export(FlightPathTemplate template, OutputStream stream) throws IOException {
		FlightPathModel model = new FlightPathModelBuilder(simulation, data, options).build();

		Mustache.Compiler compiler = Mustache.compiler()
				.defaultValue("")
				.nullValue("")
				.withEscaper(escaperFor(template.getExtension()));

		String rendered = compiler.compile(template.getSource()).execute(model);

		Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
		writer.write(rendered);
		writer.flush();
	}

	private static Mustache.Escaper escaperFor(String extension) {
		String ext = extension == null ? "" : extension.toLowerCase(Locale.ENGLISH);
		switch (ext) {
			case "kml":
			case "gpx":
			case "xml":
				return XML_ESCAPER;
			case "csv":
				return CSV_ESCAPER;
			default:
				return Escapers.NONE;
		}
	}
}
