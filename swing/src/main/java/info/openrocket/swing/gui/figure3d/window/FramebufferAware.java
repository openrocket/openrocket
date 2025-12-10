package info.openrocket.swing.gui.figure3d.window;

/**
 * Provides access to framebuffer size and resize callbacks.
 */
public interface FramebufferAware {
    /**
     * @return array [width, height] in framebuffer pixels
     */
    int[] getFramebufferSize();

    /**
     * Register a callback invoked when the framebuffer size changes.
     */
    void setupFramebufferSizeCallback(FramebufferAware.FramebufferSizeCallback callback);

    @FunctionalInterface
    interface FramebufferSizeCallback {
        void invoke(int width, int height);
    }
}
