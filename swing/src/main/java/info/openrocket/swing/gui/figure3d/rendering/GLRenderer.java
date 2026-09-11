package info.openrocket.swing.gui.figure3d.rendering;


import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;

/**
 * Contract for an OpenGL renderer that produces and presents a resolved scene frame.
 */
public interface GLRenderer extends GpuResource {
	/**
	 * Renders a single frame of the given scene to the active framebuffer.
	 *
	 * @param scene scene to render
	 * @param renderBackground whether to render the background or leave it transparent
	 */
	void render(SceneView scene, boolean renderBackground);

	/** Draws the last resolved frame into the currently bound framebuffer. */
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

	/**
	 * @param displayScale framebuffer pixels per logical pixel; 1.0 on an unscaled display
	 */
	default void setDisplayScale(float displayScale) {}
}
