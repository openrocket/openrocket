package info.openrocket.swing.gui.figure3d.geometry.basic;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a thin tube that follows a flight trajectory, so the whole-flight camera has a visible
 * path to look at (the rocket itself is sub-pixel small at that zoom). The tube is emitted
 * double-sided so it is never back-face culled regardless of segment winding.
 */
public final class TrajectoryTrailGenerator {

	private TrajectoryTrailGenerator() {
	}

	public static Mesh create(List<Vector3f> pathPoints, float radius, int segments) {
		return create(pathPoints, radius, segments, null);
	}

	/**
	 * Builds the tube with its first ring oriented from {@code seedU} (projected onto the ring's
	 * plane), so pieces of one path built separately line up where they meet. Take the seeds from
	 * {@link #ringFrames} of the whole path.
	 */
	public static Mesh create(List<Vector3f> pathPoints, float radius, int segments, Vector3f seedU) {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();

		// Drop consecutive duplicate points so tangents are well defined.
		List<Vector3f> points = new ArrayList<>();
		for (Vector3f p : pathPoints) {
			if (points.isEmpty() || points.get(points.size() - 1).distanceSquared(p) > 1.0e-6f) {
				points.add(new Vector3f(p));
			}
		}
		if (points.size() < 2 || radius <= 0.0f || segments < 3) {
			return new Mesh(vertices, indices);
		}

		int ringCount = points.size();
		// A rotation-minimizing frame: carry the ring's "u" axis from one point to the next by
		// projecting it onto the new perpendicular plane instead of re-deriving it from a fixed
		// reference axis. Re-deriving per point flips the basis ~90 degrees wherever the tangent
		// crosses the reference-axis threshold, which pinches the tube (its connecting quads cross
		// the axis). Propagating the frame keeps consecutive rings aligned, so the tube stays round.
		Vector3f previousU = seedU;
		for (int i = 0; i < ringCount; i++) {
			Vector3f tangent = tangentAt(points, i);
			Vector3f u = nextFrameU(previousU, tangent);
			Vector3f v = new Vector3f(tangent).cross(u).normalize();
			previousU = u;

			Vector3f center = points.get(i);
			for (int s = 0; s < segments; s++) {
				double angle = 2.0 * Math.PI * s / segments;
				float cos = (float) Math.cos(angle);
				float sin = (float) Math.sin(angle);
				Vector3f normal = new Vector3f(u).mul(cos).add(new Vector3f(v).mul(sin)).normalize();
				Vector3f position = new Vector3f(center).add(new Vector3f(normal).mul(radius));
				vertices.add(new Vertex(position, new Vector3f(normal),
						new Vector2f((float) s / segments, (float) i / (ringCount - 1)),
						RenderingConstants.SURFACE_ID_OUTSIDE));
			}
		}

		for (int i = 0; i < ringCount - 1; i++) {
			int ring = i * segments;
			int nextRing = (i + 1) * segments;
			for (int s = 0; s < segments; s++) {
				int s2 = (s + 1) % segments;
				int a = ring + s;
				int b = ring + s2;
				int c = nextRing + s;
				int d = nextRing + s2;
				// Double-sided: emit each quad's two triangles in both windings.
				indices.addTriangle(a, c, b);
				indices.addTriangle(b, c, d);
				indices.addTriangle(a, b, c);
				indices.addTriangle(b, d, c);
			}
		}

		return new Mesh(vertices, indices);
	}

	/**
	 * Returns the rotation-minimizing ring axis at every point of the path, the same frame
	 * {@link #create} carries along it. Consecutive duplicate points repeat the previous axis.
	 */
	public static List<Vector3f> ringFrames(List<Vector3f> points) {
		List<Vector3f> frames = new ArrayList<>(points.size());
		Vector3f previousU = null;
		for (int i = 0; i < points.size(); i++) {
			if (points.size() < 2 || (previousU != null
					&& points.get(i).distanceSquared(points.get(i - 1)) <= 1.0e-6f)) {
				frames.add(previousU != null ? new Vector3f(previousU) : new Vector3f(1, 0, 0));
				continue;
			}
			previousU = nextFrameU(previousU, tangentAt(points, i));
			frames.add(new Vector3f(previousU));
		}
		return frames;
	}

	private static Vector3f tangentAt(List<Vector3f> points, int i) {
		int last = points.size() - 1;
		Vector3f tangent = points.get(Math.min(i + 1, last)).sub(points.get(Math.max(i - 1, 0)), new Vector3f());
		if (tangent.lengthSquared() < 1.0e-12f) {
			tangent.set(0.0f, 1.0f, 0.0f);
		}
		return tangent.normalize();
	}

	private static Vector3f nextFrameU(Vector3f previousU, Vector3f tangent) {
		Vector3f u = null;
		if (previousU != null) {
			// Remove the tangential component of the previous u to get the new perpendicular.
			u = new Vector3f(previousU).sub(new Vector3f(tangent).mul(previousU.dot(tangent)));
		}
		if (u == null || u.lengthSquared() < 1.0e-10f) {
			u = new Vector3f(tangent).cross(referenceAxis(tangent));
		}
		return u.normalize();
	}

	/** A world axis that is not near-parallel to the tangent, for seeding the initial ring frame. */
	private static Vector3f referenceAxis(Vector3f tangent) {
		return Math.abs(tangent.y) < 0.9f ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
	}
}
