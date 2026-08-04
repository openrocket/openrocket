package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;

/**
 * Centralises the display-mode rules that decide whether scene geometry is transparent.
 *
 * <p>The renderer and material binder must agree on these rules: classifying an object as
 * opaque while the shader emits an alpha below one writes it into the depth buffer and hides
 * transparency behind it. Keeping the effective opacity here prevents those two paths from
 * drifting apart.</p>
 */
public final class TransparencyPolicy {

	private static final float UNFINISHED_BODY_TUBE_OPACITY = 0.2f;

	private TransparencyPolicy() {
	}

	public static boolean isTransparent(SceneObject object, RenderingConfiguration config) {
		return getEffectiveOpacity(object.getRocketComponent(), object.getAppearance(), config) < 1.0f;
	}

	/**
	 * Whether a transparent draw may still contain fully opaque texture fragments.
	 *
	 * <p>When component opacity does not affect its texture, opaque texels must remain in
	 * the depth-writing pass. Weighted transparency deliberately approximates the ordering
	 * of translucent layers and would otherwise average those texels with geometry behind
	 * them.</p>
	 */
	public static boolean mayProduceOpaqueTextureFragments(RocketComponent component,
													   Appearance3D appearance,
													   RenderingConfiguration config) {
		if (getEffectiveOpacity(component, appearance, config) >= 1.0f) {
			return false;
		}

		DisplaySettings.RenderMode mode = config.getDisplay().getMode();
		if (mode == DisplaySettings.RenderMode.XRAY || config.getDisplay().isWireframeMode()) {
			return false;
		}
		if (mode == DisplaySettings.RenderMode.UNFINISHED) {
			// The material binder substitutes a generated default appearance in this mode.
			// Let the shader decide whether that appearance actually contains opaque texels.
			return true;
		}
		return appearance.getStyle() == Appearance3D.RenderStyle.TEXTURED
				&& !appearance.isOpacityAffectsTexture();
	}

	public static float getEffectiveOpacity(RocketComponent component, Appearance3D appearance,
											 RenderingConfiguration config) {
		DisplaySettings.RenderMode mode = config.getDisplay().getMode();
		if (mode == DisplaySettings.RenderMode.XRAY) {
			return isFigureTransparentComponent(component) ? config.getQuality().getXrayOpacity() : 1.0f;
		}
		if (mode == DisplaySettings.RenderMode.UNFINISHED && component instanceof BodyTube) {
			return UNFINISHED_BODY_TUBE_OPACITY;
		}
		return appearance.getOpacity();
	}

	/**
	 * Components made translucent by the Figure/X-ray display mode.
	 * Nose cones deliberately remain opaque, matching the legacy Figure rendering.
	 */
	public static boolean isFigureTransparentComponent(RocketComponent component) {
		return component instanceof BodyTube ||
				(component instanceof Transition && !(component instanceof NoseCone));
	}

	/**
	 * Curved shells whose front and back faces should both contribute when transparent.
	 */
	public static boolean isTransparentBodyComponent(RocketComponent component) {
		return component instanceof BodyTube || component instanceof Transition;
	}
}
