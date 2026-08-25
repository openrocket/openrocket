package info.openrocket.swing.gui.figure3d.scene.properties;

import info.openrocket.core.util.MathUtil;

/** Controls quality and performance trade-offs in geometry and rendering. */
public class GraphicsQualitySettings {
	public static final RenderQuality DEFAULT_QUALITY = RenderQuality.HIGH;
	public static final float DEFAULT_XRAY_OPACITY = 0.1f;
	public static final boolean DEFAULT_BACKFACE_CULLING = true;
	public static final boolean DEFAULT_ROUGHNESS_BUMP = true;
	public static final boolean DEFAULT_FXAA = true;
	public static final boolean DEFAULT_MSAA = true;
	public static final boolean DEFAULT_SHADOWS = false;
	public static final boolean DEFAULT_AMBIENT_OCCLUSION = false;
	public static final boolean DEFAULT_REDUCE_EFFECTS_DURING_INTERACTION = false;

	/** Overall quality tiers used by size- and sample-count decisions. */
	public enum RenderQuality {
		/** Lowest GPU cost. */
		LOW,
		/** Balanced detail and GPU cost. */
		MEDIUM,
		/** Highest available detail. */
		HIGH
	}

	private RenderQuality quality = DEFAULT_QUALITY;
	private float xrayOpacity = DEFAULT_XRAY_OPACITY;
	private boolean useBackfaceCulling = DEFAULT_BACKFACE_CULLING;
	private boolean enableRoughnessBump = DEFAULT_ROUGHNESS_BUMP;
	private boolean enableFXAA = DEFAULT_FXAA; // Fast Approximate Anti-Aliasing
	private volatile boolean enableMSAA = DEFAULT_MSAA; // Multisample Anti-Aliasing
	private boolean shadowsEnabled = DEFAULT_SHADOWS;
	private boolean ambientOcclusionEnabled = DEFAULT_AMBIENT_OCCLUSION;
	private volatile boolean reduceEffectsDuringInteraction = DEFAULT_REDUCE_EFFECTS_DURING_INTERACTION;

	public RenderQuality getQuality() {
		return quality;
	}

	public void setQuality(RenderQuality quality) {
		this.quality = quality;
	}

	public float getXrayOpacity() {
		return xrayOpacity;
	}

	public void setXrayOpacity(float xrayOpacity) {
		this.xrayOpacity = xrayOpacity;
	}

	public boolean isBackfaceCullingEnabled() {
		return useBackfaceCulling;
	}

	public void setBackfaceCullingEnabled(boolean useBackfaceCulling) {
		this.useBackfaceCulling = useBackfaceCulling;
	}

	public boolean isRoughnessBumpEnabled() {
		return enableRoughnessBump;
	}

	public void setRoughnessBumpEnabled(boolean enableRoughnessBump) {
		this.enableRoughnessBump = enableRoughnessBump;
	}

	public boolean isFXAAEnabled() {
		return enableFXAA;
	}

	public void setFXAAEnabled(boolean enableFXAA) {
		this.enableFXAA = enableFXAA;
	}

	public boolean isMSAAEnabled() {
		return enableMSAA;
	}

	public void setMSAAEnabled(boolean enableMSAA) {
		this.enableMSAA = enableMSAA;
	}

	/**
	 * Returns the quality tier's scene sample count, or zero when MSAA is disabled.
	 *
	 * @return requested off-screen sample count
	 */
	public int getSceneSampleCount() {
		if (!enableMSAA) {
			return 0;
		}
		return switch (quality) {
			case LOW -> 0;
			case MEDIUM -> 2;
			case HIGH -> 4;
		};
	}

	/** Bump strength at the roughest finish; the smoothest is always 0. */
	private static final float MAX_ROUGHNESS_STRENGTH = 1.2f;
	/**
	 * How strength grows across the roughness range at the levels below HIGH. The finishes
	 * are not spread evenly over that range — everything from polished to regular paint sits
	 * in the first eighth — so a linear ramp makes the ordinary ones grainier than they
	 * should be. An exponent above 1 leaves the roughest alone and pulls the fine ones down.
	 */
	private static final double SOFT_ROUGHNESS_EXPONENT = 1.25;
	/** Grain frequency in noise cells per world unit (1 unit = 50 mm), below HIGH. */
	private static final float SOFT_FINE_FREQUENCY = 14.0f;
	private static final float SOFT_COARSE_FREQUENCY = 8.0f;
	/** Grain frequency at HIGH, which keeps the original finer and more pronounced look. */
	private static final float DETAILED_MAX_FREQUENCY = 300.0f;

	/**
	 * Bump strength to draw a surface of the given roughness with.
	 *
	 * @param roughnessAmount surface roughness normalised to 0 (mirror) .. 1 (roughest)
	 * @return the bump strength, 0 for a surface with no visible grain
	 */
	public float getRoughnessStrength(float roughnessAmount) {
		float amount = MathUtil.clamp(roughnessAmount, 0.0f, 1.0f);
		if (quality == RenderQuality.HIGH) {
			return MAX_ROUGHNESS_STRENGTH * amount;
		}
		return (float) (MAX_ROUGHNESS_STRENGTH * Math.pow(amount, SOFT_ROUGHNESS_EXPONENT));
	}

	/**
	 * Grain frequency, in noise cells per world unit, to draw a surface of the given
	 * roughness with.
	 *
	 * <p>The two levels differ in character, not just in cost. Below HIGH the grain is
	 * coarse and grows with the roughness, which is how a rougher surface actually behaves.
	 * HIGH keeps the original finer, denser grain, which reads as more detailed up close
	 * even though it inverts that relationship.</p>
	 *
	 * @param roughnessAmount surface roughness normalised to 0 (mirror) .. 1 (roughest)
	 * @return grain frequency; higher means smaller features
	 */
	public float getRoughnessFrequency(float roughnessAmount) {
		float amount = MathUtil.clamp(roughnessAmount, 0.0f, 1.0f);
		if (quality == RenderQuality.HIGH) {
			return DETAILED_MAX_FREQUENCY * amount;
		}
		return SOFT_FINE_FREQUENCY + (SOFT_COARSE_FREQUENCY - SOFT_FINE_FREQUENCY) * amount;
	}

	/**
	 * Whether the procedural roughness bump should be evaluated at this quality level.
	 *
	 * <p>It is the most expensive per-fragment work the shader does, so the lowest quality
	 * level skips it regardless of the user's preference — that is what "low" is for.</p>
	 *
	 * @return {@code true} when the bump is both wanted and affordable
	 */
	public boolean isRoughnessBumpRendered() {
		return enableRoughnessBump && quality != RenderQuality.LOW;
	}

	public float getShadowResolutionScale() {
		return switch (quality) {
			case LOW -> 0.6f;
			case MEDIUM -> 1.0f;
			case HIGH -> 1.4f;
		};
	}

	public boolean isShadowsEnabled() {
		return shadowsEnabled;
	}

	public void setShadowsEnabled(boolean enabled) {
		this.shadowsEnabled = enabled;
	}

	public boolean isAmbientOcclusionEnabled() {
		return ambientOcclusionEnabled;
	}

	public void setAmbientOcclusionEnabled(boolean enabled) {
		this.ambientOcclusionEnabled = enabled;
	}

	public boolean shouldReduceEffectsDuringInteraction() {
		return reduceEffectsDuringInteraction;
	}

	public void setReduceEffectsDuringInteraction(boolean reduce) {
		this.reduceEffectsDuringInteraction = reduce;
	}

	/** @return SSAO kernel sample count for the current quality tier */
	public int getAmbientOcclusionSampleCount() {
		return switch (quality) {
			case LOW -> 12;
			case MEDIUM -> 24;
			case HIGH -> 32;
		};
	}

	/** @return screen-space AO radius in view-space units */
	public float getAmbientOcclusionRadius() {
		return switch (quality) {
			case LOW -> 0.30f;
			case MEDIUM -> 0.40f;
			case HIGH -> 0.50f;
		};
	}

	/** @return AO intensity multiplier for the current quality tier */
	public float getAmbientOcclusionStrength() {
		return switch (quality) {
			case LOW -> 0.70f;
			case MEDIUM -> 0.85f;
			case HIGH -> 1.0f;
		};
	}

	/** @return depth bias used to limit AO self-occlusion */
	public float getAmbientOcclusionBias() {
		return 0.015f;
	}

	/** Restores the built-in defaults. */
	public void resetToDefaults() {
		quality = DEFAULT_QUALITY;
		xrayOpacity = DEFAULT_XRAY_OPACITY;
		useBackfaceCulling = DEFAULT_BACKFACE_CULLING;
		enableRoughnessBump = DEFAULT_ROUGHNESS_BUMP;
		enableFXAA = DEFAULT_FXAA;
		enableMSAA = DEFAULT_MSAA;
		shadowsEnabled = DEFAULT_SHADOWS;
		ambientOcclusionEnabled = DEFAULT_AMBIENT_OCCLUSION;
		reduceEffectsDuringInteraction = DEFAULT_REDUCE_EFFECTS_DURING_INTERACTION;
	}
}
