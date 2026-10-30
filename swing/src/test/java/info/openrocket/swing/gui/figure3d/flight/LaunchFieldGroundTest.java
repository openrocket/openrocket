package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchFieldGroundTest {
	@Test
	void patchesTileTheWholeGroundExactlyOnce() {
		List<LaunchFieldGround.Patch> patches = LaunchFieldGround.create(10_000.0f, 2_400.0f, 160.0f);

		double area = patches.stream().mapToDouble(patch -> topArea(patch.mesh())).sum();

		assertEquals(20_000.0 * 20_000.0, area, 1.0, "Cells must cover the ground without overlap or gaps");
	}

	@Test
	void theFieldAroundThePadIsMownInEqualStripes() {
		List<LaunchFieldGround.Patch> patches = LaunchFieldGround.create(10_000.0f, 2_400.0f, 160.0f);

		assertEquals(LaunchFieldGround.OPEN_GROUND_COLOR, patches.get(0).color());
		double mown = topArea(patches.get(1).mesh());
		double alternate = topArea(patches.get(2).mesh());
		assertEquals(4_800.0 * 4_800.0, mown + alternate, 1.0);
		assertEquals(mown, alternate, 1.0, "Two colors alternating over an even number of stripes");
	}

	@Test
	void groundLiesFlatAtZeroAndIsVisibleFromBelow() {
		Mesh mesh = LaunchFieldGround.create(1_000.0f, 200.0f, 50.0f).get(0).mesh();

		assertTrue(mesh.getVertices().stream().allMatch(vertex -> vertex.position.y == 0.0f));
		assertEquals(0, mesh.getIndices().size() % 12, "Each quad in both windings");
	}

	/** Area of the up-facing triangles (each quad is stored in both windings). */
	private static double topArea(Mesh mesh) {
		IntList indices = mesh.getIndices();
		double area = 0.0;
		for (int i = 0; i + 2 < indices.size(); i += 3) {
			Vector3f a = mesh.getVertices().get(indices.get(i)).position;
			Vector3f b = mesh.getVertices().get(indices.get(i + 1)).position;
			Vector3f c = mesh.getVertices().get(indices.get(i + 2)).position;
			Vector3f normal = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a));
			if (normal.y > 0.0f) {
				area += normal.length() / 2.0;
			}
		}
		return area;
	}
}
