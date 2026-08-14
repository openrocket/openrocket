package info.openrocket.swing.gui.figure3d.scene.properties;

/** Logical window and physical framebuffer dimensions for a 3D viewport. */
public final class ViewportDimensions {

	private int windowWidth;
	private int windowHeight;
	private int framebufferWidth;
	private int framebufferHeight;

	/**
	 * Creates viewport dimensions with initial values.
	 *
	 * @param windowWidth The logical window width in pixels
	 * @param windowHeight The logical window height in pixels
	 * @param framebufferWidth The framebuffer width in pixels
	 * @param framebufferHeight The framebuffer height in pixels
	 */
	public ViewportDimensions(int windowWidth, int windowHeight, int framebufferWidth, int framebufferHeight) {
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.framebufferWidth = framebufferWidth;
		this.framebufferHeight = framebufferHeight;
	}

	/**
	 * Gets the logical window width in pixels.
	 *
	 * @return the window width as reported by the windowing system
	 */
	public int getWindowWidth() {
		return windowWidth;
	}

	/**
	 * Gets the logical window height in pixels.
	 *
	 * @return the window height as reported by the windowing system
	 */
	public int getWindowHeight() {
		return windowHeight;
	}

	/**
	 * Gets the physical framebuffer width in pixels.
	 *
	 * @return the actual OpenGL framebuffer width
	 */
	public int getFramebufferWidth() {
		return framebufferWidth;
	}

	/**
	 * Gets the physical framebuffer height in pixels.
	 *
	 * @return the actual OpenGL framebuffer height
	 */
	public int getFramebufferHeight() {
		return framebufferHeight;
	}

	/**
	 * Updates all viewport dimensions in a single operation.
	 * This method is typically called during window resize events.
	 *
	 * @param windowWidth the new logical window width
	 * @param windowHeight the new logical window height
	 * @param framebufferWidth the new physical framebuffer width
	 * @param framebufferHeight the new physical framebuffer height
	 */
	public void update(int windowWidth, int windowHeight, int framebufferWidth, int framebufferHeight) {
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.framebufferWidth = framebufferWidth;
		this.framebufferHeight = framebufferHeight;
	}

	/**
	 * Gets the aspect ratio based on framebuffer dimensions.
	 *
	 * @return The aspect ratio (width / height)
	 */
	public float getAspectRatio() {
		return framebufferHeight > 0 ? (float) framebufferWidth / framebufferHeight : 1.0f;
	}

	/**
	 * Gets the horizontal scaling factor from window to framebuffer coordinates.
	 *
	 * @return The horizontal scale factor
	 */
	public float getHorizontalScale() {
		return windowWidth > 0 ? (float) framebufferWidth / windowWidth : 1.0f;
	}

	/**
	 * Gets the vertical scaling factor from window to framebuffer coordinates.
	 *
	 * @return The vertical scale factor
	 */
	public float getVerticalScale() {
		return windowHeight > 0 ? (float) framebufferHeight / windowHeight : 1.0f;
	}

	/**
	 * Converts a window X coordinate to framebuffer coordinates.
	 *
	 * @param windowX The X coordinate in window space
	 * @return The X coordinate in framebuffer space
	 */
	public int windowToFramebufferX(int windowX) {
		return Math.round(windowX * getHorizontalScale());
	}

	/**
	 * Converts a window Y coordinate to framebuffer coordinates.
	 *
	 * @param windowY The Y coordinate in window space
	 * @return The Y coordinate in framebuffer space
	 */
	public int windowToFramebufferY(int windowY) {
		return Math.round(windowY * getVerticalScale());
	}

	@Override
	public String toString() {
		return String.format("ViewportDimensions{window=%dx%d, framebuffer=%dx%d, scale=%.2fx%.2f}",
				windowWidth, windowHeight, framebufferWidth, framebufferHeight,
				getHorizontalScale(), getVerticalScale());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		ViewportDimensions that = (ViewportDimensions) obj;
		return windowWidth == that.windowWidth &&
			   windowHeight == that.windowHeight &&
			   framebufferWidth == that.framebufferWidth &&
			   framebufferHeight == that.framebufferHeight;
	}

	@Override
	public int hashCode() {
		int result = windowWidth;
		result = 31 * result + windowHeight;
		result = 31 * result + framebufferWidth;
		result = 31 * result + framebufferHeight;
		return result;
	}
}
