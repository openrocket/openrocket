package info.openrocket.swing.gui.figure3d.rendering.backgrounds;

import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import org.joml.Vector3f;

/** Vertical gradient whose endpoint colors are stored in linear space. */
public class GradientBackground implements Background {

	private final Vector3f topColor;
	private final Vector3f bottomColor;
	private final boolean worldAligned;
	// gradient_fragment.glsl blends world-aligned skies with smoothstep(-0.25, 0.35, elevation).
	private static final float HORIZON_ELEVATION_START = -0.25f;
	private static final float HORIZON_ELEVATION_END = 0.35f;

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
		this(srgbTopColor, srgbBottomColor, false);
	}

	private GradientBackground(Vector3f srgbTopColor, Vector3f srgbBottomColor, boolean worldAligned) {
		this.topColor = ColorUtils.srgbToLinear(srgbTopColor);
		this.bottomColor = ColorUtils.srgbToLinear(srgbBottomColor);
		this.worldAligned = worldAligned;
	}

	/**
	 * Creates a horizon-like gradient aligned to world up. Pitching the camera changes the
	 * visible sky/ground balance, unlike the ordinary screen-aligned design background.
	 */
	public static GradientBackground worldAligned(Vector3f srgbTopColor, Vector3f srgbBottomColor) {
		return new GradientBackground(srgbTopColor, srgbBottomColor, true);
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

	/**
	 * The color the sky shows at the horizon, which distant hazed ground should fade into.
	 * A screen-aligned gradient has no horizon; its bottom color is used.
	 *
	 * @param destination receives the linear RGB color
	 * @return the destination
	 */
	public Vector3f getHorizonColor(Vector3f destination) {
		destination.set(bottomColor);
		if (worldAligned) {
			float x = -HORIZON_ELEVATION_START / (HORIZON_ELEVATION_END - HORIZON_ELEVATION_START);
			destination.lerp(topColor, x * x * (3.0f - 2.0f * x));
		}
		return destination;
	}

	public boolean isWorldAligned() {
		return worldAligned;
	}

	@Override
	public void cleanup() {
	}
}
