package net.sf.openrocket.gui.print;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public abstract class AbstractPrintable<T> extends PrintableComponent {
    /**
     * A thin stroke.
     */
    public final static BasicStroke thinStroke = new BasicStroke(1.0f);

    /**
     * A thick stroke.
     */
    public final static BasicStroke thickStroke = new BasicStroke(4.0f);

    /**
     * Constructor. Initialize this printable with the component to be printed.
     *
     * @param component  the component to be printed
     */
    public AbstractPrintable(T component) {
        init(component);
    }

    /**
     * Initialize the printable.
     *
     * @param component the component
     */
    protected abstract void init(T component);

    /**
     * Draw the component onto the graphics context.
     *
     * @param g2 the graphics context
     */
    protected abstract void draw(Graphics2D g2);

    /**
     * Returns a generated image of the component.  May then be used wherever AWT images can be used, or converted to
     * another image/picture format and used accordingly.
     *
     * @return an awt image of the printable component
     */
    public Image createImage() {
        int width = getWidth();
        int height = getHeight();
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
        // Draw graphics
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, width, height);
        paintComponent(g2d);
        // Graphics context no longer needed so dispose it
        g2d.dispose();
        return bufferedImage;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.BLACK);
        g2.setStroke(thinStroke);
        translate(g2);

        draw(g2);
    }

    protected void translate(final Graphics2D theG2) {
        theG2.translate(getOffsetX(), getOffsetY());
    }
}
