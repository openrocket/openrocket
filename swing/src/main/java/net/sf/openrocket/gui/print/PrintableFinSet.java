/*
 * PrintableFinSet.java
 */
package net.sf.openrocket.gui.print;

import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.util.Coordinate;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

/**
 * This class allows for a FinSet to be printable.  It does so by decorating an existing finset (which will not be
 * modified) and rendering it within a JPanel.  The JPanel is not actually visualized on a display, but instead renders
 * it to a print device.
 */
public class PrintableFinSet extends AbstractPrintable<FinSet> {

    /**
     * The object that represents the shape (outline) of the fin.  This gets drawn onto the Swing component.
     */
    protected GeneralPath finPolygon;
    /**
     * The object that represents the tab (outline) of the fin.  This gets drawn onto the Swing component.
     */
    protected GeneralPath finTabPolygon;

    /**
     * The minimum X coordinate.
     */
    private int minX;
    /**
     * The minimum Y coordinate.
     */
    private int minY;

    /**
     * Constructor.
     *
     * @param fs the finset to print
     */
    public PrintableFinSet (FinSet fs) {
        super(fs);
    }

    /**
     * Initialize the fin set polygon and set the size of the component.
     *
     * @param component the fin set
     */
    protected void init (FinSet component) {

        Coordinate[] points = component.getFinPointsWithRoot();
        Coordinate[] tabPoints = component.getTabPoints();

        finPolygon = new GeneralPath(GeneralPath.WIND_NON_ZERO, points.length);
        finTabPolygon = new GeneralPath(GeneralPath.WIND_NON_ZERO, tabPoints.length);

        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;;
        int maxX = Integer.MIN_VALUE;;
        int maxY = Integer.MIN_VALUE;

        for (int i = 0; i < points.length; i++) {
            final float x = (float) PrintUnit.METERS.toPoints(points[i].x);
            final float y = (float) PrintUnit.METERS.toPoints(points[i].y);
            minX = (int) Math.min(x, minX);
            minY = (int) Math.min(y, minY);
            maxX = (int) Math.max(x, maxX);
            maxY = (int) Math.max(y, maxY);
            if (i == 0) {
                finPolygon.moveTo(x, y);
            } else {
                finPolygon.lineTo(x, y);
            }
        }
        finPolygon.closePath();

        for (int i = 0; i < tabPoints.length; i++) {
            final float x = (float) PrintUnit.METERS.toPoints(tabPoints[i].x);
            final float y = (float) PrintUnit.METERS.toPoints(tabPoints[i].y);
            minX = (int) Math.min(x, minX);
            minY = (int) Math.min(y, minY);
            maxX = (int) Math.max(x, maxX);
            maxY = (int) Math.max(y, maxY);
            if (i == 0) {
                finTabPolygon.moveTo(x, y);
            } else {
                finTabPolygon.lineTo(x, y);
            }
        }
        if (tabPoints.length > 0) {
            finTabPolygon.closePath();
        }

        setSize(maxX - minX + 1, maxY - minY + 1);
    }

    /**
     * Render the fin set onto the graphics context.  This is done by creating a GeneralPath component that follows the
     * outline of the fin set coordinates to create a polygon, which is then drawn onto the graphics context.
     * Through-the-wall fin tabs are supported if they are present.
     *
     * @param g2d the Java2D graphics context
     */
    protected void draw(Graphics2D g2d) {
        int x = 0;
        int y = 0;

        int marginX = this.getOffsetX();
        int marginY = this.getOffsetY();

        // The minimum X/Y can be negative (primarily only Y due to fin tabs; rarely (never) X, but protect both anyway).
        if (minX < marginX) {
            x = marginX + Math.abs(minX);
        }
        if (minY < marginY) {
            y = marginY + Math.abs(minY);
        }
        // Reset the origin.
        g2d.translate(x, y);
        g2d.setPaint(TemplateProperties.getFillColor());
        g2d.fill(finPolygon);
        g2d.fill(finTabPolygon);
        g2d.setPaint(TemplateProperties.getLineColor());
        g2d.draw(finPolygon);
        g2d.draw(finTabPolygon);
    }

    /**
     * Don't let super class translate the coordinates - we'll do that ourselves in the draw method.
     *
     * @param theG2 the graphics context
     */
    @Override
    protected void translate(final Graphics2D theG2) {
    }
}
