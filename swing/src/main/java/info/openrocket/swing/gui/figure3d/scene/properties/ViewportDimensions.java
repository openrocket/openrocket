package info.openrocket.swing.gui.figure3d.scene.properties;

/**
 * Encapsulates viewport dimensions and provides coordinate conversion utilities.
 * 
 * <p>This class manages the distinction between logical window coordinates and
 * physical framebuffer pixels, which is important for high-DPI displays where
 * the framebuffer may be larger than the window coordinates suggest.</p>
 * 
 * <h3>Coordinate Systems:</h3>
 * <ul>
 *   <li><b>Window coordinates:</b> Logical pixels as reported by the windowing system</li>
 *   <li><b>Framebuffer coordinates:</b> Physical pixels in the actual OpenGL framebuffer</li>
 * </ul>
 * 
 * <p>On high-DPI displays (e.g., Retina), the framebuffer may be 2x or more the
 * size of the window coordinates. This class provides methods to convert between
 * these coordinate systems and calculate scaling factors.</p>
 */
public class ViewportDimensions {
    
    private int windowWidth;
    private int windowHeight;
    private int framebufferWidth;
    private int framebufferHeight;
    
    /**
     * Creates viewport dimensions with initial values.
     * 
     * @param windowWidth The logical window width in pixels
     * @param windowHeight The logical window height in pixels
     * @param framebufferWidth The framebuffer width in pixels
     * @param framebufferHeight The framebuffer height in pixels
     */
    public ViewportDimensions(int windowWidth, int windowHeight, int framebufferWidth, int framebufferHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.framebufferWidth = framebufferWidth;
        this.framebufferHeight = framebufferHeight;
    }
    
    /**
     * Creates viewport dimensions with equal window and framebuffer sizes.
     * 
     * @param width The width in pixels
     * @param height The height in pixels
     */
    public ViewportDimensions(int width, int height) {
        this(width, height, width, height);
    }
    
    // Getters
    
    /**
     * Gets the logical window width in pixels.
     * 
     * @return the window width as reported by the windowing system
     */
    public int getWindowWidth() {
        return windowWidth;
    }
    
    /**
     * Gets the logical window height in pixels.
     * 
     * @return the window height as reported by the windowing system
     */
    public int getWindowHeight() {
        return windowHeight;
    }
    
    /**
     * Gets the physical framebuffer width in pixels.
     * 
     * @return the actual OpenGL framebuffer width
     */
    public int getFramebufferWidth() {
        return framebufferWidth;
    }
    
    /**
     * Gets the physical framebuffer height in pixels.
     * 
     * @return the actual OpenGL framebuffer height
     */
    public int getFramebufferHeight() {
        return framebufferHeight;
    }
    
    // Setters
    
    /**
     * Updates all viewport dimensions in a single operation.
     * This method is typically called during window resize events.
     * 
     * @param windowWidth the new logical window width
     * @param windowHeight the new logical window height
     * @param framebufferWidth the new physical framebuffer width
     * @param framebufferHeight the new physical framebuffer height
     */
    public void update(int windowWidth, int windowHeight, int framebufferWidth, int framebufferHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.framebufferWidth = framebufferWidth;
        this.framebufferHeight = framebufferHeight;
    }
    
    // Calculated Properties
    
    /**
     * Gets the aspect ratio based on framebuffer dimensions.
     * 
     * @return The aspect ratio (width / height)
     */
    public float getAspectRatio() {
        return framebufferHeight > 0 ? (float) framebufferWidth / framebufferHeight : 1.0f;
    }
    
    /**
     * Gets the horizontal scaling factor from window to framebuffer coordinates.
     * 
     * @return The horizontal scale factor
     */
    public float getHorizontalScale() {
        return windowWidth > 0 ? (float) framebufferWidth / windowWidth : 1.0f;
    }
    
    /**
     * Gets the vertical scaling factor from window to framebuffer coordinates.
     * 
     * @return The vertical scale factor
     */
    public float getVerticalScale() {
        return windowHeight > 0 ? (float) framebufferHeight / windowHeight : 1.0f;
    }
    
    /**
     * Gets the average scaling factor (useful for uniform scaling operations).
     * 
     * @return The average of horizontal and vertical scale factors
     */
    public float getAverageScale() {
        return (getHorizontalScale() + getVerticalScale()) / 2.0f;
    }
    
    // Coordinate Conversion
    
    /**
     * Converts a window X coordinate to framebuffer coordinates.
     * 
     * @param windowX The X coordinate in window space
     * @return The X coordinate in framebuffer space
     */
    public int windowToFramebufferX(int windowX) {
        return Math.round(windowX * getHorizontalScale());
    }
    
    /**
     * Converts a window Y coordinate to framebuffer coordinates.
     * 
     * @param windowY The Y coordinate in window space
     * @return The Y coordinate in framebuffer space
     */
    public int windowToFramebufferY(int windowY) {
        return Math.round(windowY * getVerticalScale());
    }
    
    /**
     * Converts framebuffer X coordinate to window coordinates.
     * 
     * @param framebufferX The X coordinate in framebuffer space
     * @return The X coordinate in window space
     */
    public int framebufferToWindowX(int framebufferX) {
        float scale = getHorizontalScale();
        return scale > 0 ? Math.round(framebufferX / scale) : framebufferX;
    }
    
    /**
     * Converts framebuffer Y coordinate to window coordinates.
     * 
     * @param framebufferY The Y coordinate in framebuffer space
     * @return The Y coordinate in window space
     */
    public int framebufferToWindowY(int framebufferY) {
        float scale = getVerticalScale();
        return scale > 0 ? Math.round(framebufferY / scale) : framebufferY;
    }
    
    // Utility Methods
    
    /**
     * Checks if the viewport has valid (positive) dimensions.
     * 
     * @return true if all dimensions are positive
     */
    public boolean isValid() {
        return windowWidth > 0 && windowHeight > 0 && framebufferWidth > 0 && framebufferHeight > 0;
    }
    
    /**
     * Checks if this is a high-DPI display (framebuffer larger than window).
     * 
     * @return true if either scale factor is greater than 1.0
     */
    public boolean isHighDPI() {
        return getHorizontalScale() > 1.0f || getVerticalScale() > 1.0f;
    }
    
    /**
     * Gets the total number of pixels in the framebuffer.
     * 
     * @return The framebuffer area in pixels
     */
    public int getFramebufferArea() {
        return framebufferWidth * framebufferHeight;
    }
    
    @Override
    public String toString() {
        return String.format("ViewportDimensions{window=%dx%d, framebuffer=%dx%d, scale=%.2fx%.2f}", 
                windowWidth, windowHeight, framebufferWidth, framebufferHeight, 
                getHorizontalScale(), getVerticalScale());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ViewportDimensions that = (ViewportDimensions) obj;
        return windowWidth == that.windowWidth &&
               windowHeight == that.windowHeight &&
               framebufferWidth == that.framebufferWidth &&
               framebufferHeight == that.framebufferHeight;
    }
    
    @Override
    public int hashCode() {
        int result = windowWidth;
        result = 31 * result + windowHeight;
        result = 31 * result + framebufferWidth;
        result = 31 * result + framebufferHeight;
        return result;
    }
}