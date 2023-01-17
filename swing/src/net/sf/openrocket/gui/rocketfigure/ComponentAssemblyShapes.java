package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;

public class ComponentAssemblyShapes extends RocketComponentShape {

    public static RocketComponentShape[] getShapesSide(final RocketComponent component, final Transformation transformation) {
        double radius = getDisplayRadius(component);

        Shape[] s = EmptyShapes.getShapesSideWithSelectionSquare(transformation, radius);
        RocketComponentShape[] shapes = RocketComponentShape.toArray(s, component);

        // Set the color of the shapes
        Color color = getColor(component);
        for (int i = 0; i < shapes.length - 1; i++) {
            shapes[i].setColor(color);
        }

        return shapes;
    }

    public static RocketComponentShape[] getShapesBack(final RocketComponent component, final Transformation transformation) {
        double radius = getDisplayRadius(component);

        Shape[] s = EmptyShapes.getShapesBackWithSelectionSquare(transformation, radius);
        RocketComponentShape[] shapes = RocketComponentShape.toArray(s, component);

        // Set the color of the shapes
        Color color = getColor(component);
        for (int i = 0; i < shapes.length - 1; i++) {
            shapes[i].setColor(color);
        }

        return shapes;
    }

    private static double getDisplayRadius(RocketComponent component) {
        return component.getRocket().getBoundingRadius() * 0.03;
    }

    private static Color getColor(RocketComponent component) {
        if (component instanceof PodSet) {
            return new Color(160,160,215);
        } else if (component instanceof ParallelStage) {
            return new Color(198,163,184);
        } else {
            return new Color(160, 160, 160);
        }
    }
}
