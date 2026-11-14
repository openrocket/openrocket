package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.geometry.basic.AxesGenerator;
import info.openrocket.swing.gui.figure3d.rendering.FrameOverlay;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderableMesh;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * A corner HUD gizmo showing the world's cardinal directions (N/E/S/W and up) oriented by the
 * scene camera, so the viewer can tell which way they are looking during a flight replay. Drawn
 * as a {@link FrameOverlay} into the resolved frame after the scene, in its own small viewport.
 */
final class FlightOrientationGizmo implements FrameOverlay {

	private static final Vector3f X_AXIS = new Vector3f(1.0f, 0.0f, 0.0f);
	private static final Vector3f LABEL_COLOR = new Vector3f(0.96f, 0.96f, 0.98f);

	private final GLShader shader;
	private final int mvpUniform;
	private final int colorUniform;
	private final Renderable arrow;
	private final List<Cardinal> cardinals = new ArrayList<>();

	private record Cardinal(Vector3f direction, Vector3f color, Renderable label) {
	}

	FlightOrientationGizmo() {
		shader = new GLShader("/shaders/gizmo_vertex.glsl", "/shaders/gizmo_fragment.glsl");
		mvpUniform = shader.requireUniformLocation("mvp");
		colorUniform = shader.requireUniformLocation("color");
		arrow = new GLRenderableMesh(originArrow());

		cardinals.add(new Cardinal(new Vector3f(0, 0, -1), new Vector3f(0.95f, 0.27f, 0.27f), letter('N')));
		cardinals.add(new Cardinal(new Vector3f(1, 0, 0), new Vector3f(0.32f, 0.82f, 0.42f), letter('E')));
		cardinals.add(new Cardinal(new Vector3f(0, 0, 1), new Vector3f(0.40f, 0.58f, 1.0f), letter('S')));
		cardinals.add(new Cardinal(new Vector3f(-1, 0, 0), new Vector3f(1.0f, 0.82f, 0.22f), letter('W')));
		cardinals.add(new Cardinal(new Vector3f(0, 1, 0), new Vector3f(0.85f, 0.85f, 0.90f), null));
	}

	@Override
	public void render(Matrix4f cameraViewMatrix, int width, int height) {
		int size = Math.max(90, Math.min(170, Math.min(width, height) / 5));
		int margin = 12;
		int viewportX = Math.max(0, width - size - margin);
		int viewportY = margin;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer previousViewport = stack.mallocInt(4);
			glGetIntegerv(GL_VIEWPORT, previousViewport);
			boolean scissorWasEnabled = glIsEnabled(GL_SCISSOR_TEST);
			boolean cullWasEnabled = glIsEnabled(GL_CULL_FACE);
			boolean depthWasEnabled = glIsEnabled(GL_DEPTH_TEST);

			glViewport(viewportX, viewportY, size, size);
			glEnable(GL_SCISSOR_TEST);
			glScissor(viewportX, viewportY, size, size);
			glClear(GL_DEPTH_BUFFER_BIT);
			glDisable(GL_CULL_FACE);
			glEnable(GL_DEPTH_TEST);

			Matrix4f rotation = new Matrix4f(cameraViewMatrix);
			rotation.m30(0.0f);
			rotation.m31(0.0f);
			rotation.m32(0.0f);
			// Distance chosen so the arrows (reach ~1.1) and their labels (centered at ~1.35, each
			// spanning ~0.2) fit inside the square viewport with margin: half-width at this distance
			// is tan(17.5 deg) * 6.5 ~= 2.05, comfortably beyond the label extent of ~1.56.
			Matrix4f viewProjection = new Matrix4f()
					.perspective((float) Math.toRadians(35.0), 1.0f, 0.1f, 100.0f)
					.translate(0.0f, 0.0f, -6.5f)
					.mul(rotation);
			Matrix4f faceCamera = new Matrix4f(rotation).invert();

			shader.use();
			Matrix4f model = new Matrix4f();
			Matrix4f mvp = new Matrix4f();
			Quaternionf orientation = new Quaternionf();

			for (Cardinal cardinal : cardinals) {
				model.identity().rotate(orientation.rotationTo(X_AXIS, cardinal.direction()));
				shader.setUniformMatrix4f(mvpUniform, mvp.set(viewProjection).mul(model));
				shader.setUniformVector3f(colorUniform, cardinal.color());
				arrow.render();
			}

			for (Cardinal cardinal : cardinals) {
				if (cardinal.label() == null) {
					continue;
				}
				model.identity()
						.translate(new Vector3f(cardinal.direction()).mul(1.35f))
						.mul(faceCamera)
						.scale(0.42f);
				shader.setUniformMatrix4f(mvpUniform, mvp.set(viewProjection).mul(model));
				shader.setUniformVector3f(colorUniform, LABEL_COLOR);
				cardinal.label().render();
			}

			glViewport(previousViewport.get(0), previousViewport.get(1),
					previousViewport.get(2), previousViewport.get(3));
			if (!scissorWasEnabled) {
				glDisable(GL_SCISSOR_TEST);
			}
			if (cullWasEnabled) {
				glEnable(GL_CULL_FACE);
			}
			if (!depthWasEnabled) {
				glDisable(GL_DEPTH_TEST);
			}
		}
	}

	@Override
	public void cleanup() {
		shader.cleanup();
		arrow.cleanup();
		for (Cardinal cardinal : cardinals) {
			if (cardinal.label() != null) {
				cardinal.label().cleanup();
			}
		}
	}

	private static Mesh originArrow() {
		Mesh mesh = AxesGenerator.createArrowMesh(0.8f, 0.05f, 0.34f, 0.12f);
		Vector3f min = mesh.getBoundsMin(new Vector3f());
		for (Vertex vertex : mesh.getVertices()) {
			vertex.position.x -= min.x;
		}
		return mesh;
	}

	private static Renderable letter(char c) {
		return new GLRenderableMesh(buildLetterMesh(strokesFor(c), 0.16f));
	}

	private static float[][] strokesFor(char c) {
		return switch (c) {
			case 'N' -> new float[][] { {0, 0, 0, 1}, {0, 1, 1, 0}, {1, 0, 1, 1} };
			case 'E' -> new float[][] { {0, 0, 0, 1}, {0, 1, 1, 1}, {0, 0.5f, 0.8f, 0.5f}, {0, 0, 1, 0} };
			case 'S' -> new float[][] { {0, 1, 1, 1}, {0, 0.5f, 0, 1}, {0, 0.5f, 1, 0.5f}, {1, 0, 1, 0.5f}, {0, 0, 1, 0} };
			case 'W' -> new float[][] { {0, 1, 0.25f, 0}, {0.25f, 0, 0.5f, 0.55f}, {0.5f, 0.55f, 0.75f, 0}, {0.75f, 0, 1, 1} };
			default -> new float[0][];
		};
	}

	// Builds a letter as flat quads (one per stroke) in the local XY plane, centered on the origin.
	private static Mesh buildLetterMesh(float[][] strokes, float strokeWidth) {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();
		float half = strokeWidth * 0.5f;
		for (float[] stroke : strokes) {
			float x0 = stroke[0] - 0.5f;
			float y0 = stroke[1] - 0.5f;
			float x1 = stroke[2] - 0.5f;
			float y1 = stroke[3] - 0.5f;
			float dx = x1 - x0;
			float dy = y1 - y0;
			float length = (float) Math.sqrt(dx * dx + dy * dy);
			if (length < 1.0e-6f) {
				continue;
			}
			float px = -dy / length * half;
			float py = dx / length * half;
			int base = vertices.size();
			addLetterVertex(vertices, x0 + px, y0 + py);
			addLetterVertex(vertices, x0 - px, y0 - py);
			addLetterVertex(vertices, x1 - px, y1 - py);
			addLetterVertex(vertices, x1 + px, y1 + py);
			indices.addTriangle(base, base + 1, base + 2);
			indices.addTriangle(base, base + 2, base + 3);
		}
		return new Mesh(vertices, indices);
	}

	private static void addLetterVertex(List<Vertex> vertices, float x, float y) {
		vertices.add(new Vertex(new Vector3f(x, y, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f),
				new Vector2f(0.0f, 0.0f), RenderingConstants.SURFACE_ID_OUTSIDE));
	}
}
