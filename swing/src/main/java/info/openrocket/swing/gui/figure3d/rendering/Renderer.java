package info.openrocket.swing.gui.figure3d.rendering;


import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.window.WindowManager;

/**
 * Core interface for the OpenRocket 3D rendering system.
 * 
 * Defines the contract for rendering engines that transform 3D scene data into
 * 2D output. Implementations handle the complete rendering pipeline including
 * geometry rendering, lighting, materials, post-processing, and particle systems.
 */
public interface Renderer {
	/**
	 * Renders a single frame of the given scene to the active framebuffer.
	 * 
	 * This method performs the complete rendering pipeline including geometry
	 * rendering, lighting calculations, material application, particle systems,
	 * and post-processing effects.
	 * 
	 * @param scene The scene containing geometry, lights, camera, and particle systems to render
	 * @param windowManager The window manager for viewport and context information
	 * @param renderBackground Whether to render the scene background or leave it transparent
	 */
    void render(SceneView scene, WindowManager windowManager, boolean renderBackground);

	/**
	 * Draws the last rendered off-screen frame into the currently bound framebuffer.
	 * 
	 * Implementations typically use a full-screen quad to display the resolved
	 * color texture that was produced during {@link #render(SceneView, WindowManager, boolean)}.
	 */
	void presentResolvedToCurrentFramebuffer();

	/**
	 * @return the OpenGL texture id that contains the color output of the most recent frame
	 */
	int getResolvedTextureId();

	/**
	 * @return the framebuffer object id whose color attachment stores the resolved frame
	 */
	int getResolvedFramebufferId();

	/**
	 * @return the width, in pixels, of the renderer's framebuffer
	 */
	int getRenderWidth();

	/**
	 * @return the height, in pixels, of the renderer's framebuffer
	 */
	int getRenderHeight();

	/**
	 * Resizes underlying framebuffers/textures to match the viewport.
	 *
	 * @param width framebuffer width in pixels
	 * @param height framebuffer height in pixels
	 */
	void resize(int width, int height);

	/**
	 * Resets any cached OpenGL texture binding state.
	 *
	 * Useful when external code binds textures outside the renderer's control.
	 */
	void resetTextureState();

	/**
	 * Cleans up all rendering resources and releases GPU memory.
	 *
	 * This method should be called when the renderer is no longer needed.
	 * It releases all OpenGL resources including shaders, buffers, textures,
	 * and framebuffer objects.
	 */
	void cleanup();

	/**
	 * Hint that the user is currently interacting with the camera. Implementations
	 * may skip expensive post-processing passes (AO, motion blur, outline) while
	 * this is true to keep drag-rotate / pan / zoom responsive.
	 */
	default void setInteractionMode(boolean active) {}
}
