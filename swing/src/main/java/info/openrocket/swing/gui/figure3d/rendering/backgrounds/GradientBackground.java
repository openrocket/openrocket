package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import org.joml.Vector3f;

/** Vertical gradient whose endpoint colors are stored in linear space. */
public class GradientBackground implements Background {

	private final Vector3f topColor;
	private final Vector3f bottomColor;

	/**
	 * Creates a new gradient background with specified top and bottom colors.
	 *
	 * Colors are automatically converted from sRGB to linear color space for
	 * accurate color interpolation during rendering. This ensures proper
	 * gamma correction and realistic color blending.
	 *
	 * @param srgbTopColor The top color in sRGB color space (typically sky color)
	 * @param srgbBottomColor The bottom color in sRGB color space (typically ground color)
	 */
	public GradientBackground(Vector3f srgbTopColor, Vector3f srgbBottomColor) {
		this.topColor = ColorUtils.srgbToLinear(srgbTopColor);
		this.bottomColor = ColorUtils.srgbToLinear(srgbBottomColor);
	}

	/**
	 * Gets the top color of the gradient in linear color space.
	 *
	 * @return The top color as a linear RGB vector
	 */
	public Vector3f getTopColor() {
		return topColor;
	}

	/**
	 * Gets the bottom color of the gradient in linear color space.
	 *
	 * @return The bottom color as a linear RGB vector
	 */
	public Vector3f getBottomColor() {
		return bottomColor;
	}

	@Override
	public void cleanup() {
	}
}
