package info.openrocket.core.file.flightpath;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.arch.SystemInfo;

/**
 * Provides the templates available for flight-path export: a set of built-in templates
 * bundled with OpenRocket, plus any user templates found in the
 * <code>ExportTemplates</code> folder of the OpenRocket user directory.
 * <p>
 * User templates follow the naming convention <code>&lt;name&gt;.&lt;ext&gt;.mustache</code>,
 * where <code>&lt;ext&gt;</code> becomes the output file extension (e.g.
 * <code>my-waypoints.csv.mustache</code> renders a <code>.csv</code> file).
 */
public class FlightPathTemplateRepository {

	private static final Logger log = LoggerFactory.getLogger(FlightPathTemplateRepository.class);

	private static final String USER_TEMPLATE_DIR = "ExportTemplates";
	private static final String SUFFIX = ".mustache";
	private static final String RESOURCE_BASE = "/templates/flightpath/";

	/** Built-in templates, in display order. */
	private static final BuiltIn[] BUILT_INS = {
			new BuiltIn("kml", "KML (Google Earth)", "kml", "flightpath.kml.mustache"),
			new BuiltIn("waypoints-csv", "Waypoint CSV", "csv", "waypoints.csv.mustache"),
			new BuiltIn("gpx", "GPX track", "gpx", "flightpath.gpx.mustache"),
	};

	/**
	 * Return all available templates: built-ins first, then user templates sorted by name.
	 */
	public List<FlightPathTemplate> getTemplates() {
		List<FlightPathTemplate> templates = new ArrayList<>();

		for (BuiltIn b : BUILT_INS) {
			String source = readResource(RESOURCE_BASE + b.resource());
			if (source != null)
				templates.add(new FlightPathTemplate(b.id(), b.displayName(), b.extension(), source, true));
		}

		templates.addAll(getUserTemplates());
		return templates;
	}

	/** Return the built-in default template (KML). */
	public FlightPathTemplate getDefault() {
		List<FlightPathTemplate> templates = getTemplates();
		return templates.isEmpty() ? null : templates.get(0);
	}

	/** The directory scanned for user templates. Not created by this method. */
	public File getUserTemplateDirectory() {
		return new File(SystemInfo.getUserApplicationDirectory(), USER_TEMPLATE_DIR);
	}

	private List<FlightPathTemplate> getUserTemplates() {
		List<FlightPathTemplate> result = new ArrayList<>();
		File dir = getUserTemplateDirectory();
		File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(SUFFIX));
		if (files == null)
			return result;

		java.util.Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
		for (File file : files) {
			try {
				String source = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
				String base = file.getName().substring(0, file.getName().length() - SUFFIX.length());
				String extension = "txt";
				String display = base;
				int dot = base.lastIndexOf('.');
				if (dot > 0 && dot < base.length() - 1) {
					extension = base.substring(dot + 1);
					display = base.substring(0, dot);
				}
				result.add(new FlightPathTemplate(file.getName(), display, extension, source, false));
			} 
			catch (IOException e) {
				log.warn("Could not read export template {}", file.getAbsolutePath(), e);
			}
		}
		return result;
	}

	private String readResource(String path) {
		try (InputStream in = FlightPathTemplateRepository.class.getResourceAsStream(path)) {
			if (in == null) {
				log.warn("Built-in export template not found: {}", path);
				return null;
			}
			StringBuilder sb = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
				char[] buf = new char[4096];
				int read;
				while ((read = reader.read(buf)) != -1)
					sb.append(buf, 0, read);
			}
			return sb.toString();
		} 
		catch (IOException e) {
			log.warn("Could not read built-in export template {}", path, e);
			return null;
		}
	}

	/** Descriptor for a bundled template: its identity and the classpath resource to load. */
	private record BuiltIn(String id, String displayName, String extension, String resource) {
	}
}
