package info.openrocket.swing.gui.figure3d.materials;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.appearance.defaults.DefaultAppearance;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.gui.figure3d.rendering.GLException;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A factory class to create engine-specific Appearance objects
 * from OpenRocket's data model.
 */
public abstract class AppearanceFactory {

	private static final Logger log = LoggerFactory.getLogger(AppearanceFactory.class);

	/** Range of surface roughness the finishes span, in metres. */
	private static final double ROUGHNESS_SIZE_MIN = 0.5e-6;
	private static final double ROUGHNESS_SIZE_MAX = 500e-6;
	private static final DecalTextureCache DEFAULT_DECAL_TEXTURE_CACHE = new DecalTextureCache();
	private static final ThreadLocal<DecalTextureCache> ACTIVE_DECAL_TEXTURE_CACHE = new ThreadLocal<>();

	/** Immutable component appearance state captured on the model-owning thread. */
	public record ComponentAppearanceSnapshot(
			Appearance appearance,
			ExternalComponent.Finish finish,
			boolean scaleTextureFromTop) {
	}

	@FunctionalInterface
	interface TextureLoader {
		Texture load(DecalImage decalImage);
	}

	public static DecalTextureCache createDecalTextureCache() {
		return new DecalTextureCache();
	}

	public static void withDecalTextureCache(DecalTextureCache cache, Runnable task) {
		withDecalTextureCache(cache, () -> {
			task.run();
			return null;
		});
	}

	public static <T> T withDecalTextureCache(DecalTextureCache cache, Supplier<T> task) {
		if (cache == null) {
			return task.get();
		}
		DecalTextureCache previous = ACTIVE_DECAL_TEXTURE_CACHE.get();
		ACTIVE_DECAL_TEXTURE_CACHE.set(cache);
		try {
			return task.get();
		} finally {
			if (previous == null) {
				ACTIVE_DECAL_TEXTURE_CACHE.remove();
			} else {
				ACTIVE_DECAL_TEXTURE_CACHE.set(previous);
			}
		}
	}

	private static DecalTextureCache getActiveDecalTextureCache() {
		DecalTextureCache cache = ACTIVE_DECAL_TEXTURE_CACHE.get();
		return cache != null ? cache : DEFAULT_DECAL_TEXTURE_CACHE;
	}

	/**
	 * Creates and configures an engine-specific Appearance object based on an
	 * OpenRocket Appearance object.
	 *
	 * @param component The RocketComponent for which the appearance is being created.
	 * @return A configured Appearance object for the rendering engine.
	 */
	public static Appearance3D createFrom(RocketComponent component) {
		return createFrom(captureComponentAppearance(component));
	}

	public static Appearance3D createFrom(ComponentAppearanceSnapshot snapshot) {
		Appearance3D appearance3D = new Appearance3D();
		updateFrom(appearance3D, snapshot);
		return appearance3D;
	}

	public static void updateFrom(Appearance3D appearance3D, RocketComponent component) {
		updateFrom(appearance3D, captureComponentAppearance(component));
	}

	public static void updateFrom(Appearance3D appearance3D, ComponentAppearanceSnapshot snapshot) {
		applyOrAppearanceToAppearance3D(appearance3D, snapshot.appearance(), snapshot.finish());
		appearance3D.getTextureTransform().setScaleFromTop(snapshot.scaleTextureFromTop());
	}

	public static ComponentAppearanceSnapshot captureComponentAppearance(RocketComponent component) {
		Appearance appearance = getAppearance(component);
		ExternalComponent.Finish finish = null;
		if (component instanceof ExternalComponent) {
			finish = ((ExternalComponent) component).getFinish();
		}
		return new ComponentAppearanceSnapshot(appearance, finish, !(component instanceof FinSet));
	}

	public static Appearance3D createDefaultFrom(RocketComponent component) {
		Appearance defaultAppearance = DefaultAppearance.getDefaultAppearance(component);
		Appearance3D appearance3D = new Appearance3D();
		applyOrAppearanceToAppearance3D(appearance3D, defaultAppearance, null);
		appearance3D.getTextureTransform().setScaleFromTop(!(component instanceof FinSet));
		return appearance3D;
	}

	public static Appearance3D createFrom(Motor motor) {
		Appearance appearance = DefaultAppearance.getDefaultAppearance(motor);
		Appearance3D appearance3D = new Appearance3D();
		applyOrAppearanceToAppearance3D(appearance3D, appearance, null);
		appearance3D.setShine(0);
		return appearance3D;
	}

	private static void applyOrAppearanceToAppearance3D(Appearance3D engineAppearance, Appearance orAppearance, ExternalComponent.Finish finish) {
		engineAppearance.setRenderStyle(Appearance3D.RenderStyle.SOLID);

		// Map color and opacity from ORColor
		ORColor orColor = orAppearance.getPaint();
		if (orColor != null) {
			engineAppearance.setColor(new Vector3f(orColor.getRed() / 255.0f, orColor.getGreen() / 255.0f, orColor.getBlue() / 255.0f));
			engineAppearance.setOpacity(orColor.getAlpha() / 255.0f);
		}
		engineAppearance.setOpacityAffectsTexture(orAppearance.isOpacityAffectsTexture());

		// Map shine
		engineAppearance.setShine((float) orAppearance.getShine());

		// Map texture/decal from OpenRocket's Decal object
		Decal orDecal = orAppearance.getTexture();
		if (orDecal != null && orDecal.getImage() != null) {
			updateTexture(engineAppearance, orDecal);
		} else {
			engineAppearance.clearTexture();
		}

		// Roughness. Only external components carry a surface finish; everything else
		// (internal components, motors) renders smooth. Leaving those at the bump-map
		// default made parts like inner tubes, which are often visible from the outside,
		// look rougher than a default-painted body tube.
		if (finish != null) {
			applyFinishRoughness(engineAppearance, finish.getRoughnessSize()); // roughness size in meters
		} else {
			engineAppearance.setRoughnessAmount(0.0f);
		}
	}

	private static void updateTexture(Appearance3D engineAppearance, Decal orDecal) {
		DecalImage decalImage = orDecal.getImage();
		Texture engineTexture = engineAppearance.getTexture();
		if (engineTexture == null || engineAppearance.getTextureSourceImage() != decalImage
				|| !isCachedTextureCurrent(decalImage, engineTexture)) {
			engineAppearance.clearTexture();
			engineTexture = getCachedTexture(decalImage);
			engineAppearance.setTexture(engineTexture, false);
			engineAppearance.setTextureSourceImage(decalImage);
		}

		if (engineTexture != null) {
			if (orDecal.getEdgeMode() == Decal.EdgeMode.REPEAT) {
				engineAppearance.setTextureMode(Appearance3D.TextureMode.REPEAT);
			} else {
				engineAppearance.setTextureMode(Appearance3D.TextureMode.STRETCH);
			}

			engineAppearance.setRenderStyle(Appearance3D.RenderStyle.TEXTURED);

			TextureTransform transform = engineAppearance.getTextureTransform();
			transform.scale.set((float) orDecal.getScale().getX(), (float) orDecal.getScale().getY());
			transform.offset.set((float) orDecal.getOffset().getX(), (float) orDecal.getOffset().getY());
			transform.center.set((float) orDecal.getCenter().getX(), (float) orDecal.getCenter().getY());
			transform.rotation = (float) orDecal.getRotation();
		}
	}

	private static Texture getCachedTexture(DecalImage decalImage) {
		return getActiveDecalTextureCache().getTexture(decalImage);
	}

	private static boolean isCachedTextureCurrent(DecalImage decalImage, Texture texture) {
		return getActiveDecalTextureCache().isCurrent(decalImage, texture);
	}

	public static void clearCachedDecalTextures() {
		getActiveDecalTextureCache().cleanup();
	}

	static void setTextureLoaderForTesting(TextureLoader loader) {
		DEFAULT_DECAL_TEXTURE_CACHE.setTextureLoaderForTesting(loader);
	}

	static void clearCachedDecalTexturesForTesting(boolean cleanupTextures) {
		DEFAULT_DECAL_TEXTURE_CACHE.clearForTesting(cleanupTextures);
	}

	public static class DecalTextureCache {
		private final Object lock = new Object();
		private final Map<DecalImage, CachedDecalTexture> cache = new IdentityHashMap<>();
		private final Map<DecalImage, DecalImageState> imageStates = new IdentityHashMap<>();
		private final List<Texture> staleTextures = new ArrayList<>();
		private TextureLoader textureLoader = AppearanceFactory::loadTextureUncached;

		private Texture getTexture(DecalImage decalImage) {
			DecalImageState state;
			long versionToken;
			synchronized (lock) {
				state = getDecalImageState(decalImage);
				versionToken = getVersionToken(decalImage, state);
				CachedDecalTexture cached = cache.get(decalImage);
				if (cached != null && cached.versionToken == versionToken) {
					return cached.texture;
				}
			}

			Texture texture = textureLoader.load(decalImage);

			synchronized (lock) {
				state = getDecalImageState(decalImage);
				versionToken = getVersionToken(decalImage, state);
				CachedDecalTexture cached = cache.get(decalImage);
				if (cached != null && cached.versionToken == versionToken) {
					if (texture != null) {
						staleTextures.add(texture);
					}
					return cached.texture;
				}
				if (cached != null) {
					staleTextures.add(cached.texture);
				}
				if (texture != null) {
					cache.put(decalImage, new CachedDecalTexture(texture, versionToken));
				} else {
					cache.remove(decalImage);
				}
				return texture;
			}
		}

		private boolean isCurrent(DecalImage decalImage, Texture texture) {
			synchronized (lock) {
				DecalImageState state = getDecalImageState(decalImage);
				CachedDecalTexture cached = cache.get(decalImage);
				return cached != null
						&& cached.texture == texture
						&& cached.versionToken == getVersionToken(decalImage, state);
			}
		}

		private DecalImageState getDecalImageState(DecalImage decalImage) {
			DecalImageState state = imageStates.get(decalImage);
			if (state == null) {
				state = new DecalImageState();
				imageStates.put(decalImage, state);
				decalImage.addChangeListener(state.listener);
			}
			return state;
		}

		private long getVersionToken(DecalImage decalImage, DecalImageState state) {
			long token = state.changeVersion;
			File decalFile = decalImage.getDecalFile();
			if (decalFile != null) {
				token = 31 * token + decalFile.getAbsolutePath().hashCode();
				token = 31 * token + decalFile.lastModified();
				token = 31 * token + decalFile.length();
			}
			return token;
		}

		public void cleanup() {
			clear(true);
		}

		private void setTextureLoaderForTesting(TextureLoader loader) {
			synchronized (lock) {
				textureLoader = loader != null ? loader : AppearanceFactory::loadTextureUncached;
			}
		}

		private void clearForTesting(boolean cleanupTextures) {
			clear(cleanupTextures);
			synchronized (lock) {
				textureLoader = AppearanceFactory::loadTextureUncached;
			}
		}

		private void clear(boolean cleanupTextures) {
			synchronized (lock) {
				if (cleanupTextures) {
					for (CachedDecalTexture cached : cache.values()) {
						if (cached.texture != null) {
							cached.texture.cleanup();
						}
					}
					for (Texture staleTexture : staleTextures) {
						if (staleTexture != null) {
							staleTexture.cleanup();
						}
					}
				}
				for (Map.Entry<DecalImage, DecalImageState> entry : imageStates.entrySet()) {
					entry.getKey().removeChangeListener(entry.getValue().listener);
				}
				cache.clear();
				staleTextures.clear();
				imageStates.clear();
			}
		}

		private class DecalImageState {
			private int changeVersion;
			private final StateChangeListener listener = e -> {
				synchronized (lock) {
					changeVersion++;
				}
			};
		}
	}

	private static class CachedDecalTexture {
		private final Texture texture;
		private final long versionToken;

		private CachedDecalTexture(Texture texture, long versionToken) {
			this.texture = texture;
			this.versionToken = versionToken;
		}
	}

	private static Texture loadTextureUncached(DecalImage decalImage) {
		ByteBuffer buffer = null;
		try {
			try (InputStream stream = decalImage.getBytes()) {
				if (stream == null) {
					log.warn("Decal image stream missing for {}", decalImage.getName());
					return null;
				}
				byte[] bytes = stream.readAllBytes();
				buffer = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
				return new Texture(buffer);
			}
		} catch (GLException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to load decal image from OpenRocket component.", e);
			return null;
		} finally {
			if (buffer != null) {
				MemoryUtil.memFree(buffer);
			}
		}
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
		// Only how rough the surface is; the grain size and bump strength it gets drawn with
		// depend on the render quality and are decided by the renderer.
		engineAppearance.setRoughnessAmount((float) MathUtil.clamp(
				(roughnessSize - ROUGHNESS_SIZE_MIN) / (ROUGHNESS_SIZE_MAX - ROUGHNESS_SIZE_MIN), 0.0, 1.0));
	}
}
