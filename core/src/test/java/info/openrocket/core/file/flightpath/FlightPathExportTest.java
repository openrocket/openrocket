package info.openrocket.core.file.flightpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
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

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

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
		// Both halves, since the track's reference and the pins' are set separately.
		FlightPathExportOptions groundRelative = new FlightPathExportOptions();
		groundRelative.setAltitudeReference(AltitudeReference.GROUND);
		groundRelative.setWaypointAltitudeReference(AltitudeReference.GROUND);
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
	public void summaryIncludesTimingRangeAndLanding() {
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildFlightData(), new FlightPathExportOptions()).build();

		assertEquals("2.0", model.timeToApogee);
		assertEquals("3.0", model.flightTime);
		// The fixture drifts to 120 m east by landing, and that is also the farthest it gets.
		assertTrue(model.maxRange.startsWith("120"), model.maxRange);

		FlightPathModel.Branch branch = model.branches.get(0);
		assertTrue(branch.hasLanding);
		assertEquals(120.0, branch.maxRangeMeters, 1e-9);
		assertTrue(branch.landingDistance.startsWith("120"), branch.landingDistance);
		assertEquals("90", branch.landingBearing);
		assertEquals("3.0", branch.landingTime);
	}

	/**
	 * The farthest point from the pad is not necessarily where the rocket lands: it can drift out
	 * under the chute and back again.
	 */
	@Test
	public void maxRangeIsTheFarthestPointNotTheLandingPoint() {
		FlightDataBranch branch = new FlightDataBranch("Sustainer",
				FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE,
				FlightDataType.TYPE_LATITUDE, FlightDataType.TYPE_LONGITUDE,
				FlightDataType.TYPE_POSITION_X, FlightDataType.TYPE_POSITION_Y,
				FlightDataType.TYPE_VELOCITY_TOTAL, FlightDataType.TYPE_ACCELERATION_TOTAL);
		addPoint(branch, 30.6146, -97.4966, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		addPoint(branch, 30.6146, -97.4966, 2.0, 250.0, 0.0, 300.0, 20.0, -9.0); // 300 m north
		addPoint(branch, 30.6146, -97.4966, 4.0, 0.0, 0.0, 50.0, 5.0, 0.0);      // back to 50 m
		branch.addEvent(new FlightEvent(FlightEvent.Type.APOGEE, 2.0));
		branch.addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, 4.0));

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), new FlightData(branch), new FlightPathExportOptions()).build();

		assertTrue(model.maxRange.startsWith("300"), model.maxRange);
		assertTrue(model.branches.get(0).landingDistance.startsWith("50"), model.branches.get(0).landingDistance);
		assertEquals("0", model.branches.get(0).landingBearing);
	}

	/** A simulation that hit its time limit never came down, and the summary must not pretend it did. */
	@Test
	public void aFlightCutShortHasNoLanding() throws Exception {
		FlightDataBranch branch = new FlightDataBranch("Sustainer",
				FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE,
				FlightDataType.TYPE_LATITUDE, FlightDataType.TYPE_LONGITUDE,
				FlightDataType.TYPE_POSITION_X, FlightDataType.TYPE_POSITION_Y,
				FlightDataType.TYPE_VELOCITY_TOTAL, FlightDataType.TYPE_ACCELERATION_TOTAL);
		addPoint(branch, 30.6146, -97.4966, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		addPoint(branch, 30.6146, -97.4966, 1.0, 100.0, 10.0, 0.0, 80.0, 50.0);

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), new FlightData(branch), new FlightPathExportOptions()).build();
		FlightPathModel.Branch b = model.branches.get(0);
		assertFalse(b.hasLanding);
		assertEquals("", b.landingDistance);
		// The flight was still climbing when it was cut off, so its highest point is its last one.
		assertEquals("1.0", model.timeToApogee);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new FlightPathExporter(buildSimulation(), new FlightData(branch), new FlightPathExportOptions())
				.export(template("kml"), out);
		String kml = out.toString(StandardCharsets.UTF_8);
		assertFalse(kml.contains("landing:&lt;/b&gt;"), kml);
		assertFalse(kml.contains("Landing:&lt;/b&gt;"), kml);
	}

	/**
	 * Google Earth shows a placemark's description in a balloon when it is clicked, which is
	 * where the numbers behind the picture belong: the document carries the flight summary, each
	 * stage's folder its range and landing, and each pin its own time, altitude and offset.
	 */
	@Test
	public void kmlCarriesTheFlightSummaryInBalloons() throws Exception {
		String kml = render("kml", new FlightPathExportOptions());

		// Document balloon.
		assertTrue(kml.contains("&lt;b&gt;Rocket:&lt;/b&gt; LPV2&lt;br/&gt;"), kml);
		assertTrue(kml.contains("&lt;b&gt;Launch site:&lt;/b&gt; 30.6146, -97.4966 at 200.0 m above sea level&lt;br/&gt;"),
				kml);
		assertTrue(kml.contains("&lt;b&gt;Max altitude:&lt;/b&gt; 250"), kml);
		assertTrue(kml.contains("&lt;b&gt;Max range:&lt;/b&gt; 120"), kml);
		assertTrue(kml.contains("&lt;b&gt;Time to apogee:&lt;/b&gt; 2.0 s&lt;br/&gt;"), kml);
		assertTrue(kml.contains("&lt;b&gt;Flight time:&lt;/b&gt; 3.0 s&lt;br/&gt;"), kml);
		assertTrue(kml.contains("&lt;b&gt;Sustainer landing:&lt;/b&gt; 120"), kml);
		assertTrue(kml.contains("at 90\u00b0 from the pad, 3.0 s after liftoff"), kml);

		// Stage folder balloon.
		assertTrue(kml.contains("&lt;b&gt;Landing:&lt;/b&gt; 120"), kml);

		// The apogee pin: 250 m up, 100 m due east, and the chute that came out there.
		assertTrue(kml.contains("&lt;b&gt;Time:&lt;/b&gt; 2.00 s after liftoff&lt;br/&gt;"), kml);
		assertTrue(kml.contains("&lt;b&gt;Altitude:&lt;/b&gt; 250"), kml);
		assertTrue(kml.contains("above the pad, 450"), kml);
		assertTrue(kml.contains("&lt;b&gt;Position:&lt;/b&gt; 100"), kml);
		// Two fragments, because the line between them carries the template's own line ending.
		assertTrue(kml.contains("at 90\u00b0 from the pad&lt;br/&gt;"), kml);
		assertTrue(kml.contains("&lt;b&gt;Device:&lt;/b&gt; Main</description>"), kml);

		// Descriptions must not spill into the places tree under every name.
		assertTrue(kml.contains("<Snippet maxLines=\"0\"/>"), kml);
	}

	/**
	 * The balloon markup is written pre-escaped rather than wrapped in CDATA, so that a name
	 * carrying an ampersand reaches the balloon as the user typed it instead of as "&amp;amp;".
	 * The document stays well-formed either way, which is what the parse checks.
	 */
	@Test
	public void aNameWithMarkupCharactersSurvivesIntoTheBalloon() throws Exception {
		Simulation simulation = buildSimulation();
		simulation.getRocket().setName("Bill & Ted's <Excellent> Rocket");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new FlightPathExporter(simulation, buildFlightData(), new FlightPathExportOptions())
				.export(template("kml"), out);
		String kml = out.toString(StandardCharsets.UTF_8);

		// Escaped once in the file, so an XML parser hands the balloon back the original text.
		assertTrue(kml.contains("Bill &amp; Ted&apos;s &lt;Excellent&gt; Rocket"), kml);

		Document parsed = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new ByteArrayInputStream(kml.getBytes(StandardCharsets.UTF_8)));
		String description = parsed.getElementsByTagName("description").item(0).getTextContent();
		assertTrue(description.contains("<b>Rocket:</b> Bill & Ted's <Excellent> Rocket"), description);
	}

	/** The balloons are a switch, for a file going somewhere they would only get in the way. */
	@Test
	public void balloonsCanBeTurnedOff() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setIncludeDescriptions(false);
		String kml = render("kml", options);

		assertFalse(kml.contains("<description>"), kml);
		assertFalse(kml.contains("<Snippet"), kml);
		// Everything else is still there.
		assertTrue(kml.contains("<name>Apogee</name>"), kml);
		assertTrue(kml.contains("<LineString>"), kml);
	}

	/** Without a launch altitude, "above sea level" would just repeat the height above the pad. */
	@Test
	public void balloonsSkipSeaLevelWhenTheSimulationHasNoLaunchAltitude() throws Exception {
		Simulation simulation = buildSimulation();
		simulation.getOptions().setLaunchAltitude(0.0);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new FlightPathExporter(simulation, buildFlightData(), new FlightPathExportOptions())
				.export(template("kml"), out);
		String kml = out.toString(StandardCharsets.UTF_8);

		assertFalse(kml.contains("above sea level"), kml);
		assertTrue(kml.contains("&lt;b&gt;Launch site:&lt;/b&gt; 30.6146, -97.4966&lt;br/&gt;"), kml);
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
		// The component still names the pin's balloon, which is why this looks at the name element.
		assertTrue(kml.contains("<name>Booster Ejection</name>"), kml);
		assertFalse(kml.contains("<name>Booster Chute</name>"), kml);
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

	/**
	 * The track's altitude reference and the waypoints' are set separately. A flight is worth
	 * seeing suspended in the air, while the pins that label it read better against the ground
	 * they sit over, so the common pairing is a real track altitude with clamped pins.
	 */
	@Test
	public void trackAndWaypointAltitudesAreReferencedSeparately() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setAltitudeReference(AltitudeReference.SEA_LEVEL);
		options.setWaypointAltitudeReference(AltitudeReference.CLAMPED);
		String kml = render("kml", options);

		assertTrue(kml.contains("<altitudeMode>absolute</altitudeMode>"), kml);
		assertTrue(kml.contains("<altitudeMode>clampToGround</altitudeMode>"), kml);

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildFlightData(), options).build();
		assertEquals("absolute", model.kmlAltitudeMode);
		assertEquals("clampToGround", model.kmlWaypointAltitudeMode);

		// The pins carry height above the ground, the track height above sea level: a clamped
		// altitude is ignored by KML, but the number stays meaningful to anything else reading it.
		FlightPathModel.Branch branch = model.branches.get(0);
		assertEquals(250.0, waypointOfType(branch, "apogee").altitudeKmlMeters, 1e-9);
		assertEquals(450.0, branch.path.get(2).altitudeKmlMeters, 1e-9);
	}

	/** A clamped track is tessellated, or it cuts through hills instead of draping over them. */
	@Test
	public void aClampedTrackIsTessellated() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setAltitudeReference(AltitudeReference.CLAMPED);
		assertTrue(render("kml", options).contains("<tessellate>1</tessellate>"));

		options.setAltitudeReference(AltitudeReference.GROUND);
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildFlightData(), options).build();
		assertFalse(model.tessellatePath, "only a clamped track needs tessellating");
	}

	/**
	 * The shadow is KML's extrude: a curtain under the track and a plumb line under each pin. It
	 * is meaningless once the geometry is already lying on the ground, so a clamped reference
	 * drops it on that half alone.
	 */
	@Test
	public void theShadowIsDroppedForWhicheverHalfIsClamped() {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setDrawShadow(true);
		options.setAltitudeReference(AltitudeReference.SEA_LEVEL);
		options.setWaypointAltitudeReference(AltitudeReference.CLAMPED);

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildFlightData(), options).build();
		assertTrue(model.extrudePath, "the airborne track still has somewhere to cast to");
		assertFalse(model.extrudeWaypoints, "a clamped pin is already on the ground");

		options.setDrawShadow(false);
		model = new FlightPathModelBuilder(buildSimulation(), buildFlightData(), options).build();
		assertFalse(model.extrudePath);
		assertFalse(model.extrudeWaypoints);
	}

	/** With the shadow on, the extrude elements reach the file; with it off, nothing is emitted. */
	@Test
	public void theShadowReachesTheKml() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setAltitudeReference(AltitudeReference.SEA_LEVEL);
		options.setWaypointAltitudeReference(AltitudeReference.SEA_LEVEL);

		assertFalse(render("kml", options).contains("<extrude>"), "no shadow by default");

		options.setDrawShadow(true);
		String kml = render("kml", options);
		assertTrue(kml.contains("<extrude>1</extrude>"), kml);
		// One curtain under the track, and one plumb line under every pin.
		assertEquals(countOccurrences(kml, "<Point>") + countOccurrences(kml, "<LineString>")
				- countOccurrences(kml, "clampToGround"),
				countOccurrences(kml, "<extrude>1</extrude>"), kml);
	}

	/**
	 * A mission name names the document, the folders and the tracks, so two files opened together
	 * can be told apart in the tree. It stays off the waypoint markers unless asked for, because a
	 * near-vertical flight already packs those together.
	 */
	@Test
	public void aMissionNameNamesTheDocumentFoldersAndTracks() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setMissionName("Sod Blaster");
		String kml = stagedKml(options);

		assertTrue(kml.contains("<name>Sod Blaster KML test</name>"), kml);
		assertTrue(kml.contains("<name>Sod Blaster Sustainer</name>"), kml);
		assertTrue(kml.contains("<name>Sod Blaster Booster</name>"), kml);
		assertTrue(kml.contains("<name>Sod Blaster Sustainer flight path</name>"), kml);
		assertTrue(kml.contains("<name>Sod Blaster Booster ground track</name>"), kml);

		// The markers keep their stage-qualified names and nothing more.
		assertTrue(kml.contains("<name>Sustainer Apogee</name>"), kml);
		assertFalse(kml.contains("<name>Sod Blaster Sustainer Apogee</name>"), kml);
	}

	/** Asked for, the mission name reaches the markers as well. */
	@Test
	public void theMissionNameCanReachTheWaypointsToo() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setMissionName("Sod Blaster");
		options.setLabelWaypointsWithMission(true);

		String kml = stagedKml(options);
		assertTrue(kml.contains("<name>Sod Blaster Sustainer Apogee</name>"), kml);
		assertTrue(kml.contains("<name>Sod Blaster Booster Landing</name>"), kml);
	}

	/**
	 * A mission named after the thing it is already prefixing is not doubled up, so naming a
	 * mission after the rocket does not produce "Sustainer Sustainer Apogee".
	 */
	@Test
	public void aMissionNameIsNotRepeatedWhenItAlreadyLeadsTheName() {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setMissionName("Sustainer");
		options.setLabelWaypointsWithMission(true);

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), options).build();
		assertEquals("Sustainer", model.branches.get(0).name);
		assertEquals("Sustainer Apogee", waypointOfType(model.branches.get(0), "apogee").qualifiedLabel);
	}

	/** With no mission name the export is named exactly as it was before the option existed. */
	@Test
	public void noMissionNameLeavesEveryNameAlone() throws Exception {
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), new FlightPathExportOptions()).build();

		assertEquals("", model.missionName);
		assertEquals("KML test", model.title);
		assertEquals("Sustainer", model.branches.get(0).name);
		assertEquals("Sustainer Apogee", waypointOfType(model.branches.get(0), "apogee").qualifiedLabel);
	}

	/** Blank input is treated as no mission at all, rather than prefixing a space. */
	@Test
	public void aBlankMissionNameIsNoMissionName() {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setMissionName("   ");

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), options).build();
		assertEquals("", model.missionName);
		assertEquals("Sustainer", model.branches.get(0).name);
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

	/**
	 * A stage's flight-path color can be overridden without disturbing its other two. The three are
	 * set independently, so changing the line in the air leaves the ground track and the pins where
	 * their defaults put them.
	 */
	@Test
	public void aStageTrackColorCanBeOverridden() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setBranchColor(0, 0x112233);

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), options).build();
		assertEquals("112233", model.branches.get(0).colorRgb);
		// The other two stay on their defaults rather than following the new path color.
		assertEquals(String.format("%06x", FlightPathModelBuilder.defaultGroundColor(0)),
				model.branches.get(0).groundColorRgb);
		assertEquals(String.format("%06x", FlightPathModelBuilder.defaultPinColor(0)),
				model.branches.get(0).pinColorRgb);
		// Untouched stages keep their palette entry.
		assertEquals(String.format("%06x", FlightPathModelBuilder.defaultBranchColor(1)),
				model.branches.get(1).colorRgb);

		String kml = stagedKml(options);
		// KML is aabbggrr, so 0x112233 is the flight path at ff332211.
		assertTrue(kml.contains("<Style id=\"flightPath0\"><LineStyle><color>ff332211</color>"), kml);
		assertTrue(kml.contains("<Style id=\"groundTrack0\"><LineStyle><color>ff552dff</color>"), kml);
		assertTrue(kml.contains("<Style id=\"waypoint0\">"), kml);
	}

	/** A stage's ground track can be given a color of its own, and it is exported exactly as picked. */
	@Test
	public void aStageGroundTrackColorCanBeSetOnItsOwn() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setBranchGroundColor(0, 0x00FF00);

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), options).build();
		assertEquals("00ff00", model.branches.get(0).groundColorRgb);
		// The flight path and the pins are left where they were.
		assertEquals(String.format("%06x", FlightPathModelBuilder.defaultBranchColor(0)),
				model.branches.get(0).colorRgb);
		assertEquals(model.branches.get(0).colorRgb, model.branches.get(0).pinColorRgb);
		// A stage left alone keeps its default ground track.
		assertEquals(String.format("%06x", FlightPathModelBuilder.defaultGroundColor(1)),
				model.branches.get(1).groundColorRgb);

		String kml = stagedKml(options);
		assertTrue(kml.contains("<Style id=\"groundTrack0\"><LineStyle><color>ff00ff00</color>"), kml);
		assertTrue(kml.contains("<Style id=\"flightPath0\"><LineStyle><color>ffbd7200</color>"), kml);
		assertTrue(kml.contains("<Style id=\"groundTrack1\"><LineStyle><color>ffa4b300</color>"), kml);
	}

	/**
	 * A stage's waypoint pins can be given a color of their own, so the markers can be made to
	 * stand out against a track they would otherwise disappear into.
	 */
	@Test
	public void aStagePinColorCanBeSetOnItsOwn() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setBranchPinColor(0, 0xFFFF00);

		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), options).build();
		assertEquals("ffff00", model.branches.get(0).pinColorRgb);
		// Neither track moved, and a stage left alone still takes its pins from its flight path.
		assertEquals(String.format("%06x", FlightPathModelBuilder.defaultBranchColor(0)),
				model.branches.get(0).colorRgb);
		assertEquals(model.branches.get(1).colorRgb, model.branches.get(1).pinColorRgb);

		String kml = stagedKml(options);
		// 0xFFFF00 as aabbggrr is ff00ffff, and it appears on the pin and nowhere else.
		assertTrue(kml.contains("<color>ff00ffff</color>"), kml);
		assertTrue(kml.contains("<Style id=\"flightPath0\"><LineStyle><color>ffbd7200</color>"), kml);
		assertTrue(kml.contains("<Style id=\"groundTrack0\"><LineStyle><color>ff552dff</color>"), kml);
	}

	/** All three colors of one stage can be set at once, and none of them bleeds into another. */
	@Test
	public void aStageCanCarryThreeIndependentColors() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setBranchColor(0, 0x112233);
		options.setBranchGroundColor(0, 0x445566);
		options.setBranchPinColor(0, 0x778899);

		String kml = stagedKml(options);
		assertTrue(kml.contains("<Style id=\"flightPath0\"><LineStyle><color>ff332211</color>"), kml);
		assertTrue(kml.contains("<Style id=\"groundTrack0\"><LineStyle><color>ff665544</color>"), kml);
		assertTrue(kml.contains("<color>ff998877</color>"), kml);
	}

	/** Clearing the overrides puts all three colors of every stage back to the palette. */
	@Test
	public void clearingStageColorsDropsEveryOverride() {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setBranchColor(0, 0x112233);
		options.setBranchGroundColor(0, 0x445566);
		options.setBranchPinColor(0, 0x778899);
		options.clearBranchColors();

		assertNull(options.getBranchColor(0));
		assertNull(options.getBranchGroundColor(0));
		assertNull(options.getBranchPinColor(0));
	}

	/** With nothing overridden the export is exactly what the palette gives. */
	@Test
	public void stageColorsFallBackToThePalette() {
		FlightPathModel model = new FlightPathModelBuilder(
				buildSimulation(), buildStagedFlightData(), new FlightPathExportOptions()).build();
		for (FlightPathModel.Branch branch : model.branches) {
			assertEquals(String.format("%06x", FlightPathModelBuilder.defaultBranchColor(branch.index)),
					branch.colorRgb);
		}
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

		// Ground tracks come from a palette of their own, picked to contrast with the flight path
		// each one runs under.
		assertTrue(kml.contains("<Style id=\"groundTrack0\"><LineStyle><color>ff552dff</color>"), kml);
		assertTrue(kml.contains("<Style id=\"groundTrack1\"><LineStyle><color>ffa4b300</color>"), kml);
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

		assertFalse(new FlightPathExporter(sim, buildFlightData(0, 0), new FlightPathExportOptions())
				.hasLaunchPosition(), "both coordinates at zero is the unset case");

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
	 * Only both coordinates at zero is OpenRocket's "not set". A single zero is a real coordinate:
	 * a site on the equator, or on the prime meridian, is exported where the user put it rather
	 * than being second-guessed.
	 */
	@Test
	public void onlyABothZeroLaunchPositionCountsAsUnset() {
		for (double[] position : new double[][] { { 30.6146, 0 }, { 0, -97.4966 }, { 51.5, 0 } }) {
			Simulation sim = buildSimulation();
			sim.getOptions().setLaunchLatitude(position[0]);
			sim.getOptions().setLaunchLongitude(position[1]);

			FlightPathModel model = new FlightPathModelBuilder(
					sim, buildFlightData(position[0], position[1]), new FlightPathExportOptions()).build();

			String where = position[0] + ", " + position[1];
			assertEquals(position[0], model.launchLatitude, 1e-9, where);
			assertEquals(position[1], model.launchLongitude, 1e-9, where);

			// The warning the user sees has to agree with what the export actually did.
			assertTrue(new FlightPathExporter(sim, buildFlightData(), new FlightPathExportOptions())
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
