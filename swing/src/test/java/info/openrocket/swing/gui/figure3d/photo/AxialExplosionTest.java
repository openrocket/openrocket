package info.openrocket.swing.gui.figure3d.photo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AxialExplosionTest {
	@Test
	void emptyAndSingleGroupRemainInPlace() {
		assertArrayEquals(new float[0], AxialExplosion.offsets(List.of()), 1e-5f);
		assertArrayEquals(new float[] {0}, AxialExplosion.offsets(List.of(span(2, 5))), 1e-5f);
	}

	@Test
	void overlappingGroupsSeparateAndStayCentered() {
		float[] offsets = AxialExplosion.offsets(List.of(span(0, 10), span(2, 4), span(5, 7)));
		assertTrue(4 + offsets[1] < offsets[0]);
		assertTrue(10 + offsets[0] < 5 + offsets[2]);
		assertEquals(5, (2 + offsets[1] + 7 + offsets[2]) / 2, 1e-5f);
	}

	@Test
	void disjointGroupsDoNotLoseExistingGap() {
		float[] offsets = AxialExplosion.offsets(List.of(span(10, 12), span(0, 2)));
		assertArrayEquals(new float[] {0, 0}, offsets, 1e-5f);
	}

	@Test
	void coincidentGroupsFollowStableInputOrder() {
		float[] offsets = AxialExplosion.offsets(List.of(span(0, 4), span(0, 4)));
		assertTrue(4 + offsets[0] < offsets[1]);
		assertEquals(-offsets[0], offsets[1], 1e-5f);
	}

	private static AxialExplosion.Span span(float min, float max) {
		return new AxialExplosion.Span(min, max);
	}
}
