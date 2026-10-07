package info.openrocket.core.file.flightpath;

/**
 * A named Mustache template that renders a {@link FlightPathModel} into a concrete output
 * format. Templates are either built in (bundled on the classpath) or user supplied (from
 * the OpenRocket user directory).
 */
public class FlightPathTemplate {

	private final String id;
	private final String displayName;
	private final String extension;
	private final String source;
	private final boolean builtIn;

	public FlightPathTemplate(String id, String displayName, String extension, String source, boolean builtIn) {
		this.id = id;
		this.displayName = displayName;
		this.extension = extension;
		this.source = source;
		this.builtIn = builtIn;
	}

	/** Stable identifier, used to remember the user's selection. */
	public String getId() {
		return id;
	}

	/** Human-readable name shown in the format dropdown. */
	public String getDisplayName() {
		return displayName;
	}

	/** Output file extension without a dot, e.g. "kml", "csv", "gpx". */
	public String getExtension() {
		return extension;
	}

	/** The raw Mustache template text. */
	public String getSource() {
		return source;
	}

	public boolean isBuiltIn() {
		return builtIn;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
