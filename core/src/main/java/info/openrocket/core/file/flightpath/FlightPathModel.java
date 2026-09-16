package info.openrocket.core.file.flightpath;

import java.util.ArrayList;
import java.util.List;

/**
 * Format-agnostic data model describing a simulated flight path. It is populated by
 * {@link FlightPathModelBuilder} and consumed by a Mustache template in
 * {@link FlightPathExporter}. All fields are public so templates can reference them
 * directly (e.g. <code>{{rocketName}}</code>, <code>{{#branches}}...{{/branches}}</code>).
 * <p>
 * Two representations of altitude are provided because different formats need different
 * things: coordinate values (KML, GPX) require meters above sea level, while human-facing
 * labels (the waypoint CSV) typically show altitude above the pad in the user's unit.
 */
public class FlightPathModel {

	// Identity / metadata
	public String title = "";
	public String rocketName = "";
	public String simulationName = "";
	/**
	 * The user's name for this flight, already folded into {@link #title} and into each branch's
	 * name. Exposed on its own so a template can place it somewhere else instead.
	 */
	public String missionName = "";
	public String motor = "";
	public String configuration = "";

	// Launch site
	public double launchLatitude;
	public double launchLongitude;
	public double launchAltitudeMeters;

	// Units (display labels for the values below)
	public String altitudeUnit = "";
	public String distanceUnit = "";

	// Geometry toggles, mirrored from the export options so templates can gate output.
	public boolean includeFlightPath = true;
	public boolean includeGroundTrack = true;
	/**
	 * The KML {@code <altitudeMode>} that {@code altitudeKmlMeters} is expressed in, i.e. whether
	 * the track is hung off the terrain or off sea level.
	 */
	public String kmlAltitudeMode = "relativeToGround";
	/** The KML {@code <altitudeMode>} a waypoint's {@code altitudeKmlMeters} is expressed in. */
	public String kmlWaypointAltitudeMode = "relativeToGround";
	/**
	 * Draw {@code <extrude>} lines from the track and the pins down to the ground. These are
	 * Mustache sections rather than values, so a template written before they existed renders
	 * nothing for them instead of emitting an empty element. Already false when the geometry is
	 * clamped, since there is nothing to extrude to.
	 */
	public boolean extrudePath = false;
	public boolean extrudeWaypoints = false;
	/**
	 * Break the flight-path line into terrain-following pieces. KML only honors
	 * {@code <tessellate>} for a clamped line, and without it a clamped path cuts straight
	 * through hills instead of draping over them.
	 */
	public boolean tessellatePath = false;
	/**
	 * Whether waypoint names are drawn on the map. A near-vertical flight stacks its waypoints
	 * into a few hundred meters of screen, and the reader may prefer bare markers they can click.
	 */
	public boolean showWaypointLabels = true;
	/**
	 * Whether waypoint pins carry their stage's color. This needs an icon fetched from Google's
	 * servers, so it can be turned off for a file that has to render without a network.
	 */
	public boolean colorWaypointPins = true;

	// Summary values (in display units, preformatted)
	public String maxAltitude = "";
	public String maxVelocity = "";
	public String maxAcceleration = "";

	public List<Branch> branches = new ArrayList<>();

	/** A single flight branch (stage / booster), with its waypoints and sampled path. */
	public static class Branch {
		public String name = "";
		/** Zero-based position in {@link FlightPathModel#branches}, for building unique style ids. */
		public int index;
		/**
		 * This branch's color as RRGGBB, so each stage's track is distinguishable. Taken from
		 * the same palette the plot window uses, so a stage keeps its color between the two.
		 */
		public String colorRgb = "";
		/** {@link #colorRgb} as a KML aabbggrr literal, opaque, for the flight-path line. */
		public String pathColorKml = "";
		/** {@link #colorRgb} as a KML aabbggrr literal, translucent, for the ground track. */
		public String groundColorKml = "";
		public List<Waypoint> waypoints = new ArrayList<>();
		public List<PathPoint> path = new ArrayList<>();

		/** Convenience for templates: true when the path has any points. */
		public boolean hasPath() {
			return !path.isEmpty();
		}

		/** Convenience for templates: true when the branch has any waypoints. */
		public boolean hasWaypoints() {
			return !waypoints.isEmpty();
		}
	}

	/** A single labeled point of interest (pad, apogee, recovery deployment, ...). */
	public static class Waypoint {
		/** Machine key, e.g. "pad", "apogee", "main". Handy for template conditionals. */
		public String type = "";
		/** Localized human label, e.g. "Apogee". */
		public String label = "";
		/**
		 * {@link #label} qualified with the stage it belongs to, e.g. "Booster Apogee". For a
		 * single-branch flight this is identical to {@link #label}; it only differs when the
		 * rocket staged, where otherwise every stage would contribute an identically named
		 * "Apogee", "Burnout" and "Landing" and the export would be impossible to read.
		 */
		public String qualifiedLabel = "";
		/** Name of the flight branch (stage) this waypoint belongs to, e.g. "Booster". */
		public String branchName = "";
		/** For recovery deployments, the deploying component's name (e.g. "Main"). */
		public String device = "";

		public double latitude;
		public double longitude;
		/** Fixed 6-decimal lat/lon strings, convenient for CSV output. */
		public String latitudeStr = "";
		public String longitudeStr = "";
		/** Altitude above sea level, in meters. GPX elevations are defined this way. */
		public double altitudeMslMeters;
		/** Altitude above the ground, in meters. */
		public double altitudeAglMeters;
		/** The altitude to write into a KML coordinate, in {@link FlightPathModel#kmlAltitudeMode}. */
		public double altitudeKmlMeters;

		public double time;
		public String timeStr = "";

		// Display values (in the chosen units, preformatted)
		public String altitude = "";       // above the pad
		public String altitudeMsl = "";     // above sea level
		public String distance = "";        // horizontal distance from pad
		public String bearing = "";         // compass degrees from pad
	}

	/** A single sampled point along the flight path. */
	public static class PathPoint {
		public double latitude;
		public double longitude;
		/** Altitude above sea level, in meters. GPX elevations are defined this way. */
		public double altitudeMslMeters;
		/** Altitude above the ground, in meters. */
		public double altitudeAglMeters;
		/** The altitude to write into a KML coordinate, in {@link FlightPathModel#kmlAltitudeMode}. */
		public double altitudeKmlMeters;
		public double time;
		public String timeStr = "";
		public String altitude = "";
	}
}
