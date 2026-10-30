package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.rendering.FrameOverlay;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderableMesh;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Circles the tracked rocket in red once it is too small on screen to find by eye, e.g. when it
 * climbs away from the pad or the whole flight is in view. Drawn as a screen-space ring after
 * the scene, so it keeps a constant, readable size however far away the rocket is.
 */
final class FlightTargetMarker implements FrameOverlay {
	private static final Vector3f COLOR = new Vector3f(0.95f, 0.16f, 0.12f);
	// Circle the rocket while it spans less than this fraction of the view height.
	static final float SMALL_FRACTION = 0.03f;
	private static final float MIN_RADIUS_FRACTION = 0.018f;
	private static final float RING_INNER_RATIO = 0.84f;
	private static final int RING_SEGMENTS = 48;

	private final Camera camera;
	private final GLShader shader;
	private final int mvpUniform;
	private final int colorUniform;
	private final Renderable ring;
	private Vector3f targetPosition;
	private float targetSize;

	/** A ring to draw: center and radius in framebuffer pixels (origin bottom left). */
	record Circle(float x, float y, float radius) {
	}

	/** Must be created on the GL thread. */
	FlightTargetMarker(Camera camera) {
		this.camera = camera;
		shader = new GLShader("/shaders/gizmo_vertex.glsl", "/shaders/gizmo_fragment.glsl");
		mvpUniform = shader.requireUniformLocation("mvp");
		colorUniform = shader.requireUniformLocation("color");
		ring = new GLRenderableMesh(ringMesh());
	}

	/** Sets the tracked body's world center and its largest extent; call on the GL thread. */
	void setTarget(Vector3f position, float size) {
		targetPosition = position != null ? new Vector3f(position) : null;
		targetSize = size;
	}

	@Override
	public void render(Matrix4f cameraViewMatrix, int width, int height) {
		Vector3f target = targetPosition;
		if (target == null) {
			return;
		}
		Circle circle = circleFor(camera.getProjectionMatrix(), cameraViewMatrix, target, targetSize, width, height);
		if (circle == null) {
			return;
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer previousViewport = stack.mallocInt(4);
			glGetIntegerv(GL_VIEWPORT, previousViewport);
			boolean scissorWasEnabled = glIsEnabled(GL_SCISSOR_TEST);
			boolean cullWasEnabled = glIsEnabled(GL_CULL_FACE);
			boolean depthWasEnabled = glIsEnabled(GL_DEPTH_TEST);

			glViewport(0, 0, width, height);
			glDisable(GL_SCISSOR_TEST);
			glDisable(GL_CULL_FACE);
			glDisable(GL_DEPTH_TEST);

			Matrix4f mvp = new Matrix4f().ortho(0.0f, width, 0.0f, height, -1.0f, 1.0f)
					.translate(circle.x(), circle.y(), 0.0f)
					.scale(circle.radius());
			shader.use();
			shader.setUniformMatrix4f(mvpUniform, mvp);
			shader.setUniformVector3f(colorUniform, COLOR);
			ring.render();

			glViewport(previousViewport.get(0), previousViewport.get(1),
					previousViewport.get(2), previousViewport.get(3));
			if (scissorWasEnabled) {
				glEnable(GL_SCISSOR_TEST);
			}
			if (cullWasEnabled) {
				glEnable(GL_CULL_FACE);
			}
			if (depthWasEnabled) {
				glEnable(GL_DEPTH_TEST);
			}
		}
	}

	/**
	 * Returns where to circle a target of the given world size, or null when it needs no
	 * marker: it is large enough to see, behind the camera, or outside the view.
	 */
	static Circle circleFor(Matrix4f projection, Matrix4f view, Vector3f target, float worldSize,
			int width, int height) {
		if (width <= 0 || height <= 0 || worldSize <= 0.0f) {
			return null;
		}
		Vector4f clip = new Vector4f(target, 1.0f).mul(view).mul(projection);
		if (clip.w <= 0.0f) {
			return null;
		}
		float ndcX = clip.x / clip.w;
		float ndcY = clip.y / clip.w;
		if (Math.abs(ndcX) > 1.0f || Math.abs(ndcY) > 1.0f) {
			return null;
		}
		// Projected size: projection.m11() is 1 / tan(fov / 2), w the depth in front of the eye.
		float pixelSize = worldSize * projection.m11() / clip.w * height * 0.5f;
		if (pixelSize >= SMALL_FRACTION * height) {
			return null;
		}
		float radius = Math.max(MIN_RADIUS_FRACTION * height, pixelSize * 0.5f + 0.01f * height);
		return new Circle((ndcX * 0.5f + 0.5f) * width, (ndcY * 0.5f + 0.5f) * height, radius);
	}

	@Override
	public void cleanup() {
		shader.cleanup();
		ring.cleanup();
	}

	private static Mesh ringMesh() {
		List<Vertex> vertices = new ArrayList<>(RING_SEGMENTS * 2);
		IntList indices = new IntList(RING_SEGMENTS * 6);
		for (int i = 0; i < RING_SEGMENTS; i++) {
			double angle = 2.0 * Math.PI * i / RING_SEGMENTS;
			float cos = (float) Math.cos(angle);
			float sin = (float) Math.sin(angle);
			vertices.add(ringVertex(cos * RING_INNER_RATIO, sin * RING_INNER_RATIO));
			vertices.add(ringVertex(cos, sin));
			int inner = 2 * i;
			int nextInner = 2 * ((i + 1) % RING_SEGMENTS);
			indices.addTriangle(inner, inner + 1, nextInner + 1);
			indices.addTriangle(inner, nextInner + 1, nextInner);
		}
		return new Mesh(vertices, indices);
	}

	private static Vertex ringVertex(float x, float y) {
		return new Vertex(new Vector3f(x, y, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(),
				RenderingConstants.SURFACE_ID_OUTSIDE);
	}
}
