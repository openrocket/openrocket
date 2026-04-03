package info.openrocket.swing.gui.figure3d.scene.properties;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.swing.gui.figure3d.constants.CameraConstants;

/**
 * Maps application preferences onto figure3d rendering defaults.
 */
public final class Figure3DPreferences {
	public record Values(
			GraphicsQualitySettings.RenderQuality renderQuality,
			boolean antiAliasingEnabled,
			boolean shadowsEnabled,
			boolean ambientOcclusionEnabled,
			boolean roughnessBumpEnabled,
			boolean originAxesVisible,
			boolean lightVisualizersVisible,
			boolean rotateRocketOnDrag,
			float dragRotationSensitivity,
			boolean caretScaleWithView) {
	}

	private Figure3DPreferences() {
	}

	public static void applyDefaults(RenderingConfiguration config, ApplicationPreferences preferences) {
		apply(config, load(preferences));
	}

	public static void apply(RenderingConfiguration config, Values values) {
		GraphicsQualitySettings quality = config.getQuality();
		quality.setQuality(values.renderQuality());
		quality.setFXAAEnabled(values.antiAliasingEnabled());
		quality.setShadowsEnabled(values.shadowsEnabled());
		quality.setAmbientOcclusionEnabled(values.ambientOcclusionEnabled());
		quality.setRoughnessBumpEnabled(values.roughnessBumpEnabled());

		VisualEffectsSettings visualEffects = config.getVisualEffects();
		visualEffects.setOriginAxesVisible(values.originAxesVisible());
		visualEffects.setLightVisualizersVisible(values.lightVisualizersVisible());
		visualEffects.setRotateRocketOnDrag(values.rotateRocketOnDrag());
		visualEffects.setDragRotationSensitivity(values.dragRotationSensitivity());
		visualEffects.setCaretScaleWithView(values.caretScaleWithView());
	}

	public static Values load(ApplicationPreferences preferences) {
		return new Values(
				getDefaultRenderQuality(preferences),
				isAntiAliasingEnabled(preferences),
				isShadowsEnabled(preferences),
				isAmbientOcclusionEnabled(preferences),
				isRoughnessBumpEnabled(preferences),
				isOriginAxesVisible(preferences),
				areLightVisualizersVisible(preferences),
				isRotateRocketOnDrag(preferences),
				getDragRotationSensitivity(preferences),
				isCaretScaleWithView(preferences));
	}

	public static void save(ApplicationPreferences preferences, Values values) {
		setDefaultRenderQuality(preferences, values.renderQuality());
		setAntiAliasingEnabled(preferences, values.antiAliasingEnabled());
		setShadowsEnabled(preferences, values.shadowsEnabled());
		setAmbientOcclusionEnabled(preferences, values.ambientOcclusionEnabled());
		setRoughnessBumpEnabled(preferences, values.roughnessBumpEnabled());
		setOriginAxesVisible(preferences, values.originAxesVisible());
		setLightVisualizersVisible(preferences, values.lightVisualizersVisible());
		setRotateRocketOnDrag(preferences, values.rotateRocketOnDrag());
		setDragRotationSensitivity(preferences, values.dragRotationSensitivity());
		setCaretScaleWithView(preferences, values.caretScaleWithView());
	}

	public static void applyQualityDefaults(RenderingConfiguration config, ApplicationPreferences preferences) {
		applyDefaults(config, preferences);
	}

	public static void applyVisualDefaults(RenderingConfiguration config, ApplicationPreferences preferences) {
		Values values = load(preferences);
		VisualEffectsSettings visualEffects = config.getVisualEffects();
		visualEffects.setOriginAxesVisible(values.originAxesVisible());
		visualEffects.setLightVisualizersVisible(values.lightVisualizersVisible());
		visualEffects.setRotateRocketOnDrag(values.rotateRocketOnDrag());
		visualEffects.setDragRotationSensitivity(values.dragRotationSensitivity());
		visualEffects.setCaretScaleWithView(values.caretScaleWithView());
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

	public static void setAntiAliasingEnabled(ApplicationPreferences preferences, boolean enabled) {
		preferences.putBoolean(ApplicationPreferences.OPENGL_ENABLE_AA, enabled);
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
