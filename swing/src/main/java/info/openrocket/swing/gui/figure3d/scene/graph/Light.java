package info.openrocket.swing.gui.figure3d.scene.graph;

import org.joml.Vector3f;

/** Directional or point light used by the scene renderer. */
public class Light {

	/**
	 * Defines the types of lights supported by the scene management system.
	 */
	public enum LightType {
		/** Directional light with parallel rays, like sunlight */
		DIRECTIONAL(0),
		/** Point light that emits in all directions from a specific position */
		POINT(1);

		private final int shaderValue;

		LightType(int shaderValue) {
			this.shaderValue = shaderValue;
		}

		public int getShaderValue() {
			return shaderValue;
		}
	}

	private final Vector3f color;
	private final Vector3f position;  // Used for point lights
	private final Vector3f direction; // Used for directional lights
	private final LightType type;

	private Light(LightType type, Vector3f positionOrDirection, Vector3f color) {
		this.type = type;
		this.color = new Vector3f(color);
		if (type == LightType.DIRECTIONAL) {
			this.direction = new Vector3f(positionOrDirection).normalize();
			this.position = new Vector3f();
		} else {
			this.position = new Vector3f(positionOrDirection);
			this.direction = new Vector3f();
		}
	}

	/**
	 * Gets the type of this light source.
	 *
	 * @return the light type (DIRECTIONAL or POINT)
	 */
	public LightType getType() {
		return type;
	}

	/**
	 * Gets the position of this light in world coordinates.
	 * This property is only meaningful for point lights.
	 *
	 * @return the 3D position vector of the light
	 */
	public Vector3f getPosition() {
		return position;
	}

	/**
	 * Sets the position of this light in world coordinates.
	 * This method is only meaningful for point lights.
	 *
	 * @param x the X coordinate of the light position
	 * @param y the Y coordinate of the light position
	 * @param z the Z coordinate of the light position
	 */
	public void setPosition(float x, float y, float z) {
		this.position.set(x, y, z);
	}

	/**
	 * Gets the direction vector of this light.
	 * This property is only meaningful for directional lights.
	 *
	 * @return the normalized direction vector indicating light direction
	 */
	public Vector3f getDirection() {
		return direction;
	}

	/**
	 * Sets the direction of this light.
	 * The direction vector is automatically normalized. This method is only meaningful for directional lights.
	 *
	 * @param x the X component of the light direction
	 * @param y the Y component of the light direction
	 * @param z the Z component of the light direction
	 */
	public void setDirection(float x, float y, float z) {
		this.direction.set(x, y, z).normalize();
	}

	/**
	 * Gets the color and intensity of this light.
	 * Color components can exceed 1.0 for high-intensity lighting effects.
	 *
	 * @return the RGB color vector representing light color and intensity
	 */
	public Vector3f getColor() {
		return color;
	}

	/**
	 * Sets the color and intensity of this light.
	 * Color components can exceed 1.0 for high-intensity lighting effects.
	 *
	 * @param r the red component of the light color
	 * @param g the green component of the light color
	 * @param b the blue component of the light color
	 */
	public void setColor(float r, float g, float b) {
		this.color.set(r, g, b);
	}

	public static Light directional(float x, float y, float z) {
		return new Light(LightType.DIRECTIONAL, new Vector3f(x, y, z), new Vector3f(1.0f));
	}

	public static Light point(Vector3f position, Vector3f color) {
		return new Light(LightType.POINT, position, color);
	}
}
