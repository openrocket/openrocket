package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
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
import static org.lwjgl.opengl.GL11.GL_REPEAT;

/**
 * Default OpenGL material binder: sets uniforms and binds textures for an object.
 */
public class DefaultMaterialBinder implements MaterialBinder {
    private static final Map<Class<? extends RocketComponent>, ORColor> FIGURE_DEFAULT_COLOR_CACHE = new HashMap<>();

    @Override
    public void bind(SceneObject obj,
                     ShaderProgram shader,
                     RealisticRenderer.ShaderUniforms uniforms,
                     RenderingConfiguration config,
                     TextureBinder textureBinder) {
        final Appearance3D appearance = obj.getAppearance();
        boolean isFigureMode = config.getDisplay().getMode() == DisplaySettings.RenderMode.XRAY;
        boolean isXray = isFigureMode &&
                isFigureTransparentComponent(obj.getRocketComponent());
        ORColor figureSourceColor = isFigureMode ? getFigureSourceColor(obj.getRocketComponent()) : null;

        // Set object-specific matrices and flags
        shader.setUniform(uniforms.model, obj.getModelMatrix());
        GL33.glUniform1i(uniforms.isSelected, obj.isSelected() ? 1 : 0);
        GL33.glUniform1i(uniforms.isUnlit, appearance.isUnlit() ? 1 : 0);
        GL33.glUniform1i(uniforms.xrayMode, isXray ? 1 : 0);

        // Colors and material properties
        Vector3f linearColor = isFigureMode ? getFigureXrayBaseColor(figureSourceColor) : appearance.getColor();
        if (isXray) {
            linearColor = toFigureXrayColor(linearColor);
        }
        GL33.glUniform3f(uniforms.objectColor, linearColor.x, linearColor.y, linearColor.z);

        Vector3f specularColor = appearance.getSpecularColor();
        if (isFigureMode) {
            specularColor = toFigureXraySpecular(linearColor, appearance.getShine());
        }
        GL33.glUniform3f(uniforms.materialSpecular, specularColor.x, specularColor.y, specularColor.z);
        GL33.glUniform1f(uniforms.specularTintFactor, appearance.getSpecularTint());
        int renderStyle = isFigureMode ? Appearance3D.RenderStyle.SOLID.ordinal() : appearance.getStyle().ordinal();
        GL33.glUniform1i(uniforms.renderStyle, renderStyle);
        GL33.glUniform1f(uniforms.shine, appearance.getShine());
        GL33.glUniform1f(uniforms.roughnessScale, appearance.getRoughnessScale());
        GL33.glUniform1f(uniforms.roughnessStrength, appearance.getRoughnessStrength());
        GL33.glUniform1i(uniforms.hideInnerSurfaces, config.getDisplay().isRenderInternalSurfaces() ? 0 : 1);

        if (isFigureMode) {
            GL33.glUniform1f(uniforms.opacity, isXray ? config.getQuality().getXrayOpacity() : 1.0f);
        } else {
            GL33.glUniform1f(uniforms.opacity, appearance.getOpacity());
        }

        Matrix4f textureTransformMatrix = appearance.getTextureTransform().getTransformMatrix(new Matrix4f());
        shader.setUniform(uniforms.textureTransformMatrix, textureTransformMatrix);
        boolean unfinishedMode = config.getDisplay().getMode() == DisplaySettings.RenderMode.UNFINISHED;

        // Base texture
        if (!unfinishedMode && !isFigureMode && appearance.getStyle() != Appearance3D.RenderStyle.WIREFRAME) {
            Texture tex = appearance.getTexture();
            if (tex != null && tex.getId() != 0) {
                textureBinder.bindTexture(0, GL33.GL_TEXTURE_2D, tex.getId());
                int wrapMode = (appearance.getTextureMode() == Appearance3D.TextureMode.STRETCH) ? GL33.GL_CLAMP_TO_EDGE : GL_REPEAT;
                textureBinder.setTextureParams(tex.getId(), wrapMode, wrapMode, GL33.GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);
                GL33.glUniform1i(uniforms.textureSampler, 0);
                GL33.glUniform1i(uniforms.hasTexture, 1);
            } else {
                GL33.glUniform1i(uniforms.hasTexture, 0);
            }
        } else {
            GL33.glUniform1i(uniforms.hasTexture, 0);
        }

        // Decal texture
        Texture decal = (unfinishedMode || isFigureMode) ? null : appearance.getDecalTexture();
        if (decal != null) {
            Matrix4f decalTransformMatrix = appearance.getDecalTransform().getTransformMatrix(new Matrix4f());
            shader.setUniform(uniforms.decalTransformMatrix, decalTransformMatrix);
            textureBinder.bindTexture(1, GL33.GL_TEXTURE_2D, decal.getId());
            GL33.glUniform1i(uniforms.decalSampler, 1);
            GL33.glUniform1i(uniforms.hasDecal, 1);
            GL33.glUniform1i(uniforms.decalSurfaceMask, appearance.getDecalSurfaceMask());
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
}
