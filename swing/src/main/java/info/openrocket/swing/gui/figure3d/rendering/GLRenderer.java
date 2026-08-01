package info.openrocket.swing.gui.figure3d.rendering;


import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;

/**
 * Core interface for the OpenRocket 3D rendering system.
 *
 * Defines the contract for rendering engines that transform 3D scene data into
 * 2D output. Implementations handle the complete rendering pipeline including
 * geometry rendering, lighting, materials, and post-processing. Particle
 * effects are not part of this contract; renderer implementations delegate
 * them to the {@link ParticleSystemRenderer} instances they own.
 *
 * This interface is specific to the OpenGL 3.3 (LWJGL) backend: its methods
 * expose GL concepts such as framebuffer and texture ids, so implementations
 * are expected to be OpenGL-based.
 */
public interface GLRenderer {
	/**
	 * Renders a single frame of the given scene to the active framebuffer.
	 * 
	 * This method performs the complete rendering pipeline including geometry
	 * rendering, lighting calculations, material application, particle systems,
	 * and post-processing effects.
	 * 
	 * @param scene The scene containing geometry, lights, camera, and particle systems to render
	 * @param renderBackground Whether to render the scene background or leave it transparent
	 */
    void render(SceneView scene, boolean renderBackground);

	/**
	 * Draws the last rendered off-screen frame into the currently bound framebuffer.
	 * 
	 * Implementations typically use a full-screen quad to display the resolved
	 * color texture that was produced during {@link #render(SceneView, boolean)}.
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

	/**
	 * Supplies the centre of gravity and centre of pressure to show, in rocket coordinates.
	 *
	 * Implementations that draw those markers should prefer these over anything they could
	 * compute themselves: the centre of pressure depends on flight conditions that only the
	 * host knows, since the Component Analysis window can override Mach, angle of attack and
	 * roll rate.
	 *
	 * @param cg centre of gravity, or {@code null}/NaN when there is nothing to show
	 * @param cp centre of pressure, or {@code null}/NaN when there is nothing to show
	 */
	default void setCaretPositions(CoordinateIF cg, CoordinateIF cp) {}
}
