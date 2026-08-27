package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import org.joml.Vector4f;

/** Background represented by one RGBA color. */
public class SolidColorBackground implements Background {

	private final Vector4f color;

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
	public void cleanup() {
	}
}
