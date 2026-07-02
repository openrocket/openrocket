package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.particles.Particle;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.smoke.SmokeEmitter;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
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
 * Basic line-based particle renderer for general-purpose particle systems.
 * 
 * This renderer serves as the fallback for particle systems that don't require
 * specialized rendering techniques. It renders particles as line segments with
 * streak effects based on velocity, creating simple but effective visual feedback
 * for particle motion.
 * 
 * Features:
 * - Line-based rendering with velocity-based streaks
 * - Alpha blending for smooth visual integration
 * - Dynamic buffer management for varying particle counts
 * - Low computational overhead suitable for high particle counts
 * - Compatible with all particle emitter types as a fallback
 * 
 * Rendering technique:
 * - Each particle is rendered as a line from current position to a position
 *   offset by velocity, creating a motion streak effect
 * - Uses GL_LINES primitive for hardware-accelerated line rendering
 * - Supports transparency blending for particle layering
 */
public class ParticleRenderer implements ParticleSystemRenderer {

    private GLShader shader;
    private int vao;
    private int vbo;
    private FloatBuffer buffer;
    private int maxParticles = RenderingConstants.DEFAULT_MAX_PARTICLES;
    private float streakLengthFactor = 0.05f;

    /**
     * Creates a new basic particle renderer with default settings.
     * 
     * Initializes shaders, vertex buffers, and rendering state for line-based
     * particle rendering. Sets up vertex attributes for position and color data.
     * 
     * @throws Exception If shader compilation or OpenGL resource creation fails
     */
    public ParticleRenderer() throws Exception {
        shader = new GLShader("/shaders/particle_vertex.glsl", "/shaders/particle_fragment.glsl");
        buffer = MemoryUtil.memAllocFloat(maxParticles * 2 * 6); // 2 vertices per particle, 6 floats per vertex

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

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

    /**
     * Renders all particles in the scene as velocity-streaked lines.
     * 
     * Iterates through all particle emitters in the scene and renders their particles
     * as line segments. Each line extends from the particle's current position to a
     * position offset by its velocity, creating a motion trail effect.
     * 
     * @param scene The scene containing particle emitters to render
     * @param camera The camera for view and projection matrices
     */
    public void render(SceneView scene, Camera camera) {
        shader.use();
        shader.setUniformMatrix4f("projection", camera.getProjectionMatrix());
        shader.setUniformMatrix4f("view", camera.getViewMatrix());

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
                if (vertexCount >= maxParticles * 2) break;

                Vector3f p2 = new Vector3f(particle.position).add(new Vector3f(particle.velocity).mul(streakLengthFactor));

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
    public boolean canHandle(ParticleEmitter emitter) {
        // Basic particle renderer can handle any emitter as fallback
        return true;
    }

    @Override
    public int getPriority() {
        return -10; // Low priority - this is the fallback renderer
    }

    @Override
    public void setMaxParticles(int maxParticles) {
        this.maxParticles = maxParticles;
        // Reallocate buffer if needed
        if (buffer != null) {
            MemoryUtil.memFree(buffer);
        }
        buffer = MemoryUtil.memAllocFloat(maxParticles * 2 * 6);
    }

    @Override
    public int getMaxParticles() {
        return maxParticles;
    }

    @Override
    public String getRendererName() {
        return "Basic Particle GLRenderer";
    }

    @Override
    public int getRenderOrder() {
        return 2000; // Render basic particles last
    }

    @Override
    public boolean requiresDepthSorting() {
        return false; // Line particles don't need depth sorting
    }

    @Override
    public boolean supportsBatching() {
        return true; // Can batch all particles together
    }
	
    @Override
    public void cleanup() {
        shader.cleanup();
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        MemoryUtil.memFree(buffer);
    }
}
