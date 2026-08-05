package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.particles.smoke.SmokeEmitter;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Renders non-flame, non-smoke particles as velocity streaks using {@code GL_LINES}.
 */
public class ParticleRenderer implements ParticleSystemRenderer {

	private static final int MAX_PARTICLES = RenderingConstants.DEFAULT_MAX_PARTICLES;
	private static final float STREAK_LENGTH_FACTOR = 0.05f;

	private final GLShader shader;
	private final int projectionUniform;
	private final int viewUniform;
	private final int vao;
	private final int vbo;
	private final FloatBuffer buffer;

	public ParticleRenderer() {
		shader = new GLShader("/shaders/particle_vertex.glsl", "/shaders/particle_fragment.glsl");
		projectionUniform = shader.requireUniformLocation("projection");
		viewUniform = shader.requireUniformLocation("view");
		buffer = MemoryUtil.memAllocFloat(MAX_PARTICLES * 2 * 6); // 2 vertices per particle, 6 floats per vertex

		vao = glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao, "ParticleRenderer vao");
		vbo = glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, vbo, "ParticleRenderer vbo");

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, buffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

		// Position
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
		glEnableVertexAttribArray(0);

		// Color
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	/** Renders eligible particles in the scene as velocity-streaked lines. */
	@Override
	public void render(SceneView scene, Camera camera) {
		shader.use();
		shader.setUniformMatrix4f(projectionUniform, camera.getProjectionMatrix());
		shader.setUniformMatrix4f(viewUniform, camera.getViewMatrix());

		glEnable(GL_BLEND);
		glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(false);
		glLineWidth(1.0f);

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);

		buffer.clear();
		int vertexCount = 0;

		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			// Flame and smoke emitters have dedicated renderers. If we also draw them here
			// as line streaks, they show up as stray spark-like artifacts.
			if (emitter instanceof FlameEmitter || emitter instanceof SmokeEmitter) {
				continue;
			}
			for (Particle particle : emitter.getParticles()) {
				if (vertexCount >= MAX_PARTICLES * 2) break;

				Vector3f p2 = new Vector3f(particle.position)
						.add(new Vector3f(particle.velocity).mul(STREAK_LENGTH_FACTOR));

				// Vertex 1 (current position)
				buffer.put(particle.position.x).put(particle.position.y).put(particle.position.z);
				buffer.put(particle.color.x).put(particle.color.y).put(particle.color.z);

				// Vertex 2 (previous position)
				buffer.put(p2.x).put(p2.y).put(p2.z);
				buffer.put(particle.color.x).put(particle.color.y).put(particle.color.z);

				vertexCount += 2;
			}
		}
		buffer.flip();

		glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
		glDrawArrays(GL_LINES, 0, vertexCount);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		glDepthMask(true);
		glDisable(GL_BLEND);
	}

	@Override
	public void cleanup() {
		shader.cleanup();
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao);
		glDeleteVertexArrays(vao);
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, vbo);
		glDeleteBuffers(vbo);
		MemoryUtil.memFree(buffer);
	}
}
