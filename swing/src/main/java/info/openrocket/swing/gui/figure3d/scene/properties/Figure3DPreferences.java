package info.openrocket.swing.gui.figure3d.scene.properties;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.preferences.DocumentPreferences;
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

	public static void applyDefaults(RenderingConfiguration config, DocumentPreferences documentPreferences,
			ApplicationPreferences preferences) {
		apply(config, load(documentPreferences, preferences));
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

	public static Values load(DocumentPreferences documentPreferences, ApplicationPreferences preferences) {
		return new Values(
				getRenderQuality(documentPreferences, preferences),
				isAntiAliasingEnabled(preferences),
				getShadowsEnabled(documentPreferences, preferences),
				getAmbientOcclusionEnabled(documentPreferences, preferences),
				getRoughnessBumpEnabled(documentPreferences, preferences),
				getOriginAxesVisible(documentPreferences, preferences),
				getLightVisualizersVisible(documentPreferences, preferences),
				getRotateRocketOnDrag(documentPreferences, preferences),
				getDragRotationSensitivity(preferences),
				getCaretScaleWithView(documentPreferences, preferences));
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

	public static void saveToDocument(DocumentPreferences documentPreferences, ApplicationPreferences preferences,
			Values values) {
		putOrRemoveInt(documentPreferences, DocumentPreferences.PREF_3D_RENDER_QUALITY,
				values.renderQuality().ordinal(), getDefaultRenderQuality(preferences).ordinal());
		putOrRemoveBoolean(documentPreferences, DocumentPreferences.PREF_3D_SHADOWS_ENABLED,
				values.shadowsEnabled(), isShadowsEnabled(preferences));
		putOrRemoveBoolean(documentPreferences, DocumentPreferences.PREF_3D_AMBIENT_OCCLUSION_ENABLED,
				values.ambientOcclusionEnabled(), isAmbientOcclusionEnabled(preferences));
		putOrRemoveBoolean(documentPreferences, DocumentPreferences.PREF_3D_ROUGHNESS_BUMP_ENABLED,
				values.roughnessBumpEnabled(), isRoughnessBumpEnabled(preferences));
		putOrRemoveBoolean(documentPreferences, DocumentPreferences.PREF_3D_ORIGIN_AXES_VISIBLE,
				values.originAxesVisible(), isOriginAxesVisible(preferences));
		putOrRemoveBoolean(documentPreferences, DocumentPreferences.PREF_3D_LIGHT_VISUALIZERS_VISIBLE,
				values.lightVisualizersVisible(), areLightVisualizersVisible(preferences));
		putOrRemoveBoolean(documentPreferences, DocumentPreferences.PREF_3D_ROTATE_ROCKET_ON_DRAG,
				values.rotateRocketOnDrag(), isRotateRocketOnDrag(preferences));
		putOrRemoveBoolean(documentPreferences, DocumentPreferences.PREF_3D_CARET_SCALE_WITH_VIEW,
				values.caretScaleWithView(), isCaretScaleWithView(preferences));
	}

	public static void applyQualityDefaults(RenderingConfiguration config, ApplicationPreferences preferences) {
		applyDefaults(config, preferences);
	}

	public static GraphicsQualitySettings.RenderQuality getRenderQuality(DocumentPreferences documentPreferences,
			ApplicationPreferences preferences) {
		int maxIndex = GraphicsQualitySettings.RenderQuality.values().length - 1;
		int fallback = getDefaultRenderQuality(preferences).ordinal();
		int choice = documentPreferences.getInt(DocumentPreferences.PREF_3D_RENDER_QUALITY, fallback);
		choice = Math.max(0, Math.min(maxIndex, choice));
		return GraphicsQualitySettings.RenderQuality.values()[choice];
	}

	public static boolean getShadowsEnabled(DocumentPreferences documentPreferences, ApplicationPreferences preferences) {
		return documentPreferences.getBoolean(DocumentPreferences.PREF_3D_SHADOWS_ENABLED, isShadowsEnabled(preferences));
	}

	public static boolean getAmbientOcclusionEnabled(DocumentPreferences documentPreferences, ApplicationPreferences preferences) {
		return documentPreferences.getBoolean(DocumentPreferences.PREF_3D_AMBIENT_OCCLUSION_ENABLED,
				isAmbientOcclusionEnabled(preferences));
	}

	public static boolean getRoughnessBumpEnabled(DocumentPreferences documentPreferences, ApplicationPreferences preferences) {
		return documentPreferences.getBoolean(DocumentPreferences.PREF_3D_ROUGHNESS_BUMP_ENABLED,
				isRoughnessBumpEnabled(preferences));
	}

	public static boolean getOriginAxesVisible(DocumentPreferences documentPreferences, ApplicationPreferences preferences) {
		return documentPreferences.getBoolean(DocumentPreferences.PREF_3D_ORIGIN_AXES_VISIBLE,
				isOriginAxesVisible(preferences));
	}

	public static boolean getLightVisualizersVisible(DocumentPreferences documentPreferences, ApplicationPreferences preferences) {
		return documentPreferences.getBoolean(DocumentPreferences.PREF_3D_LIGHT_VISUALIZERS_VISIBLE,
				areLightVisualizersVisible(preferences));
	}

	public static boolean getRotateRocketOnDrag(DocumentPreferences documentPreferences, ApplicationPreferences preferences) {
		return documentPreferences.getBoolean(DocumentPreferences.PREF_3D_ROTATE_ROCKET_ON_DRAG,
				isRotateRocketOnDrag(preferences));
	}

	public static boolean getCaretScaleWithView(DocumentPreferences documentPreferences, ApplicationPreferences preferences) {
		return documentPreferences.getBoolean(DocumentPreferences.PREF_3D_CARET_SCALE_WITH_VIEW,
				isCaretScaleWithView(preferences));
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

	private static void putOrRemoveBoolean(DocumentPreferences documentPreferences, String key, boolean value,
			boolean fallbackValue) {
		if (value == fallbackValue) {
			documentPreferences.removePreference(key);
		} else {
			documentPreferences.putBoolean(key, value);
		}
	}

	private static void putOrRemoveInt(DocumentPreferences documentPreferences, String key, int value, int fallbackValue) {
		if (value == fallbackValue) {
			documentPreferences.removePreference(key);
		} else {
			documentPreferences.putInt(key, value);
		}
	}
}
