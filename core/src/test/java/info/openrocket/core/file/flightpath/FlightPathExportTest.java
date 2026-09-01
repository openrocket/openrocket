package info.openrocket.core.file.flightpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.document.Simulation;
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
		FlightDataBranch branch = new FlightDataBranch("Sustainer",
				FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE,
				FlightDataType.TYPE_LATITUDE, FlightDataType.TYPE_LONGITUDE,
				FlightDataType.TYPE_POSITION_X, FlightDataType.TYPE_POSITION_Y,
				FlightDataType.TYPE_VELOCITY_TOTAL, FlightDataType.TYPE_ACCELERATION_TOTAL);

		// time, altAGL, lat, lon, x(east), y(north), vel, accel
		addPoint(branch, 0.0, 0.0, 30.6146, -97.4966, 0.0, 0.0, 0.0, 0.0);
		addPoint(branch, 1.0, 100.0, 30.6147, -97.4966, 0.0, 0.0, 80.0, 50.0);
		addPoint(branch, 2.0, 250.0, 30.6148, -97.4966, 100.0, 0.0, 20.0, -9.0); // apogee, due east
		addPoint(branch, 3.0, 0.0, 30.6150, -97.4967, 120.0, 0.0, 5.0, 0.0);     // landing

		Parachute main = new Parachute();
		main.setName("Main");
		branch.addEvent(new FlightEvent(FlightEvent.Type.APOGEE, 2.0));
		branch.addEvent(new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 2.0, main));
		branch.addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, 3.0));

		return new FlightData(branch);
	}

	private static void addPoint(FlightDataBranch branch, double t, double alt, double lat, double lon,
			double x, double y, double vel, double acc) {
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, t);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, alt);
		branch.setValue(FlightDataType.TYPE_LATITUDE, lat);
		branch.setValue(FlightDataType.TYPE_LONGITUDE, lon);
		branch.setValue(FlightDataType.TYPE_POSITION_X, x);
		branch.setValue(FlightDataType.TYPE_POSITION_Y, y);
		branch.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, vel);
		branch.setValue(FlightDataType.TYPE_ACCELERATION_TOTAL, acc);
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
	public void kmlTemplateProducesLonLatMslCoordinates() throws Exception {
		String kml = render("kml", new FlightPathExportOptions());

		assertTrue(kml.contains("<kml xmlns=\"http://www.opengis.net/kml/2.2\">"), kml);
		assertTrue(kml.contains("<name>Apogee</name>"), kml);
		// Apogee: lon,lat,alt(MSL = 250 AGL + 200 launch = 450).
		assertTrue(kml.contains("-97.4966,30.6148,450.0"), kml);
		assertTrue(kml.contains("<altitudeMode>clampToGround</altitudeMode>"), kml);
		// Recovery pin named after the device.
		assertTrue(kml.contains("<name>Main</name>"), kml);
	}

	@Test
	public void kmlHonoursGeometryToggles() throws Exception {
		FlightPathExportOptions options = new FlightPathExportOptions();
		options.setIncludeGroundTrack(false);
		String kml = render("kml", options);

		assertTrue(kml.contains("<altitudeMode>absolute</altitudeMode>"), kml);
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
		assertTrue(csv.contains("\"30.614800\",\"-97.496600\""), csv);
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
		assertTrue(gpx.contains("<wpt lat=\"30.6148\" lon=\"-97.4966\">"), gpx);
		assertTrue(gpx.contains("<trkpt"), gpx);
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
