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

	private Unit altitudeUnit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
	private Unit distanceUnit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();

	private final Set<Waypoint> waypoints = EnumSet.allOf(Waypoint.class);

	private boolean includeFlightPath = true;
	private boolean includeGroundTrack = true;
	/** Keep every Nth flight-path point (1 = keep all). */
	private int pathStride = 1;

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
}
