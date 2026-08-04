package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Renders flame particles as additive camera-facing quads with age-dependent
 * size, opacity, and color.
 */
public class FlameRenderer implements ParticleSystemRenderer {

	/** Floats per vertex: pos(3) + uv(2) + rgba(4) + age(1). */
	private static final int FLOATS_PER_VERTEX = 10;
	/** Vertices per quad (two triangles). */
	private static final int VERTS_PER_QUAD = 6;
	private static final int MAX_QUADS = RenderingConstants.FLAME_MAX_QUADS;
	/** Particles smaller than this are not worth a draw. */
	private static final float MIN_VISIBLE_PARTICLE_SIZE = 0.005f;

	private final GLShader shader;
	private final int vao;
	private final int vbo;
	private final FloatBuffer buffer;
	private final Texture flameTexture;

	// Uniform locations
	private final int projectionMatrixLocation;
	private final int viewMatrixLocation;
	private final int timeLocation;
	private final int flameTextureLocation;
	private final int flickerIntensityLocation;
	private final int exposureScaleLocation;
	private final Vector3f cameraPosition = new Vector3f();
	private final Vector3f billboardToCamera = new Vector3f();
	private final Vector3f billboardRight = new Vector3f();
	private final Vector3f billboardUp = new Vector3f();

	public FlameRenderer() {
		shader = new GLShader("/shaders/flame_vertex.glsl", "/shaders/flame_fragment.glsl");
		buffer = MemoryUtil.memAllocFloat(MAX_QUADS * VERTS_PER_QUAD * FLOATS_PER_VERTEX);

		// Cache uniform locations
		projectionMatrixLocation = shader.requireUniformLocation("projection");
		viewMatrixLocation = shader.requireUniformLocation("view");
		timeLocation = shader.requireUniformLocation("time");
		flameTextureLocation = shader.requireUniformLocation("flameTexture");
		flickerIntensityLocation = shader.requireUniformLocation("flickerIntensity");
		exposureScaleLocation = shader.requireUniformLocation("exposureScale");

		flameTexture = new Texture("/textures/smoke2.png");

		vao = glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao, "FlameRenderer vao");
		vbo = glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, vbo, "FlameRenderer vbo");

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, (long) buffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

		int stride = FLOATS_PER_VERTEX * Float.BYTES;

		// location 0: position (vec3)
		glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
		glEnableVertexAttribArray(0);

		// location 1: texture coordinates (vec2)
		glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		// location 2: color + alpha (vec4)
		glVertexAttribPointer(2, 4, GL_FLOAT, false, stride, 5 * Float.BYTES);
		glEnableVertexAttribArray(2);

		// location 3: ageRatio (float)
		glVertexAttribPointer(3, 1, GL_FLOAT, false, stride, 9 * Float.BYTES);
		glEnableVertexAttribArray(3);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	public void render(SceneView scene, Camera camera) {
		shader.use();
		shader.setUniformMatrix4f(projectionMatrixLocation, camera.getProjectionMatrix());
		shader.setUniformMatrix4f(viewMatrixLocation, camera.getViewMatrix());
		glUniform1f(timeLocation, System.currentTimeMillis() * 0.001f);

		glActiveTexture(GL_TEXTURE0);
		flameTexture.bind();
		glUniform1i(flameTextureLocation, 0);

		// Additive blending for RGB so overlapping particles brighten like real fire.
		// Alpha channel uses standard blending to preserve compositing correctness.
		glEnable(GL_BLEND);
		glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

		// Keep depth testing but disable depth writes for proper blending.
		glDepthMask(false);

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);

		buffer.clear();
		int vertexCount = appendParticleBillboards(scene, camera.getPosition(cameraPosition));

		buffer.flip();
		glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
		glDrawArrays(GL_TRIANGLES, 0, vertexCount);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		glDepthMask(true);
		glDisable(GL_BLEND);
	}

	/**
	 * Fills the vertex buffer with a billboard per live flame particle, stopping once
	 * the buffer is full.
	 *
	 * @return the number of vertices written
	 */
	private int appendParticleBillboards(SceneView scene, Vector3f cameraPos) {
		int vertexCount = 0;
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			if (!(emitter instanceof FlameEmitter flameEmitter)) continue;

			glUniform1f(flickerIntensityLocation, flameEmitter.getFlickerIntensity());
			glUniform1f(exposureScaleLocation, flameEmitter.getExposureScale());
			float flameSizeMultiplier = flameEmitter.getSizeMultiplier();

			for (Particle particle : emitter.getParticles()) {
				if (vertexCount >= MAX_QUADS * VERTS_PER_QUAD) break;

				float ageRatio = 1.0f - (particle.getLife() / particle.getMaxLife());

				float size = plumeSize(particle.getSize(), ageRatio) * flameSizeMultiplier;
				if (size < MIN_VISIBLE_PARTICLE_SIZE) continue;

				vertexCount += createParticleBillboard(
						particle.getPosition(),
						size,
						plumeAlpha(ageRatio),
						particle.getColor(),
						ageRatio,
						cameraPos
				);
			}

			if (vertexCount >= MAX_QUADS * VERTS_PER_QUAD) break;
		}
		return vertexCount;
	}

	/**
	 * The width of the plume over a particle's life: a narrow throat at the nozzle
	 * exit, rapid expansion, a main plume that keeps growing slightly, then a taper
	 * back to a point.
	 */
	private static float plumeSize(float particleSize, float ageRatio) {
		float baseSize = particleSize * 0.8f;
		if (ageRatio < 0.05f) {
			float t = ageRatio / 0.05f;
			return baseSize * (0.5f + 0.5f * t);
		} else if (ageRatio < 0.35f) {
			float t = (ageRatio - 0.05f) / 0.30f;
			return baseSize * (1.0f + 1.2f * t);
		} else if (ageRatio < 0.65f) {
			float t = (ageRatio - 0.35f) / 0.30f;
			return baseSize * (2.2f + 0.2f * t);
		}
		float t = (ageRatio - 0.65f) / 0.35f;
		return baseSize * 2.4f * (1.0f - t * t);
	}

	/**
	 * The opacity of the plume over a particle's life: a ramp up out of the throat, a
	 * strong main body, then a fade towards the tip.
	 *
	 * <p>Kept well below opaque throughout because the blend is additive, so
	 * overlapping particles accumulate.</p>
	 */
	private static float plumeAlpha(float ageRatio) {
		float alpha;
		if (ageRatio < 0.10f) {
			alpha = 0.45f * (ageRatio / 0.10f);
		} else if (ageRatio < 0.50f) {
			alpha = 0.45f;
		} else {
			float t = (ageRatio - 0.50f) / 0.50f;
			alpha = 0.45f * (1.0f - t * t);
		}
		return Math.max(0.0f, alpha);
	}

	private int createParticleBillboard(Vector3f position, float size, float alpha,
										 Vector3f color, float ageRatio, Vector3f cameraPos) {
		billboardToCamera.set(cameraPos).sub(position);
		if (billboardToCamera.lengthSquared() < 0.001f) {
			billboardToCamera.set(0, 0, 1);
		} else {
			billboardToCamera.normalize();
		}

		billboardRight.set(0, 1, 0).cross(billboardToCamera);
		if (billboardRight.lengthSquared() < 0.001f) {
			billboardRight.set(1, 0, 0);
		} else {
			billboardRight.normalize();
		}

		billboardUp.set(billboardToCamera).cross(billboardRight).normalize();
		billboardRight.mul(size);
		billboardUp.mul(size);

		// Two triangles
		addVertex(position.x - billboardRight.x - billboardUp.x,
				position.y - billboardRight.y - billboardUp.y,
				position.z - billboardRight.z - billboardUp.z,
				0.0f, 0.0f, color, alpha, ageRatio);
		addVertex(position.x + billboardRight.x - billboardUp.x,
				position.y + billboardRight.y - billboardUp.y,
				position.z + billboardRight.z - billboardUp.z,
				1.0f, 0.0f, color, alpha, ageRatio);
		addVertex(position.x + billboardRight.x + billboardUp.x,
				position.y + billboardRight.y + billboardUp.y,
				position.z + billboardRight.z + billboardUp.z,
				1.0f, 1.0f, color, alpha, ageRatio);

		addVertex(position.x - billboardRight.x - billboardUp.x,
				position.y - billboardRight.y - billboardUp.y,
				position.z - billboardRight.z - billboardUp.z,
				0.0f, 0.0f, color, alpha, ageRatio);
		addVertex(position.x + billboardRight.x + billboardUp.x,
				position.y + billboardRight.y + billboardUp.y,
				position.z + billboardRight.z + billboardUp.z,
				1.0f, 1.0f, color, alpha, ageRatio);
		addVertex(position.x - billboardRight.x + billboardUp.x,
				position.y - billboardRight.y + billboardUp.y,
				position.z - billboardRight.z + billboardUp.z,
				0.0f, 1.0f, color, alpha, ageRatio);

		return 6;
	}

	private void addVertex(float x, float y, float z, float u, float v, Vector3f color, float alpha, float ageRatio) {
		buffer.put(x).put(y).put(z);
		buffer.put(u).put(v);
		buffer.put(color.x).put(color.y).put(color.z).put(alpha);
		buffer.put(ageRatio);
	}

	@Override
	public void cleanup() {
		shader.cleanup();
		flameTexture.cleanup();
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao);
		glDeleteVertexArrays(vao);
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, vbo);
		glDeleteBuffers(vbo);
		MemoryUtil.memFree(buffer);
	}
}
