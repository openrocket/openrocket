package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.spark.SparkEmitter;
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
 * Renders sparks as soft, velocity-aligned ribbons.  The bright leading edge and
 * cooling trail are blended additively so overlapping fragments emit light rather
 * than looking like opaque geometric lines.
 */
public class ParticleRenderer implements ParticleSystemRenderer {

	private static final int MAX_SPARKS = RenderingConstants.DEFAULT_MAX_PARTICLES;
	private static final int VERTICES_PER_SPARK = 6;
	/** Floats per vertex: position(3), trail coordinates(2), color(3), alpha(1). */
	private static final int FLOATS_PER_VERTEX = 9;
	private static final float TRAIL_EXPOSURE_SECONDS = 0.080f;
	private static final float MIN_TRAIL_SECONDS = 0.004f;
	private static final float MIN_VECTOR_LENGTH_SQUARED = 1.0e-8f;
	private static final float COOL_RED = 0.95f;
	private static final float COOL_GREEN = 0.08f;
	private static final float COOL_BLUE = 0.01f;

	private final GLShader shader;
	private final int projectionUniform;
	private final int viewUniform;
	private final int vao;
	private final int vbo;
	private final FloatBuffer buffer;
	private final Vector3f cameraPosition = new Vector3f();
	private final Vector3f cameraRight = new Vector3f();
	private final Vector3f trailStart = new Vector3f();
	private final Vector3f trailSide = new Vector3f();
	private final Vector3f toCamera = new Vector3f();
	private final Vector3f cooledColor = new Vector3f();
	private final Vector3f headColor = new Vector3f();

	public ParticleRenderer() {
		shader = new GLShader("/shaders/particle_vertex.glsl", "/shaders/particle_fragment.glsl");
		projectionUniform = shader.requireUniformLocation("projection");
		viewUniform = shader.requireUniformLocation("view");
		buffer = MemoryUtil.memAllocFloat(MAX_SPARKS * VERTICES_PER_SPARK * FLOATS_PER_VERTEX);

		vao = glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao, "ParticleRenderer vao");
		vbo = glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, vbo, "ParticleRenderer vbo");

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, buffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

		int stride = FLOATS_PER_VERTEX * Float.BYTES;

		// Position
		glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
		glEnableVertexAttribArray(0);

		// Coordinates along and across the spark trail
		glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		// Color
		glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 5 * Float.BYTES);
		glEnableVertexAttribArray(2);

		// Alpha
		glVertexAttribPointer(3, 1, GL_FLOAT, false, stride, 8 * Float.BYTES);
		glEnableVertexAttribArray(3);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	/** Renders spark particles in the scene as glowing velocity streaks. */
	@Override
	public void render(SceneView scene, Camera camera) {
		shader.use();
		shader.setUniformMatrix4f(projectionUniform, camera.getProjectionMatrix());
		shader.setUniformMatrix4f(viewUniform, camera.getViewMatrix());

		glEnable(GL_BLEND);
		glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(false);

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);

		buffer.clear();
		camera.getPosition(cameraPosition);
		camera.getViewMatrix().positiveX(cameraRight);
		int vertexCount = appendSparkRibbons(scene);
		buffer.flip();

		glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
		glDrawArrays(GL_TRIANGLES, 0, vertexCount);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		glDepthMask(true);
		glDisable(GL_BLEND);
	}

	private int appendSparkRibbons(SceneView scene) {
		int vertexCount = 0;
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			// Other particle types have dedicated renderers and should not inherit the
			// spark-specific cooling curve.
			if (!(emitter instanceof SparkEmitter)) {
				continue;
			}
			for (Particle particle : emitter.getParticles()) {
				if (vertexCount + VERTICES_PER_SPARK > MAX_SPARKS * VERTICES_PER_SPARK) {
					return vertexCount;
				}
				if (appendSparkRibbon(particle)) {
					vertexCount += VERTICES_PER_SPARK;
				}
			}
		}
		return vertexCount;
	}

	private boolean appendSparkRibbon(Particle particle) {
		float speedSquared = particle.velocity.lengthSquared();
		if (speedSquared < MIN_VECTOR_LENGTH_SQUARED || particle.getMaxLife() <= 0.0f) {
			return false;
		}

		float ageSeconds = Math.max(0.0f, particle.getMaxLife() - particle.getLife());
		float trailSeconds = Math.min(TRAIL_EXPOSURE_SECONDS,
				Math.max(MIN_TRAIL_SECONDS, ageSeconds));
		trailStart.set(particle.position).fma(-trailSeconds, particle.velocity);

		toCamera.set(cameraPosition)
				.sub((particle.position.x + trailStart.x) * 0.5f,
						(particle.position.y + trailStart.y) * 0.5f,
						(particle.position.z + trailStart.z) * 0.5f);
		trailSide.set(particle.velocity).cross(toCamera);
		if (trailSide.lengthSquared() < MIN_VECTOR_LENGTH_SQUARED) {
			trailSide.set(cameraRight);
		} else {
			trailSide.normalize();
		}

		float ageRatio = clamp(ageSeconds / particle.getMaxLife(), 0.0f, 1.0f);
		float halfWidth = Math.max(0.0025f, particle.size * 1.05f);
		float tailWidth = halfWidth * 0.35f;
		float headWidth = halfWidth;
		float alpha = sparkAlpha(ageRatio);
		calculateSparkColors(particle.color, ageRatio);

		float tailSideX = trailSide.x * tailWidth;
		float tailSideY = trailSide.y * tailWidth;
		float tailSideZ = trailSide.z * tailWidth;
		float headSideX = trailSide.x * headWidth;
		float headSideY = trailSide.y * headWidth;
		float headSideZ = trailSide.z * headWidth;

		addVertex(trailStart.x - tailSideX, trailStart.y - tailSideY, trailStart.z - tailSideZ,
				0.0f, -1.0f, cooledColor, alpha * 0.30f);
		addVertex(trailStart.x + tailSideX, trailStart.y + tailSideY, trailStart.z + tailSideZ,
				0.0f, 1.0f, cooledColor, alpha * 0.30f);
		addVertex(particle.position.x + headSideX, particle.position.y + headSideY,
				particle.position.z + headSideZ, 1.0f, 1.0f, headColor, alpha);

		addVertex(trailStart.x - tailSideX, trailStart.y - tailSideY, trailStart.z - tailSideZ,
				0.0f, -1.0f, cooledColor, alpha * 0.30f);
		addVertex(particle.position.x + headSideX, particle.position.y + headSideY,
				particle.position.z + headSideZ, 1.0f, 1.0f, headColor, alpha);
		addVertex(particle.position.x - headSideX, particle.position.y - headSideY,
				particle.position.z - headSideZ, 1.0f, -1.0f, headColor, alpha);
		return true;
	}

	private void calculateSparkColors(Vector3f initialColor, float ageRatio) {
		float heat = 1.0f - smoothstep(0.08f, 1.0f, ageRatio);
		cooledColor.set(
				mix(COOL_RED, initialColor.x, heat),
				mix(COOL_GREEN, initialColor.y, heat),
				mix(COOL_BLUE, initialColor.z, heat));

		float whiteHeat = 0.78f * (1.0f - smoothstep(0.02f, 0.62f, ageRatio));
		headColor.set(
				mix(cooledColor.x, 1.0f, whiteHeat),
				mix(cooledColor.y, 0.98f, whiteHeat),
				mix(cooledColor.z, 0.82f, whiteHeat));
	}

	private static float sparkAlpha(float ageRatio) {
		float coolingFade = 1.0f - smoothstep(0.68f, 1.0f, ageRatio);
		float ignition = 0.55f + 0.45f * smoothstep(0.0f, 0.025f, ageRatio);
		return 0.90f * coolingFade * ignition;
	}

	private void addVertex(float x, float y, float z, float along, float across,
			Vector3f color, float alpha) {
		buffer.put(x).put(y).put(z);
		buffer.put(along).put(across);
		buffer.put(color.x).put(color.y).put(color.z);
		buffer.put(alpha);
	}

	private static float smoothstep(float start, float end, float value) {
		float amount = clamp((value - start) / (end - start), 0.0f, 1.0f);
		return amount * amount * (3.0f - 2.0f * amount);
	}

	private static float mix(float start, float end, float amount) {
		return start + amount * (end - start);
	}

	private static float clamp(float value, float minimum, float maximum) {
		return Math.max(minimum, Math.min(maximum, value));
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
