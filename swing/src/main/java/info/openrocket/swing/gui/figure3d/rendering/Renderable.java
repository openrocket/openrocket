package info.openrocket.swing.gui.figure3d.rendering;

/**
 * Minimal contract for renderable GPU-backed resources.
 */
public interface Renderable {
    void render();
    void cleanup();
}

