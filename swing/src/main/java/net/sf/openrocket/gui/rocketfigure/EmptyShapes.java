package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * Shapes of an "empty"/virtual object, e.g. a podset without any children.
 * The shape is a center square with additional lines on the north, east, south and west side of the square.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class EmptyShapes extends RocketComponentShape {
    /**
     * Returns the empty shape in the side view.
     * @param radius radius of the center square
     */
    public static Shape[] getShapesSide(final Transformation transformation, final double radius) {
        final Coordinate instanceAbsoluteLocation = transformation.transform(Coordinate.ZERO);
        double x = instanceAbsoluteLocation.x;
        double y = instanceAbsoluteLocation.y;

        double lineLength = getLineLength(radius);     // Length of the line protruding the center square

        final Shape[] s = new Shape[5];
        // Center square
        s[0] = new Rectangle2D.Double(x - radius, y - radius, 2 * radius, 2 * radius);
        // Line North
        s[1] = new Line2D.Double(x, y + radius, x, y + radius + lineLength);
        // Line East
        s[2] = new Line2D.Double(x + radius, y, x + radius + lineLength, y);
        // Line South
        s[3] = new Line2D.Double(x, y - radius, x, y - radius - lineLength);
        // Line West
        s[4] = new Line2D.Double(x - radius, y, x - radius - lineLength, y);

        return s;
    }

    /**
     * Returns the empty shape in the side view, with an additional square encompassing the shape that can be used
     * for selecting the object.
     * @param radius radius of the center square
     */
    public static Shape[] getShapesSideWithSelectionSquare(final Transformation transformation, final double radius) {
        final Coordinate instanceAbsoluteLocation = transformation.transform(Coordinate.ZERO);
        double x = instanceAbsoluteLocation.x;
        double y = instanceAbsoluteLocation.y;

        double lineLength = getLineLength(radius);     // Length of the line protruding the center square

        Shape[] shapes = getShapesSide(transformation, radius);

        // Invisible shape for selecting the component (= a square encompassing the component)
        Shape selectionShape = new Rectangle2D.Double(x - radius - lineLength, y - radius - lineLength,
                lineLength * 2 + radius * 2, lineLength * 2 + radius * 2);

        Shape[] finalShapes = new Shape[shapes.length + 1];
        System.arraycopy(shapes, 0, finalShapes, 0, shapes.length);
        finalShapes[finalShapes.length - 1] = selectionShape;

        return finalShapes;
    }

    /**
     * Returns the empty shape in the side view.
     * @param radius radius of the center square
     */
    public static Shape[] getShapesBack(final Transformation transformation, final double radius) {
        final Coordinate instanceAbsoluteLocation = transformation.transform(Coordinate.ZERO);
        double z = instanceAbsoluteLocation.z;
        double y = instanceAbsoluteLocation.y;

        double lineLength = getLineLength(radius);     // Length of the line protruding the center square

        final Shape[] s = new Shape[5];
        // Center square
        s[0] = new Rectangle2D.Double(z - radius, y - radius, 2 * radius, 2 * radius);
        // Line North
        s[1] = new Line2D.Double(z, y + radius, z, y + radius + lineLength);
        // Line East
        s[2] = new Line2D.Double(z + radius, y, z + radius + lineLength, y);
        // Line South
        s[3] = new Line2D.Double(z, y - radius, z, y - radius - lineLength);
        // Line West
        s[4] = new Line2D.Double(z - radius, y, z - radius - lineLength, y);

        return s;
    }

    /**
     * Returns the empty shape in the back view, with an additional square encompassing the shape that can be used
     * for selecting the object.
     * @param radius radius of the center square
     */
    public static Shape[] getShapesBackWithSelectionSquare(final Transformation transformation, final double radius) {
        final Coordinate instanceAbsoluteLocation = transformation.transform(Coordinate.ZERO);
        double z = instanceAbsoluteLocation.z;
        double y = instanceAbsoluteLocation.y;

        double lineLength = getLineLength(radius);     // Length of the line protruding the center square

        Shape[] shapes = getShapesBack(transformation, radius);

        // Invisible shape for selecting the component (= a square encompassing the component)
        Shape selectionShape = new Rectangle2D.Double(z - radius - lineLength, y - radius - lineLength,
                lineLength * 2 + radius * 2, lineLength * 2 + radius * 2);

        Shape[] finalShapes = new Shape[shapes.length + 1];
        System.arraycopy(shapes, 0, finalShapes, 0, shapes.length);
        finalShapes[finalShapes.length - 1] = selectionShape;

        return finalShapes;
    }

    private static double getLineLength(double radius) {
        return radius * 3;
    }
}
