package info.openrocket.swing.gui.figure3d.rendering.backgrounds;


/**
 * Core interface for 3D scene background rendering systems.
 * 
 * Defines the contract for different background rendering techniques used in
 * OpenRocket's 3D visualization. Backgrounds provide the visual context and
 * environment for rocket simulations, ranging from simple solid colors to
 * complex high dynamic range (HDR) environments.
 * 
 * Supported background types:
 * - Solid colors for simple, uniform backgrounds
 * - Gradients for atmospheric effects and horizon simulation
 * - Skyboxes using cubemap textures for 360-degree environments
 * - HDRI backgrounds for realistic lighting and reflections
 * - Procedural backgrounds for dynamic or computed environments
 * 
 * Background implementations handle their own resource management and
 * provide type identification for specialized rendering passes.
 */
public interface Background {
	/**
	 * Enumeration of supported background rendering techniques.
	 * 
	 * Each type corresponds to a different rendering approach and
	 * requires specific shader programs and rendering logic.
	 */
	enum BackgroundType {
		SOLID_COLOR,
		GRADIENT,
		IMAGE,
		SKYBOX,
		HDRI,
		PROCEDURAL
	}

	/**
	 * Gets the type identifier for this background implementation.
	 * 
	 * This type is used by the rendering system to select appropriate
	 * shaders and rendering techniques for the background.
	 *
	 * @return The background type enum value identifying the rendering technique
	 */
	BackgroundType getType();

	/**
	 * Releases all GPU resources associated with this background.
	 * 
	 * This method should free textures, buffers, and other OpenGL resources
	 * used by the background. Must be called when the background is no longer
	 * needed to prevent resource leaks.
	 */
	void cleanup();
}
