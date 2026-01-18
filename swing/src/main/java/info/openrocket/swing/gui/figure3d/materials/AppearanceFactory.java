package info.openrocket.swing.gui.figure3d.materials;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.defaults.DefaultAppearance;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ORColor;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * A factory class to create engine-specific Appearance objects
 * from OpenRocket's data model.
 */
public abstract class AppearanceFactory {

	private static final Logger log = LoggerFactory.getLogger(AppearanceFactory.class);

	private static final double[] ROUGHNESS_SIZES = {0.5e-6, 500e-6};
	private static final float[] STRENGTH_VALUES = {0.0f, 0.8f};
	private static final float[] SCALE_VALUES    = {0.0f, 50.0f};

	/**
	 * Creates and configures an engine-specific Appearance object based on an
	 * OpenRocket Appearance object.
	 *
	 * @param component The RocketComponent for which the appearance is being created.
	 * @return A configured Appearance object for the rendering engine.
	 */
	public static Appearance3D createFrom(RocketComponent component) {
		Appearance orAppearance = getAppearance(component);
		ExternalComponent.Finish finish = null;
		if (component instanceof ExternalComponent) {
			finish = ((ExternalComponent) component).getFinish();
		}
		return orAppearanceToAppearance3D(orAppearance, finish);
	}

	public static Appearance3D createFrom(Motor motor) {
		Appearance appearance = DefaultAppearance.getDefaultAppearance(motor);
		Appearance3D appearance3D = orAppearanceToAppearance3D(appearance);
		appearance3D.setShine(0);
		return appearance3D;
	}

	private static Appearance3D orAppearanceToAppearance3D(Appearance orAppearance, ExternalComponent.Finish finish) {
		Appearance3D engineAppearance = new Appearance3D();
		engineAppearance.setRenderStyle(Appearance3D.RenderStyle.SOLID);

		// Map color and opacity from ORColor
		ORColor orColor = orAppearance.getPaint();
		if (orColor != null) {
			engineAppearance.setColor(new Vector3f(orColor.getRed() / 255.0f, orColor.getGreen() / 255.0f, orColor.getBlue() / 255.0f));
			engineAppearance.setOpacity(orColor.getAlpha() / 255.0f);
		}

		// Map shine
		engineAppearance.setShine((float) orAppearance.getShine());

		// Map texture/decal from OpenRocket's Decal object
		Decal orDecal = orAppearance.getTexture();
		if (orDecal != null && orDecal.getImage() != null) {
			ByteBuffer buffer = null;
			try {
				try (InputStream stream = orDecal.getImage().getBytes()) {
					if (stream == null) {
						log.warn("Decal image stream missing for {}", orDecal.getImage().getName());
					} else {
						byte[] bytes = stream.readAllBytes();
						buffer = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
						Texture engineTexture = new Texture(buffer);

						// Set the wrapping mode based on the OpenRocket decal's setting
						if (orDecal.getEdgeMode() == Decal.EdgeMode.REPEAT) {
							engineAppearance.setTextureMode(Appearance3D.TextureMode.REPEAT_BOTH);
						} else {
							engineAppearance.setTextureMode(Appearance3D.TextureMode.STRETCH);
						}

						// If an appearance has a texture, treat it as the main texture and set the render style accordingly.
						engineAppearance.setTexture(engineTexture);
						engineAppearance.setRenderStyle(Appearance3D.RenderStyle.TEXTURED);

						// Map the transformation from the OpenRocket Decal to our TextureTransform.
						TextureTransform transform = engineAppearance.getTextureTransform();
						transform.scale.set((float) orDecal.getScale().getX(), (float) orDecal.getScale().getY());
						transform.offset.set((float) orDecal.getOffset().getX(), (float) orDecal.getOffset().getY());
						transform.rotation = (float) orDecal.getRotation();
					}
				}
			} catch (Exception e) {
				log.error("Failed to load decal image from OpenRocket component.", e);
			} finally {
				// Always free the native buffer
				if (buffer != null) {
					MemoryUtil.memFree(buffer);
				}
			}
		}

		// Roughness
		if (finish != null) {
			double roughnessSize = finish.getRoughnessSize(); // in meters
			applyFinishRoughness(engineAppearance, roughnessSize);
		}

		return engineAppearance;
	}

	private static Appearance3D orAppearanceToAppearance3D(Appearance orAppearance) {
		return orAppearanceToAppearance3D(orAppearance, null);
	}

	private static Appearance getAppearance(RocketComponent c) {
		Appearance ret = c.getAppearance();
		if (ret == null) {
			ret = DefaultAppearance.getDefaultAppearance(c);
		}
		return ret;
	}

	/**
	 * Applies OR finish surface roughness settings to the engine appearance.
	 * <p>
	 * 0.5 microns (0.5e-6 m) -> no roughness
	 * 5 microns (5.0e-6 m) -> slight roughness
	 * 60 microns (60.0e-6 m) -> regular roughness
	 * 250 microns (250.0e-6 m) -> quite rough
	 * 500 microns (500.0e-6 m) -> really rough
	 * <p>
	 * @param engineAppearance The Appearance3D object to modify.
	 * @param roughnessSize The surface roughness size in meters.
	 */
	private static void applyFinishRoughness(Appearance3D engineAppearance, double roughnessSize) {
		// Clamp to the lower bound
		if (roughnessSize <= ROUGHNESS_SIZES[0]) {
			engineAppearance.setRoughnessStrength(STRENGTH_VALUES[0]);
			engineAppearance.setRoughnessScale(SCALE_VALUES[0]);
			return;
		}

		// Clamp to the upper bound
		int lastIndex = ROUGHNESS_SIZES.length - 1;
		if (roughnessSize >= ROUGHNESS_SIZES[lastIndex]) {
			engineAppearance.setRoughnessStrength(STRENGTH_VALUES[lastIndex]);
			engineAppearance.setRoughnessScale(SCALE_VALUES[lastIndex]);
			return;
		}

		// Find the segment that contains the roughnessSize
		int i = 0;
		while (i < lastIndex && roughnessSize > ROUGHNESS_SIZES[i + 1]) {
			i++;
		}

		// Interpolate both strength and scale within the segment
		float strength = interpolate(roughnessSize, ROUGHNESS_SIZES[i], ROUGHNESS_SIZES[i + 1], STRENGTH_VALUES[i], STRENGTH_VALUES[i + 1]);
		float scale = interpolate(roughnessSize, ROUGHNESS_SIZES[i], ROUGHNESS_SIZES[i + 1], SCALE_VALUES[i], SCALE_VALUES[i + 1]);

		engineAppearance.setRoughnessStrength(strength);
		engineAppearance.setRoughnessScale(scale);
	}

	/**
	 * Performs linear interpolation between two points.
	 */
	private static float interpolate(double x, double x1, double x2, float y1, float y2) {
		if (x1 >= x2) {
			return y1; // Avoid division by zero or invalid range
		}
		double factor = (x - x1) / (x2 - x1);
		return (float) (y1 + factor * (y2 - y1));
	}
}
