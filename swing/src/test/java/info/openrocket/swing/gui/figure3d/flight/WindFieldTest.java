package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WindFieldTest {
	private static final float SCALE = RenderingConstants.WORLD_SCALE;

	@Test
	void blowsAwayFromTheRecordedDirectionInEngineAxes() {
		// Engine axes: east is +X, north is -Z. An east wind blows toward the west.
		assertClose(new Vector3f(-5 * SCALE, 0, 0), wind(5.0, Math.PI / 2.0).velocityAt(0.0));
		assertClose(new Vector3f(0, 0, 5 * SCALE), wind(5.0, 0.0).velocityAt(0.0));
	}

	@Test
	void interpolatesComponentsSoAWindAcrossNorthDoesNotSwingSouth() {
		FlightDataBranch branch = branch();
		addSample(branch, 0.0, 4.0, Math.toRadians(350.0));
		addSample(branch, 2.0, 4.0, Math.toRadians(10.0));

		Vector3f middle = WindField.fromBranch(branch).velocityAt(1.0);

		assertEquals(0.0f, middle.x, 1e-3f);
		assertEquals(4.0 * Math.cos(Math.toRadians(10.0)) * SCALE, middle.z, 1e-3);
	}

	@Test
	void reportsTheSpeedAndTheDirectionTheWindBlowsFrom() {
		WindField west = wind(6.0, Math.toRadians(270.0));
		assertEquals(6.0, west.speedAt(0.0), 1e-9);
		assertEquals(Math.toRadians(270.0), west.directionAt(0.0), 1e-9);
		assertEquals(0.0, wind(3.0, 0.0).directionAt(0.0), 1e-9, "North must read 0, not 2 pi or -0");
	}

	@Test
	void directionAcrossNorthStaysNorthward() {
		FlightDataBranch branch = branch();
		addSample(branch, 0.0, 4.0, Math.toRadians(350.0));
		addSample(branch, 2.0, 4.0, Math.toRadians(20.0));

		assertEquals(Math.toRadians(5.0), WindField.fromBranch(branch).directionAt(1.0), 1e-9);
	}

	@Test
	void isCalmWithoutWindData() {
		FlightDataBranch branch = new FlightDataBranch("no wind", FlightDataType.TYPE_TIME);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0.0);

		assertClose(new Vector3f(), WindField.fromBranch(branch).velocityAt(3.0));
	}

	private static WindField wind(double speed, double direction) {
		FlightDataBranch branch = branch();
		addSample(branch, 0.0, speed, direction);
		return WindField.fromBranch(branch);
	}

	private static FlightDataBranch branch() {
		return new FlightDataBranch("wind", FlightDataType.TYPE_TIME, FlightDataType.TYPE_WIND_VELOCITY,
				FlightDataType.TYPE_WIND_DIRECTION);
	}

	private static void addSample(FlightDataBranch branch, double time, double speed, double direction) {
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, time);
		branch.setValue(FlightDataType.TYPE_WIND_VELOCITY, speed);
		branch.setValue(FlightDataType.TYPE_WIND_DIRECTION, direction);
	}

	private static void assertClose(Vector3f expected, Vector3f actual) {
		assertEquals(0.0f, expected.distance(actual), 1e-3f, "expected " + expected + " but was " + actual);
	}
}
