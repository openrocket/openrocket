package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BodyCenteredPoseProviderTest {
	// Climbs at 10 units/s while tumbling end over end at 1 rad/s.
	private static final PoseProvider TUMBLING = new PoseProvider() {
		@Override public Vector3f getPosition(double t) { return new Vector3f(0.0f, (float) (10.0 * t), 0.0f); }
		@Override public Quaternionf getOrientation(double t) { return new Quaternionf().rotateZ((float) t); }
		@Override public double getStartTime() { return 0.0; }
		@Override public double getEndTime() { return 20.0; }
	};

	@Test
	void bodyCenterFollowsTheSimulatedPointWhateverTheAttitude() {
		Vector3f center = new Vector3f(5.0f, 0.0f, 0.0f);
		BodyCenteredPoseProvider provider = new BodyCenteredPoseProvider(TUMBLING, List.of(), List.of(center));
		for (double t : new double[] { 0.0, 1.3, 7.9 }) {
			assertClose(TUMBLING.getPosition(t), worldPoint(provider, t, center));
		}
	}

	@Test
	void separationKeepsTheBodyContinuousThenTumblesItAboutItsOwnCenter() {
		// A stack centered 5 units down the axis; the booster half is centered 8 units down.
		Vector3f stackCenter = new Vector3f(5.0f, 0.0f, 0.0f);
		Vector3f boosterCenter = new Vector3f(8.0f, 0.0f, 0.0f);
		double separation = 2.0;
		BodyCenteredPoseProvider booster = new BodyCenteredPoseProvider(TUMBLING, List.of(separation),
				List.of(stackCenter, boosterCenter));
		BodyCenteredPoseProvider stack = new BodyCenteredPoseProvider(TUMBLING, List.of(), List.of(stackCenter));

		// Before separation the booster is rendered exactly as part of the stack.
		Vector3f fin = new Vector3f(9.5f, 1.0f, 0.0f);
		assertClose(worldPoint(stack, 1.0, fin), worldPoint(booster, 1.0, fin));
		// No jump at the moment of separation.
		assertClose(worldPoint(stack, Math.nextDown(separation), fin), worldPoint(booster, separation, fin));

		// Afterwards its own center moves only with the simulated point, not with the tumble.
		Vector3f atSeparation = worldPoint(booster, separation, boosterCenter);
		for (double t : new double[] { 3.0, 6.5 }) {
			Vector3f expected = new Vector3f(atSeparation).add(TUMBLING.getPosition(t)).sub(TUMBLING.getPosition(separation));
			assertClose(expected, worldPoint(booster, t, boosterCenter));
		}
		assertEquals(boosterCenter, booster.getBodyCenter());
	}

	private static Vector3f worldPoint(PoseProvider provider, double t, Vector3f local) {
		return provider.getOrientation(t).transform(new Vector3f(local)).add(provider.getPosition(t));
	}

	private static void assertClose(Vector3f expected, Vector3f actual) {
		assertEquals(0.0f, expected.distance(actual), 1e-3f, "expected " + expected + " but was " + actual);
	}
}
