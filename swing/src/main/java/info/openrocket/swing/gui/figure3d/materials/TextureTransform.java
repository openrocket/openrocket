package info.openrocket.swing.gui.figure3d.materials;

import org.joml.Matrix4f;
import org.joml.Vector2f;

/**
 * Represents 2D texture coordinate transformations including offset, scale, and rotation.
 * Used to modify how textures are mapped onto 3D surfaces.
 */
public class TextureTransform {

	public Vector2f offset = new Vector2f(0.0f, 0.0f);
	public Vector2f scale = new Vector2f(1.0f, 1.0f);
	public float rotation = 0.0f; // In radians

	/**
	 * Calculates a 4x4 transformation matrix for the texture coordinates.
	 * The transformation is applied in the order: scale, then rotate, then translate.
	 *
	 * @param dest The matrix to store the result in.
	 * @return The destination matrix.
	 */
	public Matrix4f getTransformMatrix(Matrix4f dest) {
		// Build a 4x4 matrix for the 2D transformation
		return dest.identity()
				.scale(scale.x, scale.y, 1.0f)
				.translate(offset.x, offset.y, 0.0f)
				.rotate(rotation, 0, 0, 1);
	}
}