package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradientBackgroundTest {
	@Test
	void alignmentIsExplicitSoDesignGradientsRemainScreenFixed() {
		Vector3f sky = new Vector3f(0.7f, 0.8f, 0.9f);
		Vector3f ground = new Vector3f(0.7f, 0.9f, 0.7f);

		assertFalse(new GradientBackground(sky, ground).isWorldAligned());
		assertTrue(GradientBackground.worldAligned(sky, ground).isWorldAligned());
	}

	@Test
	void horizonColorMatchesWhatTheSkyShowsAtZeroElevation() {
		GradientBackground sky = GradientBackground.worldAligned(new Vector3f(0.2f, 0.5f, 0.8f), new Vector3f(0.8f));
		// gradient_fragment.glsl: smoothstep(-0.25, 0.35, 0.0) = 0.37616
		Vector3f expected = new Vector3f(sky.getBottomColor()).lerp(sky.getTopColor(), 0.37616f);

		assertEquals(0.0f, expected.distance(sky.getHorizonColor(new Vector3f())), 1e-4f);
		GradientBackground screen = new GradientBackground(new Vector3f(0.2f), new Vector3f(0.8f));
		assertEquals(screen.getBottomColor(), screen.getHorizonColor(new Vector3f()),
				"A screen-aligned gradient has no horizon; hazing keeps using its bottom color");
	}
}
