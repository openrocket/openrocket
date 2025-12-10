package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import org.joml.Vector4f;

/**
 * Simple solid color background renderer for uniform backgrounds.
 * 
 * Provides the most basic background rendering using a single uniform color
 * across the entire viewport. This background type offers maximum performance
 * and is ideal for situations where a simple, distraction-free background
 * is desired, or as a fallback when other background types are unavailable.
 * 
 * Features:
 * - Minimal GPU overhead with simple color clearing
 * - Support for RGBA values including transparency
 * - Consistent appearance regardless of viewing angle
 * - Perfect for technical diagrams and simplified visualizations
 * - Ideal base background for debugging and development
 * 
 * Color handling:
 * - Supports both RGB and RGBA color specifications
 * - Colors are stored as RGBA for consistency with OpenGL
 * - Alpha channel can be used for transparency effects
 * - No color space conversion required (stored as provided)
 */
public class SolidColorBackground implements Background {

	private final Vector4f color;
	private final static BackgroundType TYPE = BackgroundType.SOLID_COLOR;

	/**
	 * Creates a new solid color background with RGBA values.
	 * 
	 * @param r Red component (0.0 to 1.0)
	 * @param g Green component (0.0 to 1.0)
	 * @param b Blue component (0.0 to 1.0)
	 * @param a Alpha component (0.0 to 1.0, where 0.0 is transparent)
	 */
	public SolidColorBackground(float r, float g, float b, float a) {
		this.color = new Vector4f(r, g, b, a);
	}

	/**
	 * Creates a new opaque solid color background with RGB values.
	 * 
	 * Alpha is automatically set to 1.0 (fully opaque).
	 * 
	 * @param r Red component (0.0 to 1.0)
	 * @param g Green component (0.0 to 1.0)
	 * @param b Blue component (0.0 to 1.0)
	 */
	public SolidColorBackground(float r, float g, float b) {
		this(r, g, b, 1.0f);
	}

	/**
	 * Gets the RGBA color of this background.
	 * 
	 * @return The background color as an RGBA vector
	 */
	public Vector4f getColor() {
		return color;
	}

	@Override
	public BackgroundType getType() {
		return TYPE;
	}

	@Override
	public void cleanup() {
		// No resources to clean up for solid color background
	}
}