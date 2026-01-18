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
	private boolean scaleFromTop = true;

	public void setScaleFromTop(boolean scaleFromTop) {
		this.scaleFromTop = scaleFromTop;
	}

	/**
	 * Calculates a 4x4 transformation matrix for the texture coordinates.
	 * The transformation is applied in the order: scale, then rotate, then translate.
	 *
	 * @param dest The matrix to store the result in.
	 * @return The destination matrix.
	 */
	public Matrix4f getTransformMatrix(Matrix4f dest) {
		// Build a 4x4 matrix for the 2D transformation.
		// Apply offset after scaling so the offset is not scaled.
		// Optionally scale around the top edge (v=1) to match legacy behavior.
		// Order here is reverse of application to coordinates (post-multiply).
		dest.identity()
				.translate(offset.x, offset.y, 0.0f)
				.rotate(rotation, 0, 0, 1);
		if (scaleFromTop) {
			dest.translate(0.0f, 1.0f, 0.0f)
				.scale(scale.x, scale.y, 1.0f)
				.translate(0.0f, -1.0f, 0.0f);
		} else {
			dest.scale(scale.x, scale.y, 1.0f);
		}
		return dest;
	}
}
