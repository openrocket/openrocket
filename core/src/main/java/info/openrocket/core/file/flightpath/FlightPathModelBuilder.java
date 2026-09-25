package info.openrocket.core.file.flightpath;

import java.util.List;
import java.util.Locale;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.flightpath.FlightPathExportOptions.Waypoint;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;

/**
 * Builds a {@link FlightPathModel} from a simulation's flight data. The latitude and
 * longitude come straight from the simulated flight (OpenRocket extrapolates them from the
 * launch position during the run); this class adds unit conversion, per-waypoint distance
 * and bearing from the pad, and the metadata a template needs.
 */
public class FlightPathModelBuilder {

	private static final Translator trans = Application.getTranslator();

	/**
	 * Per-branch track colors, so the stages of a staged flight can be told apart. These are the
	 * same values the plot window assigns to its series, so a stage keeps its color whether you
	 * look at it in a graph or on a map.
	 */
	private static final int[] BRANCH_COLORS = {
			0x0072BD, 0xD95319, 0xEDB120, 0x7E318E, 0x77AC30,
			0x4DBEEE, 0xA2142F, 0xC56A7A, 0xFF7F50, 0x556B2F,
	};

	/**
	 * The palette color a stage gets when nothing overrides it. Exposed so a chooser can show what
	 * it would export before the user changes anything.
	 *
	 * @param index the stage's position in the flight data, counting from zero
	 */
	public static int defaultBranchColor(int index) {
		return BRANCH_COLORS[Math.floorMod(index, BRANCH_COLORS.length)];
	}

	/**
	 * Per-branch ground-track colors. A separate palette from {@link #BRANCH_COLORS} rather than a
	 * shade of it: seen from straight overhead a ground track sits directly under its flight path,
	 * so entry <em>i</em> here is picked to contrast with entry <em>i</em> there. They are also
	 * saturated enough to hold up over aerial imagery, which is what a ground track is read against.
	 */
	private static final int[] GROUND_COLORS = {
			0xFF2D55, 0x00B3A4, 0x8E44AD, 0x2ECC40, 0xE01B84,
			0xD35400, 0x1ABC9C, 0x2E86C1, 0x27AE60, 0xE74C3C,
	};

	/**
	 * The ground-track color a stage gets when nothing overrides it, from a palette of its own.
	 *
	 * @param index the stage's position in the flight data, counting from zero
	 */
	public static int defaultGroundColor(int index) {
		return GROUND_COLORS[Math.floorMod(index, GROUND_COLORS.length)];
	}

	/**
	 * The waypoint-pin color a stage gets when nothing overrides it: its palette entry.
	 *
	 * @param index the stage's position in the flight data, counting from zero
	 */
	public static int defaultPinColor(int index) {
		return defaultBranchColor(index);
	}

	/**
	 * Coordinates written into the exported file when the simulation carries no launch position:
	 * the Kennedy Space Center. Both coordinates at zero is OpenRocket's "not set" rather than a
	 * real position in the Gulf of Guinea, and dropping a flight on Null Island tells the reader
	 * nothing.
	 *
	 * <p>These are used for the exported coordinates only. Nothing here writes to the simulation,
	 * and its launch position is left exactly as the user set it.
	 */
	public static final double EXPORT_FALLBACK_LATITUDE = 28.61;
	public static final double EXPORT_FALLBACK_LONGITUDE = -80.6;

	private final Simulation simulation;
	private final FlightData data;
	private final FlightPathExportOptions options;

	private final Unit altUnit;
	private final Unit distUnit;
	private final double launchAltitude;

	/**
	 * The latitude the exported track is anchored at: the simulation's launch latitude, or
	 * {@link #EXPORT_FALLBACK_LATITUDE} when it has none. Read from the simulation, never written
	 * back to it.
	 */
	private final double originLatitude;
	/** The longitude the exported track is anchored at. See {@link #originLatitude}. */
	private final double originLongitude;
	/** Meters per degree of latitude, and of longitude at {@link #originLatitude}. */
	private final double metersPerDegreeLatitude;
	private final double metersPerDegreeLongitude;

	/**
	 * Whether waypoint labels are qualified with the stage they belong to. Only true for a
	 * staged flight, where every branch otherwise contributes an identically named "Apogee",
	 * "Burnout" and "Landing" and the reader cannot tell the stages apart.
	 */
	private boolean qualifyLabels;

	/**
	 * @param simulation the simulation whose launch position and metadata are used
	 * @param data       the flight data to export
	 * @param options    the export options (units, waypoints, path sampling)
	 */
	public FlightPathModelBuilder(Simulation simulation, FlightData data, FlightPathExportOptions options) {
		this.simulation = simulation;
		this.data = data;
		this.options = options;
		this.altUnit = options.getAltitudeUnit();
		this.distUnit = options.getDistanceUnit();
		this.launchAltitude = simulation.getOptions().getLaunchAltitude();

		double launchLat = simulation.getOptions().getLaunchLatitude();
		double launchLon = simulation.getOptions().getLaunchLongitude();
		// Both coordinates at zero is OpenRocket's "not set". A single zero is a real coordinate --
		// the equator, or the prime meridian -- and a launch site on one of them is exported where
		// the user put it. This only chooses what coordinates to write out; the simulation's own
		// launch position is untouched either way.
		boolean unset = (launchLat == 0 && launchLon == 0);
		this.originLatitude = unset ? EXPORT_FALLBACK_LATITUDE : launchLat;
		this.originLongitude = unset ? EXPORT_FALLBACK_LONGITUDE : launchLon;

		// WGS84 degree lengths at that latitude, good to a few centimeters per kilometer.
		double phi = Math.toRadians(originLatitude);
		this.metersPerDegreeLatitude = 111132.92 - 559.82 * Math.cos(2 * phi) + 1.175 * Math.cos(4 * phi);
		this.metersPerDegreeLongitude = 111412.84 * Math.cos(phi) - 93.5 * Math.cos(3 * phi);
	}

	/** The exported latitude for a point the given distance north of the launch site. */
	private double latitudeFromNorth(double north) {
		return originLatitude + north / metersPerDegreeLatitude;
	}

	/** The exported longitude for a point the given distance east of the launch site. */
	private double longitudeFromEast(double east) {
		return originLongitude + east / metersPerDegreeLongitude;
	}

	/**
	 * Build the flight-path model from the simulation's flight data.
	 *
	 * @return a populated {@link FlightPathModel} ready to be rendered by a template
	 */
	public FlightPathModel build() {
		FlightPathModel model = new FlightPathModel();

		model.missionName = options.getMissionName();
		model.title = withMission(simulation.getName());
		model.simulationName = simulation.getName();
		model.rocketName = safe(simulation.getRocket().getName());
		try {
			model.configuration = safe(simulation.getActiveConfiguration().getName());
			model.motor = model.configuration;
		} 
		catch (Exception ignore) {
			// Metadata is best-effort; a missing configuration must not fail the export.
		}
		model.launchLatitude = originLatitude;
		model.launchLongitude = originLongitude;
		model.launchLatitudeStr = degrees(originLatitude);
		model.launchLongitudeStr = degrees(originLongitude);
		model.launchAltitudeMeters = launchAltitude;

		model.altitudeUnit = altUnit.getUnit();
		model.distanceUnit = distUnit.getUnit();
		model.velocityUnit = UnitGroup.UNITS_VELOCITY.getDefaultUnit().getUnit();
		model.accelerationUnit = UnitGroup.UNITS_ACCELERATION.getDefaultUnit().getUnit();
		model.includeFlightPath = options.isIncludeFlightPath();
		model.includeGroundTrack = options.isIncludeGroundTrack();
		model.kmlAltitudeMode = altitudeReference().getKmlAltitudeMode();
		model.kmlWaypointAltitudeMode = waypointAltitudeReference().getKmlAltitudeMode();
		// Nothing to extrude to once the geometry is already lying on the ground.
		model.extrudePath = options.isDrawShadow()
				&& altitudeReference() != FlightPathExportOptions.AltitudeReference.CLAMPED;
		model.extrudeWaypoints = options.isDrawShadow()
				&& waypointAltitudeReference() != FlightPathExportOptions.AltitudeReference.CLAMPED;
		model.tessellatePath = altitudeReference() == FlightPathExportOptions.AltitudeReference.CLAMPED;
		model.showWaypointLabels = options.isShowWaypointLabels();
		model.colorWaypointPins = options.isColorWaypointPins();
		model.includeDescriptions = options.isIncludeDescriptions();

		if (data != null) {
			// Summary values. Altitude uses the chosen altitude unit; velocity and
			// acceleration use their default units since they have no dedicated option.
			model.maxAltitude = altUnit.toString(data.getMaxAltitude());
			model.maxVelocity = UnitGroup.UNITS_VELOCITY.getDefaultUnit().toString(data.getMaxVelocity());
			model.maxAcceleration = UnitGroup.UNITS_ACCELERATION.getDefaultUnit()
					.toString(data.getMaxAcceleration());
			model.timeToApogee = seconds(data.getTimeToApogee());
			model.flightTime = seconds(data.getFlightTime());

			qualifyLabels = data.getBranchCount() > 1;

			boolean primary = true;
			for (FlightDataBranch branch : data.getBranches()) {
				FlightPathModel.Branch b = buildBranch(branch, primary);
				if (b != null) {
					b.index = model.branches.size();
					// Each of the three is a color in its own right: it is either the one the user
					// picked or this stage's default, and nothing is computed from the others.
					int rgb = color(options.getBranchColor(b.index), defaultBranchColor(b.index));
					int groundRgb = color(options.getBranchGroundColor(b.index), defaultGroundColor(b.index));
					int pinRgb = color(options.getBranchPinColor(b.index), defaultPinColor(b.index));

					b.colorRgb = String.format(Locale.US, "%06x", rgb);
					b.groundColorRgb = String.format(Locale.US, "%06x", groundRgb);
					b.pinColorRgb = String.format(Locale.US, "%06x", pinRgb);
					b.pathColorKml = kmlColor(rgb, 0xFF);
					b.groundColorKml = kmlColor(groundRgb, 0xFF);
					b.pinColorKml = kmlColor(pinRgb, 0xFF);

					model.branches.add(b);
					primary = false;
				}
			}

			double maxRange = 0;
			for (FlightPathModel.Branch b : model.branches)
				maxRange = Math.max(maxRange, b.maxRangeMeters);
			model.maxRange = distUnit.toString(maxRange);
		}

		return model;
	}

	/**
	 * @param primary whether this is the branch the flight started on. The stages fly as one
	 *                stack until separation, and every later branch repeats that shared ascent,
	 *                so the pad and liftoff waypoints are emitted on the primary branch only.
	 */
	private FlightPathModel.Branch buildBranch(FlightDataBranch branch, boolean primary) {
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);
		List<Double> alt = branch.get(FlightDataType.TYPE_ALTITUDE);
		List<Double> lat = branch.get(FlightDataType.TYPE_LATITUDE);
		List<Double> lon = branch.get(FlightDataType.TYPE_LONGITUDE);
		if (time == null || alt == null || lat == null || lon == null || lat.isEmpty())
			return null;

		List<Double> x = branch.get(FlightDataType.TYPE_POSITION_X);
		List<Double> y = branch.get(FlightDataType.TYPE_POSITION_Y);
		List<Double> xy = branch.get(FlightDataType.TYPE_POSITION_XY);
		List<Double> vel = branch.get(FlightDataType.TYPE_VELOCITY_TOTAL);
		List<Double> acc = branch.get(FlightDataType.TYPE_ACCELERATION_TOTAL);

		final int n = min(time.size(), alt.size(), lat.size(), lon.size());
		final int start = startIndex(branch, time, n, primary);

		FlightPathModel.Branch modelBranch = new FlightPathModel.Branch();
		// The stage's own name qualifies the waypoint labels, while the branch's display name also
		// carries the mission. Keeping them apart is what lets the mission stay off the markers.
		String stageName = branch.getName();
		modelBranch.name = withMission(stageName);

		Ctx ctx = new Ctx(time, alt, lat, lon, x, y, xy, n, stageName, primary);

		// Leaving the pad is something the whole vehicle does, not any one stage, so it is not
		// qualified with a stage name the way the per-stage waypoints are.
		if (primary && options.hasWaypoint(Waypoint.PAD))
			modelBranch.waypoints.add(waypoint(ctx, 0, "pad", trans.get("FlightPathExport.waypoint.pad"), null, null));

		for (FlightEvent event : branch.getEvents())
			addEventWaypoint(modelBranch, ctx, event);

		// Scanned from the separation point so a spent booster reports its own peaks. Scanning
		// the copied ascent instead would label the whole stack's maxima as the booster's, at
		// altitudes it reached while still bolted to the sustainer.
		if (options.hasWaypoint(Waypoint.MAX_VELOCITY) && vel != null && !vel.isEmpty()) {
			int idx = indexOfMax(vel, start, Math.min(n, vel.size()));
			modelBranch.waypoints.add(waypoint(ctx, idx, "maxvelocity",
					trans.get("FlightPathExport.waypoint.maxVelocity"), null));
		}
		if (options.hasWaypoint(Waypoint.MAX_ACCELERATION) && acc != null && !acc.isEmpty()) {
			int idx = indexOfMax(acc, start, Math.min(n, acc.size()));
			modelBranch.waypoints.add(waypoint(ctx, idx, "maxacceleration",
					trans.get("FlightPathExport.waypoint.maxAcceleration"), null));
		}

		modelBranch.waypoints.sort((p, q) -> Double.compare(p.time, q.time));

		// The range is scanned over the whole branch, shared ascent included: the stack's excursion
		// counts against every stage that was part of it, and the summary is a safety figure.
		double maxRange = 0;
		for (int i = 0; i < n; i++)
			maxRange = Math.max(maxRange, ctx.distance(i));
		modelBranch.maxRangeMeters = maxRange;
		modelBranch.maxRange = distUnit.toString(maxRange);

		// Where the stage came down is recorded whether or not a landing pin was asked for, since
		// the summary wants it either way.
		for (FlightEvent event : branch.getEvents()) {
			if (event.getType() != FlightEvent.Type.GROUND_HIT)
				continue;
			int idx = indexOfTime(time, event.getTime(), n);
			modelBranch.hasLanding = true;
			modelBranch.landingDistance = distUnit.toString(ctx.distance(idx));
			modelBranch.landingBearing = String.format(Locale.US, "%.0f", ctx.bearing(idx));
			modelBranch.landingLatitude = degrees(latitude(ctx, idx));
			modelBranch.landingLongitude = degrees(longitude(ctx, idx));
			modelBranch.landingTime = seconds(time.get(idx));
			break;
		}

		if (options.isIncludeFlightPath() || options.isIncludeGroundTrack()) {
			int stride = options.getPathStride();
			for (int i = start; i < n; i += stride)
				modelBranch.path.add(pathPoint(ctx, i));

			// Always include the final point so the track ends at landing.
			if ((n - 1 - start) % stride != 0 && n > start)
				modelBranch.path.add(pathPoint(ctx, n - 1));
		}

		return modelBranch;
	}

	private void addEventWaypoint(FlightPathModel.Branch modelBranch, Ctx ctx, FlightEvent event) {
		int idx = indexOfTime(ctx.time, event.getTime(), ctx.n);
		switch (event.getType()) {
			case LIFTOFF:
				// As with the pad: the stack lifts off as a whole. OpenRocket only records the
				// event on the primary branch anyway, and naming it after the branch would claim
				// the sustainer left the pad under its own power.
				if (ctx.primary && options.hasWaypoint(Waypoint.LIFTOFF))
					modelBranch.waypoints.add(waypoint(ctx, idx, "liftoff",
							trans.get("FlightPathExport.waypoint.liftoff"), null, null));
				break;
			case BURNOUT:
				if (options.hasWaypoint(Waypoint.BURNOUT)) {
					// Until separation the stages fly as one stack, so a booster's burnout is
					// recorded in the sustainer's branch as well. Qualify it with the stage that
					// actually burned out rather than the branch, otherwise the sustainer ends up
					// with two waypoints both named after the sustainer.
					String stage = stageName(event.getSource());
					modelBranch.waypoints.add(waypoint(ctx, idx, "burnout",
							trans.get("FlightPathExport.waypoint.burnout"), null,
							stage != null ? stage : ctx.branchName));
				}
				break;
			case APOGEE:
				if (options.hasWaypoint(Waypoint.APOGEE))
					modelBranch.waypoints.add(waypoint(ctx, idx, "apogee", trans.get("FlightPathExport.waypoint.apogee"), null));
				break;
			case RECOVERY_DEVICE_DEPLOYMENT:
				if (options.hasWaypoint(Waypoint.RECOVERY)) {
					// Named for the event, like every other pin, but qualified with the device that
					// deployed. A dual-deployment flight sets off two of these hundreds of meters
					// apart, and without the device both markers read "Ejection" and cannot be told
					// apart on the map without clicking each one.
					RocketComponent source = event.getSource();
					String device = (source != null) ? safe(source.getName()) : "";
					modelBranch.waypoints.add(waypoint(ctx, idx, "recovery",
							prefix(device, trans.get("FlightPathExport.waypoint.recovery")), device));
				}
				break;
			case GROUND_HIT:
				if (options.hasWaypoint(Waypoint.LANDING))
					modelBranch.waypoints.add(waypoint(ctx, idx, "landing", trans.get("FlightPathExport.waypoint.landing"), null));
				break;
			default:
				break;
		}
	}

	private FlightPathModel.Waypoint waypoint(Ctx ctx, int i, String type, String label, String device) {
		return waypoint(ctx, i, type, label, device, ctx.branchName);
	}

	/**
	 * @param qualifier the stage name to prefix the label with on a staged flight; usually the
	 *                  branch name, but see the burnout case in
	 *                  {@link #addEventWaypoint(FlightPathModel.Branch, Ctx, FlightEvent)}
	 */
	private FlightPathModel.Waypoint waypoint(Ctx ctx, int i, String type, String label, String device,
			String qualifier) {
		FlightPathModel.Waypoint w = new FlightPathModel.Waypoint();
		w.type = type;
		w.label = label;
		w.branchName = safe(ctx.branchName);
		String qualified = qualify(qualifier, label);
		w.qualifiedLabel = options.isLabelWaypointsWithMission() ? withMission(qualified) : qualified;
		w.device = device == null ? "" : device;

		double altAgl = ctx.alt.get(i);
		w.latitude = latitude(ctx, i);
		w.longitude = longitude(ctx, i);
		w.latitudeStr = String.format(Locale.US, "%.6f", w.latitude);
		w.longitudeStr = String.format(Locale.US, "%.6f", w.longitude);
		w.altitudeMslMeters = altAgl + launchAltitude;
		w.altitudeAglMeters = altAgl;
		w.altitudeKmlMeters = kmlAltitude(altAgl, waypointAltitudeReference());
		w.time = ctx.time.get(i);
		w.timeStr = String.format(Locale.US, "%.2f", w.time);

		w.altitude = altUnit.toString(altAgl);
		w.altitudeMsl = altUnit.toString(w.altitudeMslMeters);

		double distSI = ctx.distance(i);
		w.distance = distUnit.toString(distSI);
		w.bearing = String.format(Locale.US, "%.0f", ctx.bearing(i));
		return w;
	}

	private FlightPathModel.PathPoint pathPoint(Ctx ctx, int i) {
		FlightPathModel.PathPoint p = new FlightPathModel.PathPoint();
		p.latitude = latitude(ctx, i);
		p.longitude = longitude(ctx, i);
		p.altitudeMslMeters = ctx.alt.get(i) + launchAltitude;
		p.altitudeAglMeters = ctx.alt.get(i);
		p.altitudeKmlMeters = kmlAltitude(ctx.alt.get(i), altitudeReference());
		p.time = ctx.time.get(i);
		p.timeStr = String.format(Locale.US, "%.2f", p.time);
		p.altitude = altUnit.toString(ctx.alt.get(i));
		return p;
	}

	/**
	 * Bundles the per-branch value lists so helpers can compute distance/bearing. This is a
	 * short-lived read-only view over the (mutable) flight-data lists, not owned data, so it
	 * is a plain class rather than a record.
	 */
	private static final class Ctx {
		final List<Double> time, alt, lat, lon, x, y, xy;
		final int n;
		final String branchName;
		final boolean primary;

		Ctx(List<Double> time, List<Double> alt, List<Double> lat, List<Double> lon,
				List<Double> x, List<Double> y, List<Double> xy, int n, String branchName,
				boolean primary) {
			this.time = time;
			this.alt = alt;
			this.lat = lat;
			this.lon = lon;
			this.x = x;
			this.y = y;
			this.xy = xy;
			this.n = n;
			this.branchName = branchName;
			this.primary = primary;
		}

		/** Meters east of the launch site, or null when the branch carries no position data. */
		Double east(int i) {
			return (x != null && i < x.size()) ? x.get(i) : null;
		}

		/** Meters north of the launch site, or null when the branch carries no position data. */
		Double north(int i) {
			return (y != null && i < y.size()) ? y.get(i) : null;
		}

		double distance(int i) {
			if (x != null && y != null && i < x.size() && i < y.size())
				return Math.hypot(x.get(i), y.get(i));

			if (xy != null && i < xy.size())
				return xy.get(i);

			return 0;
		}

		double bearing(int i) {
			if (x != null && y != null && i < x.size() && i < y.size()) {
				double deg = Math.toDegrees(Math.atan2(x.get(i), y.get(i)));
				return (deg + 360) % 360;
			}
			return 0;
		}
	}

	/**
	 * Prefix a waypoint label with the stage it belongs to, so a staged flight does not export
	 * several identically named "Apogee" / "Burnout" / "Landing" waypoints. Labels that already
	 * name the stage (recovery devices are typically called "Booster Chute", "Sustainer Main")
	 * are left alone rather than doubled up.
	 */
	/**
	 * Put the mission name in front of something, unless there is no mission or the text already
	 * begins with it. The second case matters because a mission named after the rocket would
	 * otherwise read "Sod Blaster Sod Blaster Sustainer".
	 */
	private String withMission(String text) {
		String mission = options.getMissionName();
		if (mission == null || mission.isEmpty() || text == null) {
			return safe(text);
		}
		if (text.toLowerCase(Locale.ROOT).startsWith(mission.toLowerCase(Locale.ROOT))) {
			return text;
		}
		return mission + " " + text;
	}

	private String qualify(String qualifier, String label) {
		return qualifyLabels ? prefix(qualifier, label) : safe(label);
	}

	/**
	 * Put a qualifier in front of a label, unless there is no qualifier or the label already
	 * begins with it. That last case is what keeps a booster's "Booster Chute" from becoming
	 * "Booster Booster Chute Ejection" once the stage name is applied as well.
	 *
	 * <p>The qualifier keeps the label's own capitalization rather than lowercasing it, which
	 * would be wrong in languages that capitalize the noun.
	 */
	private static String prefix(String qualifier, String label) {
		if (qualifier == null || qualifier.isEmpty() || label == null)
			return safe(label);

		if (label.toLowerCase(Locale.ROOT).startsWith(qualifier.toLowerCase(Locale.ROOT)))
			return label;

		return qualifier + " " + label;
	}

	/**
	 * The name of the stage a flight event's source component belongs to, or {@code null} when
	 * the event has no source or the source is the rocket itself.
	 */
	private static String stageName(RocketComponent component) {
		if (component == null || component instanceof Rocket)
			return null;

		AxialStage stage = (component instanceof AxialStage) ? (AxialStage) component : component.getStage();
		return (stage == null || stage.getName().isEmpty()) ? null : stage.getName();
	}

	/**
	 * Format an RRGGBB color as a KML {@code <color>} literal, which is aabbggrr: alpha first
	 * and the channels in the opposite order to the usual web notation.
	 */
	private static String kmlColor(int rgb, int alpha) {
		return String.format(Locale.US, "%02x%02x%02x%02x",
				alpha & 0xFF, rgb & 0xFF, (rgb >> 8) & 0xFF, (rgb >> 16) & 0xFF);
	}

	/**
	 * The exported latitude of a data point.
	 *
	 * <p>This prefers the distance north of the launch site over the simulated latitude, because a
	 * simulation loaded from a saved file has had its coordinates rounded to three decimal places.
	 * In degrees that is about 94 m, so the whole flight collapses onto two or three positions and
	 * the track comes out as a staircase of right angles. The same rounding applied to a distance
	 * in meters leaves it accurate to a millimeter. A file-loaded simulation counts as up to date
	 * and is never re-run, so this is the ordinary case, not a corner one.
	 */
	private double latitude(Ctx ctx, int i) {
		Double north = ctx.north(i);
		return (north != null) ? latitudeFromNorth(north) : ctx.lat.get(i) + originLatitude
				- simulation.getOptions().getLaunchLatitude();
	}

	/** The exported longitude of a data point. See {@link #latitude(Ctx, int)}. */
	private double longitude(Ctx ctx, int i) {
		Double east = ctx.east(i);
		if (east != null) 
			return longitudeFromEast(east);

		// Without a position to work from, fall back to the simulated coordinate, rescaled when the
		// export is anchored at a different latitude than the simulation flew from.
		double launchLon = simulation.getOptions().getLaunchLongitude();
		double scale = Math.cos(Math.toRadians(simulation.getOptions().getLaunchLatitude()))
				/ Math.cos(Math.toRadians(originLatitude));
		return originLongitude + (ctx.lon.get(i) - launchLon) * scale;
	}

	/**
	 * The altitude a KML coordinate should carry, for the given reference. Hanging geometry off the
	 * terrain is the default because OpenRocket's launch altitude defaults to zero: measured
	 * against sea level, a flight from a 1200 m site is drawn 1200 m underground and simply does
	 * not appear.
	 *
	 * <p>A clamped coordinate's altitude is ignored by KML, but writing the height above the ground
	 * rather than a bare zero keeps the number meaningful to anything else that reads the file.
	 */
	private double kmlAltitude(double altAgl, FlightPathExportOptions.AltitudeReference reference) {
		return (reference == FlightPathExportOptions.AltitudeReference.SEA_LEVEL)
				? altAgl + launchAltitude
				: altAgl;
	}

	/**
	 * The altitude reference the track is exported in, with {@code AUTOMATIC} resolved against this
	 * simulation's launch altitude.
	 */
	private FlightPathExportOptions.AltitudeReference altitudeReference() {
		return options.getAltitudeReference().resolve(launchAltitude);
	}

	/** The altitude reference the waypoints are exported in. See {@link #altitudeReference()}. */
	private FlightPathExportOptions.AltitudeReference waypointAltitudeReference() {
		return options.getWaypointAltitudeReference().resolve(launchAltitude);
	}

	/** The color the user picked, or this stage's default when they have not picked one. */
	private static int color(Integer chosen, int fallback) {
		return (chosen != null) ? chosen : fallback;
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}

	/** A coordinate at a fixed six decimal places, which is about a tenth of a meter. */
	private static String degrees(double value) {
		return String.format(Locale.US, "%.6f", value);
	}

	/** A time in seconds to one decimal, or empty for a value the flight never reached. */
	private static String seconds(double t) {
		return Double.isNaN(t) ? "" : String.format(Locale.US, "%.1f", t);
	}

	private static int min(int... values) {
		int m = Integer.MAX_VALUE;
		for (int v : values)
			m = Math.min(m, v);

		return m;
	}

	/**
	 * The first data index that belongs to this branch alone. A branch created at stage
	 * separation starts life as a verbatim copy of its parent's points, so every non-primary
	 * branch repeats the ascent the stages flew together. Exporting that prefix again would
	 * draw the shared ascent once per stage and attribute the stack's flight to a stage that
	 * was not yet flying on its own.
	 *
	 * @return the index of the separation point, or 0 for the primary branch, for any branch
	 *         with no recorded separation, and when the user asked for every stage's track to
	 *         start on the pad
	 */
	private int startIndex(FlightDataBranch branch, List<Double> time, int n, boolean primary) {
		if (primary || options.getStageTrackStart() == FlightPathExportOptions.StageTrackStart.PAD)
			return 0;

		double separation = branch.getSeparationTime();
		if (Double.isNaN(separation))
			return 0;

		int idx = indexOfTime(time, separation, n);
		return (idx > 0 && idx < n) ? idx : 0;
	}

	private static int indexOfMax(List<Double> values, int from, int n) {
		int maxIndex = from;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = from; i < n; i++) {
			double v = values.get(i);
			if (!Double.isNaN(v) && v > max) {
				max = v;
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	private static int indexOfTime(List<Double> time, double t, int n) {
		int best = 0;
		double bestDiff = Double.POSITIVE_INFINITY;
		final int limit = Math.min(n, time.size());
		for (int i = 0; i < limit; i++) {
			double diff = Math.abs(time.get(i) - t);
			if (diff < bestDiff) {
				bestDiff = diff;
				best = i;
			}
		}
		return best;
	}
}
