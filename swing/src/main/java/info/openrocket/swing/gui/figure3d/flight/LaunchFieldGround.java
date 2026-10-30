package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Ground for the flight replay: a mown launch field of alternating stripes around the pad,
 * which gives scale and motion cues up close, inside open ground reaching far enough that the
 * scene fog hides its edge. Built as a grid of cells sharing whole edges, so the colored areas
 * meet without overlap (no z-fighting) and without T-junctions (no cracks).
 */
final class LaunchFieldGround {
	static final Vector3f OPEN_GROUND_COLOR = new Vector3f(0.21f, 0.29f, 0.19f);
	static final Vector3f MOWN_COLOR = new Vector3f(0.25f, 0.35f, 0.21f);
	static final Vector3f MOWN_ALTERNATE_COLOR = new Vector3f(0.29f, 0.40f, 0.24f);

	/** One flat, double-sided piece of ground in a single color. */
	record Patch(Mesh mesh, Vector3f color) {
	}

	private LaunchFieldGround() {
	}

	/**
	 * @param groundHalfSize half the width of the whole ground
	 * @param fieldHalfSize  half the width of the striped field around the pad
	 * @param stripeWidth    width of one mown stripe
	 */
	static List<Patch> create(float groundHalfSize, float fieldHalfSize, float stripeWidth) {
		float field = Math.min(fieldHalfSize, groundHalfSize * 0.5f);
		int stripes = Math.max(2, Math.round(2.0f * field / stripeWidth));
		List<Float> xBreaks = new ArrayList<>();
		xBreaks.add(-groundHalfSize);
		for (int i = 0; i <= stripes; i++) {
			xBreaks.add(-field + 2.0f * field * i / stripes);
		}
		xBreaks.add(groundHalfSize);
		float[] zBreaks = { -groundHalfSize, -field, field, groundHalfSize };

		List<float[]> open = new ArrayList<>();
		List<float[]> mown = new ArrayList<>();
		List<float[]> mownAlternate = new ArrayList<>();
		for (int xi = 0; xi + 1 < xBreaks.size(); xi++) {
			for (int zi = 0; zi + 1 < zBreaks.length; zi++) {
				float[] cell = { xBreaks.get(xi), zBreaks[zi], xBreaks.get(xi + 1), zBreaks[zi + 1] };
				boolean inField = xi >= 1 && xi <= stripes && zi == 1;
				if (!inField) {
					open.add(cell);
				} else if ((xi - 1) % 2 == 0) {
					mown.add(cell);
				} else {
					mownAlternate.add(cell);
				}
			}
		}
		return List.of(new Patch(quads(open), new Vector3f(OPEN_GROUND_COLOR)),
				new Patch(quads(mown), new Vector3f(MOWN_COLOR)),
				new Patch(quads(mownAlternate), new Vector3f(MOWN_ALTERNATE_COLOR)));
	}

	/** Flat quads at y = 0 from {minX, minZ, maxX, maxZ} cells, visible from above and below. */
	private static Mesh quads(List<float[]> cells) {
		List<Vertex> vertices = new ArrayList<>(cells.size() * 4);
		IntList indices = new IntList(cells.size() * 12);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		for (float[] cell : cells) {
			int base = vertices.size();
			vertices.add(groundVertex(cell[0], cell[3], up));
			vertices.add(groundVertex(cell[0], cell[1], up));
			vertices.add(groundVertex(cell[2], cell[1], up));
			vertices.add(groundVertex(cell[2], cell[3], up));
			// Both windings, so the ground is not culled when the camera orbits below it.
			indices.addTriangle(base, base + 1, base + 2);
			indices.addTriangle(base, base + 2, base + 3);
			indices.addTriangle(base, base + 2, base + 1);
			indices.addTriangle(base, base + 3, base + 2);
		}
		return new Mesh(vertices, indices);
	}

	private static Vertex groundVertex(float x, float z, Vector3f normal) {
		return new Vertex(new Vector3f(x, 0.0f, z), new Vector3f(normal), new Vector2f(),
				RenderingConstants.SURFACE_ID_OUTSIDE);
	}
}
