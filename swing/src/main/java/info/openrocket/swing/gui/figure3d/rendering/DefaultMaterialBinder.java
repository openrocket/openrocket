package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.rocketcomponent.BodyComponent;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;

/**
 * Default OpenGL material binder: sets uniforms and binds textures for an object.
 */
public class DefaultMaterialBinder implements MaterialBinder {

    @Override
    public void bind(SceneObject obj,
                     ShaderProgram shader,
                     RealisticRenderer.ShaderUniforms uniforms,
                     RenderingConfiguration config,
                     TextureBinder textureBinder) {
        final Appearance3D appearance = obj.getAppearance();

        // Set object-specific matrices and flags
        shader.setUniform(uniforms.model, obj.getModelMatrix());
        GL33.glUniform1i(uniforms.isSelected, obj.isSelected() ? 1 : 0);
        GL33.glUniform1i(uniforms.isUnlit, appearance.isUnlit() ? 1 : 0);

        // Colors and material properties
        Vector3f linearColor = appearance.getColor();
        GL33.glUniform3f(uniforms.objectColor, linearColor.x, linearColor.y, linearColor.z);

        Vector3f specularColor = appearance.getSpecularColor();
        GL33.glUniform3f(uniforms.materialSpecular, specularColor.x, specularColor.y, specularColor.z);
        GL33.glUniform1f(uniforms.specularTintFactor, appearance.getSpecularTint());
        GL33.glUniform1i(uniforms.renderStyle, appearance.getStyle().ordinal());
        GL33.glUniform1f(uniforms.shine, appearance.getShine());
        GL33.glUniform1f(uniforms.roughnessScale, appearance.getRoughnessScale());
        GL33.glUniform1f(uniforms.roughnessStrength, appearance.getRoughnessStrength());

        boolean isXray = config.getDisplay().getMode() == DisplaySettings.RenderMode.XRAY &&
                obj.getRocketComponent() instanceof BodyComponent;
        if (isXray) {
            GL33.glUniform1f(uniforms.opacity, config.getQuality().getXrayOpacity());
        } else {
            GL33.glUniform1f(uniforms.opacity, appearance.getOpacity());
        }

        Matrix4f textureTransformMatrix = appearance.getTextureTransform().getTransformMatrix(new Matrix4f());
        shader.setUniform(uniforms.textureTransformMatrix, textureTransformMatrix);

        // Base texture
        if (appearance.getStyle() != Appearance3D.RenderStyle.WIREFRAME) {
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
        Texture decal = appearance.getDecalTexture();
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
}

