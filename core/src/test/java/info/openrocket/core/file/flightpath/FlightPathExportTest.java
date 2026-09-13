package info.openrocket.core.file.flightpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.flightpath.FlightPathExportOptions.AltitudeReference;
import info.openrocket.core.file.flightpath.FlightPathExportOptions.StageTrackStart;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.util.BaseTestCase;

import org.junit.jupiter.api.Test;

public class FlightPathExportTest extends BaseTestCase {

	private static Simulation buildSimulation() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		rocket.setName("LPV2");
		Simulation simulation = new Simulation(document, rocket);
		simulation.setName("KML test");
		simulation.getOptions().setLaunchLatitude(30.6146);
		simulation.getOptions().setLaunchLongitude(-97.4966);
		simulation.getOptions().setLaunchAltitude(200.0);
		return simulation;
	}

	private static FlightData buildFlightData() {
		return buildFlightData(30.6146, -97.4966);
	}

	/**
	 * @param lat0 latitude the flight starts from; the simulation extrapolates every coordinate
	 *             from the launch position, so a fixture must agree with the launch conditions
	 * @param lon0 longitude the flight starts from
	 */
	private static FlightData buildFlightData(double lat0, double lon0) {
		FlightDataBranch branch = new FlightDataBranch("Sustainer",
				FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE,
				FlightDataType.TYPE_LATITUDE, FlightDataType.TYPE_LONGITUDE,
				FlightDataType.TYPE_POSITION_X, FlightDataType.TYPE_POSITION_Y,
				FlightDataType.TYPE_VELOCITY_TOTAL, FlightDataType.TYPE_ACCELERATION_TOTAL);

		// time, altAGL, east, north, vel, accel. The latitude and longitude are derived from the
		// east/north offsets so that the two agree, as they do in a real simulation.
		addPoint(branch, lat0, lon0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		addPoint(branch, lat0, lon0, 1.0, 100.0, 0.0, 0.0, 80.0, 50.0);
		addPoint(branch, lat0, lon0, 2.0, 250.0, 100.0, 0.0, 20.0, -9.0); // apogee, 100 m due east
		addPoint(branch, lat0, lon0, 3.0, 0.0, 120.0, 0.0, 5.0, 0.0);     // landing

		Parachute main = new Parachute();
		main.setName("Main");
		branch.addEvent(new FlightEvent(FlightEvent.Type.APOGEE, 2.0));
		branch.addEvent(new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 2.0, main));
		branch.addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, 3.0));

		return new FlightData(branch);
	}

	/**
	 * Add one data point, given the launch site and the offset from it in meters. The simulated
	 * latitude and longitude are derived from that offset so the two describe the same flight.
	 */
	private static void addPoint(FlightDataBranch branch, double lat0, double lon0,
			double t, double alt, double east, double north, double vel, double acc) {
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, t);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, alt);
		branch.setValue(FlightDataType.TYPE_LATITUDE, latAt(lat0, north));
		branch.setValue(FlightDataType.TYPE_LONGITUDE, lonAt(lat0, lon0, east));
		branch.setValue(FlightDataType.TYPE_POSITION_X, east);
		branch.setValue(FlightDataType.TYPE_POSITION_Y, north);
		branch.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, vel);
		branch.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, acc);
	}

	private static double latAt(double lat0, double north) {
		return lat0 + north / 111320.0;
	}

	private static double lonAt(double lat0, double lon0, double east) {
		return lon0 + east / (111320.0 * Math.cos(Math.toRadians(lat0)));
	}

	private static FlightPathTemplate template(String id) {
		FlightPathTemplateRepository repo = new FlightPathTemplateRepository();
		for (FlightPathTemplate t : repo.getTemplates()) {
			if (t.getId().equals(id)) {
				return t;
			}
		}
		return null;
	}

	private static String render(String templateId, FlightPathExportOptions options) throws Exception {
		FlightPathTemplate template = template(templateId);
		assertNotNull(template, "template " + templateId + " should be available");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new FlightPathExporter(buildSimulation(), buildFlightData(), options).export(template, out);
		return out.toString(StandardCharsets.UTF_8);
	}

	@Test
	public void builtInTemplatesAreDiscovered() {
		List<FlightPathTemplate> templates = new FlightPathTemplateRepository().getTemplates();
		assertTrue(templates.size() >= 3, "expected built-in kml/csv/gpx templates");
		assertNotNull(template("kml"));
		assertNotNull(template("waypoints-csv"));
		assertNotNull(template("gpx"));
	}

	@Test
	public void kmlTemplateProducesLonLatAglCoordinatesWhenReferencedToGround() throws Exception {
		FlightPathExportOptions groundRelative = new FlightPathExportOptions();
		groundRelative.setAltitudeReference(AltitudeReference.GROUND);
		String kml = render("kml", groundRelative);

		assertTrue(kml.contains("<kml xmlns=\"http://www.opengis.net/kml/2.2\">"), kml);
		assertTrue(kml.contains("<name>Apogee</name>"), kml);
		// Apogee sits 250 m above the ground, hung off the terrain.
		assertTrue(kml.contains(",250.0</coordinates>"), kml);
		assertTrue(kml.contains("<altitudeMode>relativeToGround</altitudeMode>"), kml);
		assertTrue(kml.contains("<altitudeMode>clampToGround</altitudeMode>"), kml);
		// The pin says a chute came out, not which one; the component name stays in the model.
		assertTrue(kml.contains("<name>Ejection</name>"), kml);
		assertFalse(kml.contains("<name>Main</name>"), kml);
	}

	/**
	 * OpenRocket's launch altitude defaults to zero, so measuring against sea level draws a flight
	 * from a site that is actually 1200 m up as 1200 m underground, where it simply does not
	 * appear. Heights above the terrain are right whatever the launch altitude says, so that is
	 * the default; true elevations are available for a simulation that carries a real one.
	 */
	@Test
	public void kmlCanBeReferencedToSeaLevelInstead() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setAltitudeReference(AltitudeReference.SEA_LEVEL);
		String kml = render("kml", options);

		// Apogee: 250 above the ground + the 200 m launch altitude = 450 above sea level.
		assertTrue(kml.contains(",450.0</coordinates>"), kml);
		assertTrue(kml.contains("<altitudeMode>absolute</altitudeMode>"), kml);
		assertFalse(kml.contains("<altitudeMode>relativeToGround</altitudeMode>"), kml);
	}

	/** GPX elevations are defined as height above sea level, whatever the KML is referenced to. */
	@Test
	public void gpxAlwaysReportsElevationAboveSeaLevel() throws Exception {
		String gpx = render("gpx", new FlightPathExportOptions());
		assertTrue(gpx.contains("<ele>450.0</ele>"), gpx);
	}

	@Test
	public void kmlHonoursGeometryToggles() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setAltitudeReference(AltitudeReference.GROUND);
		options.setIncludeGroundTrack(false);
		String kml = render("kml", options);

		assertTrue(kml.contains("<altitudeMode>relativeToGround</altitudeMode>"), kml);
		assertFalse(kml.contains("<altitudeMode>clampToGround</altitudeMode>"), kml);
	}

	@Test
	public void waypointCsvMatchesExampleShape() throws Exception {
		String csv = render("waypoints-csv", new FlightPathExportOptions());
		String[] lines = csv.split("\r?\n");

		assertEquals("\"altitude(m)\",\"latitude\",\"longitude\",\"label\",\"symbol\",\"color\",\"label_color\",\"name\"",
				lines[0], csv);
		// A pad row and an apogee row should be present.
		assertTrue(csv.contains("\"pad\""), csv);
		assertTrue(csv.contains("\"apogee\""), csv);
		// Apogee is due east of the pad: bearing 90 deg, distance 100 m.
		assertTrue(csv.contains("@ 90 deg"), csv);
		// The flight never leaves its latitude, so every row carries the launch one.
		assertTrue(csv.contains("\"30.614600\""), csv);
	}

	@Test
	public void summaryMaxValuesArePopulated() {
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildFlightData(), new FlightPathExportOptions()).build();

		// Peak altitude is 250 m AGL; the other maxima just need to be filled in.
		assertTrue(model.maxAltitude.contains("250"), model.maxAltitude);
		assertFalse(model.maxVelocity.isEmpty(), "max velocity should be populated");
		assertFalse(model.maxAcceleration.isEmpty(), "max acceleration should be populated");
	}

	@Test
	public void gpxTemplateProducesWaypointsAndTrack() throws Exception {
		String gpx = render("gpx", new FlightPathExportOptions());

		assertTrue(gpx.contains("<gpx"), gpx);
		assertTrue(gpx.contains("<wpt lat=\"30.6146\""), gpx);
		assertTrue(gpx.contains("<trkpt"), gpx);
	}

	/**
	 * A staged flight produces one branch per stage, each with its own pad, apogee and landing.
	 * Waypoint labels must name the stage, otherwise the export is a pile of indistinguishable
	 * "Apogee"/"Landing" pins. A booster's burnout is recorded in the sustainer's branch too
	 * (they fly as one stack until separation), so burnout is named after the stage whose motor
	 * burned out rather than the branch it appears in.
	 */
	@Test
	public void stagedFlightLabelsWaypointsPerStage() throws Exception {
		FlightPathTemplate template = template("kml");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new FlightPathExporter(buildSimulation(), buildStagedFlightData(), new FlightPathExportOptions())
				.export(template, out);
		String kml = out.toString(StandardCharsets.UTF_8);

		assertTrue(kml.contains("<name>Sustainer Apogee</name>"), kml);
		assertTrue(kml.contains("<name>Booster Apogee</name>"), kml);
		assertTrue(kml.contains("<name>Sustainer Landing</name>"), kml);
		assertTrue(kml.contains("<name>Booster Landing</name>"), kml);

		// Leaving the pad belongs to the whole stack, so it is emitted once and unqualified
		// rather than claimed by either stage.
		assertEquals(1, countOccurrences(kml, "<name>Pad</name>"), kml);
		assertEquals(1, countOccurrences(kml, "<name>Liftoff</name>"), kml);
		assertFalse(kml.contains("Sustainer Pad"), kml);
		assertFalse(kml.contains("Booster Pad"), kml);
		assertFalse(kml.contains("Sustainer Liftoff"), kml);

		// Both burnouts recorded in the sustainer branch are attributed to their own stage.
		assertTrue(kml.contains("<name>Booster Burnout</name>"), kml);
		assertTrue(kml.contains("<name>Sustainer Burnout</name>"), kml);
		assertFalse(kml.contains("<name>Burnout</name>"), kml);

		// Recovery pins are named for the event, not the component, and still take the stage prefix.
		assertTrue(kml.contains("<name>Booster Ejection</name>"), kml);
		assertFalse(kml.contains("Booster Chute"), kml);
	}

	/**
	 * A branch created at separation begins as a verbatim copy of its parent's points, so the
	 * ascent the stages flew together is repeated in every later branch. That prefix must not
	 * be re-exported: it would draw the shared ascent once per stage, and it would report the
	 * whole stack's peak velocity and acceleration as the spent booster's own.
	 */
	@Test
	public void stagedFlightTrimsTheSharedAscentFromLaterStages() {
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), new FlightPathExportOptions()).build();

		assertEquals(2, model.branches.size());
		FlightPathModel.Branch sustainer = model.branches.get(0);
		FlightPathModel.Branch booster = model.branches.get(1);

		// The primary branch records the separation too, but flies the ascent for real.
		assertEquals(0.0, sustainer.path.get(0).time, "sustainer track should start on the pad");
		assertEquals(5, sustainer.path.size());

		// The booster's track picks up where it stopped being part of the stack.
		assertEquals(1.0, booster.path.get(0).time, "booster track should start at separation");
		assertEquals(3, booster.path.size());

		// Peaks are the booster's own, not the stack's 95 m/s and 90 m/s^2 from the copied ascent.
		assertEquals(1.0, waypointOfType(booster, "maxvelocity").time);
		assertEquals(1.0, waypointOfType(booster, "maxacceleration").time);
	}

	/**
	 * The opposite choice: every stage's track runs pad to landing, so each reads as a complete
	 * flight. The peaks follow the track, otherwise a stage would carry a max-velocity pin at an
	 * altitude its own drawn track never passes through.
	 */
	@Test
	public void stageTracksCanStartOnThePadInstead() {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setStageTrackStart(StageTrackStart.PAD);
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), options).build();

		FlightPathModel.Branch booster = model.branches.get(1);
		assertEquals(0.0, booster.path.get(0).time, "booster track should start on the pad");
		assertEquals(5, booster.path.size());

		// The stack's peaks, which this branch now draws the whole of.
		assertEquals(0.5, waypointOfType(booster, "maxvelocity").time);
		assertEquals(0.0, waypointOfType(booster, "maxacceleration").time);
	}

	/**
	 * Waypoint pins are tinted to match their stage's track, so a pin can be attributed at a
	 * glance without reading its label. The white pushpin is used as the base because tinting
	 * multiplies channels: the default yellow pin would turn any blue tint into dark green.
	 */
	@Test
	public void waypointPinsMatchTheirStageColor() throws Exception {
		String kml = stagedKml(new FlightPathExportOptions());

		assertTrue(kml.contains("<Style id=\"waypoint0\">"), kml);
		assertTrue(kml.contains("<Style id=\"waypoint1\">"), kml);
		assertTrue(kml.contains("<color>ffbd7200</color>"), kml);
		assertTrue(kml.contains("<color>ff1953d9</color>"), kml);
		assertTrue(kml.contains("wht-pushpin.png"), kml);

		// Every pin must reference its own branch's style, not a bare "#waypoint".
		assertTrue(kml.contains("<styleUrl>#waypoint0</styleUrl>"), kml);
		assertTrue(kml.contains("<styleUrl>#waypoint1</styleUrl>"), kml);
		assertFalse(kml.contains("<styleUrl>#waypoint</styleUrl>"), kml);
	}

	/**
	 * The simulation already knows whether it can place the flight at a true elevation, so the
	 * default reads that rather than asking. A launch altitude the user set means sea level; the
	 * default of zero means heights above the terrain, which is the only thing that renders.
	 */
	@Test
	public void automaticAltitudeReferenceFollowsTheLaunchAltitude() {
		assertEquals(AltitudeReference.AUTOMATIC, new FlightPathExportOptions().getAltitudeReference());
		assertEquals(AltitudeReference.SEA_LEVEL, AltitudeReference.AUTOMATIC.resolve(1190));
		assertEquals(AltitudeReference.GROUND, AltitudeReference.AUTOMATIC.resolve(0));
		assertEquals(AltitudeReference.GROUND, AltitudeReference.AUTOMATIC.resolve(Double.NaN));

		// An explicit choice is never overridden.
		assertEquals(AltitudeReference.GROUND, AltitudeReference.GROUND.resolve(1190));
		assertEquals(AltitudeReference.SEA_LEVEL, AltitudeReference.SEA_LEVEL.resolve(0));
	}

	/** buildSimulation() launches from 200 m, so the automatic default places it at true elevation. */
	@Test
	public void automaticUsesSeaLevelWhenTheSimulationHasALaunchAltitude() throws Exception {
		String kml = render("kml", new FlightPathExportOptions());

		assertTrue(kml.contains("<altitudeMode>absolute</altitudeMode>"), kml);
		assertTrue(kml.contains(",450.0</coordinates>"), kml);
	}

	/** Pin tinting needs an icon off the network, so it can be turned off. */
	@Test
	public void waypointPinColorsCanBeTurnedOff() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setColorWaypointPins(false);
		String kml = stagedKml(options);

		assertFalse(kml.contains("<IconStyle>"), kml);
		assertFalse(kml.contains("pushpin"), kml);
		// The pins still exist, they just use the viewer's default marker.
		assertTrue(kml.contains("<name>Sustainer Apogee</name>"), kml);
	}

	/** Labels can be switched off, leaving markers that name themselves when clicked. */
	@Test
	public void waypointLabelsCanBeHidden() throws Exception {
		assertFalse(stagedKml(new FlightPathExportOptions()).contains("<LabelStyle>"),
				"labels are shown by default");

		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setShowWaypointLabels(false);
		String kml = stagedKml(options);

		assertTrue(kml.contains("<LabelStyle><scale>0</scale></LabelStyle>"), kml);
		// The names stay in the file so a click still identifies the pin.
		assertTrue(kml.contains("<name>Sustainer Apogee</name>"), kml);
	}

	private static String stagedKml(FlightPathExportOptions options) throws Exception {
		FlightPathTemplate template = template("kml");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new FlightPathExporter(buildSimulation(), buildStagedFlightData(), options).export(template, out);
		return out.toString(StandardCharsets.UTF_8);
	}

	/** Each stage gets its own line color so the tracks can be told apart on the map. */
	@Test
	public void stagesGetDistinctTrackColors() throws Exception {
		FlightPathTemplate template = template("kml");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new FlightPathExporter(buildSimulation(), buildStagedFlightData(), new FlightPathExportOptions())
				.export(template, out);
		String kml = out.toString(StandardCharsets.UTF_8);

		// KML colors are aabbggrr, so 0x0072BD renders as ffbd7200.
		assertTrue(kml.contains("<Style id=\"flightPath0\"><LineStyle><color>ffbd7200</color>"), kml);
		assertTrue(kml.contains("<Style id=\"flightPath1\"><LineStyle><color>ff1953d9</color>"), kml);
		assertTrue(kml.contains("<styleUrl>#flightPath0</styleUrl>"), kml);
		assertTrue(kml.contains("<styleUrl>#flightPath1</styleUrl>"), kml);

		// Ground tracks keep their branch's hue but are darkened hard, so a bright line is always
		// in the air and a dark one is always on the ground.
		assertTrue(kml.contains("<Style id=\"groundTrack0\"><LineStyle><color>d0553300</color>"), kml);
		assertTrue(kml.contains("<Style id=\"groundTrack1\"><LineStyle><color>d00b2561</color>"), kml);
		assertTrue(kml.contains("<styleUrl>#groundTrack1</styleUrl>"), kml);

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), new FlightPathExportOptions()).build();
		assertEquals("0072bd", model.branches.get(0).colorRgb);
		assertEquals("d95319", model.branches.get(1).colorRgb);
	}

	private static FlightPathModel.Waypoint waypointOfType(FlightPathModel.Branch branch, String type) {
		for (FlightPathModel.Waypoint w : branch.waypoints) {
			if (type.equals(w.type)) {
				return w;
			}
		}
		throw new AssertionError("no " + type + " waypoint in branch " + branch.name);
	}

	/**
	 * A simulation with no launch position set has both coordinates at zero, which is OpenRocket's
	 * "not set" rather than a spot in the Gulf of Guinea. The exported file gets substitute
	 * coordinates with the flight's shape intact; the simulation's own launch position is left
	 * exactly as it was.
	 */
	@Test
	public void exportSubstitutesCoordinatesWhenTheSimulationHasNoLaunchPosition() {
		Simulation sim = buildSimulation();
		sim.getOptions().setLaunchLatitude(0);
		sim.getOptions().setLaunchLongitude(0);

		FlightPathModel substituted = new FlightPathModelBuilder(
				sim, buildFlightData(0, 0), new FlightPathExportOptions()).build();

		assertEquals(FlightPathModelBuilder.EXPORT_FALLBACK_LATITUDE, substituted.launchLatitude, 1e-9);
		assertEquals(FlightPathModelBuilder.EXPORT_FALLBACK_LONGITUDE, substituted.launchLongitude, 1e-9);

		FlightPathModel.Waypoint pad = waypointOfType(substituted.branches.get(0), "pad");
		assertEquals(FlightPathModelBuilder.EXPORT_FALLBACK_LATITUDE, pad.latitude, 1e-9);
		assertEquals(FlightPathModelBuilder.EXPORT_FALLBACK_LONGITUDE, pad.longitude, 1e-9);

		// Only the exported position is substituted. Distance and bearing are measured on the
		// ground, so writing the track out half a world away must not change either.
		FlightPathModel inPlace = new FlightPathModelBuilder(
				buildSimulation(), buildFlightData(), new FlightPathExportOptions()).build();
		assertEquals(describeShape(inPlace), describeShape(substituted),
				"substituting coordinates must not change the shape of the flight");

		// And above all: the simulation itself is left alone.
		assertEquals(0.0, sim.getOptions().getLaunchLatitude(), 0.0,
				"the export must not write to the simulation's launch position");
		assertEquals(0.0, sim.getOptions().getLaunchLongitude(), 0.0,
				"the export must not write to the simulation's launch position");
	}

	/** The distance and bearing of every waypoint, which describe the flight independent of where it flew. */
	private static List<String> describeShape(FlightPathModel model) {
		List<String> shape = new ArrayList<>();
		for (FlightPathModel.Branch branch : model.branches) {
			for (FlightPathModel.Waypoint w : branch.waypoints) {
				shape.add(w.type + " " + w.distance + " @ " + w.bearing);
			}
		}
		return shape;
	}

	/**
	 * A simulation loaded from a saved file has had its coordinates rounded to three decimal
	 * places, which in degrees is about 94 m: the whole flight collapses onto two or three
	 * positions and the exported track becomes a staircase of right angles. The distance from the
	 * launch site survives that rounding, being in meters, so the coordinates are rebuilt from it.
	 * A file-loaded simulation counts as up to date and is never re-run, so this is the ordinary
	 * case rather than a corner one.
	 */
	@Test
	public void coordinatesSurviveTheRoundingAppliedToASavedFile() {
		FlightDataBranch branch = newBranch("Sustainer");
		double lat0 = 32.367;
		double lon0 = -106.83;
		for (int i = 0; i <= 20; i++) {
			double east = i * 5.0;   // 0 .. 100 m, far below the 94 m coordinate resolution
			branch.addPoint();
			branch.setValue(FlightDataType.TYPE_TIME, i);
			branch.setValue(FlightDataType.TYPE_ALTITUDE, 10.0 * i);
			// What a saved file gives back: three decimal places of degrees.
			branch.setValue(FlightDataType.TYPE_LATITUDE, round3(latAt(lat0, 0)));
			branch.setValue(FlightDataType.TYPE_LONGITUDE, round3(lonAt(lat0, lon0, east)));
			branch.setValue(FlightDataType.TYPE_POSITION_X, east);
			branch.setValue(FlightDataType.TYPE_POSITION_Y, 0.0);
		}

		Simulation sim = buildSimulation();
		sim.getOptions().setLaunchLatitude(lat0);
		sim.getOptions().setLaunchLongitude(lon0);

		FlightPathModel model = new FlightPathModelBuilder(
				sim, new FlightData(branch), new FlightPathExportOptions()).build();
		List<FlightPathModel.PathPoint> path = model.branches.get(0).path;

		// The rounded coordinates hold at most a couple of distinct longitudes; the rebuilt ones
		// have to distinguish all 21 points.
		Set<Double> rounded = new HashSet<>();
		Set<Double> exported = new HashSet<>();
		for (int i = 0; i < path.size(); i++) {
			rounded.add(round3(lonAt(lat0, lon0, i * 5.0)));
			exported.add(path.get(i).longitude);
		}
		assertTrue(rounded.size() <= 2, "the fixture should be quantized, got " + rounded.size());
		assertEquals(path.size(), exported.size(),
				"every point should have its own longitude, not snap to a 94 m grid");

		// And the track must still be anchored at the launch site and run due east.
		assertEquals(lat0, path.get(0).latitude, 1e-9);
		assertEquals(lon0, path.get(0).longitude, 1e-9);
		for (FlightPathModel.PathPoint point : path) {
			assertEquals(lat0, point.latitude, 1e-9, "the flight never leaves its latitude");
			assertTrue(point.longitude >= lon0, "the flight only ever moves east");
		}
	}

	private static double round3(double value) {
		return Math.round(value * 1000.0) / 1000.0;
	}

	/**
	 * A half-filled position is no more real than an empty one: a latitude with no longitude puts
	 * the flight on the prime meridian, a longitude with no latitude puts it on the equator. Both
	 * look plausible on a map and neither is where the rocket flew, so either coordinate left at
	 * zero makes the export fall back to its substitute coordinates.
	 */
	@Test
	public void aHalfSetLaunchPositionCountsAsUnset() {
		for (double[] position : new double[][] { { 30.6146, 0 }, { 0, -97.4966 }, { 0, 0 } }) {
			Simulation sim = buildSimulation();
			sim.getOptions().setLaunchLatitude(position[0]);
			sim.getOptions().setLaunchLongitude(position[1]);

			FlightPathModel model = new FlightPathModelBuilder(
					sim, buildFlightData(position[0], position[1]), new FlightPathExportOptions()).build();

			String where = position[0] + ", " + position[1];
			assertEquals(FlightPathModelBuilder.EXPORT_FALLBACK_LATITUDE, model.launchLatitude, 1e-9, where);
			assertEquals(FlightPathModelBuilder.EXPORT_FALLBACK_LONGITUDE, model.launchLongitude, 1e-9, where);

			// The warning the user sees has to agree with what the export actually did.
			assertFalse(new FlightPathExporter(sim, buildFlightData(), new FlightPathExportOptions())
					.hasLaunchPosition(), where);
		}
	}

	/** A position with both coordinates set is left alone, and draws no warning. */
	@Test
	public void aFullySetLaunchPositionIsKeptAndNotWarnedAbout() {
		Simulation sim = buildSimulation();
		assertTrue(new FlightPathExporter(sim, buildFlightData(), new FlightPathExportOptions())
				.hasLaunchPosition());

		FlightPathModel model = new FlightPathModelBuilder(
				sim, buildFlightData(), new FlightPathExportOptions()).build();
		assertEquals(30.6146, model.launchLatitude, 1e-9);
		assertEquals(-97.4966, model.launchLongitude, 1e-9);
	}

	/** A simulation that does carry a launch position is exported exactly where it flew. */
	@Test
	public void exportUsesTheSimulationsOwnLaunchPositionWhenItHasOne() {
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildFlightData(), new FlightPathExportOptions()).build();

		FlightPathModel.Waypoint pad = waypointOfType(model.branches.get(0), "pad");
		assertEquals(30.6146, pad.latitude, 1e-9);
		assertEquals(-97.4966, pad.longitude, 1e-9);
	}

	/**
	 * Thinning keeps every Nth point but must never drop the last one, or the track stops short of
	 * the landing. The check is on the offset from the first exported point, not from zero, because
	 * a trimmed stage does not start at index zero.
	 */
	@Test
	public void pathStrideKeepsTheFirstAndLastPoint() {
		for (int stride : new int[] { 1, 2, 3, 100 }) {
			FlightPathExportOptions options = new FlightPathExportOptions();
			options.setPathStride(stride);
			FlightPathModel model = new FlightPathModelBuilder(
					buildSimulation(), buildFlightData(), options).build();

			List<FlightPathModel.PathPoint> path = model.branches.get(0).path;
			assertEquals(0.0, path.get(0).time, "stride " + stride + " should start on the pad");
			assertEquals(3.0, path.get(path.size() - 1).time,
					"stride " + stride + " should still end at landing");
			assertEquals(path.size(), new HashSet<>(path).size(),
					"stride " + stride + " should not repeat the last point");
		}
	}

	/** The same, for a stage whose track is trimmed and so does not begin at index zero. */
	@Test
	public void pathStrideOnATrimmedStageKeepsSeparationAndLanding() {
		for (int stride : new int[] { 1, 2, 3, 100 }) {
			FlightPathExportOptions options = new FlightPathExportOptions();
			options.setPathStride(stride);
			FlightPathModel model = new FlightPathModelBuilder(
					buildSimulation(), buildStagedFlightData(), options).build();

			List<FlightPathModel.PathPoint> path = model.branches.get(1).path;
			assertEquals(1.0, path.get(0).time, "stride " + stride + " should start at separation");
			assertEquals(3.0, path.get(path.size() - 1).time,
					"stride " + stride + " should still end at landing");
			assertEquals(path.size(), new HashSet<>(path).size(),
					"stride " + stride + " should not repeat the last point");
		}
	}

	/** Clearing a waypoint type drops exactly that type and leaves the rest alone. */
	@Test
	public void clearedWaypointTypesAreNotExported() {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setWaypoint(FlightPathExportOptions.Waypoint.PAD, false);
		options.setWaypoint(FlightPathExportOptions.Waypoint.MAX_VELOCITY, false);
		options.setWaypoint(FlightPathExportOptions.Waypoint.MAX_ACCELERATION, false);

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildFlightData(), options).build();

		Set<String> types = new HashSet<>();
		for (FlightPathModel.Waypoint w : model.branches.get(0).waypoints) {
			types.add(w.type);
		}
		assertEquals(Set.of("apogee", "recovery", "landing"), types);
	}

	/**
	 * Stage qualification does not double up a label that already names its stage. Nothing built in
	 * hits this now that ejections are named for the event, but a custom stage name can.
	 */
	@Test
	public void aLabelThatAlreadyNamesItsStageIsNotPrefixedTwice() {
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), new FlightPathExportOptions()).build();

		// The fixture's second stage is called "Booster", so its "Booster Burnout" waypoint is
		// already prefixed and must come through unchanged rather than as "Booster Booster Burnout".
		FlightPathModel.Waypoint burnout = waypointOfType(model.branches.get(1), "burnout");
		assertEquals("Booster Burnout", burnout.qualifiedLabel);
	}

	/** CSV output doubles embedded quotes so a rocket name cannot break the row. */
	@Test
	public void csvSpecialCharactersAreEscaped() throws Exception {
		Simulation sim = buildSimulation();
		sim.getRocket().setName("The \"Big\" One");

		FlightPathTemplate template = template("waypoints-csv");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new FlightPathExporter(sim, buildFlightData(), new FlightPathExportOptions())
				.export(template, out);
		String csv = out.toString(StandardCharsets.UTF_8);

		assertTrue(csv.contains("The \"\"Big\"\" One"), csv);
	}

	/** A single-branch flight keeps bare labels; there is no other stage to confuse it with. */
	@Test
	public void singleStageFlightLeavesLabelsUnqualified() {
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildFlightData(), new FlightPathExportOptions()).build();

		assertEquals(1, model.branches.size());
		for (FlightPathModel.Waypoint w : model.branches.get(0).waypoints) {
			assertEquals(w.label, w.qualifiedLabel, "label " + w.label + " should not be qualified");
			assertEquals("Sustainer", w.branchName);
		}
	}

	/**
	 * Two branches shaped like a real staged flight: the booster branch repeats the shared
	 * ascent before diverging, and the booster's burnout appears in both branches.
	 */
	private static FlightData buildStagedFlightData() {
		AxialStage sustainerStage = new AxialStage();
		sustainerStage.setName("Sustainer");
		BodyTube sustainerBody = new BodyTube();
		sustainerStage.addChild(sustainerBody);
		InnerTube sustainerMount = new InnerTube();
		sustainerBody.addChild(sustainerMount);

		AxialStage boosterStage = new AxialStage();
		boosterStage.setName("Booster");
		BodyTube boosterBody = new BodyTube();
		boosterStage.addChild(boosterBody);
		InnerTube boosterMount = new InnerTube();
		boosterBody.addChild(boosterMount);

		Parachute boosterChute = new Parachute();
		boosterChute.setName("Booster Chute");
		boosterBody.addChild(boosterChute);

		FlightDataBranch sustainer = newBranch("Sustainer");
		addPoint(sustainer, 30.6146, -97.4966, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		addPoint(sustainer, 30.6146, -97.4966, 1.0, 100.0, 10.0, 0.0, 80.0, 50.0);   // booster burnout
		addPoint(sustainer, 30.6146, -97.4966, 2.0, 300.0, 50.0, 0.0, 90.0, 40.0);   // sustainer burnout
		addPoint(sustainer, 30.6146, -97.4966, 3.0, 500.0, 100.0, 0.0, 10.0, -9.0);  // apogee
		addPoint(sustainer, 30.6146, -97.4966, 4.0, 0.0, 150.0, 0.0, 5.0, 0.0);      // landing
		sustainer.addEvent(new FlightEvent(FlightEvent.Type.LIFTOFF, 0.1));
		sustainer.addEvent(new FlightEvent(FlightEvent.Type.BURNOUT, 1.0, boosterMount));
		sustainer.addEvent(new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 1.0, boosterStage));
		sustainer.addEvent(new FlightEvent(FlightEvent.Type.BURNOUT, 2.0, sustainerMount));
		sustainer.addEvent(new FlightEvent(FlightEvent.Type.APOGEE, 3.0));
		sustainer.addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, 4.0));

		// The copied ascent (t < 1.0) carries the stack's peak velocity and acceleration, which
		// the booster only ever reached while bolted to the sustainer.
		FlightDataBranch booster = newBranch("Booster");
		addPoint(booster, 30.6146, -97.4966, 0.0, 0.0, 0.0, 0.0, 0.0, 90.0);
		addPoint(booster, 30.6146, -97.4966, 0.5, 40.0, 4.0, 0.0, 95.0, 60.0);
		addPoint(booster, 30.6146, -97.4966, 1.0, 100.0, 10.0, 0.0, 70.0, 1.0);     // separation
		addPoint(booster, 30.6146, -97.4966, 2.0, 130.0, 20.0, 0.0, 15.0, -9.0);    // apogee
		addPoint(booster, 30.6146, -97.4966, 3.0, 0.0, 40.0, 0.0, 6.0, -2.0);       // landing
		booster.addEvent(new FlightEvent(FlightEvent.Type.LIFTOFF, 0.1));
		booster.addEvent(new FlightEvent(FlightEvent.Type.BURNOUT, 1.0, boosterMount));
		booster.addEvent(new FlightEvent(FlightEvent.Type.STAGE_SEPARATION, 1.0, boosterStage));
		booster.addEvent(new FlightEvent(FlightEvent.Type.APOGEE, 2.0));
		booster.addEvent(new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 2.0, boosterChute));
		booster.addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, 3.0));

		return new FlightData(sustainer, booster);
	}

	private static int countOccurrences(String haystack, String needle) {
		int count = 0;
		for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
			count++;
		}
		return count;
	}

	private static FlightDataBranch newBranch(String name) {
		return new FlightDataBranch(name,
				FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE,
				FlightDataType.TYPE_LATITUDE, FlightDataType.TYPE_LONGITUDE,
				FlightDataType.TYPE_POSITION_X, FlightDataType.TYPE_POSITION_Y,
				FlightDataType.TYPE_VELOCITY_TOTAL, FlightDataType.TYPE_ACCELERATION_TOTAL);
	}

	@Test
	public void xmlSpecialCharactersAreEscaped() throws Exception {
		Simulation sim = buildSimulation();
		sim.setName("Rocket <A> & \"B\"");
		FlightPathTemplate template = template("kml");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new FlightPathExporter(sim, buildFlightData(), new FlightPathExportOptions()).export(template, out);
		String kml = out.toString(StandardCharsets.UTF_8);

		assertTrue(kml.contains("Rocket &lt;A&gt; &amp; &quot;B&quot;"), kml);
	}
}
