package info.openrocket.swing.gui.figure3d.scene.properties;

import info.openrocket.core.preferences.ApplicationPreferences;

/**
 * Maps application preferences onto figure3d rendering defaults.
 */
public final class Figure3DPreferences {

	private Figure3DPreferences() {
	}

	public static void applyQualityDefaults(RenderingConfiguration config, ApplicationPreferences preferences) {
		GraphicsQualitySettings quality = config.getQuality();
		quality.setQuality(getDefaultRenderQuality(preferences));
		quality.setFXAAEnabled(isAntiAliasingEnabled(preferences));
		quality.setShadowsEnabled(isShadowsEnabled(preferences));
		quality.setAmbientOcclusionEnabled(isAmbientOcclusionEnabled(preferences));
		quality.setRoughnessBumpEnabled(isRoughnessBumpEnabled(preferences));
	}

	public static GraphicsQualitySettings.RenderQuality getDefaultRenderQuality(ApplicationPreferences preferences) {
		int maxIndex = GraphicsQualitySettings.RenderQuality.values().length - 1;
		int choice = preferences.getChoice(ApplicationPreferences.OPENGL_RENDER_QUALITY, maxIndex,
				GraphicsQualitySettings.RenderQuality.HIGH.ordinal());
		return GraphicsQualitySettings.RenderQuality.values()[choice];
	}

	public static void setDefaultRenderQuality(ApplicationPreferences preferences,
			GraphicsQualitySettings.RenderQuality quality) {
		preferences.putChoice(ApplicationPreferences.OPENGL_RENDER_QUALITY, quality.ordinal());
	}

	public static boolean isAntiAliasingEnabled(ApplicationPreferences preferences) {
		return preferences.getBoolean(ApplicationPreferences.OPENGL_ENABLE_AA, true);
	}

	public static boolean isShadowsEnabled(ApplicationPreferences preferences) {
		return preferences.getBoolean(ApplicationPreferences.OPENGL_ENABLE_SHADOWS, false);
	}

	public static boolean isAmbientOcclusionEnabled(ApplicationPreferences preferences) {
		return preferences.getBoolean(ApplicationPreferences.OPENGL_ENABLE_AMBIENT_OCCLUSION, false);
	}

	public static boolean isRoughnessBumpEnabled(ApplicationPreferences preferences) {
		return preferences.getBoolean(ApplicationPreferences.OPENGL_ENABLE_ROUGHNESS_BUMP, true);
	}

	public static void setShadowsEnabled(ApplicationPreferences preferences, boolean enabled) {
		preferences.putBoolean(ApplicationPreferences.OPENGL_ENABLE_SHADOWS, enabled);
	}

	public static void setAmbientOcclusionEnabled(ApplicationPreferences preferences, boolean enabled) {
		preferences.putBoolean(ApplicationPreferences.OPENGL_ENABLE_AMBIENT_OCCLUSION, enabled);
	}

	public static void setRoughnessBumpEnabled(ApplicationPreferences preferences, boolean enabled) {
		preferences.putBoolean(ApplicationPreferences.OPENGL_ENABLE_ROUGHNESS_BUMP, enabled);
	}
}
