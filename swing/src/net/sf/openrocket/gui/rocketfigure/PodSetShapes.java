package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;

public class PodSetShapes extends RocketComponentShape {
    public static final Color podsetColor = new Color(160,160,215);

    public static RocketComponentShape[] getShapesSide(final RocketComponent component, final Transformation transformation) {
        PodSet podset = (PodSet)component;
        double radius = getDisplayRadius(podset);

        Shape[] s = EmptyShapes.getShapesSideWithSelectionSquare(transformation, radius);
        RocketComponentShape[] shapes = RocketComponentShape.toArray(s, component);

        // Set the color of the shapes
        for (int i = 0; i < shapes.length - 1; i++) {
            shapes[i].setColor(podsetColor);
        }
        shapes[shapes.length - 1].setColor(Color.INVISIBLE);

        return shapes;
    }

    public static RocketComponentShape[] getShapesBack(final RocketComponent component, final Transformation transformation) {
        PodSet podset = (PodSet)component;
        double radius = getDisplayRadius(podset);

        Shape[] s = EmptyShapes.getShapesBackWithSelectionSquare(transformation, radius);
        RocketComponentShape[] shapes = RocketComponentShape.toArray(s, component);

        // Set the color of the shapes
        for (int i = 0; i < shapes.length - 1; i++) {
            shapes[i].setColor(podsetColor);
        }
        shapes[shapes.length - 1].setColor(Color.INVISIBLE);

        return shapes;
    }

    private static double getDisplayRadius(PodSet podset) {
        return podset.getRocket().getBoundingRadius() * 0.03;
    }
}
