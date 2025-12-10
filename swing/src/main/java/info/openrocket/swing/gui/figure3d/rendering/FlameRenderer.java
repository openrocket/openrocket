package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.particles.Particle;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.flame.FlameEmitter;
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
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Specialized renderer for realistic rocket flame particle systems.
 * 
 * This renderer creates rocket motor flames using billboarded textured quads
 * with particle sizing and opacity techniques (throat constriction, plume expansion, and
 * tapered tips).
 * 
 * Key features:
 * - Flame geometry modeling (throat -> expansion -> taper)
 * - Age-based particle size progression for natural flame shape
 * - Dynamic opacity management for flame intensity variation
 * - Texture-based rendering with flame-specific textures
 * - Billboard orientation for consistent camera-facing appearance
 * - Proper alpha blending for realistic flame translucency
 * - Time-based shader effects for flame flickering
 * 
 * Flame physics simulation:
 * - Initial 5%: Small throat size (60-100% of base size)
 * - Next 40%: Rapid expansion phase (100-250% of base size)
 * - Middle 25%: Maintained plume size (250% of base size)
 * - Final 30%: Sharp taper to point (250% -> 25% of base size)
 * 
 * This creates a visually accurate representation of rocket motor exhaust
 * with proper expansion characteristics and realistic flame behavior.
 */
public class FlameRenderer implements ParticleSystemRenderer {

    private final Shader shader;
    private final int vao;
    private final int vbo;
    private final FloatBuffer buffer;
    private int maxQuads = RenderingConstants.FLAME_MAX_QUADS;
    private final Texture flameTexture;

    // Uniform locations
    private final int projectionMatrixLocation;
    private final int viewMatrixLocation;
    private final int timeLocation;
    private final int flameTextureLocation;
    private final int flickerIntensityLocation;

    /**
     * Initializes the flame rendering pipeline including specialized shaders for
     * flame effects, texture resources, and vertex buffer objects optimized for
     * quad-based billboard rendering.
     * 
     * @throws Exception If shader compilation, texture loading, or OpenGL resource creation fails
     */
    public FlameRenderer() throws Exception {
        shader = new Shader("/shaders/flame_vertex.glsl", "/shaders/flame_fragment.glsl");
        buffer = MemoryUtil.memAllocFloat(maxQuads * 6 * 9); // 6 vertices per quad, 9 floats per vertex

        // Cache uniform locations
        projectionMatrixLocation = shader.getUniformLocation("projection");
        viewMatrixLocation = shader.getUniformLocation("view");
        timeLocation = shader.getUniformLocation("time");
        flameTextureLocation = shader.getUniformLocation("flameTexture");
        flickerIntensityLocation = shader.getUniformLocation("flickerIntensity");
        
        // Reuse smoke texture for flames
        flameTexture = new Texture("/textures/smoke2.png");

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
     * Renders flame particles using realistic rocket exhaust physics.
     * 
     * Creates billboarded quads for each flame particle with size and opacity
     * calculated based on particle age to simulate realistic flame shape progression.
     * Only processes FlameEmitter particle systems.
     * 
     * @param scene The scene containing flame emitters to render
     * @param camera The camera for billboard calculations and matrices
     */
    public void render(SceneView scene, Camera camera) {
        shader.use();
        shader.setUniform(projectionMatrixLocation, camera.getProjectionMatrix());
        shader.setUniform(viewMatrixLocation, camera.getViewMatrix());
        glUniform1f(timeLocation, System.currentTimeMillis() * 0.001f);

        glActiveTexture(GL_TEXTURE0);
        flameTexture.bind();
        glUniform1i(flameTextureLocation, 0);

        // Enable blending for cohesive flame appearance
        glEnable(GL_BLEND);
        // Use standard alpha blending for more unified flame look
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // Keep depth testing enabled but disable depth writes
        // This allows flames to respect depth but still blend properly
        glDepthMask(false);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        buffer.clear();
        int vertexCount = 0;

        // Get camera position for billboarding
        Vector3f cameraPos = camera.getPosition();

        for (ParticleEmitter emitter : scene.getParticleEmitters()) {
            if (!(emitter instanceof FlameEmitter)) continue;
            
            FlameEmitter flameEmitter = (FlameEmitter) emitter;
            glUniform1f(flickerIntensityLocation, flameEmitter.getFlickerIntensity());
            
            // Render flame particles
            for (Particle particle : emitter.getParticles()) {
                if (vertexCount >= maxQuads * 6) break;
                
                // Calculate age progression (0 = just born, 1 = about to die)
                float ageRatio = 1.0f - (particle.getLife() / particle.getMaxLife());
                
                // Create rocket motor flame shape: small base, expand, then taper
                float baseSize = particle.getSize() * 0.8f;
                float size;
                
                if (ageRatio < 0.05f) {
                    // First 5% of life: small at the base (throat)
                    float t = ageRatio / 0.05f; // 0 to 1
                    size = baseSize * (0.6f + 0.4f * t); // Start at 60%, grow to 100%
                } else if (ageRatio < 0.45f) {
                    // Next 40% of life: rapid expansion (exhaust plume)
                    float t = (ageRatio - 0.05f) / 0.40f; // 0 to 1, ensures smooth transition
                    size = baseSize * (1.0f + 1.5f * t); // Expand from 100% to 250%
                } else if (ageRatio < 0.7f) {
                    // Middle 25% of life: maintain size (main plume)
                    size = baseSize * 2.5f;
                } else {
                    // Final 30% of life: taper to sharp point
                    float t = (ageRatio - 0.7f) / 0.3f; // 0 to 1
                    size = baseSize * 2.5f * (1.0f - 0.9f * t); // Shrink to 10% of max size
                }
                
                if (size < 0.01f) continue;
                
                // Alpha: strong at base, fade towards tip
                float alpha;
                if (ageRatio < 0.45f) { // Corresponds to the expansion phase
                    alpha = 1.0f; // Full intensity in base and expansion
                } else if (ageRatio < 0.7f) {
                    alpha = 0.9f; // Slightly dimmer in main plume
                } else {
                    // Fade out in the tapered tip
                    float t = (ageRatio - 0.7f) / 0.3f;
                    alpha = 0.9f * (1.0f - 0.8f * t); // Fade from 0.9 to 0.18
                }
                alpha = Math.max(0.0f, alpha);
                
                // Create billboard quad
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
     * Creates a camera-facing billboard quad for a single flame particle.
     * 
     * Generates two triangles forming a textured quad that always faces the camera.
     * The quad is properly oriented using camera position for consistent appearance
     * from any viewing angle.
     * 
     * @param position World position of the particle
     * @param size Size of the billboard quad
     * @param alpha Transparency value for the particle
     * @param color RGB color of the particle
     * @param cameraPos Camera position for billboard orientation
     * @return Number of vertices added to the buffer (always 6)
     */
    private int createParticleBillboard(Vector3f position, float size, float alpha, Vector3f color, Vector3f cameraPos) {
        // Same billboarding logic as smoke
        Vector3f toCamera = new Vector3f(cameraPos).sub(position);
        
        if (toCamera.lengthSquared() < 0.001f) {
            toCamera.set(0, 0, 1);
        } else {
            toCamera.normalize();
        }
        
        Vector3f worldUp = new Vector3f(0, 1, 0);
        Vector3f right = new Vector3f(worldUp).cross(toCamera);
        if (right.lengthSquared() < 0.001f) {
            right.set(1, 0, 0);
        } else {
            right.normalize();
        }
        
        Vector3f up = new Vector3f(toCamera).cross(right).normalize();
        
        right.mul(size);
        up.mul(size);
        
        Vector3f[] vertices = new Vector3f[4];
        vertices[0] = new Vector3f(position).sub(right).sub(up);
        vertices[1] = new Vector3f(position).add(right).sub(up);
        vertices[2] = new Vector3f(position).add(right).add(up);
        vertices[3] = new Vector3f(position).sub(right).add(up);

        float[][] texCoords = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
        
        // First triangle
        addVertex(vertices[0], texCoords[0], color, alpha);
        addVertex(vertices[1], texCoords[1], color, alpha);
        addVertex(vertices[2], texCoords[2], color, alpha);
        
        // Second triangle
        addVertex(vertices[0], texCoords[0], color, alpha);
        addVertex(vertices[2], texCoords[2], color, alpha);
        addVertex(vertices[3], texCoords[3], color, alpha);

        return 6;
    }

    /**
     * Adds a single vertex to the rendering buffer.
     * 
     * @param position 3D world position of the vertex
     * @param texCoord 2D texture coordinates [u, v]
     * @param color RGB color values
     * @param alpha Alpha transparency value
     */
    private void addVertex(Vector3f position, float[] texCoord, Vector3f color, float alpha) {
        buffer.put(position.x).put(position.y).put(position.z);
        buffer.put(texCoord[0]).put(texCoord[1]);
        buffer.put(color.x).put(color.y).put(color.z).put(alpha);
    }

    @Override
    public boolean canHandle(ParticleEmitter emitter) {
        return emitter instanceof FlameEmitter;
    }

    @Override
    public int getPriority() {
        return 100; // High priority for flame rendering
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
        return "Flame Renderer";
    }

    @Override
    public int getRenderOrder() {
        return 500; // Render flames before smoke but after opaque geometry
    }

    @Override
    public boolean requiresDepthSorting() {
        return true; // Flames need proper alpha blending
    }

    @Override
    public boolean supportsBatching() {
        return true; // Can batch multiple flame emitters
    }

    @Override
    public void cleanup() {
        shader.cleanup();
        flameTexture.cleanup();
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        MemoryUtil.memFree(buffer);
    }
}
