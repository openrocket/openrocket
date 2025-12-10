package info.openrocket.swing.gui.figure3d.materials;

import org.joml.Vector3f;

/**
 * Interface defining the basic properties of a renderable material.
 * Provides access to color, lighting, and rendering style information.
 */
public interface Material {
	/**
	 * Gets the base color of the material.
	 * @return RGB color vector
	 */
	Vector3f getColor();

	/**
	 * Gets the specular color for highlights.
	 * @return RGB specular color vector
	 */
	Vector3f getSpecularColor();

	/**
	 * Gets the shine/glossiness factor.
	 * @return Shine value between 0.0 and 1.0
	 */
	float getShine();

	/**
	 * Gets the render style for this material.
	 * @return Render style enum
	 */
	Appearance3D.RenderStyle getRenderStyle();
}
