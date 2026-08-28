package info.openrocket.swing.gui.figure3d.rendering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlameRendererTest {

	@Test
	void animationTimeUsesElapsedNanosInsteadOfEpochSeconds() {
		long origin = 1_800_000_000_000_000_000L;

		assertEquals(0.016f,
				FlameRenderer.animationTimeSeconds(origin, origin + 16_000_000L),
				0.000_001f);
	}
}
