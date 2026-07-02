package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.gui.util.SwingPreferences;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;

/**
 * Default OpenGL material binder: sets uniforms and binds textures for an object.
 */
public class DefaultMaterialBinder implements MaterialBinder {
    private static final Map<Class<? extends RocketComponent>, ORColor> FIGURE_DEFAULT_COLOR_CACHE = new HashMap<>();
    // Unfinished appearances own GL textures, so they must stay scoped to a single renderer/context.
    private final Map<Class<? extends RocketComponent>, Appearance3D> unfinishedAppearanceCache = new HashMap<>();

    @Override
    public void bind(SceneObject obj,
                     Shader shader,
                     RealisticRenderer.ShaderUniforms uniforms,
                     RenderingConfiguration config,
                     TextureBinder textureBinder) {
        final Appearance3D appearance = obj.getAppearance();
        boolean unfinishedMode = config.getDisplay().getMode() == DisplaySettings.RenderMode.UNFINISHED;
        boolean isFigureMode = config.getDisplay().getMode() == DisplaySettings.RenderMode.XRAY;
        boolean isXray = isFigureMode &&
                isFigureTransparentComponent(obj.getRocketComponent());
        ORColor figureSourceColor = isFigureMode ? getFigureSourceColor(obj.getRocketComponent()) : null;
        Appearance3D unfinishedAppearance = unfinishedMode && obj.getRocketComponent() != null
                ? unfinishedAppearanceCache.computeIfAbsent(
                        obj.getRocketComponent().getClass(),
                        k -> AppearanceFactory.createDefaultFrom(obj.getRocketComponent()))
                : null;

        // Set object-specific matrices and flags
        shader.setUniformMatrix4f(uniforms.model, obj.getModelMatrix());
        GL33.glUniform1i(uniforms.isSelected, obj.isSelected() ? 1 : 0);
        GL33.glUniform1i(uniforms.isUnlit, appearance.isUnlit() ? 1 : 0);
        GL33.glUniform1i(uniforms.xrayMode, isXray ? 1 : 0);

        // Colors and material properties
        Vector3f linearColor = unfinishedMode
                ? getAppearanceColor(unfinishedAppearance, appearance.getColor())
                : isFigureMode ? getFigureXrayBaseColor(figureSourceColor) : appearance.getColor();
        if (isXray) {
            linearColor = toFigureXrayColor(linearColor);
        }
        GL33.glUniform3f(uniforms.objectColor, linearColor.x, linearColor.y, linearColor.z);

        Vector3f specularColor = unfinishedMode
                ? getUnfinishedSpecularColor(unfinishedAppearance)
                : appearance.getSpecularColor();
        if (isFigureMode) {
            specularColor = toFigureXraySpecular(linearColor, appearance.getShine());
        }
        GL33.glUniform3f(uniforms.materialSpecular, specularColor.x, specularColor.y, specularColor.z);
        GL33.glUniform1f(uniforms.specularTintFactor, appearance.getSpecularTint());
        int renderStyle = unfinishedMode
                ? unfinishedAppearance.getStyle().ordinal()
                : isFigureMode ? Appearance3D.RenderStyle.SOLID.ordinal() : appearance.getStyle().ordinal();
        GL33.glUniform1i(uniforms.renderStyle, renderStyle);
        float shine = unfinishedMode ? getAppearanceShine(unfinishedAppearance, appearance.getShine()) : appearance.getShine();
        GL33.glUniform1f(uniforms.shine, shine);
        GL33.glUniform1f(uniforms.roughnessScale, appearance.getRoughnessScale());
        GL33.glUniform1f(uniforms.roughnessStrength, appearance.getRoughnessStrength());
        boolean textureOpacityAffectsAlpha = unfinishedMode
                ? unfinishedAppearance.isOpacityAffectsTexture()
                : appearance.isOpacityAffectsTexture();
        GL33.glUniform1i(uniforms.textureOpacityAffectsAlpha, textureOpacityAffectsAlpha ? 1 : 0);
        float effectiveOpacity = getEffectiveOpacity(obj.getRocketComponent(), appearance, config, unfinishedMode, isFigureMode, isXray);
        boolean hideInnerSurfaces = !config.getDisplay().isRenderInternalSurfaces();
        GL33.glUniform1i(uniforms.hideInnerSurfaces, hideInnerSurfaces ? 1 : 0);

        if (isFigureMode) {
            GL33.glUniform1f(uniforms.opacity, isXray ? config.getQuality().getXrayOpacity() : 1.0f);
        } else if (unfinishedMode && obj.getRocketComponent() instanceof BodyTube) {
            GL33.glUniform1f(uniforms.opacity, 0.2f);
        } else {
            GL33.glUniform1f(uniforms.opacity, appearance.getOpacity());
        }

        Matrix4f textureTransformMatrix = unfinishedMode
                ? unfinishedAppearance.getTextureTransform().getTransformMatrix(new Matrix4f())
                : appearance.getTextureTransform().getTransformMatrix(new Matrix4f());
        shader.setUniformMatrix4f(uniforms.textureTransformMatrix, textureTransformMatrix);

        // Base texture
        if (!isFigureMode && renderStyle != Appearance3D.RenderStyle.WIREFRAME.ordinal()) {
            Texture tex = unfinishedMode ? unfinishedAppearance.getTexture() : appearance.getTexture();
            if (tex != null && tex.getId() != 0) {
                textureBinder.bindTexture(0, GL33.GL_TEXTURE_2D, tex.getId());
                Appearance3D.TextureMode textureMode = unfinishedMode ? unfinishedAppearance.getTextureMode() : appearance.getTextureMode();
                int wrapMode = (textureMode == Appearance3D.TextureMode.STRETCH) ? GL33.GL_CLAMP_TO_EDGE : GL_REPEAT;
                // Enable sharper close-up texture inspection while keeping trilinear mipmapping at distance.
                textureBinder.setTextureParams(tex.getId(), wrapMode, wrapMode, GL33.GL_LINEAR_MIPMAP_LINEAR, GL_NEAREST);
                GL33.glUniform1i(uniforms.textureSampler, 0);
                GL33.glUniform1i(uniforms.hasTexture, 1);
            } else {
                GL33.glUniform1i(uniforms.hasTexture, 0);
            }
        } else {
            GL33.glUniform1i(uniforms.hasTexture, 0);
        }

        // Decal texture
        Texture decal = isFigureMode ? null : unfinishedMode ? unfinishedAppearance.getDecalTexture() : appearance.getDecalTexture();
        if (decal != null) {
            Matrix4f decalTransformMatrix = unfinishedMode
                    ? unfinishedAppearance.getDecalTransform().getTransformMatrix(new Matrix4f())
                    : appearance.getDecalTransform().getTransformMatrix(new Matrix4f());
            shader.setUniformMatrix4f(uniforms.decalTransformMatrix, decalTransformMatrix);
            textureBinder.bindTexture(1, GL33.GL_TEXTURE_2D, decal.getId());
            // Keep decal linework crisp when zoomed in.
            textureBinder.setTextureParams(decal.getId(), GL33.GL_CLAMP_TO_EDGE, GL33.GL_CLAMP_TO_EDGE,
                    GL33.GL_LINEAR_MIPMAP_LINEAR, GL_NEAREST);
            GL33.glUniform1i(uniforms.decalSampler, 1);
            GL33.glUniform1i(uniforms.hasDecal, 1);
            GL33.glUniform1i(uniforms.decalSurfaceMask, unfinishedMode ? unfinishedAppearance.getDecalSurfaceMask() : appearance.getDecalSurfaceMask());
        } else {
            GL33.glUniform1i(uniforms.hasDecal, 0);
        }
    }

    private static boolean isFigureTransparentComponent(RocketComponent component) {
        if (component instanceof BodyTube) {
            return true;
        }
        return component instanceof Transition && !(component instanceof NoseCone);
    }

    private static float getEffectiveOpacity(RocketComponent component, Appearance3D appearance,
                                             RenderingConfiguration config, boolean unfinishedMode,
                                             boolean isFigureMode, boolean isXray) {
        if (isFigureMode) {
            return isXray ? config.getQuality().getXrayOpacity() : 1.0f;
        }
        if (unfinishedMode && component instanceof BodyTube) {
            return 0.2f;
        }
        return appearance.getOpacity();
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

    @Override
    public void cleanup() {
        for (Appearance3D appearance : unfinishedAppearanceCache.values()) {
            appearance.cleanup();
        }
        unfinishedAppearanceCache.clear();
    }

}
