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
	private final Matrix4f legacyMatrix = new Matrix4f();
	private final Matrix4f flipUvMatrix = new Matrix4f();
	private final Matrix4f halfTurnMatrix = new Matrix4f();

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
		legacyMatrix.identity()
				.translate(-center.x, -center.y, 0.0f)
				.rotate(rotation, 0, 0, 1)
				.translate(center.x, center.y, 0.0f)
				.scale(scale.x, scale.y, 1.0f)
				.translate(offset.x, offset.y, 0.0f);

		// Saved decal transforms use the opposite UV orientation. Conjugating the
		// transform preserves offset, scale origin, center, and rotation together.
		flipUvMatrix.identity()
				.translate(1.0f, 1.0f, 0.0f)
				.scale(-1.0f, -1.0f, 1.0f);

		dest.set(flipUvMatrix)
				.mul(legacyMatrix)
				.mul(flipUvMatrix);

		if (!scaleFromTop) {
			halfTurnMatrix.identity()
					.translate(0.5f, 0.5f, 0.0f)
					.rotate((float) Math.PI, 0, 0, 1)
					.translate(-0.5f, -0.5f, 0.0f);
			dest.mul(halfTurnMatrix);
		}

		return dest;
	}
}
