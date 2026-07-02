package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;

/**
 * Binds material- and object-specific shader uniforms and textures for a draw.
 */
public interface MaterialBinder {
    void bind(SceneObject obj,
              Shader shader,
              RealisticRenderer.ShaderUniforms uniforms,
              RenderingConfiguration config,
              TextureBinder textureBinder);

    default void cleanup() {
    }
}
