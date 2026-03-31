package info.openrocket.swing.gui.figure3d.utils;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * A utility class for color space conversions.
 */
public final class ColorUtils {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ColorUtils() {}

	/**
	 * Converts a color from sRGB space to linear space.
	 * Uses the standard sRGB transfer function.
	 *
	 * @param srgbColor The input color in sRGB space.
	 * @return A new Vector3f representing the color in linear space.
	 */
	public static Vector3f srgbToLinear(Vector3f srgbColor) {
		return new Vector3f(
				srgbChannelToLinear(srgbColor.x),
				srgbChannelToLinear(srgbColor.y),
				srgbChannelToLinear(srgbColor.z)
		);
	}

	/**
	 * Converts a color from sRGB space to linear space, preserving the alpha channel.
	 * Uses the standard sRGB transfer function.
	 *
	 * @param srgbColor The input color in sRGB space (with alpha).
	 * @return A new Vector4f representing the color in linear space (with alpha).
	 */
	public static Vector4f srgbToLinear(Vector4f srgbColor) {
		return new Vector4f(
				srgbChannelToLinear(srgbColor.x),
				srgbChannelToLinear(srgbColor.y),
				srgbChannelToLinear(srgbColor.z),
				srgbColor.w
		);
	}

	private static float srgbChannelToLinear(float srgb) {
		if (srgb <= 0.04045f) {
			return srgb / 12.92f;
		}
		return (float) Math.pow((srgb + 0.055f) / 1.055f, 2.4f);
	}
}
