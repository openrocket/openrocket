package info.openrocket.swing.gui.figure3d.scene.properties;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.swing.gui.figure3d.constants.CameraConstants;

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
		applyVisualDefaults(config, preferences);
	}

	public static void applyVisualDefaults(RenderingConfiguration config, ApplicationPreferences preferences) {
		VisualEffectsSettings visualEffects = config.getVisualEffects();
		visualEffects.setOriginAxesVisible(isOriginAxesVisible(preferences));
		visualEffects.setLightVisualizersVisible(areLightVisualizersVisible(preferences));
		visualEffects.setRotateRocketOnDrag(isRotateRocketOnDrag(preferences));
		visualEffects.setDragRotationSensitivity(getDragRotationSensitivity(preferences));
		visualEffects.setCaretScaleWithView(isCaretScaleWithView(preferences));
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

	public static boolean isOriginAxesVisible(ApplicationPreferences preferences) {
		return preferences.getBoolean(ApplicationPreferences.OPENGL_SHOW_ORIGIN_AXES, false);
	}

	public static void setOriginAxesVisible(ApplicationPreferences preferences, boolean enabled) {
		preferences.putBoolean(ApplicationPreferences.OPENGL_SHOW_ORIGIN_AXES, enabled);
	}

	public static boolean areLightVisualizersVisible(ApplicationPreferences preferences) {
		return preferences.getBoolean(ApplicationPreferences.OPENGL_SHOW_LIGHT_VISUALIZERS, false);
	}

	public static void setLightVisualizersVisible(ApplicationPreferences preferences, boolean enabled) {
		preferences.putBoolean(ApplicationPreferences.OPENGL_SHOW_LIGHT_VISUALIZERS, enabled);
	}

	public static boolean isRotateRocketOnDrag(ApplicationPreferences preferences) {
		return preferences.getBoolean(ApplicationPreferences.OPENGL_ROTATE_ROCKET_ON_DRAG, true);
	}

	public static void setRotateRocketOnDrag(ApplicationPreferences preferences, boolean enabled) {
		preferences.putBoolean(ApplicationPreferences.OPENGL_ROTATE_ROCKET_ON_DRAG, enabled);
	}

	public static float getDragRotationSensitivity(ApplicationPreferences preferences) {
		double factor = preferences.getDouble(ApplicationPreferences.OPENGL_DRAG_ROTATION_SENSITIVITY, Double.NaN);
		if (!Double.isNaN(factor)) {
			return (float) Math.max(0.05d, factor);
		}

		return CameraConstants.DEFAULT_ROTATION_SENSITIVITY_FACTOR;
	}

	public static void setDragRotationSensitivity(ApplicationPreferences preferences, float sensitivity) {
		preferences.putDouble(ApplicationPreferences.OPENGL_DRAG_ROTATION_SENSITIVITY,
				Math.max(0.05f, sensitivity));
	}

	public static boolean isCaretScaleWithView(ApplicationPreferences preferences) {
		return preferences.getBoolean(ApplicationPreferences.OPENGL_SCALE_CARETS_WITH_VIEW, false);
	}

	public static void setCaretScaleWithView(ApplicationPreferences preferences, boolean enabled) {
		preferences.putBoolean(ApplicationPreferences.OPENGL_SCALE_CARETS_WITH_VIEW, enabled);
	}
}
