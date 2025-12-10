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
	 * This is an approximate conversion using a gamma of 2.2.
	 *
	 * @param srgbColor The input color in sRGB space.
	 * @return A new Vector3f representing the color in linear space.
	 */
	public static Vector3f srgbToLinear(Vector3f srgbColor) {
		return new Vector3f(
				(float) Math.pow(srgbColor.x, 2.2),
				(float) Math.pow(srgbColor.y, 2.2),
				(float) Math.pow(srgbColor.z, 2.2)
		);
	}

	/**
	 * Converts a color from sRGB space to linear space, preserving the alpha channel.
	 * This is an approximate conversion using a gamma of 2.2.
	 *
	 * @param srgbColor The input color in sRGB space (with alpha).
	 * @return A new Vector4f representing the color in linear space (with alpha).
	 */
	public static Vector4f srgbToLinear(Vector4f srgbColor) {
		return new Vector4f(
				(float) Math.pow(srgbColor.x, 2.2),
				(float) Math.pow(srgbColor.y, 2.2),
				(float) Math.pow(srgbColor.z, 2.2),
				srgbColor.w
		);
	}
}
