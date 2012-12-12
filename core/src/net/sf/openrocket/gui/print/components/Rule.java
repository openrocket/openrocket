package net.sf.openrocket.gui.print.components;

import net.sf.openrocket.gui.print.PrintUnit;
import net.sf.openrocket.gui.print.PrintableComponent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * This class creates a Swing ruler.  The ruler has both vertical and horizontal rules, as well as divisions for both
 * inches and centimeters.
 */
public class Rule extends PrintableComponent {

    public static enum Orientation {
        TOP,
        BOTTOM
    }

    public static final int TINIEST_TICK_LENGTH = 3;
    public static final int MINOR_TICK_LENGTH = 6;
    public static final int MID_MAJOR_TICK_LENGTH = 9;
    public static final int MAJOR_TICK_LENGTH = 14;

    private Orientation orientation;

    /**
     * Constructor.
     *
     * @param theOrientation defines if the horizontal ruler should be on the top or bottom; the vertical is always
     *                       left justified
     */
    public Rule(Orientation theOrientation) {
        orientation = theOrientation;
        int dim = (int) PrintUnit.INCHES.toPoints(2) + 32;
        setSize(dim, dim);
    }

    /**
     * Render the component onto a graphics context.
     *
     * @param g the opaque graphics context
     */
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        double div = PrintUnit.INCHES.toPoints(1) / 8;  //1/8 inch increment
        final int width = (int) PrintUnit.INCHES.toPoints(2);
        int x = 20;
        int y = x + 20;
        boolean inchOutSide = true;

        g2.translate(getOffsetX(), getOffsetY());

        if (orientation == Orientation.TOP) {
            Font f = g.getFont();
            g.setFont(f.deriveFont(f.getSize() - 2f));
            g.drawString("in  cm", x - MAJOR_TICK_LENGTH, y + width + 20);
            g.drawString("in", x + width + 4, y + 4);
            g.drawString("cm", x + width + 4, y + 18);
            y += 6;

            drawVerticalRule(g2, true, inchOutSide, x, y, width, 0, div * 2, div * 4, div * 8);
            drawHorizontalRule(g2, true, !inchOutSide, x, y, width, 0, div * 2, div * 4, div * 8);
            div = PrintUnit.MILLIMETERS.toPoints(1);  //mm increment
            drawVerticalRule(g2, true, !inchOutSide, x, y, width, 0, 0, div * 5, div * 10);
            drawHorizontalRule(g2, true, inchOutSide, x, y, width, 0, 0, div * 5, div * 10);
        }
        else {
            Font f = g.getFont();
            g.setFont(f.deriveFont(f.getSize() - 2f));
            g.drawString("in  cm", x - MAJOR_TICK_LENGTH, y);
            g.drawString("cm", x + width + 6, y + width + 4);
            g.drawString("in", x + width + 6, y + width + 18);
            y += 6;

            //Draw Inches first, with 1/2", 1/4", and 1/8" tick marks.
            drawVerticalRule(g2, false, inchOutSide, x, y, width, 0, div * 2, div * 4, div * 8);
            drawHorizontalRule(g2, true, inchOutSide, x, y + width, width, 0, div * 2, div * 4, div * 8);
            div = PrintUnit.MILLIMETERS.toPoints(1);  //mm increment
            //Draw cm (10mm) and 1/2 cm (5mm) marks
            drawVerticalRule(g2, false, !inchOutSide, x, y, width, 0, 0, div * 5, div * 10);
            drawHorizontalRule(g2, true, !inchOutSide, x, y + width, width, 0, 0, div * 5, div * 10);
        }
    }

    /**
     * Draw a horizontal ruler.
     *
     * @param g              the graphics context
     * @param vertexAtLeft   true if the horizontal/vertical vertex is oriented to the top
     * @param drawTicksDown  true if the ruler should draw interval tick marks to the underside of the solid ruler line
     * @param x              starting x position of the ruler
     * @param y              starting y position of the rule
     * @param length         the number of points in length to extend the vertical ruler
     * @param tinyEveryX     the number of points for each tiny division tick line; if zero or negative tiny will not be
     *                       drawn
     * @param minorEveryX    the number of points for each minor division tick line; if zero or negative minor will not
     *                       be drawn
     * @param midMajorEveryX the number of points for each mid-major division tick line
     * @param majorEveryX    the number of points for each major division tick line (this is typically the inch or cm
     *                       distance in points).
     */
    private void drawHorizontalRule(Graphics2D g,
                                   boolean vertexAtLeft,
                                   boolean drawTicksDown,
                                   int x, int y, int length,
                                   double tinyEveryX,
                                   double minorEveryX,
                                   double midMajorEveryX,
                                   double majorEveryX) {

        //Draw solid horizontal line
        g.setColor(Color.black);
        g.drawLine(x, y, x + length, y);

        int tiniest = drawTicksDown ? TINIEST_TICK_LENGTH : -1 * TINIEST_TICK_LENGTH;
        int minor = drawTicksDown ? MINOR_TICK_LENGTH : -1 * MINOR_TICK_LENGTH;
        int mid = drawTicksDown ? MID_MAJOR_TICK_LENGTH : -1 * MID_MAJOR_TICK_LENGTH;
        int major = drawTicksDown ? MAJOR_TICK_LENGTH : -1 * MAJOR_TICK_LENGTH;

        //Draw vertical rule ticks for the horizontal ruler
        //Draw minor ticks
        int initial = x;
        int end = initial + length;
        double increment = tinyEveryX;
        boolean lessThanEqual = true;
        if (!vertexAtLeft) {
            initial = x + length;
            end = x;
            lessThanEqual = false;
        }

        if (tinyEveryX > 0) {
            if (!vertexAtLeft) {
                increment = -1 * increment;
            }
            for (double xtick = initial; lessThanEqual ? (xtick <= end) : (xtick >= end); xtick += increment) {
                g.drawLine((int) xtick, y, (int) xtick, y + tiniest);
            }
        }
        //Draw minor ticks
        if (minorEveryX > 0) {
            if (!vertexAtLeft) {
                increment = -1 * minorEveryX;
            }
            else {
                increment = minorEveryX;
            }
            for (double xtick = initial; lessThanEqual ? (xtick <= end) : (xtick >= end); xtick += increment) {
                g.drawLine((int) xtick, y, (int) xtick, y + minor);
            }
        }

        //Draw mid-major ticks
        if (midMajorEveryX > 0) {
            if (!vertexAtLeft) {
                increment = -1 * midMajorEveryX;
            }
            else {
                increment = midMajorEveryX;
            }
            for (double xtick = initial; lessThanEqual ? (xtick <= end) : (xtick >= end); xtick += increment) {
                g.drawLine((int) xtick, y, (int) xtick, y + mid);
            }
        }
        if (!vertexAtLeft) {
            increment = -1 * majorEveryX;
        }
        else {
            increment = majorEveryX;
        }
        //Draw major ticks
        for (double xtick = initial; lessThanEqual ? (xtick <= end) : (xtick >= end); xtick += increment) {
            g.drawLine((int) xtick, y, (int) xtick, y + major);
        }

    }

    /**
     * Draw a vertical ruler.
     *
     * @param g              the graphics context
     * @param vertexAtTop    true if the horizontal/vertical vertex is oriented to the top
     * @param drawTicksRight true if the ruler should draw interval tick marks to the right side of the solid ruler
     *                       line
     * @param x              starting x position of the ruler
     * @param y              starting y position of the rule
     * @param length         the number of points in length to extend the vertical ruler
     * @param tinyEveryY     the number of points for each tiny division tick line; if zero or negative tiny will not be
     *                       drawn
     * @param minorEveryY    the number of points for each minor division tick line; if zero or negative minor will not
     *                       be drawn
     * @param midMajorEveryY the number of points for each mid-major division tick line
     * @param majorEveryY    the number of points for each major division tick line (this is typically the inch or cm
     *                       distance in points).
     */
    private void drawVerticalRule(Graphics2D g,
                                 boolean vertexAtTop,
                                 boolean drawTicksRight, int x, int y, int length,
                                 double tinyEveryY,
                                 double minorEveryY,
                                 double midMajorEveryY,
                                 double majorEveryY) {

        int tiniest = drawTicksRight ? TINIEST_TICK_LENGTH : -1 * TINIEST_TICK_LENGTH;
        int minor = drawTicksRight ? MINOR_TICK_LENGTH : -1 * MINOR_TICK_LENGTH;
        int mid = drawTicksRight ? MID_MAJOR_TICK_LENGTH : -1 * MID_MAJOR_TICK_LENGTH;
        int major = drawTicksRight ? MAJOR_TICK_LENGTH : -1 * MAJOR_TICK_LENGTH;

        //Draw solid vertical line
        g.setColor(Color.black);
        g.drawLine(x, y, x, y + length);

        //Draw horizontal rule ticks for the vertical ruler
        //Draw tiny ticks
        int initial = y;
        int end = initial + length;
        double increment = tinyEveryY;
        boolean lessThanEqual = true;
        if (!vertexAtTop) {
            initial = y + length;
            end = y;
            lessThanEqual = false;
        }

        if (tinyEveryY > 0) {
            if (!vertexAtTop) {
                increment = -1 * increment;
            }
            for (double tick = initial; lessThanEqual ? (tick <= end) : (tick >= end); tick += increment) {
                g.drawLine(x, (int) tick, x - tiniest, (int) tick);
            }
        }

        //Draw minor ticks
        if (minorEveryY > 0) {
            if (!vertexAtTop) {
                increment = -1 * minorEveryY;
            }
            else {
                increment = minorEveryY;
            }
            for (double tick = initial; lessThanEqual ? (tick <= end) : (tick >= end); tick += increment) {
                g.drawLine(x, (int) tick, x - minor, (int) tick);
            }
        }

        //Draw mid-major ticks
        if (!vertexAtTop) {
            increment = -1 * midMajorEveryY;
        }
        else {
            increment = midMajorEveryY;
        }
        for (double tick = initial; lessThanEqual ? (tick <= end) : (tick >= end); tick += increment) {
            g.drawLine(x, (int) tick, x - mid, (int) tick);
        }

        //Draw major ticks
        if (!vertexAtTop) {
            increment = -1 * majorEveryY;
        }
        else {
            increment = majorEveryY;
        }
        for (double tick = initial; lessThanEqual ? (tick <= end) : (tick >= end); tick += increment) {
            g.drawLine(x, (int) tick, x - major, (int) tick);
        }

    }
}