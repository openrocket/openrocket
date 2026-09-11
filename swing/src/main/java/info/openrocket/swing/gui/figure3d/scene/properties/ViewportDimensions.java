package info.openrocket.swing.gui.figure3d.scene.properties;

/** Logical and framebuffer dimensions, including HiDPI coordinate conversion. */
public final class ViewportDimensions {

	private int windowWidth;
	private int windowHeight;
	private int framebufferWidth;
	private int framebufferHeight;

	public ViewportDimensions(int windowWidth, int windowHeight, int framebufferWidth, int framebufferHeight) {
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.framebufferWidth = framebufferWidth;
		this.framebufferHeight = framebufferHeight;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public int getFramebufferWidth() {
		return framebufferWidth;
	}

	public int getFramebufferHeight() {
		return framebufferHeight;
	}

	public void update(int windowWidth, int windowHeight, int framebufferWidth, int framebufferHeight) {
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.framebufferWidth = framebufferWidth;
		this.framebufferHeight = framebufferHeight;
	}

	/** @return framebuffer width divided by height, or 1 for a zero height */
	public float getAspectRatio() {
		return framebufferHeight > 0 ? (float) framebufferWidth / framebufferHeight : 1.0f;
	}

	/** @return framebuffer pixels per logical pixel on the X axis */
	public float getHorizontalScale() {
		return windowWidth > 0 ? (float) framebufferWidth / windowWidth : 1.0f;
	}

	/** @return framebuffer pixels per logical pixel on the Y axis */
	public float getVerticalScale() {
		return windowHeight > 0 ? (float) framebufferHeight / windowHeight : 1.0f;
	}

	/** Converts a logical X coordinate to the nearest framebuffer pixel. */
	public int windowToFramebufferX(int windowX) {
		return Math.round(windowX * getHorizontalScale());
	}

	/** Converts a logical Y coordinate to the nearest framebuffer pixel. */
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
