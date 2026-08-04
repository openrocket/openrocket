package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.particles.smoke.SmokeEmitter;
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
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Renders smoke emitters as camera-facing textured billboards, lit by the flames
 * around them.
 *
 * <p>The brightest flame emitter in the scene acts as the light source; how
 * strongly a given emitter responds to it is configurable, over a constant
 * ambient term. Particles start at a fifth of their maximum size and grow
 * linearly with age, holding their opacity for the first 80% of their life and
 * then fading.</p>
 */
public class VolumetricSmokeRenderer implements ParticleSystemRenderer {

	// Smoke quad vertex layout (independent of the mesh layout in Vertex)
	private static final int POSITION_FLOATS = 3;
	private static final int TEX_COORD_FLOATS = 2;
	private static final int COLOR_FLOATS = 4;
	private static final int FLOATS_PER_VERTEX = POSITION_FLOATS + TEX_COORD_FLOATS + COLOR_FLOATS;
	private static final int VERTICES_PER_QUAD = 6;

	/** How much larger than its nominal size a smoke particle is drawn, to give it volume. */
	private static final float VOLUMETRIC_SIZE_MULTIPLIER = 4.0f;
	/** Fraction of its full size a particle starts at, before growing as it disperses. */
	private static final float PARTICLE_BIRTH_SIZE_FRACTION = 0.2f;
	/** Particles smaller than this are not worth a draw. */
	private static final float MIN_VISIBLE_PARTICLE_SIZE = 0.01f;
	private static final float PARTICLE_MAX_ALPHA = 0.7f;
	/** Age at which a particle starts fading, having been fully opaque until then. */
	private static final float PARTICLE_FADE_START = 0.8f;
	/** Age remaining once fading starts. Kept exact rather than derived from the start. */
	private static final float PARTICLE_FADE_SPAN = 0.2f;

	private final GLShader shader;
	private final int vao;
	private final int vbo;
	private final FloatBuffer buffer;
	private int maxQuads = RenderingConstants.SMOKE_MAX_QUADS;
	private final Texture smokeTexture;

	// Uniform locations
	private final int projectionMatrixLocation;
	private final int viewMatrixLocation;
	private final int smokeTextureLocation;
	private final int lightPosLocation;
	private final int lightColorLocation;
	private final int lightIntensityLocation;
	private final int ambientLightLocation;
	private final int lightSensitivityLocation;
	private final Vector3f scratchFlameLight = new Vector3f();
	private final Vector3f scratchFlameLightColor = new Vector3f();

	/**
	 * Creates a new volumetric smoke renderer with lighting integration.
	 * 
	 * Initializes specialized shaders for volumetric smoke effects, sets up
	 * vertex buffers for billboard quad rendering, and loads smoke textures.
	 * Caches uniform locations for efficient per-frame updates.
	 * 
	 * @throws ShaderException If shader compilation fails
	 */
	public VolumetricSmokeRenderer() {
		shader = new GLShader("/shaders/volumetric_smoke_vertex.glsl", "/shaders/volumetric_smoke_fragment.glsl");
		buffer = MemoryUtil.memAllocFloat(maxQuads * VERTICES_PER_QUAD * FLOATS_PER_VERTEX);

		// Cache uniform locations
		projectionMatrixLocation = shader.requireUniformLocation("projection");
		viewMatrixLocation = shader.requireUniformLocation("view");
		smokeTextureLocation = shader.requireUniformLocation("smokeTexture");
		lightPosLocation = shader.requireUniformLocation("lightPos");
		lightColorLocation = shader.requireUniformLocation("lightColor");
		lightIntensityLocation = shader.requireUniformLocation("lightIntensity");
		ambientLightLocation = shader.requireUniformLocation("ambientLight");
		lightSensitivityLocation = shader.requireUniformLocation("lightSensitivity");
		
		smokeTexture = new Texture("/textures/smoke2.png");

		vao = glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao, "VolumetricSmokeRenderer vao");
		vbo = glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, vbo, "VolumetricSmokeRenderer vbo");

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, buffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

		int stride = FLOATS_PER_VERTEX * Float.BYTES;

		// Position
		glVertexAttribPointer(0, POSITION_FLOATS, GL_FLOAT, false, stride, 0);
		glEnableVertexAttribArray(0);

		// Texture coordinates
		glVertexAttribPointer(1, TEX_COORD_FLOATS, GL_FLOAT, false, stride, POSITION_FLOATS * Float.BYTES);
		glEnableVertexAttribArray(1);

		// Color + alpha
		glVertexAttribPointer(2, COLOR_FLOATS, GL_FLOAT, false, stride,
				(POSITION_FLOATS + TEX_COORD_FLOATS) * Float.BYTES);
		glEnableVertexAttribArray(2);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	/**
	 * Renders volumetric smoke particles with dynamic lighting from flame emitters.
	 * 
	 * Automatically detects flame emitters in the scene to use as light sources,
	 * then renders all smoke particles as billboarded quads with lighting calculations.
	 * Each smoke emitter can have different light sensitivity settings.
	 * 
	 * @param scene The scene containing smoke and flame emitters
	 * @param camera The camera for billboard calculations and view matrices
	 */
	public void render(SceneView scene, Camera camera) {
		if (!hasSmokeEmitters(scene)) {
			return;
		}

		shader.use();
		shader.setUniformMatrix4f(projectionMatrixLocation, camera.getProjectionMatrix());
		shader.setUniformMatrix4f(viewMatrixLocation, camera.getViewMatrix());

		bindLighting(scene);

		glActiveTexture(GL_TEXTURE0);
		smokeTexture.bind();
		glUniform1i(smokeTextureLocation, 0);

		glEnable(GL_BLEND);
		glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(false);

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);

		buffer.clear();
		int vertexCount = appendParticleBillboards(scene, camera.getPosition());

		buffer.flip();
		glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
		glDrawArrays(GL_TRIANGLES, 0, vertexCount);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		glDepthMask(true);
		glDisable(GL_BLEND);
	}

	private static boolean hasSmokeEmitters(SceneView scene) {
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			if (emitter instanceof SmokeEmitter) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Lights the smoke from the brightest flame in the scene, falling back to a fixed
	 * warm light when there is none.
	 */
	private void bindLighting(SceneView scene) {
		Vector3f flameLight = scratchFlameLight.set(10.0f, 10.0f, 10.0f);
		Vector3f flameLightColor = scratchFlameLightColor.set(1.0f, 0.9f, 0.8f);
		float flameLightIntensity = 2.0f;

		float maxFlameIntensity = 0.0f;
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			if (emitter instanceof FlameEmitter flameEmitter) {
				float intensity = flameEmitter.calculateEffectiveLightIntensity();

				if (intensity > maxFlameIntensity) {
					maxFlameIntensity = intensity;
					flameLight = flameEmitter.calculateLightPosition(scratchFlameLight);
					flameLightColor = scratchFlameLightColor.set(1.0f, 0.6f, 0.2f);
					flameLightIntensity = intensity + 1.0f;
				}
			}
		}

		glUniform3f(lightPosLocation, flameLight.x, flameLight.y, flameLight.z);
		glUniform3f(lightColorLocation, flameLightColor.x, flameLightColor.y, flameLightColor.z);
		glUniform1f(lightIntensityLocation, flameLightIntensity);
		glUniform3f(ambientLightLocation, 0.2f, 0.2f, 0.3f);
	}

	/**
	 * Fills the vertex buffer with a billboard per live smoke particle, stopping once
	 * the buffer is full.
	 *
	 * @return the number of vertices written
	 */
	private int appendParticleBillboards(SceneView scene, Vector3f cameraPos) {
		int vertexCount = 0;
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			if (!(emitter instanceof SmokeEmitter smokeEmitter)) continue;

			glUniform1f(lightSensitivityLocation, smokeEmitter.getLightSensitivity());

			for (Particle particle : emitter.getParticles()) {
				if (vertexCount >= maxQuads * 6) break;

				// 0 when just born, 1 when about to die.
				float ageRatio = 1.0f - (particle.getLife() / particle.getMaxLife());
				float size = particleSize(particle, ageRatio);
				if (size < MIN_VISIBLE_PARTICLE_SIZE) continue;

				float alpha = particleAlpha(ageRatio) * smokeEmitter.getOpacityMultiplier();

				vertexCount += createParticleBillboard(
						particle.getPosition(), size, alpha, particle.getColor(), cameraPos);
			}

			if (vertexCount >= maxQuads * 6) break;
		}
		return vertexCount;
	}

	/** Grows the particle from a fraction of its full size as it ages, to disperse it. */
	private static float particleSize(Particle particle, float ageRatio) {
		float maxSize = particle.getSize() * VOLUMETRIC_SIZE_MULTIPLIER;
		float minSize = maxSize * PARTICLE_BIRTH_SIZE_FRACTION;
		return minSize + (maxSize - minSize) * ageRatio;
	}

	/** Holds full opacity for most of the particle's life, then fades it out. */
	private static float particleAlpha(float ageRatio) {
		if (ageRatio < PARTICLE_FADE_START) {
			return PARTICLE_MAX_ALPHA;
		}
		float fadeProgress = (ageRatio - PARTICLE_FADE_START) / PARTICLE_FADE_SPAN;
		return PARTICLE_MAX_ALPHA * (1.0f - fadeProgress);
	}

	/**
	 * Creates a camera-facing billboard quad for a single smoke particle.
	 * 
	 * Generates a textured quad that always faces the camera using proper
	 * billboard mathematics. The quad is constructed from two triangles with
	 * appropriate texture coordinates for smoke texture mapping.
	 * 
	 * @param position World position of the smoke particle
	 * @param size Size of the billboard quad
	 * @param alpha Transparency value for volumetric blending
	 * @param color RGB color of the smoke particle
	 * @param cameraPos Camera position for billboard orientation calculation
	 * @return Number of vertices added to the buffer (always 6)
	 */
	private int createParticleBillboard(Vector3f position, float size, float alpha, Vector3f color, Vector3f cameraPos) {
		// Proper camera-facing billboard approach
		Vector3f toCamera = new Vector3f(cameraPos).sub(position);
		
		// Handle edge case where camera is at particle position
		if (toCamera.lengthSquared() < 0.001f) {
			toCamera.set(0, 0, 1);
		} else {
			toCamera.normalize();
		}
		
		// Use world up vector
		Vector3f worldUp = new Vector3f(0, 1, 0);
		
		// Calculate billboard right vector (perpendicular to both toCamera and worldUp)
		Vector3f right = new Vector3f(worldUp).cross(toCamera);
		if (right.lengthSquared() < 0.001f) {
			// Handle case where toCamera is parallel to worldUp
			right.set(1, 0, 0);
		} else {
			right.normalize();
		}
		
		// Calculate billboard up vector (perpendicular to both toCamera and right)
		Vector3f up = new Vector3f(toCamera).cross(right).normalize();
		
		// Scale by size
		right.mul(size);
		up.mul(size);
		
		Vector3f[] vertices = new Vector3f[4];
		vertices[0] = new Vector3f(position).sub(right).sub(up); // Bottom-left
		vertices[1] = new Vector3f(position).add(right).sub(up); // Bottom-right  
		vertices[2] = new Vector3f(position).add(right).add(up); // Top-right
		vertices[3] = new Vector3f(position).sub(right).add(up); // Top-left

		float[][] texCoords = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
		
		// First triangle (0, 1, 2)
		addVertex(vertices[0], texCoords[0], color, alpha);
		addVertex(vertices[1], texCoords[1], color, alpha);
		addVertex(vertices[2], texCoords[2], color, alpha);
		
		// Second triangle (0, 2, 3)
		addVertex(vertices[0], texCoords[0], color, alpha);
		addVertex(vertices[2], texCoords[2], color, alpha);
		addVertex(vertices[3], texCoords[3], color, alpha);

		return 6; // 6 vertices added
	}


	/**
	 * Adds a single vertex to the volumetric smoke rendering buffer.
	 * 
	 * @param position 3D world position of the vertex
	 * @param texCoord 2D texture coordinates [u, v]
	 * @param color RGB color values for the smoke
	 * @param alpha Alpha transparency value for volumetric blending
	 */
	private void addVertex(Vector3f position, float[] texCoord, Vector3f color, float alpha) {
		buffer.put(position.x).put(position.y).put(position.z);
		buffer.put(texCoord[0]).put(texCoord[1]);
		buffer.put(color.x).put(color.y).put(color.z).put(alpha); // Store alpha separately
	}

	@Override
	public boolean canHandle(ParticleEmitter emitter) {
		return emitter instanceof SmokeEmitter;
	}

	@Override
	public int getPriority() {
		return 50; // Medium-high priority for smoke rendering
	}

	@Override
	public void setMaxParticles(int maxParticles) {
		this.maxQuads = maxParticles / 4; // Assuming 4 particles per quad on average
		// Note: In a real implementation, you might want to reallocate the buffer here
	}

	@Override
	public int getMaxParticles() {
		return maxQuads * 4; // Approximate conversion
	}

	@Override
	public String getRendererName() {
		return "Volumetric Smoke Renderer";
	}

	@Override
	public int getRenderOrder() {
		return 1000; // Render smoke after flames
	}

	@Override
	public boolean requiresDepthSorting() {
		return true; // Smoke needs proper alpha blending
	}

	@Override
	public boolean supportsBatching() {
		return false; // Each smoke emitter has different light sensitivity
	}

	@Override
	public void cleanup() {
		shader.cleanup();
		smokeTexture.cleanup();
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao);
		glDeleteVertexArrays(vao);
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, vbo);
		glDeleteBuffers(vbo);
		MemoryUtil.memFree(buffer);
	}
}
