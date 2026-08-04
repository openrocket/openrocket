package info.openrocket.swing.gui.figure3d.scene.graph;

import org.joml.Vector3f;

/** Directional or point light used by the scene renderer. */
public class Light {

	/**
	 * Defines the types of lights supported by the scene management system.
	 */
	public enum LightType {
		/** Directional light with parallel rays, like sunlight */
		DIRECTIONAL,
		/** Point light that emits in all directions from a specific position */
		POINT
	}

	private final Vector3f color;
	private final Vector3f position;  // Used for point lights
	private final Vector3f direction; // Used for directional lights
	private final LightType type;

	/**
	 * Creates a directional light (like the sun).
	 * @param direction The direction the light is shining IN.
	 * @param color The color of the light.
	 */
	private Light(Vector3f direction, Vector3f color) {
		this.type = LightType.DIRECTIONAL;
		this.direction = new Vector3f(direction).normalize();
		this.color = new Vector3f(color);
		this.position = new Vector3f(0, 0, 0); // Not used, but initialized
	}

	/**
	 * Creates a point light.
	 * @param position The position of the light in world space.
	 * @param color The color of the light.
	 */
	private Light(Vector3f position, Vector3f color, boolean isPointLight) {
		if (!isPointLight) {
			throw new IllegalArgumentException("Use the directional light constructor.");
		}
		this.type = LightType.POINT;
		this.position = new Vector3f(position);
		this.color = new Vector3f(color);
		this.direction = new Vector3f(0, 0, 0); // Not used, but initialized
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
	
	/**
	 * Builder class for creating Light instances with clear configuration.
	 * 
	 * <p>This builder provides a fluent API for creating lights with explicit
	 * type specification and optional intensity/attenuation settings.</p>
	 * 
	 * <h3>Usage Examples:</h3>
	 * <pre>
	 * // Directional light (sun-like)
	 * Light sun = Light.directional()
	 *     .withDirection(-0.5f, -1.0f, -0.3f)
	 *     .withColor(1.0f, 0.95f, 0.8f)
	 *     .withIntensity(1.2f)
	 *     .build();
	 * 
	 * // Point light (lamp-like)
	 * Light lamp = Light.point()
	 *     .withPosition(2.0f, 5.0f, 1.0f)
	 *     .withColor(1.0f, 0.8f, 0.6f)
	 *     .withIntensity(0.8f)
	 *     .build();
	 * 
	 * // Preset lights
	 * Light sunlight = Light.sunlight();
	 * Light studioKey = Light.studioKeyLight();
	 * </pre>
	 */
	public static class Builder {
		private LightType type;
		private Vector3f position = new Vector3f(0, 0, 0);
		private Vector3f direction = new Vector3f(0, -1, 0);
		private Vector3f color = new Vector3f(1, 1, 1);
		private float intensity = 1.0f;
		
		private Builder(LightType type) {
			this.type = type;
		}
		
		/**
		 * Sets the light direction (for directional lights).
		 * @param x X component of direction
		 * @param y Y component of direction
		 * @param z Z component of direction
		 * @return This builder instance
		 */
		public Builder withDirection(float x, float y, float z) {
			this.direction = new Vector3f(x, y, z);
			return this;
		}
		
		/**
		 * Sets the light direction (for directional lights).
		 * @param direction The direction vector
		 * @return This builder instance
		 */
		public Builder withDirection(Vector3f direction) {
			this.direction = new Vector3f(direction);
			return this;
		}
		
		/**
		 * Sets the light position (for point lights).
		 * @param x X coordinate
		 * @param y Y coordinate
		 * @param z Z coordinate
		 * @return This builder instance
		 */
		public Builder withPosition(float x, float y, float z) {
			this.position = new Vector3f(x, y, z);
			return this;
		}
		
		/**
		 * Sets the light position (for point lights).
		 * @param position The position vector
		 * @return This builder instance
		 */
		public Builder withPosition(Vector3f position) {
			this.position = new Vector3f(position);
			return this;
		}
		
		/**
		 * Sets the light color.
		 * @param r Red component (0.0 to 1.0+)
		 * @param g Green component (0.0 to 1.0+)
		 * @param b Blue component (0.0 to 1.0+)
		 * @return This builder instance
		 */
		public Builder withColor(float r, float g, float b) {
			this.color = new Vector3f(r, g, b);
			return this;
		}
		
		/**
		 * Sets the light color.
		 * @param color The color vector
		 * @return This builder instance
		 */
		public Builder withColor(Vector3f color) {
			this.color = new Vector3f(color);
			return this;
		}
		
		/**
		 * Sets the light intensity (multiplies the color).
		 * @param intensity The intensity multiplier
		 * @return This builder instance
		 */
		public Builder withIntensity(float intensity) {
			this.intensity = intensity;
			return this;
		}
		
		/**
		 * Configures the light as warm white (yellowish).
		 * @return This builder instance
		 */
		public Builder withWarmWhite() {
			this.color = new Vector3f(1.0f, 0.9f, 0.7f);
			return this;
		}
		
		/**
		 * Configures the light as cool white (bluish).
		 * @return This builder instance
		 */
		public Builder withCoolWhite() {
			this.color = new Vector3f(0.9f, 0.95f, 1.0f);
			return this;
		}
		
		/**
		 * Builds the Light with the configured settings.
		 * @return A new Light instance
		 */
		public Light build() {
			// Apply intensity to color
			Vector3f finalColor = new Vector3f(color).mul(intensity);
			
			if (type == LightType.DIRECTIONAL) {
				return new Light(direction, finalColor);
			} else {
				return new Light(position, finalColor, true);
			}
		}
	}
	
	/**
	 * Creates a builder for a directional light.
	 * @return A new builder for directional lights
	 */
	public static Builder directional() {
		return new Builder(LightType.DIRECTIONAL);
	}
	
	/**
	 * Creates a builder for a point light.
	 * @return A new builder for point lights
	 */
	public static Builder point() {
		return new Builder(LightType.POINT);
	}
	
	// Preset light configurations
	
	/**
	 * Creates a sunlight-like directional light.
	 * @return A warm, bright directional light from above
	 */
	public static Light sunlight() {
		return directional()
			.withDirection(-0.3f, -1.0f, -0.4f)
			.withColor(1.0f, 0.95f, 0.8f)
			.withIntensity(1.2f)
			.build();
	}
	
	/**
	 * Creates a studio key light (main light in photography).
	 * @return A bright directional light from the side
	 */
	public static Light studioKeyLight() {
		return directional()
			.withDirection(-0.7f, -0.5f, -0.5f)
			.withColor(1.0f, 1.0f, 1.0f)
			.withIntensity(1.0f)
			.build();
	}
	
	/**
	 * Creates a studio fill light (secondary light in photography).
	 * @return A softer directional light from the opposite side
	 */
	public static Light studioFillLight() {
		return directional()
			.withDirection(0.5f, -0.3f, 0.8f)
			.withCoolWhite()
			.withIntensity(0.6f)
			.build();
	}
	
	/**
	 * Creates a rim light (backlight in photography).
	 * @return A light that creates edge highlights
	 */
	public static Light rimLight() {
		return directional()
			.withDirection(0.0f, 0.8f, -1.0f)
			.withColor(0.8f, 0.8f, 1.0f)
			.withIntensity(0.4f)
			.build();
	}
	
	/**
	 * Creates a warm table lamp-like point light.
	 * @return A warm point light at a typical lamp height
	 */
	public static Light tableLamp() {
		return point()
			.withPosition(0.5f, 2.0f, 0.5f)
			.withWarmWhite()
			.withIntensity(0.8f)
			.build();
	}
}
