package info.openrocket.core.file.flightpath;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
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
		SEA_LEVEL("absolute"),
		/**
		 * Altitudes are ignored and the geometry is laid flat on the terrain. The one to pick when
		 * the question is what the rocket drifts <em>over</em> rather than how high it went.
		 */
		CLAMPED("clampToGround");

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
	private AltitudeReference waypointAltitudeReference = AltitudeReference.AUTOMATIC;
	private final Map<Integer, Integer> branchColors = new HashMap<>();
	private final Map<Integer, Integer> branchGroundColors = new HashMap<>();
	private final Map<Integer, Integer> branchPinColors = new HashMap<>();
	private boolean drawShadow = false;
	private String missionName = "";
	private boolean labelWaypointsWithMission = false;
	private boolean showWaypointLabels = true;
	private boolean colorWaypointPins = true;
	private boolean includeDescriptions = true;

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

	/**
	 * What the exported waypoints' altitudes are measured from, which is set separately from the
	 * track's: a flight is worth seeing suspended in the air, while the pins that label it are
	 * easier to read against the ground they sit over.
	 */
	public AltitudeReference getWaypointAltitudeReference() {
		return waypointAltitudeReference;
	}

	public void setWaypointAltitudeReference(AltitudeReference waypointAltitudeReference) {
		this.waypointAltitudeReference =
				(waypointAltitudeReference == null) ? AltitudeReference.AUTOMATIC : waypointAltitudeReference;
	}

	/**
	 * Draw a line from the track and from each pin straight down to the ground. This is KML's
	 * {@code <extrude>}, which Google Earth renders as a curtain under the path and a plumb line
	 * under a pin, and it is how you read where a point in the air sits on the map. Meaningless
	 * once the geometry is already lying on the ground, so it is ignored for a clamped reference.
	 */
	/**
	 * The color to draw a stage's flight path in, as RRGGBB, or {@code null} to use the built-in
	 * palette entry for that stage.
	 *
	 * <p>Keyed by the stage's position in the flight data rather than by its name, because two
	 * stages of one rocket can carry the same name and would otherwise share a color.
	 *
	 * <p>This is the flight-path line only. The ground track and the waypoint pins have defaults and
	 * overrides of their own, through {@link #setBranchGroundColor(int, Integer)} and
	 * {@link #setBranchPinColor(int, Integer)}, and nothing here changes either of them.
	 *
	 * @param index the stage's position in the flight data, counting from zero
	 */
	public Integer getBranchColor(int index) {
		return branchColors.get(index);
	}

	/** Override a stage's flight-path color, or pass {@code null} to go back to the palette. */
	public void setBranchColor(int index, Integer rgb) {
		if (rgb == null) {
			branchColors.remove(index);
		} else {
			branchColors.put(index, rgb & 0xFFFFFF);
		}
	}

	/**
	 * The color to draw a stage's ground track in, as RRGGBB, or {@code null} to use the built-in
	 * default for that stage, which comes from a ground-track palette of its own.
	 *
	 * @param index the stage's position in the flight data, counting from zero
	 */
	public Integer getBranchGroundColor(int index) {
		return branchGroundColors.get(index);
	}

	/** Override a stage's ground-track color, or pass {@code null} to go back to the default. */
	public void setBranchGroundColor(int index, Integer rgb) {
		if (rgb == null) {
			branchGroundColors.remove(index);
		} else {
			branchGroundColors.put(index, rgb & 0xFFFFFF);
		}
	}

	/**
	 * The color to tint a stage's waypoint pins with, as RRGGBB, or {@code null} to use the built-in
	 * default for that stage, which is its palette entry. Only used when
	 * {@link #isColorWaypointPins()} is set.
	 *
	 * @param index the stage's position in the flight data, counting from zero
	 */
	public Integer getBranchPinColor(int index) {
		return branchPinColors.get(index);
	}

	/** Override a stage's pin color, or pass {@code null} to go back to the default. */
	public void setBranchPinColor(int index, Integer rgb) {
		if (rgb == null) {
			branchPinColors.remove(index);
		} else {
			branchPinColors.put(index, rgb & 0xFFFFFF);
		}
	}

	/** Drop every override of all three kinds, so every stage goes back to its built-in defaults. */
	public void clearBranchColors() {
		branchColors.clear();
		branchGroundColors.clear();
		branchPinColors.clear();
	}

	/**
	 * A name for this flight, put in front of the exported document, folder and track names.
	 *
	 * <p>Without one, two files loaded into the same viewer collide: the rocket's stages are both
	 * called "Sustainer", both tracks are called "Sustainer flight path", and two designs that each
	 * have a "Simulation 1" are indistinguishable at the top of the tree. A mission name makes each
	 * file say which flight it is.
	 *
	 * <p>Empty by default, and not remembered between exports - a stale one silently mislabeling
	 * the next file is worse than typing it again.
	 */
	public String getMissionName() {
		return missionName;
	}

	public void setMissionName(String missionName) {
		this.missionName = (missionName == null) ? "" : missionName.trim();
	}

	/**
	 * Whether the mission name is also put in front of every waypoint name.
	 *
	 * <p>Off by default. A near-vertical flight already packs its markers into a small patch of
	 * screen, which is why their names can be switched off entirely; making every one longer makes
	 * that worse. Worth turning on when the markers of two flights genuinely overlap on the map.
	 */
	public boolean isLabelWaypointsWithMission() {
		return labelWaypointsWithMission;
	}

	public void setLabelWaypointsWithMission(boolean labelWaypointsWithMission) {
		this.labelWaypointsWithMission = labelWaypointsWithMission;
	}

	public boolean isDrawShadow() {
		return drawShadow;
	}

	public void setDrawShadow(boolean drawShadow) {
		this.drawShadow = drawShadow;
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

	/**
	 * Whether each exported feature carries a description: the flight summary on the document, a
	 * stage's range and landing on its folder, and a waypoint's own time, altitude and offset on
	 * its marker. Google Earth shows these in a balloon when the feature is clicked.
	 *
	 * <p>On by default. Worth clearing for a file that is going somewhere the descriptions would
	 * only get in the way, such as a viewer that prints them into the list rather than into a
	 * balloon, or a track being handed to something that reads the geometry and nothing else.
	 */
	public boolean isIncludeDescriptions() {
		return includeDescriptions;
	}

	public void setIncludeDescriptions(boolean includeDescriptions) {
		this.includeDescriptions = includeDescriptions;
	}
}
