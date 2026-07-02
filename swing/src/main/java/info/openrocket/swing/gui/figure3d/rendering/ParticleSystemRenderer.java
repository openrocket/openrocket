package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import org.joml.Matrix4f;

/**
 * Interface for rendering different types of particle systems.
 * Implementations should handle specific particle types (flames, smoke, sparks, etc.)
 * and their unique rendering requirements.
 */
public interface ParticleSystemRenderer {
	
	/**
	 * Renders particle systems from the scene using this renderer.
	 * 
	 * @param scene The scene containing particle emitters
	 * @param camera The camera for view/projection matrices
	 */
    void render(SceneView scene, Camera camera);
	
	/**
	 * Alternative render method with explicit matrices for more control.
	 * Useful for multi-pass rendering or custom camera setups.
	 * 
	 * @param scene The scene containing particle emitters
	 * @param camera The camera for camera position
	 * @param viewMatrix The view matrix to use
	 * @param projectionMatrix The projection matrix to use
	 */
    default void render(SceneView scene, Camera camera, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        render(scene, camera);
    }
	
	/**
	 * Checks if this renderer can handle the given particle emitter type.
	 * This allows the system to automatically route different particle types
	 * to appropriate renderers.
	 * 
	 * @param emitter The particle emitter to check
	 * @return true if this renderer can handle this emitter type
	 */
	boolean canHandle(ParticleEmitter emitter);
	
	/**
	 * Gets the priority of this renderer when multiple renderers can handle
	 * the same emitter type. Higher priority renderers are preferred.
	 * 
	 * @return Priority value (higher = more preferred)
	 */
	default int getPriority() {
		return 0;
	}
	
	/**
	 * Sets the maximum number of particles this renderer can handle.
	 * This is used for performance optimization and memory management.
	 * 
	 * @param maxParticles Maximum particle count
	 */
	void setMaxParticles(int maxParticles);
	
	/**
	 * Gets the current maximum particle limit for this renderer.
	 * 
	 * @return Maximum number of particles this renderer can handle
	 */
	int getMaxParticles();
	
	/**
	 * Gets a human-readable name for this renderer type.
	 * Useful for debugging and user interfaces.
	 * 
	 * @return GLRenderer name (e.g., "Flame GLRenderer", "Volumetric Smoke")
	 */
	String getRendererName();
	
	/**
	 * Gets the rendering order hint for this renderer.
	 * Lower values render earlier, higher values render later.
	 * This helps with proper alpha blending and depth sorting.
	 * 
	 * @return Rendering order (lower = earlier)
	 */
	default int getRenderOrder() {
		return 1000; // Default middle order
	}
	
	/**
	 * Indicates whether this renderer requires depth sorting of particles.
	 * This affects performance but improves visual quality for transparent particles.
	 * 
	 * @return true if particles should be depth-sorted before rendering
	 */
	default boolean requiresDepthSorting() {
		return true;
	}
	
	/**
	 * Indicates whether this renderer supports batched rendering of multiple emitters.
	 * Batched renderers can process multiple compatible emitters in a single draw call.
	 * 
	 * @return true if batching is supported
	 */
	default boolean supportsBatching() {
		return false;
	}
	
	/**
	 * Resizes renderer resources when the viewport changes.
	 * Some renderers may need to adjust based on screen resolution.
	 * 
	 * @param width New viewport width
	 * @param height New viewport height
	 */
	default void resize(int width, int height) {
		// Default implementation does nothing
	}
	
	/**
	 * Cleans up all rendering resources (shaders, textures, buffers).
	 * This should be called when the renderer is no longer needed.
	 */
	void cleanup();
	
	/**
	 * Gets performance statistics for this renderer.
	 * Useful for debugging and optimization.
	 * 
	 * @return Performance info string
	 */
	default String getPerformanceInfo() {
		return getRendererName() + " - Max Particles: " + getMaxParticles();
	}
}
