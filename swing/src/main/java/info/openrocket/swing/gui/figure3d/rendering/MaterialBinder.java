package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;

/**
 * Binds material- and object-specific shader uniforms and textures for a draw.
 */
public interface MaterialBinder {
	void bind(SceneObject obj,
			  GLShader shader,
			  MainShaderUniforms uniforms,
			  RenderingConfiguration config,
			  TextureBinder textureBinder);

	default void cleanup() {
	}
}
