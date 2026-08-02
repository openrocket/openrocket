package info.openrocket.swing.gui.figure3d.utils;

import info.openrocket.core.util.CoordinateIF;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Utility class for converting OpenRocket coordinate system objects to JOML vector types.
 * Provides conversions from OpenRocket's Coordinate class to Vector3f and Vector4f for 3D rendering.
 */
public final class VectorUtils {

	private VectorUtils() {
	}
	/**
	 * Converts an OpenRocket Coordinate to a JOML Vector3f.
	 * @param coordinate the OpenRocket coordinate to convert
	 * @return a Vector3f with x, y, z components
	 */
	public static Vector3f coordinateToVector3f(CoordinateIF coordinate) {
		return new Vector3f((float) coordinate.getX(), (float) coordinate.getY(), (float) coordinate.getZ());
	}

	/**
	 * Converts an OpenRocket Coordinate to a JOML Vector4f.
	 * @param coordinate the OpenRocket coordinate to convert
	 * @return a Vector4f with x, y, z components and weight as w component
	 */
	public static Vector4f coordinateToVector4f(CoordinateIF coordinate) {
		return new Vector4f((float) coordinate.getX(), (float) coordinate.getY(), (float) coordinate.getZ(), (float) coordinate.getWeight());
	}
}
