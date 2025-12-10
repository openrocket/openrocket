package info.openrocket.swing.gui.figure3d.rendering;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Abstraction for a GPU shader program used in rendering.
 */
public interface ShaderProgram {
    void use();
    void unbind();
    int getProgramId();

    int getUniformLocation(String name);
    void cacheUniformLocations(String... uniformNames);

    void setUniform(String name, Matrix4f matrix);
    void setUniform(int location, Matrix4f matrix);
    void setUniform(String name, Matrix3f matrix);
    void setUniform(int location, Matrix3f matrix);
    void setUniform(String name, Vector4f vector);
    void setUniform(String name, Vector3f vector);
    void setUniform(String name, float value);

    void cleanup();
}

