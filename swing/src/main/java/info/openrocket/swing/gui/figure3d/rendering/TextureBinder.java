package info.openrocket.swing.gui.figure3d.rendering;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

/**
 * Abstraction for texture state changes and parameter updates.
 */
public interface TextureBinder {
    void bindTexture(int unit, int textureType, int textureId);

    default void setTextureParams(int textureId, int wrapS, int wrapT, int minFilter, int magFilter) {
        setTextureParams(GL_TEXTURE_2D, textureId, wrapS, wrapT, minFilter, magFilter);
    }

    void setTextureParams(int textureType, int textureId, int wrapS, int wrapT, int minFilter, int magFilter);

    default void unbindTexture(int unit) {
        unbindTexture(unit, GL_TEXTURE_2D);
    }

    void unbindTexture(int unit, int textureType);
    void reset();
}
