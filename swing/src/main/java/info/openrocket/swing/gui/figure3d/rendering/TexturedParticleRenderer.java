package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.core.particles.Particle;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.smoke.SmokeEmitter;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.Scene;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
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
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Renders textured particles with per-particle quaternion orientation.
 */
public class TexturedParticleRenderer {

    private Shader shader;
    private int vao;
    private int vbo;
    private FloatBuffer buffer;
    private int maxParticles = 10000;
    private Texture smokeTexture;

    /**
     * Creates a new 3D textured particle renderer with quaternion support.
     * 
     * Initializes shaders for 3D particle transformation, sets up vertex buffers
     * for textured quad rendering, and loads particle textures. Configures vertex
     * attributes for position, texture coordinates, and color data.
     * 
     * @throws Exception If shader compilation, texture loading, or OpenGL resource creation fails
     */
    public TexturedParticleRenderer() throws Exception {
        shader = new Shader("/shaders/textured_particle_vertex.glsl", "/shaders/textured_particle_fragment.glsl");
        buffer = MemoryUtil.memAllocFloat(maxParticles * 6 * 8); // 6 vertices per particle (2 triangles), 8 floats per vertex
        
        // Load smoke texture
        smokeTexture = new Texture("/textures/smoke2.png");

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

        // Position
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Texture coordinates
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Color
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 8 * Float.BYTES, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    /**
     * Renders smoke particles with full 3D orientation using quaternion rotations.
     * 
     * Processes smoke emitters and renders their particles as 3D-oriented textured quads.
     * Each particle's quaternion orientation is applied to transform the quad from local
     * space to world space, allowing for realistic tumbling and rotation effects.
     * 
     * @param scene The scene containing smoke emitters to render
     * @param camera The camera for view and projection matrices
     */
    public void render(Scene scene, Camera camera) {
        shader.use();
        shader.setUniform("projection", camera.getProjectionMatrix());
        shader.setUniform("view", camera.getViewMatrix());

        glActiveTexture(GL_TEXTURE0);
        smokeTexture.bind();
        glUniform1i(shader.getUniformLocation("smokeTexture"), 0);

        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        buffer.clear();
        int vertexCount = 0;

        // Get view matrix for billboard calculations
        Matrix4f viewMatrix = camera.getViewMatrix();
        Vector3f cameraRight = new Vector3f();
        Vector3f cameraUp = new Vector3f();
        
        // Extract right and up vectors from view matrix
        viewMatrix.getColumn(0, cameraRight).negate();
        viewMatrix.getColumn(1, cameraUp);

        for (ParticleEmitter emitter : scene.getParticleEmitters()) {
            // Only render smoke particles with this renderer
            if (!(emitter instanceof SmokeEmitter)) continue;
            
            for (Particle particle : emitter.getParticles()) {
                if (vertexCount >= maxParticles * 6) break;

                // Create 3D rotated quad
                Vector3f position = particle.position;
                float size = particle.size;
                
                // Create rotation matrix from quaternion
                Matrix3f rotationMatrix = new Matrix3f();
                particle.orientation.get(rotationMatrix);
                
                // Define quad corners in local space (before rotation)
                Vector3f[] localVertices = new Vector3f[4];
                localVertices[0] = new Vector3f(-size, -size, 0); // Bottom-left
                localVertices[1] = new Vector3f( size, -size, 0); // Bottom-right
                localVertices[2] = new Vector3f( size,  size, 0); // Top-right
                localVertices[3] = new Vector3f(-size,  size, 0); // Top-left
                
                // Apply 3D rotation and translate to world position
                Vector3f[] vertices = new Vector3f[4];
                for (int i = 0; i < 4; i++) {
                    vertices[i] = new Vector3f();
                    rotationMatrix.transform(localVertices[i], vertices[i]);
                    vertices[i].add(position);
                }

                float[][] texCoords = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
                
                // First triangle (0, 1, 2)
                addVertex(vertices[0], texCoords[0], particle.color);
                addVertex(vertices[1], texCoords[1], particle.color);
                addVertex(vertices[2], texCoords[2], particle.color);
                
                // Second triangle (0, 2, 3)
                addVertex(vertices[0], texCoords[0], particle.color);
                addVertex(vertices[2], texCoords[2], particle.color);
                addVertex(vertices[3], texCoords[3], particle.color);

                vertexCount += 6;
            }
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
     * Adds a single vertex to the 3D particle rendering buffer.
     * 
     * @param position 3D world position of the vertex after transformation
     * @param texCoord 2D texture coordinates [u, v]
     * @param color RGB color values for the particle
     */
    private void addVertex(Vector3f position, float[] texCoord, Vector3f color) {
        buffer.put(position.x).put(position.y).put(position.z);
        buffer.put(texCoord[0]).put(texCoord[1]);
        buffer.put(color.x).put(color.y).put(color.z);
    }

    /**
     * Releases all OpenGL resources used by the textured particle renderer.
     * 
     * Cleans up shaders, textures, vertex arrays, and vertex buffers.
     * Should be called when the renderer is no longer needed.
     */
    public void cleanup() {
        shader.cleanup();
        smokeTexture.cleanup();
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        MemoryUtil.memFree(buffer);
    }
}
