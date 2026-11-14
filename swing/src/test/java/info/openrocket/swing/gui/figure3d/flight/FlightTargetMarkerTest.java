package info.openrocket.swing.gui.figure3d.flight;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightTargetMarkerTest {
	private static final int WIDTH = 1600;
	private static final int HEIGHT = 1000;
	private static final Matrix4f PROJECTION = new Matrix4f()
			.perspective((float) Math.toRadians(45.0), WIDTH / (float) HEIGHT, 0.1f, 1.0e6f);
	// Eye at the origin looking down -Z.
	private static final Matrix4f VIEW = new Matrix4f().lookAt(0, 0, 0, 0, 0, -1, 0, 1, 0);

	@Test
	void circlesADistantRocketAtItsScreenPosition() {
		FlightTargetMarker.Circle circle = FlightTargetMarker.circleFor(PROJECTION, VIEW,
				new Vector3f(0.0f, 0.0f, -5_000.0f), 6.0f, WIDTH, HEIGHT);

		assertNotNull(circle);
		assertEquals(WIDTH / 2.0f, circle.x(), 0.5f);
		assertEquals(HEIGHT / 2.0f, circle.y(), 0.5f);
		assertTrue(circle.radius() >= 0.018f * HEIGHT, "The ring must stay large enough to see");
	}

	@Test
	void followsTheRocketAcrossTheScreen() {
		FlightTargetMarker.Circle upperRight = FlightTargetMarker.circleFor(PROJECTION, VIEW,
				new Vector3f(1_000.0f, 800.0f, -5_000.0f), 6.0f, WIDTH, HEIGHT);

		assertNotNull(upperRight);
		assertTrue(upperRight.x() > WIDTH / 2.0f && upperRight.y() > HEIGHT / 2.0f,
				"GL window coordinates grow right and up");
	}

	@Test
	void noCircleWhenTheRocketIsBigBehindOrOutOfView() {
		assertNull(FlightTargetMarker.circleFor(PROJECTION, VIEW, new Vector3f(0, 0, -20), 6.0f, WIDTH, HEIGHT),
				"A rocket filling the view needs no marker");
		assertNull(FlightTargetMarker.circleFor(PROJECTION, VIEW, new Vector3f(0, 0, 5_000), 6.0f, WIDTH, HEIGHT),
				"Behind the camera");
		assertNull(FlightTargetMarker.circleFor(PROJECTION, VIEW, new Vector3f(50_000, 0, -5_000), 6.0f, WIDTH,
				HEIGHT), "Outside the view");
	}
}
