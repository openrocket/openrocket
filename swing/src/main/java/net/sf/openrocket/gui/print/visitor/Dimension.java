package net.sf.openrocket.gui.print.visitor;

/**
 * Convenience class to model a dimension.
 */
public class Dimension {
    /**
     * Width, in points.
     */
    public float width;
    /**
     * Height, in points.
     */
    public float height;
    /**
     * Breadth, in points.
     */
    public float breadth = 0f;

    /**
     * Constructor.
     *
     * @param w width
     * @param h height
     */
    public Dimension(float w, float h) {
        width = w;
        height = h;
    }

    /**
     * Constructor.
     *
     * @param w width
     * @param h height
     * @param b breadth; optionally used to represent radius
     */
    public Dimension(float w, float h, float b) {
        width = w;
        height = h;
        breadth = b;
    }

    /**
     * Get the width.
     *
     * @return the width
     */
    public float getWidth() {
        return width;
    }

    /**
     * Get the height.
     *
     * @return the height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Get the breadth.
     *
     * @return the breadth
     */
    public float getBreadth() {
        return breadth;
    }
}