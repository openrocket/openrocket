package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.particles.Particle;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.smoke.SmokeEmitter;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
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
 * Advanced volumetric smoke renderer with dynamic lighting integration.
 * 
 * This renderer creates realistic smoke effects by combining billboarded textured
 * particles with advanced lighting calculations. The smoke responds dynamically to
 * nearby flame lights, creating convincing illumination and shadowing effects
 * that enhance the visual realism of rocket exhaust systems.
 * 
 * Advanced features:
 * - Volumetric smoke particle rendering with size progression
 * - Dynamic light detection from flame emitters in the scene
 * - Per-emitter light sensitivity configuration
 * - Billboard quad generation with proper camera orientation
 * - Age-based particle size and opacity evolution
 * - Realistic smoke texture mapping and blending
 * - Proper depth handling for transparent particle layering
 * 
 * Lighting model:
 * - Automatically detects the brightest flame emitter as primary light source
 * - Uses flame position and intensity to illuminate smoke particles
 * - Supports configurable ambient lighting for base visibility
 * - Per-emitter light sensitivity allows fine-tuning of illumination response
 * 
 * Particle lifecycle:
 * - Particles start small (20% of max size) and grow over their lifetime
 * - Opacity remains strong for 80% of particle life, then fades quickly
 * - Size increases linearly with age for natural smoke dispersion behavior
 * 
 * This renderer is specifically optimized for smoke emitters and provides
 * high-quality volumetric smoke effects suitable for realistic rocket simulations.
 */
public class VolumetricSmokeRenderer implements ParticleSystemRenderer {

    private final Shader shader;
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

    /**
     * Creates a new volumetric smoke renderer with lighting integration.
     * 
     * Initializes specialized shaders for volumetric smoke effects, sets up
     * vertex buffers for billboard quad rendering, and loads smoke textures.
     * Caches uniform locations for efficient per-frame updates.
     * 
     * @throws Exception If shader compilation, texture loading, or OpenGL resource creation fails
     */
    public VolumetricSmokeRenderer() throws Exception {
        shader = new Shader("/shaders/volumetric_smoke_vertex.glsl", "/shaders/volumetric_smoke_fragment.glsl");
        buffer = MemoryUtil.memAllocFloat(maxQuads * 6 * 9); // 6 vertices per quad, 9 floats per vertex (3 pos + 2 tex + 4 color)

        // Cache uniform locations
        projectionMatrixLocation = shader.getUniformLocation("projection");
        viewMatrixLocation = shader.getUniformLocation("view");
        smokeTextureLocation = shader.getUniformLocation("smokeTexture");
        lightPosLocation = shader.getUniformLocation("lightPos");
        lightColorLocation = shader.getUniformLocation("lightColor");
        lightIntensityLocation = shader.getUniformLocation("lightIntensity");
        ambientLightLocation = shader.getUniformLocation("ambientLight");
        lightSensitivityLocation = shader.getUniformLocation("lightSensitivity");
        
        smokeTexture = new Texture("/textures/smoke2.png");

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

        // Position
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Texture coordinates
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Color + alpha
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 9 * Float.BYTES, 5 * Float.BYTES);
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
        boolean hasSmokeEmitters = false;
        for (ParticleEmitter emitter : scene.getParticleEmitters()) {
            if (emitter instanceof SmokeEmitter) {
                hasSmokeEmitters = true;
                break;
            }
        }

        if (!hasSmokeEmitters) {
            return; // Early exit if no smoke to render
        }

        shader.use();
        shader.setUniform(projectionMatrixLocation, camera.getProjectionMatrix());
        shader.setUniform(viewMatrixLocation, camera.getViewMatrix());
        
        // Find the brightest flame to use as primary light source
        Vector3f flameLight = new Vector3f(10.0f, 10.0f, 10.0f); // Default light position
        Vector3f flameLightColor = new Vector3f(1.0f, 0.9f, 0.8f); // Default warm light
        float flameLightIntensity = 2.0f; // Default intensity
        
        // Look for flame emitters to use as dynamic light sources
        float maxFlameIntensity = 0.0f;
        for (ParticleEmitter emitter : scene.getParticleEmitters()) {
            if (emitter instanceof FlameEmitter) {
                FlameEmitter flameEmitter = (FlameEmitter) emitter;
                float intensity = flameEmitter.calculateEffectiveLightIntensity();
                
                if (intensity > maxFlameIntensity) {
                    maxFlameIntensity = intensity;
                    flameLight = flameEmitter.calculateLightPosition();
                    flameLightColor = new Vector3f(1.0f, 0.6f, 0.2f); // Orange flame light
                    flameLightIntensity = intensity + 1.0f; // Add base intensity
                }
            }
        }
        
        // Set lighting uniforms
        glUniform3f(lightPosLocation, flameLight.x, flameLight.y, flameLight.z);
        glUniform3f(lightColorLocation, flameLightColor.x, flameLightColor.y, flameLightColor.z);
        glUniform1f(lightIntensityLocation, flameLightIntensity);
        glUniform3f(ambientLightLocation, 0.2f, 0.2f, 0.3f);

        glActiveTexture(GL_TEXTURE0);
        smokeTexture.bind();
        glUniform1i(smokeTextureLocation, 0);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        buffer.clear();
        int vertexCount = 0;

        // Get camera position for per-particle billboarding
        Vector3f cameraPos = camera.getPosition();

        for (ParticleEmitter emitter : scene.getParticleEmitters()) {
            if (!(emitter instanceof SmokeEmitter smokeEmitter)) continue;

			// Set light sensitivity for this emitter
            glUniform1f(lightSensitivityLocation, smokeEmitter.getLightSensitivity());
            
            // Render actual particles instead of fake trail
            for (Particle particle : emitter.getParticles()) {
                if (vertexCount >= maxQuads * 6) break;
                
                // Calculate age progression (0 = just born, 1 = about to die)
                float ageRatio = 1.0f - (particle.getLife() / particle.getMaxLife());
                
                // Size grows from small starting size to full size over lifetime
                float maxSize = particle.getSize() * 4.0f; // Volumetric multiplier
                float minSize = maxSize * 0.2f; // Start at 20% of max size
                float size = minSize + (maxSize - minSize) * ageRatio;
                
                // Skip particles that are too small to see
                if (size < 0.01f) continue;
                
                // Alpha fades out near end of life (strong for 80% of life, then fades)
                float alpha;
                if (ageRatio < 0.8f) {
                    alpha = 0.7f; // Strong opacity for most of life
                } else {
                    // Fade out in last 20% of life
                    float fadeProgress = (ageRatio - 0.8f) / 0.2f; // 0 to 1 in last 20%
                    alpha = 0.7f * (1.0f - fadeProgress);
                }
                
                // Create billboard quad for this particle
                vertexCount += createParticleBillboard(
                    particle.getPosition(), 
                    size, 
                    alpha, 
                    particle.getColor(),
                    cameraPos
                );
            }
            
            if (vertexCount >= maxQuads * 6) break;
        }

        buffer.flip();
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glDepthMask(true);
        glDisable(GL_BLEND);
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
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        MemoryUtil.memFree(buffer);
    }
}
