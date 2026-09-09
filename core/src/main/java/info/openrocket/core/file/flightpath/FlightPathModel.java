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
 * things: coordinate values (KML, GPX) require metres above sea level, while human-facing
 * labels (the waypoint CSV) typically show altitude above the pad in the user's unit.
 */
public class FlightPathModel {

	// Identity / metadata
	public String title = "";
	public String rocketName = "";
	public String simulationName = "";
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

	// Summary values (in display units, preformatted)
	public String maxAltitude = "";
	public String maxVelocity = "";
	public String maxAcceleration = "";

	public List<Branch> branches = new ArrayList<>();

	/** A single flight branch (stage / booster), with its waypoints and sampled path. */
	public static class Branch {
		public String name = "";
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

	/** A single labelled point of interest (pad, apogee, recovery deployment, ...). */
	public static class Waypoint {
		/** Machine key, e.g. "pad", "apogee", "main". Handy for template conditionals. */
		public String type = "";
		/** Localized human label, e.g. "Apogee". */
		public String label = "";
		/** For recovery deployments, the deploying component's name (e.g. "Main"). */
		public String device = "";

		public double latitude;
		public double longitude;
		/** Fixed 6-decimal lat/lon strings, convenient for CSV output. */
		public String latitudeStr = "";
		public String longitudeStr = "";
		/** Altitude above sea level, in metres, for coordinate output. */
		public double altitudeMslMeters;

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
		public double altitudeMslMeters;
		public double time;
		public String timeStr = "";
		public String altitude = "";
	}
}
