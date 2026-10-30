package info.openrocket.swing.gui.figure3d.rendering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlameRendererTest {

	@Test
	void replayFlickerUsesReplayTimeRegardlessOfWallClockOrSeekDirection() {
		assertEquals(0.5f, FlameRenderer.animationTimeSeconds(0.5, 0, 5_000_000_000L));
		assertEquals(0.5f, FlameRenderer.animationTimeSeconds(0.5, 0, 90_000_000_000L));
		assertEquals(0.1f, FlameRenderer.animationTimeSeconds(0.1, 0, 95_000_000_000L));
		assertEquals(0.0f, FlameRenderer.animationTimeSeconds(0.0, 0, 99_000_000_000L));
		assertEquals(3.0f, FlameRenderer.animationTimeSeconds(Double.NaN, 0, 3_000_000_000L));
	}

	@Test
	void animationTimeUsesElapsedNanosInsteadOfEpochSeconds() {
		long origin = 1_800_000_000_000_000_000L;

		assertEquals(0.016f,
				FlameRenderer.animationTimeSeconds(origin, origin + 16_000_000L),
				0.000_001f);
	}
}
