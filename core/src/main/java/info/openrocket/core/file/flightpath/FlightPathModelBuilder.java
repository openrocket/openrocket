package info.openrocket.core.file.flightpath;

import java.util.List;
import java.util.Locale;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.flightpath.FlightPathExportOptions.Waypoint;
import info.openrocket.core.l10n.Translator;
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

	private final Simulation simulation;
	private final FlightData data;
	private final FlightPathExportOptions options;

	private final Unit altUnit;
	private final Unit distUnit;
	private final double launchAltitude;

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
	}

	/**
	 * Build the flight-path model from the simulation's flight data.
	 *
	 * @return a populated {@link FlightPathModel} ready to be rendered by a template
	 */
	public FlightPathModel build() {
		FlightPathModel model = new FlightPathModel();

		model.title = simulation.getName();
		model.simulationName = simulation.getName();
		model.rocketName = safe(simulation.getRocket().getName());
		try {
			model.configuration = safe(simulation.getActiveConfiguration().getName());
			model.motor = model.configuration;
		} 
		catch (Exception ignore) {
			// Metadata is best-effort; a missing configuration must not fail the export.
		}
		model.launchLatitude = simulation.getOptions().getLaunchLatitude();
		model.launchLongitude = simulation.getOptions().getLaunchLongitude();
		model.launchAltitudeMeters = launchAltitude;

		model.altitudeUnit = altUnit.getUnit();
		model.distanceUnit = distUnit.getUnit();
		model.includeFlightPath = options.isIncludeFlightPath();
		model.includeGroundTrack = options.isIncludeGroundTrack();

		if (data != null) {
			// Summary values. Altitude uses the chosen altitude unit; velocity and
			// acceleration use their default units since they have no dedicated option.
			model.maxAltitude = altUnit.toString(data.getMaxAltitude());
			model.maxVelocity = UnitGroup.UNITS_VELOCITY.getDefaultUnit().toString(data.getMaxVelocity());
			model.maxAcceleration = UnitGroup.UNITS_ACCELERATION.getDefaultUnit()
					.toString(data.getMaxAcceleration());

			for (FlightDataBranch branch : data.getBranches()) {
				FlightPathModel.Branch b = buildBranch(branch);
				if (b != null) 
					model.branches.add(b);
			}
		}

		return model;
	}

	private FlightPathModel.Branch buildBranch(FlightDataBranch branch) {
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

		FlightPathModel.Branch modelBranch = new FlightPathModel.Branch();
		modelBranch.name = branch.getName();

		Ctx ctx = new Ctx(time, alt, lat, lon, x, y, xy, n);

		if (options.hasWaypoint(Waypoint.PAD))
			modelBranch.waypoints.add(waypoint(ctx, 0, "pad", trans.get("FlightPathExport.waypoint.pad"), null));

		for (FlightEvent event : branch.getEvents())
			addEventWaypoint(modelBranch, ctx, event);

		if (options.hasWaypoint(Waypoint.MAX_VELOCITY) && vel != null && !vel.isEmpty()) {
			int idx = indexOfMax(vel, Math.min(n, vel.size()));
			modelBranch.waypoints.add(waypoint(ctx, idx, "maxvelocity",
					trans.get("FlightPathExport.waypoint.maxVelocity"), null));
		}
		if (options.hasWaypoint(Waypoint.MAX_ACCELERATION) && acc != null && !acc.isEmpty()) {
			int idx = indexOfMax(acc, Math.min(n, acc.size()));
			modelBranch.waypoints.add(waypoint(ctx, idx, "maxacceleration",
					trans.get("FlightPathExport.waypoint.maxAcceleration"), null));
		}

		modelBranch.waypoints.sort((p, q) -> Double.compare(p.time, q.time));

		if (options.isIncludeFlightPath() || options.isIncludeGroundTrack()) {
			int stride = options.getPathStride();
			for (int i = 0; i < n; i += stride)
				modelBranch.path.add(pathPoint(ctx, i));

			// Always include the final point so the track ends at landing.
			if ((n - 1) % stride != 0 && n > 0)
				modelBranch.path.add(pathPoint(ctx, n - 1));
		}

		return modelBranch;
	}

	private void addEventWaypoint(FlightPathModel.Branch modelBranch, Ctx ctx, FlightEvent event) {
		int idx = indexOfTime(ctx.time, event.getTime(), ctx.n);
		switch (event.getType()) {
			case LIFTOFF:
				if (options.hasWaypoint(Waypoint.LIFTOFF))
					modelBranch.waypoints.add(waypoint(ctx, idx, "liftoff", trans.get("FlightPathExport.waypoint.liftoff"), null));
				break;
			case BURNOUT:
				if (options.hasWaypoint(Waypoint.BURNOUT))
					modelBranch.waypoints.add(waypoint(ctx, idx, "burnout", trans.get("FlightPathExport.waypoint.burnout"), null));

				break;
			case APOGEE:
				if (options.hasWaypoint(Waypoint.APOGEE))
					modelBranch.waypoints.add(waypoint(ctx, idx, "apogee", trans.get("FlightPathExport.waypoint.apogee"), null));
				break;
			case RECOVERY_DEVICE_DEPLOYMENT:
				if (options.hasWaypoint(Waypoint.RECOVERY)) {
					RocketComponent source = event.getSource();
					String device = (source != null) ? safe(source.getName()) : "";
					String label = device.isEmpty() ? trans.get("FlightPathExport.waypoint.recovery") : device;
					modelBranch.waypoints.add(waypoint(ctx, idx, "recovery", label, device));
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
		FlightPathModel.Waypoint w = new FlightPathModel.Waypoint();
		w.type = type;
		w.label = label;
		w.device = device == null ? "" : device;

		double altAgl = ctx.alt.get(i);
		w.latitude = ctx.lat.get(i);
		w.longitude = ctx.lon.get(i);
		w.latitudeStr = String.format(Locale.US, "%.6f", w.latitude);
		w.longitudeStr = String.format(Locale.US, "%.6f", w.longitude);
		w.altitudeMslMeters = altAgl + launchAltitude;
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
		p.latitude = ctx.lat.get(i);
		p.longitude = ctx.lon.get(i);
		p.altitudeMslMeters = ctx.alt.get(i) + launchAltitude;
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

		Ctx(List<Double> time, List<Double> alt, List<Double> lat, List<Double> lon,
				List<Double> x, List<Double> y, List<Double> xy, int n) {
			this.time = time;
			this.alt = alt;
			this.lat = lat;
			this.lon = lon;
			this.x = x;
			this.y = y;
			this.xy = xy;
			this.n = n;
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

	private static String safe(String s) {
		return s == null ? "" : s;
	}

	private static int min(int... values) {
		int m = Integer.MAX_VALUE;
		for (int v : values)
			m = Math.min(m, v);

		return m;
	}

	private static int indexOfMax(List<Double> values, int n) {
		int maxIndex = 0;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < n; i++) {
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
