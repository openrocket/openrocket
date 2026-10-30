package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindIndicatorTest extends BaseTestCase {
	// Engine axes: east is +X, north is -Z, up is +Y.
	private static final Vector3f NORTH = new Vector3f(0, 0, -1);
	private static final Vector3f EAST = new Vector3f(1, 0, 0);

	@Test
	void dialTurnsWithTheCameraSoUpIsAwayFromTheViewer() {
		Matrix4f facingNorth = lookFrom(new Vector3f(0, 0, 0), NORTH, new Vector3f(0, 1, 0));
		assertEquals(0.0, degrees(facingNorth, NORTH), 1e-4);
		assertEquals(90.0, degrees(facingNorth, EAST), 1e-4);

		Matrix4f facingEast = lookFrom(new Vector3f(0, 0, 0), EAST, new Vector3f(0, 1, 0));
		assertEquals(0.0, degrees(facingEast, EAST), 1e-4);
		assertEquals(-90.0, degrees(facingEast, NORTH), 1e-4);

		// Looking down and slightly north, as the overview does, north stays up.
		Matrix4f overview = lookFrom(new Vector3f(0, 10, 1), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
		assertEquals(0.0, degrees(overview, NORTH), 1e-3);
	}

	@Test
	void straightDownViewUsesTheCameraRightForTheDial() {
		Matrix4f topDown = lookFrom(new Vector3f(0, 10, 0), new Vector3f(0, 0, 0), NORTH);
		assertEquals(0.0, degrees(topDown, NORTH), 1e-3);
		assertEquals(90.0, degrees(topDown, EAST), 1e-3);
	}

	@Test
	void arrowPointsDownwind() {
		// A west wind (from 270°) blows toward the east.
		WindIndicator.Dial dial = dial(4.0, 270.0);

		assertEquals(90, dial.windAngle(), "Facing north, a wind blowing east points right");
		assertEquals(0, dial.northAngle());
		assertTrue(dial.speed().contains("4"), dial.speed());
	}

	@Test
	void calmAirShowsNoArrow() {
		WindIndicator.Dial dial = dial(0.0, 0.0);

		assertTrue(dial.calm());
		assertEquals("", dial.direction());
		assertFalse(dial(0.5, 0.0).calm(), "A light breeze still shows its direction");
	}

	@Test
	void paintsTheArrowOnTheDownwindSide() {
		// Wind blowing to the viewer's right.
		WindIndicator.Dial dial = new WindIndicator.Dial(90, 0, false, "4 m/s", "Wind from 270°",
				WindIndicator.WIDTH, WindIndicator.HEIGHT);
		BufferedImage image = new BufferedImage(WindIndicator.WIDTH, WindIndicator.HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			WindIndicator.paint(graphics, dial);
		} finally {
			graphics.dispose();
		}

		int x = WindIndicator.WIDTH / 2;
		int y = (int) WindIndicator.DIAL_CENTER_Y;
		int offset = (int) (WindIndicator.DIAL_RADIUS * 0.6f);
		assertTrue(isArrowColored(image.getRGB(x + offset, y)), "The arrow head sits right of the center");
		assertTrue(isArrowColored(image.getRGB(x - offset, y)), "The arrow tail crosses the center");
		assertFalse(isArrowColored(image.getRGB(x, y - offset)), "Nothing arrow-colored above the center");
	}

	private static boolean isArrowColored(int argb) {
		int red = (argb >> 16) & 0xFF;
		int green = (argb >> 8) & 0xFF;
		int blue = argb & 0xFF;
		return blue > 200 && green > 180 && red < 150;
	}

	private static WindIndicator.Dial dial(double speed, double fromDegrees) {
		FlightDataBranch branch = new FlightDataBranch("wind", FlightDataType.TYPE_TIME,
				FlightDataType.TYPE_WIND_VELOCITY, FlightDataType.TYPE_WIND_DIRECTION);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0.0);
		branch.setValue(FlightDataType.TYPE_WIND_VELOCITY, speed);
		branch.setValue(FlightDataType.TYPE_WIND_DIRECTION, Math.toRadians(fromDegrees));
		Matrix4f facingNorth = lookFrom(new Vector3f(0, 0, 0), NORTH, new Vector3f(0, 1, 0));
		return WindIndicator.dialFor(WindField.fromBranch(branch), 0.0, facingNorth, 124, 172);
	}

	private static double degrees(Matrix4f view, Vector3f direction) {
		return Math.toDegrees(WindIndicator.screenAngle(view, direction));
	}

	private static Matrix4f lookFrom(Vector3f eye, Vector3f target, Vector3f up) {
		return new Matrix4f().lookAt(eye, target, up);
	}
}
