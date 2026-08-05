package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.gui.util.SwingPreferences;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL33.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL33.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL33.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL33.glUniform1f;
import static org.lwjgl.opengl.GL33.glUniform1i;
import static org.lwjgl.opengl.GL33.glUniform3f;

/**
 * Default OpenGL material binder: sets uniforms and binds textures for an object.
 */
public final class DefaultMaterialBinder implements GpuResource {
	private static final Map<Class<? extends RocketComponent>, ORColor> FIGURE_DEFAULT_COLOR_CACHE = new HashMap<>();
	// Unfinished appearances own GL textures, so they must stay scoped to a single renderer/context.
	private final Map<Class<? extends RocketComponent>, Appearance3D> unfinishedAppearanceCache = new HashMap<>();
	private final AppearanceFactory.DecalTextureCache decalTextureCache = AppearanceFactory.createDecalTextureCache();
	private final Matrix4f textureTransformMatrix = new Matrix4f();
	private final Matrix4f decalTransformMatrix = new Matrix4f();
	private final Matrix3f normalMatrix = new Matrix3f();

	public void bind(SceneObject obj,
					 GLShader shader,
					 MainShaderUniforms uniforms,
					 RenderingConfiguration config,
					 TextureBinder textureBinder) {
		final Appearance3D appearance = obj.getAppearance();
		boolean unfinishedMode = config.getDisplay().getMode() == DisplaySettings.RenderMode.UNFINISHED;
		boolean isFigureMode = config.getDisplay().getMode() == DisplaySettings.RenderMode.XRAY;
		boolean isXray = isFigureMode &&
				TransparencyPolicy.isFigureTransparentComponent(obj.getRocketComponent());
		ORColor figureSourceColor = isFigureMode ? getFigureSourceColor(obj.getRocketComponent()) : null;
		Appearance3D unfinishedAppearance = unfinishedMode && obj.getRocketComponent() != null
				? unfinishedAppearanceCache.computeIfAbsent(
						obj.getRocketComponent().getClass(),
						k -> createDefaultAppearance(obj.getRocketComponent()))
				: null;

		// Set object-specific matrices and flags. The normal matrix is derived here rather
		// than in the shaders: it is constant for the draw call, so deriving it there costs
		// a matrix inverse per vertex and, in the roughness path, per fragment.
		Matrix4f modelMatrix = obj.getModelMatrix();
		shader.setUniformMatrix4f(uniforms.model, modelMatrix);
		shader.setUniformMatrix3f(uniforms.normalMatrix, modelMatrix.normal(normalMatrix));
		glUniform1i(uniforms.isSelected, obj.isSelected() ? 1 : 0);
		glUniform1i(uniforms.isUnlit, appearance.isUnlit() ? 1 : 0);
		glUniform1i(uniforms.xrayMode, isXray ? 1 : 0);

		// Colors and material properties
		Vector3f linearColor = unfinishedMode
				? getAppearanceColor(unfinishedAppearance, appearance.getColor())
				: isFigureMode ? getFigureXrayBaseColor(figureSourceColor) : appearance.getColor();
		if (isXray) {
			linearColor = toFigureXrayColor(linearColor);
		}
		glUniform3f(uniforms.objectColor, linearColor.x, linearColor.y, linearColor.z);

		Vector3f specularColor = unfinishedMode
				? getUnfinishedSpecularColor(unfinishedAppearance)
				: appearance.getSpecularColor();
		if (isFigureMode) {
			specularColor = toFigureXraySpecular(linearColor, appearance.getShine());
		}
		glUniform3f(uniforms.materialSpecular, specularColor.x, specularColor.y, specularColor.z);
		glUniform1f(uniforms.specularTintFactor, appearance.getSpecularTint());
		int renderStyle = unfinishedMode
				? unfinishedAppearance.getStyle().ordinal()
				: isFigureMode ? Appearance3D.RenderStyle.SOLID.ordinal() : appearance.getStyle().ordinal();
		glUniform1i(uniforms.renderStyle, renderStyle);
		float shine = unfinishedMode ? getAppearanceShine(unfinishedAppearance, appearance.getShine()) : appearance.getShine();
		glUniform1f(uniforms.shine, shine);
		// The material carries how rough the surface is; the quality level decides the grain
		// size and bump strength that roughness is drawn with.
		float roughnessAmount = appearance.getRoughnessAmount();
		glUniform1f(uniforms.roughnessScale, config.getQuality().getRoughnessFrequency(roughnessAmount));
		glUniform1f(uniforms.roughnessStrength, config.getQuality().getRoughnessStrength(roughnessAmount));
		boolean textureOpacityAffectsAlpha = unfinishedMode
				? unfinishedAppearance.isOpacityAffectsTexture()
				: appearance.isOpacityAffectsTexture();
		glUniform1i(uniforms.textureOpacityAffectsAlpha, textureOpacityAffectsAlpha ? 1 : 0);
		boolean hideInnerSurfaces = !config.getDisplay().isRenderInternalSurfaces();
		glUniform1i(uniforms.hideInnerSurfaces, hideInnerSurfaces ? 1 : 0);
		glUniform1f(uniforms.opacity,
				TransparencyPolicy.getEffectiveOpacity(obj.getRocketComponent(), appearance, config));

		Matrix4f textureTransformMatrix = unfinishedMode
				? unfinishedAppearance.getTextureTransform().getTransformMatrix(this.textureTransformMatrix)
				: appearance.getTextureTransform().getTransformMatrix(this.textureTransformMatrix);
		shader.setUniformMatrix4f(uniforms.textureTransformMatrix, textureTransformMatrix);

		// Base texture
		if (!isFigureMode && renderStyle != Appearance3D.RenderStyle.WIREFRAME.ordinal()) {
			Texture tex = unfinishedMode ? unfinishedAppearance.getTexture() : appearance.getTexture();
			if (tex != null && tex.getId() != 0) {
				textureBinder.bindTexture(0, GL_TEXTURE_2D, tex.getId());
				Appearance3D.TextureMode textureMode = unfinishedMode ? unfinishedAppearance.getTextureMode() : appearance.getTextureMode();
				int wrapMode = (textureMode == Appearance3D.TextureMode.STRETCH) ? GL_CLAMP_TO_EDGE : GL_REPEAT;
				// Enable sharper close-up texture inspection while keeping trilinear mipmapping at distance.
				textureBinder.setTextureParams(GL_TEXTURE_2D, tex.getId(), wrapMode, wrapMode,
						GL_LINEAR_MIPMAP_LINEAR, GL_NEAREST);
				glUniform1i(uniforms.textureSampler, 0);
				glUniform1i(uniforms.hasTexture, 1);
			} else {
				glUniform1i(uniforms.hasTexture, 0);
			}
		} else {
			glUniform1i(uniforms.hasTexture, 0);
		}

		// Decal texture
		Texture decal = isFigureMode ? null : unfinishedMode ? unfinishedAppearance.getDecalTexture() : appearance.getDecalTexture();
		int decalId = decal != null ? decal.getId() : 0;
		if (decalId != 0) {
			Matrix4f decalTransformMatrix = unfinishedMode
					? unfinishedAppearance.getDecalTransform().getTransformMatrix(this.decalTransformMatrix)
					: appearance.getDecalTransform().getTransformMatrix(this.decalTransformMatrix);
			shader.setUniformMatrix4f(uniforms.decalTransformMatrix, decalTransformMatrix);
			textureBinder.bindTexture(1, GL_TEXTURE_2D, decalId);
			// Keep decal linework crisp when zoomed in.
			textureBinder.setTextureParams(GL_TEXTURE_2D, decalId, GL_CLAMP_TO_EDGE,
					GL_CLAMP_TO_EDGE, GL_LINEAR_MIPMAP_LINEAR, GL_NEAREST);
			glUniform1i(uniforms.decalSampler, 1);
			glUniform1i(uniforms.hasDecal, 1);
			glUniform1i(uniforms.decalSurfaceMask, unfinishedMode ? unfinishedAppearance.getDecalSurfaceMask() : appearance.getDecalSurfaceMask());
		} else {
			glUniform1i(uniforms.hasDecal, 0);
		}
	}

	private static ORColor getFigureSourceColor(RocketComponent component) {
		ORColor figureColor = component != null ? component.getColor() : null;
		if (figureColor == null && component != null) {
			@SuppressWarnings("unchecked")
			Class<? extends RocketComponent> componentClass = (Class<? extends RocketComponent>) component.getClass();
			figureColor = FIGURE_DEFAULT_COLOR_CACHE.computeIfAbsent(componentClass,
					key -> ((SwingPreferences) Application.getPreferences()).getDefaultColor(key));
		}
		return figureColor;
	}

	private static Vector3f getFigureXrayBaseColor(ORColor figureColor) {
		if (figureColor == null) {
			return ColorUtils.srgbToLinear(new Vector3f(1.0f, 1.0f, 0.0f));
		}

		float srgbR = figureColor.getRed() / 255.0f;
		float srgbG = figureColor.getGreen() / 255.0f;
		float srgbB = figureColor.getBlue() / 255.0f;
		return ColorUtils.srgbToLinear(new Vector3f(srgbR, srgbG, srgbB));
	}

	private static Vector3f toFigureXrayColor(Vector3f baseLinearColor) {
		return new Vector3f(baseLinearColor);
	}

	private static Vector3f toFigureXraySpecular(Vector3f figureColor, float shine) {
		float shineMix = Math.max(shine, 0.15f);
		return new Vector3f(
				Math.max(figureColor.x, 0.9f) * shineMix,
				Math.max(figureColor.y, 0.9f) * shineMix,
				Math.max(figureColor.z, 0.9f) * shineMix
		);
	}

	private static Vector3f getAppearanceColor(Appearance3D appearance, Vector3f fallbackLinearColor) {
		if (appearance == null) {
			return new Vector3f(fallbackLinearColor);
		}
		return new Vector3f(appearance.getColor());
	}

	private static float getAppearanceShine(Appearance3D appearance, float fallbackShine) {
		return appearance != null ? appearance.getShine() : fallbackShine;
	}

	private static Vector3f getUnfinishedSpecularColor(Appearance3D appearance) {
		float shine = appearance != null ? appearance.getShine() : 0.0f;
		return new Vector3f(shine, shine, shine);
	}

	private Appearance3D createDefaultAppearance(RocketComponent component) {
		final Appearance3D[] appearance = new Appearance3D[1];
		AppearanceFactory.withDecalTextureCache(decalTextureCache,
				() -> appearance[0] = AppearanceFactory.createDefaultFrom(component));
		return appearance[0];
	}

	@Override
	public void cleanup() {
		for (Appearance3D appearance : unfinishedAppearanceCache.values()) {
			appearance.cleanup();
		}
		unfinishedAppearanceCache.clear();
		decalTextureCache.cleanup();
	}

}
