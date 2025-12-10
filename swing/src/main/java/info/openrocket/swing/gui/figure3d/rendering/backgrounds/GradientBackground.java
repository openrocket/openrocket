package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import org.joml.Vector3f;

/**
 * Vertical gradient background renderer for atmospheric and horizon effects.
 * 
 * Creates smooth color transitions from top to bottom of the viewport, commonly
 * used to simulate sky-to-ground color gradients or atmospheric lighting effects.
 * The gradient provides a more visually interesting background than solid colors
 * while maintaining excellent performance.
 * 
 * Features:
 * - Smooth linear interpolation between top and bottom colors
 * - Automatic sRGB to linear color space conversion for accurate blending
 * - Lightweight rendering with minimal GPU overhead
 * - Perfect for simulating atmospheric gradients and horizon effects
 * - Compatible with fog systems for enhanced atmospheric rendering
 * 
 * The gradient is rendered using a full-screen quad with vertex colors,
 * allowing the GPU to perform smooth interpolation across the entire viewport.
 */
public class GradientBackground implements Background {

	private final Vector3f topColor;
	private final Vector3f bottomColor;
	private final static BackgroundType TYPE = BackgroundType.GRADIENT;

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
	public BackgroundType getType() {
		return TYPE;
	}

	@Override
	public void cleanup() {
		// No resources to clean up for gradient background
	}
}
