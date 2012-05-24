package net.sf.openrocket.gui.print;

import net.sf.openrocket.rocketcomponent.CenteringRing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 * This class creates a renderable centering ring.  It depends only on AWT/Swing and can be called from other
 * actors (like iText handlers) to render the centering ring on different graphics contexts.
 */
public class PrintableCenteringRing extends AbstractPrintable<CenteringRing> {
    /**
     * If the component to be drawn is a centering ring, save a reference to it.
     */
    private CenteringRing target;

    /**
     * The X margin.
     */
    protected int marginX = (int) PrintUnit.INCHES.toPoints(0.25f);

    /**
     * The Y margin.
     */
    protected int marginY = (int) PrintUnit.INCHES.toPoints(0.25f);

    /**
     * The line length of the cross hairs.
     */
    private final int lineLength = 10;

    /**
     * Construct a printable nose cone.
     *
     * @param theRing the component to print
     */
    public PrintableCenteringRing(CenteringRing theRing) {
        super(false, theRing);
    }

    /**
     * @param component the centering ring component
     */
    @Override
    protected void init(final CenteringRing component) {

        target = component;

        double radius = target.getOuterRadius();
        setSize((int) PrintUnit.METERS.toPoints(2 * radius) + marginX,
                (int) PrintUnit.METERS.toPoints(2 * radius) + marginY);
    }

    /**
     * Draw a centering ring.
     *
     * @param g2 the graphics context
     */
    @Override
    protected void draw(Graphics2D g2) {
        double radius = PrintUnit.METERS.toPoints(target.getOuterRadius());

        Color original = g2.getBackground();
        double x = marginX;
        double y = marginY;
        Shape outerCircle = new Ellipse2D.Double(x, y, radius * 2, radius * 2);
        g2.setColor(Color.lightGray);
        g2.fill(outerCircle);
        g2.setColor(Color.black);
        g2.draw(outerCircle);
        x += radius;
        y += radius;

        double innerRadius = PrintUnit.METERS.toPoints(target.getInnerRadius());
        Shape innerCircle = new Ellipse2D.Double(x - innerRadius, y - innerRadius, innerRadius * 2, innerRadius * 2);
        g2.setColor(original);
        g2.fill(innerCircle);
        g2.setColor(Color.black);
        g2.draw(innerCircle);

        drawCross(g2, (int) x, (int) y, lineLength, lineLength);
    }

    /**
     * Draw the center cross-hair.
     *
     * @param g  the graphics context
     * @param x  the x coordinate of the center point
     * @param y  the y coordinate of the center point
     * @param width the width in pixels of the horizontal hair
     * @param height the width in pixels of the vertical hair
     */
    private void drawCross(Graphics g, int x, int y, int width, int height) {
        g.setColor(Color.black);
        ((Graphics2D) g).setStroke(thinStroke);
        g.drawLine(x - width / 2, y, x + width / 2, y);
        g.drawLine(x, y - height / 2, x, y + height / 2);
    }

}
