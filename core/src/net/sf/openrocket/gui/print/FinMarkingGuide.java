package net.sf.openrocket.gui.print;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the core Swing representation of a fin marking guide.  It can handle multiple fin sets on the same or
 * different body tubes. One marking guide will be created for any body tube that has a finset.  If a tube has multiple
 * finsets, then they are combined onto one marking guide. It also includes launch lug marking line(s) if lugs are
 * present. If (and only if) a launch lug exists, then the word 'Front' is affixed to the leading edge of the guide to
 * give orientation.
 * <p/>
 */
public class FinMarkingGuide extends JPanel {

    /**
     * The stroke of normal lines.
     */
    private final static BasicStroke thinStroke = new BasicStroke(1.0f);

    /**
     * The size of the arrow in points.
     */
    private static final int ARROW_SIZE = 10;

    /**
     * Typical thickness of a piece of printer paper (~20-24 lb paper). Wrapping paper around a tube results in the
     * radius being increased by the thickness of the paper. The smaller the tube, the more pronounced this becomes as a
     * percentage of circumference.  Using 1/10mm as an approximation here.
     */
    private static final double PAPER_THICKNESS_IN_METERS = PrintUnit.MILLIMETERS.toMeters(0.1d);

    /**
     * The default guide width in inches.
     */
    public final static double DEFAULT_GUIDE_WIDTH = 3d;

    /**
     * 2 PI radians (represents a circle).
     */
    public final static double TWO_PI = 2 * Math.PI;

    /**
     * The I18N translator.
     */
    private static final Translator trans = Application.getTranslator();

    /**
     * The margin.
     */
    private static final int MARGIN = (int) PrintUnit.INCHES.toPoints(0.25f);

    /**
     * The height (circumference) of the biggest body tube with a finset.
     */
    private int maxHeight = 0;

    /**
     * A map of body tubes, to a list of components that contains finsets and launch lugs.
     */
    private Map<BodyTube, java.util.List<ExternalComponent>> markingGuideItems;

    /**
     * Constructor.
     *
     * @param rocket the rocket instance
     */
    public FinMarkingGuide(Rocket rocket) {
        super(false);
        setBackground(Color.white);
        markingGuideItems = init(rocket);
        //Max of 2 drawing guides horizontally per page.
        setSize((int) PrintUnit.INCHES.toPoints(DEFAULT_GUIDE_WIDTH) * 2 + 3 * MARGIN, maxHeight);
    }

    /**
     * Initialize the marking guide class by iterating over a rocket and finding all finsets.
     *
     * @param component the root rocket component - this is iterated to find all finset and launch lugs
     *
     * @return a map of body tubes to lists of finsets and launch lugs.
     */
    private Map<BodyTube, java.util.List<ExternalComponent>> init(Rocket component) {
        Iterator<RocketComponent> iter = component.iterator(false);
        Map<BodyTube, java.util.List<ExternalComponent>> results = new LinkedHashMap<BodyTube, List<ExternalComponent>>();
        BodyTube current = null;
        int totalHeight = 0;
        int iterationHeight = 0;
        int count = 0;

        while (iter.hasNext()) {
            RocketComponent next = iter.next();
            if (next instanceof BodyTube) {
                current = (BodyTube) next;
            }
            else if (next instanceof FinSet || next instanceof LaunchLug) {
                java.util.List<ExternalComponent> list = results.get(current);
                if (list == null && current != null) {
                    list = new ArrayList<ExternalComponent>();
                    results.put(current, list);

                    double radius = current.getOuterRadius();
                    int circumferenceInPoints = (int) PrintUnit.METERS.toPoints(radius * TWO_PI);

                    // Find the biggest body tube circumference.
                    if (iterationHeight < (circumferenceInPoints + MARGIN)) {
                        iterationHeight = circumferenceInPoints + MARGIN;
                    }
                    //At most, two marking guides horizontally.  After that, move down and back to the left margin.
                    count++;
                    if (count % 2 == 0) {
                        totalHeight += iterationHeight;
                        iterationHeight = 0;
                    }
                }
                if (list != null) {
                    list.add((ExternalComponent) next);
                }
            }
        }
        maxHeight = totalHeight + iterationHeight;
        return results;
    }

    /**
     * Returns a generated image of the fin marking guide.  May then be used wherever AWT images can be used, or
     * converted to another image/picture format and used accordingly.
     *
     * @return an awt image of the fin marking guide
     */
    public Image createImage() {
        int width = getWidth() + 25;
        int height = getHeight() + 25;
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // Create a graphics context on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
        // Draw graphics
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, width, height);
        paintComponent(g2d);
        // Graphics context no longer needed so dispose it
        g2d.dispose();
        return bufferedImage;
    }

    /**
     * <pre>
     *   ---------------------- Page Edge --------------------------------------------------------
     *   |                                        ^
     *   |                                        |
     *   |
     *   |                                        y
     *   |
     *   |                                        |
     *   P                                        v
     *   a      ---                 +----------------------------+  ------------
     *   g<------^-- x ------------>+                            +       ^
     *   e       |                  +                            +       |
     *           |                  +                            +     baseYOffset
     *   E       |                  +                            +       v
     *   d       |                  +<----------Fin------------->+ -------------
     *   g       |                  +                            +
     *   e       |                  +                            +
     *   |       |                  +                            +
     *   |       |                  +                            +
     *   |       |                  +                            +   baseSpacing
     *   |       |                  +                            +
     *   |       |                  +                            +
     *   |       |                  +                            +
     *   |       |                  +                            +
     *   |       |                  +<----------Fin------------->+  --------------
     *   |       |                  +                            +
     *   | circumferenceInPoints    +                            +
     *   |       |                  +                            +
     *   |       |                  +                            +
     *   |       |                  +                            +    baseSpacing
     *   |       |                  +<------Launch Lug --------->+           -----
     *   |       |                  +                            +                 \
     *   |       |                  +                            +                 + yLLOffset
     *   |       |                  +                            +                 /
     *   |       |                  +<----------Fin------------->+  --------------
     *   |       |                  +                            +       ^
     *   |       |                  +                            +       |
     *   |       |                  +                            +    baseYOffset
     *   |       v                  +                            +       v
     *   |      ---                 +----------------------------+  --------------
     *
     *                              |<-------- width ----------->|
     *
     * yLLOffset is computed from the difference between the base rotation of the fin and the radial direction of the
     * lug.
     *
     * Note: There is a current limitation that a tube with multiple launch lugs may not render the lug lines
     * correctly.
     * </pre>
     *
     * @param g the Graphics context
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        paintFinMarkingGuide(g2);
    }

    private void paintFinMarkingGuide(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.BLACK);
        g2.setStroke(thinStroke);
        int x = MARGIN;
        int y = MARGIN;

        int width = (int) PrintUnit.INCHES.toPoints(DEFAULT_GUIDE_WIDTH);

        int column = 0;

        for (BodyTube next : markingGuideItems.keySet()) {
            double circumferenceInPoints = PrintUnit.METERS.toPoints((next.getOuterRadius() + PAPER_THICKNESS_IN_METERS) *
                    TWO_PI);
            List<ExternalComponent> componentList = markingGuideItems.get(next);
            //Don't draw the lug if there are no fins.
            if (hasFins(componentList)) {

                drawMarkingGuide(g2, x, y, (int) Math.ceil(circumferenceInPoints), width);

                //Sort so that fins always precede lugs
                sort(componentList);

                boolean hasMultipleComponents = componentList.size() > 1;

                double baseSpacing = 0d;
                double baseYOrigin = 0;
                double finRadial = 0d;
                int yFirstFin = y;
                int yLastFin = y;
                boolean firstFinSet = true;

                //fin1: 42  fin2: 25
                for (ExternalComponent externalComponent : componentList) {
                    if (externalComponent instanceof FinSet) {
                        FinSet fins = (FinSet) externalComponent;
                        int finCount = fins.getFinCount();
                        int offset = 0;
                        baseSpacing = (circumferenceInPoints / finCount);
                        double baseRotation = fins.getBaseRotation();
                        if (!firstFinSet) {
                            //Adjust the rotation to a positive number.
                            while (baseRotation < 0) {
                                baseRotation += TWO_PI / finCount;
                            }
                            offset = computeYOffset(y, circumferenceInPoints, baseSpacing, baseYOrigin, finRadial, baseRotation);
                        }
                        else {
                            //baseYOrigin is the distance from the top of the marking guide to the first fin of the first fin set.
                            //This measurement is used to base all subsequent finsets and lugs off of.
                            baseYOrigin = baseSpacing / 2;
                            offset = (int) (baseYOrigin) + y;
                            firstFinSet = false;
                        }
                        yFirstFin = y;
                        yLastFin = y;
                        finRadial = baseRotation;

                        //Draw the fin marking lines.
                        for (int fin = 0; fin < finCount; fin++) {
                            if (fin > 0) {
                                offset += baseSpacing;
                                yLastFin = offset;
                            }
                            else {
                                yFirstFin = offset;
                            }
                            drawDoubleArrowLine(g2, x, offset, x + width, offset);
                            //   if (hasMultipleComponents) {
                            g2.drawString(externalComponent.getName(), x + (width / 3), offset - 2);
                            //   }
                        }
                    }
                    else if (externalComponent instanceof LaunchLug) {
                        LaunchLug lug = (LaunchLug) externalComponent;
                        double yLLOffset = (lug.getRadialDirection() - finRadial) / TWO_PI;
                        //The placement of the lug line must respect the boundary of the outer marking guide.  In order
                        //to do that, place it above or below either the top or bottom fin line, based on the difference
                        //between their rotational directions.
                        if (yLLOffset < 0) {
                            yLLOffset = yLLOffset * circumferenceInPoints + yLastFin;
                        }
                        else {
                            yLLOffset = yLLOffset * circumferenceInPoints + yFirstFin;
                        }
                        drawDoubleArrowLine(g2, x, (int) yLLOffset, x + width, (int) yLLOffset);
                        g2.drawString(lug.getName(), x + (width / 3), (int) yLLOffset - 2);

                    }
                }
                //Only if the tube has a lug or multiple finsets does the orientation of the marking guide matter. So print 'Front'.
                if (hasMultipleComponents) {
                    drawFrontIndication(g2, x, y, (int) baseSpacing, (int) circumferenceInPoints, width);
                }

                //At most, two marking guides horizontally.  After that, move down and back to the left margin.
                column++;
                if (column % 2 == 0) {
                    x = MARGIN;
                    y += circumferenceInPoints + MARGIN;
                }
                else {
                    x += MARGIN + width;
                }
            }
        }
    }

    /**
     * Compute the y offset for the next fin line.
     *
     * @param y                     the top margin
     * @param circumferenceInPoints the circumference (height) of the guide
     * @param baseSpacing           the circumference / fin count
     * @param baseYOrigin           the offset from the top of the guide to the first fin of the first fin set drawn
     * @param prevBaseRotation      the rotation of the previous finset
     * @param baseRotation          the rotation of the current finset
     *
     * @return number of points from the top of the marking guide to the line to be drawn
     */
    private int computeYOffset(int y, double circumferenceInPoints, double baseSpacing, double baseYOrigin, double prevBaseRotation, double baseRotation) {
        int offset;
        double finRadialDifference = (baseRotation - prevBaseRotation) / TWO_PI;
        //If the fin line would be off the top of the marking guide, then readjust.
        if (baseYOrigin + finRadialDifference * circumferenceInPoints < 0) {
            offset = (int) (baseYOrigin + baseSpacing + finRadialDifference * circumferenceInPoints) + y;
        }
        else if (baseYOrigin - finRadialDifference * circumferenceInPoints > 0) {
            offset = (int) (finRadialDifference * circumferenceInPoints + baseYOrigin) + y;
        }
        else {
            offset = (int) (finRadialDifference * circumferenceInPoints - baseYOrigin) + y;
        }
        return offset;
    }

    /**
     * Determines if the list contains a FinSet.
     *
     * @param list a list of ExternalComponent
     *
     * @return true if the list contains at least one FinSet
     */
    private boolean hasFins(List<ExternalComponent> list) {
        for (ExternalComponent externalComponent : list) {
            if (externalComponent instanceof FinSet) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sort a list of ExternalComponent in-place. Forces FinSets to precede Launch Lugs.
     *
     * @param componentList a list of ExternalComponent
     */
    private void sort(List<ExternalComponent> componentList) {
        Collections.sort(componentList, new Comparator<ExternalComponent>() {
            @Override
            public int compare(ExternalComponent o1, ExternalComponent o2) {
                if (o1 instanceof FinSet) {
                    return -1;
                }
                if (o2 instanceof FinSet) {
                    return 1;
                }
                return 0;
            }
        });
    }

    /**
     * Draw the marking guide outline.
     *
     * @param g2     the graphics context
     * @param x      the starting x coordinate
     * @param y      the starting y coordinate
     * @param length the length, or height, in print units of the marking guide; should be equivalent to the outer tube
     *               circumference
     * @param width  the width of the marking guide in print units; somewhat arbitrary
     */
    private void drawMarkingGuide(Graphics2D g2, int x, int y, int length, int width) {
        Path2D outline = new Path2D.Float(GeneralPath.WIND_EVEN_ODD, 4);
        outline.moveTo(x, y);
        outline.lineTo(width + x, y);
        outline.lineTo(width + x, length + y);
        outline.lineTo(x, length + y);
        outline.closePath();
        g2.draw(outline);

        //Draw tick marks for alignment, 1/4 of the width in from either edge
        int fromEdge = (width) / 4;
        final int tickLength = 8;
        //Upper left
        g2.drawLine(x + fromEdge, y, x + fromEdge, y + tickLength);
        //Upper right
        g2.drawLine(x + width - fromEdge, y, x + width - fromEdge, y + tickLength);
        //Lower left
        g2.drawLine(x + fromEdge, y + length - tickLength, x + fromEdge, y + length);
        //Lower right
        g2.drawLine(x + width - fromEdge, y + length - tickLength, x + width - fromEdge, y + length);
    }

    /**
     * Draw a vertical string indicating the front of the rocket.  This is necessary when a launch lug exists to give
     * proper orientation of the guide (assuming that the lug is asymmetrically positioned with respect to a fin).
     *
     * @param g2      the graphics context
     * @param x       the starting x coordinate
     * @param y       the starting y coordinate
     * @param spacing the space between fin lines
     * @param length  the length, or height, in print units of the marking guide; should be equivalent to the outer tube
     *                circumference
     * @param width   the width of the marking guide in print units; somewhat arbitrary
     */
    private void drawFrontIndication(Graphics2D g2, int x, int y, int spacing, int length, int width) {
        //The magic numbers here are fairly arbitrary.  They're chosen in a manner that best positions 'Front' to be
        //readable, without going to complex string layout prediction logic.
        int rotateX = x + width - 16;
        int rotateY = y + (int) (spacing * 1.5) + 20;
        if (rotateY > y + length + 14) {
            rotateY = y + length / 2 - 10;
        }
        g2.translate(rotateX, rotateY);
        g2.rotate(Math.PI / 2);
        g2.drawString(trans.get("FinMarkingGuide.lbl.Front"), 0, 0);
        g2.rotate(-Math.PI / 2);
        g2.translate(-rotateX, -rotateY);
    }

    /**
     * Draw a horizontal line with arrows on both endpoints.  Depicts a fin alignment.
     *
     * @param g2 the graphics context
     * @param x1 the starting x coordinate
     * @param y1 the starting y coordinate
     * @param x2 the ending x coordinate
     * @param y2 the ending y coordinate
     */
    void drawDoubleArrowLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
        int len = x2 - x1;

        g2.drawLine(x1, y1, x1 + len, y2);
        g2.fillPolygon(new int[]{x1 + len, x1 + len - ARROW_SIZE, x1 + len - ARROW_SIZE, x1 + len},
                new int[]{y2, y2 - ARROW_SIZE / 2, y2 + ARROW_SIZE / 2, y2}, 4);

        g2.fillPolygon(new int[]{x1, x1 + ARROW_SIZE, x1 + ARROW_SIZE, x1},
                new int[]{y1, y1 - ARROW_SIZE / 2, y1 + ARROW_SIZE / 2, y1}, 4);
    }
}