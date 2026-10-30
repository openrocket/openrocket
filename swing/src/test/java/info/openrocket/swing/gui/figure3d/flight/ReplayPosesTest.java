package info.openrocket.swing.gui.figure3d.flight;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplayPosesTest {
	@Test
	void lowestCornerYFollowsTranslation() {
		Vector3f min = new Vector3f(-1.0f, -2.0f, -1.0f);
		Vector3f max = new Vector3f(1.0f, 2.0f, 1.0f);
		Matrix4f transform = new Matrix4f().translate(0.0f, 5.0f, 0.0f);

		float lowest = ReplayPoses.lowestTransformedCornerY(min, max, transform);

		assertEquals(3.0f, lowest, 1e-5);
	}

	@Test
	void lowestCornerYReflectsRotationOfAnElongatedBody() {
		// A body from y=0 (nose) down to y=-10 (tail), rotated 90 deg so it lies along +x:
		// the lowest point should be 0 (the rotation maps the -y extent onto the x axis).
		Vector3f min = new Vector3f(-0.5f, -10.0f, -0.5f);
		Vector3f max = new Vector3f(0.5f, 0.0f, 0.5f);
		Matrix4f transform = new Matrix4f().rotateZ((float) Math.toRadians(90.0));

		float lowest = ReplayPoses.lowestTransformedCornerY(min, max, transform);

		assertEquals(-0.5f, lowest, 1e-5);
	}
}
