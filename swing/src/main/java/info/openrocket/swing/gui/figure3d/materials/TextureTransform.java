package info.openrocket.swing.gui.figure3d.materials;

import org.joml.Matrix4f;
import org.joml.Vector2f;

/**
 * Represents 2D texture coordinate transformations including offset, scale, and rotation.
 * Used to modify how textures are mapped onto 3D surfaces.
 */
public class TextureTransform {

	public Vector2f offset = new Vector2f(0.0f, 0.0f);
	public Vector2f center = new Vector2f(0.0f, 0.0f);
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
		// T(-center) * R * T(center) * S * T(offset)
		Matrix4f legacy = new Matrix4f()
				.identity()
				.translate(-center.x, -center.y, 0.0f)
				.rotate(rotation, 0, 0, 1)
				.translate(center.x, center.y, 0.0f)
				.scale(scale.x, scale.y, 1.0f)
				.translate(offset.x, offset.y, 0.0f);

		// The new renderer's UV axes are flipped relative to the legacy JOGL path.
		// Conjugate the full legacy transform by a UV flip so offset, scale origin,
		// center and rotation all match the old visual behavior together.
		Matrix4f flipUV = new Matrix4f()
				.identity()
				.translate(1.0f, 1.0f, 0.0f)
				.scale(-1.0f, -1.0f, 1.0f);

		dest.set(flipUV)
				.mul(legacy)
				.mul(flipUV);

		if (!scaleFromTop) {
			Matrix4f halfTurn = new Matrix4f()
					.identity()
					.translate(0.5f, 0.5f, 0.0f)
					.rotate((float) Math.PI, 0, 0, 1)
					.translate(-0.5f, -0.5f, 0.0f);
			dest.mul(halfTurn);
		}

		return dest;
	}
}
