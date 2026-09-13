package info.openrocket.core.file.flightpath;

import java.util.EnumSet;
import java.util.Set;

import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;

/**
 * User-configurable options for a templated flight-path export. These control what the
 * {@link FlightPathModelBuilder} puts into the {@link FlightPathModel}; the chosen
 * template then decides how that model is rendered (KML, waypoint CSV, GPX, ...).
 */
public class FlightPathExportOptions {

	/** The kinds of single-point waypoints that can be emitted for each flight branch. */
	public enum Waypoint {
		PAD,
		LIFTOFF,
		BURNOUT,
		APOGEE,
		RECOVERY,
		LANDING,
		MAX_VELOCITY,
		MAX_ACCELERATION
	}

	/**
	 * Where each stage's track begins on a staged flight. A branch created at separation starts
	 * life as a verbatim copy of its parent's points, so the ascent the stages flew bolted
	 * together is present in every branch and can be exported once or once per stage.
	 */
	public enum StageTrackStart {
		/**
		 * Each stage's track begins where it left the stack, so the shared ascent is drawn once
		 * and each stage's peaks are its own.
		 */
		SEPARATION,
		/**
		 * Every stage's track begins on the pad, so each one reads as a complete flight at the
		 * cost of drawing the shared ascent once per stage.
		 */
		PAD
	}

	/**
	 * What the exported altitudes are measured from. This matters because OpenRocket's launch
	 * altitude defaults to zero: a site that is actually 1200 m up then reports its flight in
	 * meters above the pad, and placing that against sea level buries the whole track under the
	 * terrain.
	 */
	public enum AltitudeReference {
		/**
		 * Decide from the simulation: a launch altitude the user actually set means the flight can
		 * be placed at its true elevation, and the default of zero means it cannot.
		 */
		AUTOMATIC(null),
		/** Altitudes are height above the terrain, which is right whatever the launch altitude says. */
		GROUND("relativeToGround"),
		/** Altitudes are height above sea level, for a simulation with a correct launch altitude. */
		SEA_LEVEL("absolute");

		private final String kmlAltitudeMode;

		AltitudeReference(String kmlAltitudeMode) {
			this.kmlAltitudeMode = kmlAltitudeMode;
		}

		/**
		 * The KML {@code <altitudeMode>} that matches this reference, or {@code null} for
		 * {@link #AUTOMATIC}, which must be resolved against a simulation first.
		 */
		public String getKmlAltitudeMode() {
			return kmlAltitudeMode;
		}

		/**
		 * Resolve {@link #AUTOMATIC} against a launch altitude; any other value is returned as-is.
		 *
		 * @param launchAltitude the simulation's launch altitude in meters above sea level
		 */
		public AltitudeReference resolve(double launchAltitude) {
			if (this != AUTOMATIC) {
				return this;
			}
			return (launchAltitude != 0 && !Double.isNaN(launchAltitude)) ? SEA_LEVEL : GROUND;
		}
	}

	private Unit altitudeUnit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
	private Unit distanceUnit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();

	private final Set<Waypoint> waypoints = EnumSet.allOf(Waypoint.class);

	private boolean includeFlightPath = true;
	private boolean includeGroundTrack = true;
	/** Keep every Nth flight-path point (1 = keep all). */
	private int pathStride = 1;
	private StageTrackStart stageTrackStart = StageTrackStart.SEPARATION;
	private AltitudeReference altitudeReference = AltitudeReference.AUTOMATIC;
	private boolean showWaypointLabels = true;
	private boolean colorWaypointPins = true;

	public Unit getAltitudeUnit() {
		return altitudeUnit;
	}

	public void setAltitudeUnit(Unit altitudeUnit) {
		this.altitudeUnit = altitudeUnit;
	}

	public Unit getDistanceUnit() {
		return distanceUnit;
	}

	public void setDistanceUnit(Unit distanceUnit) {
		this.distanceUnit = distanceUnit;
	}

	public Set<Waypoint> getWaypoints() {
		return waypoints;
	}

	public boolean hasWaypoint(Waypoint w) {
		return waypoints.contains(w);
	}

	public void setWaypoint(Waypoint w, boolean enabled) {
		if (enabled)
			waypoints.add(w);
		else
			waypoints.remove(w);
	}

	public boolean isIncludeFlightPath() {
		return includeFlightPath;
	}

	public void setIncludeFlightPath(boolean includeFlightPath) {
		this.includeFlightPath = includeFlightPath;
	}

	public boolean isIncludeGroundTrack() {
		return includeGroundTrack;
	}

	public void setIncludeGroundTrack(boolean includeGroundTrack) {
		this.includeGroundTrack = includeGroundTrack;
	}

	public int getPathStride() {
		return pathStride;
	}

	public void setPathStride(int pathStride) {
		this.pathStride = Math.max(1, pathStride);
	}

	public StageTrackStart getStageTrackStart() {
		return stageTrackStart;
	}

	public void setStageTrackStart(StageTrackStart stageTrackStart) {
		this.stageTrackStart = (stageTrackStart == null) ? StageTrackStart.SEPARATION : stageTrackStart;
	}

	public AltitudeReference getAltitudeReference() {
		return altitudeReference;
	}

	public void setAltitudeReference(AltitudeReference altitudeReference) {
		this.altitudeReference = (altitudeReference == null) ? AltitudeReference.AUTOMATIC : altitudeReference;
	}

	public boolean isShowWaypointLabels() {
		return showWaypointLabels;
	}

	public void setShowWaypointLabels(boolean showWaypointLabels) {
		this.showWaypointLabels = showWaypointLabels;
	}

	public boolean isColorWaypointPins() {
		return colorWaypointPins;
	}

	public void setColorWaypointPins(boolean colorWaypointPins) {
		this.colorWaypointPins = colorWaypointPins;
	}
}
