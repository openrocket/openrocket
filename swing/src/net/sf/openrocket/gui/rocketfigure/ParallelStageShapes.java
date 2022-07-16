package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;

public class ParallelStageShapes extends RocketComponentShape {
    public static final Color boosterColor = new Color(198,163,184);

    public static RocketComponentShape[] getShapesSide(final RocketComponent component, final Transformation transformation) {
        ParallelStage booster = (ParallelStage)component;
        double radius = getDisplayRadius(booster);

        Shape[] s = EmptyShapes.getShapesSideWithSelectionSquare(transformation, radius);
        RocketComponentShape[] shapes = RocketComponentShape.toArray(s, component);

        // Set the color of the shapes
        for (int i = 0; i < shapes.length - 1; i++) {
            shapes[i].setColor(boosterColor);
        }
        shapes[shapes.length - 1].setColor(Color.INVISIBLE);

        return shapes;
    }

    public static RocketComponentShape[] getShapesBack(final RocketComponent component, final Transformation transformation) {
        ParallelStage booster = (ParallelStage)component;
        double radius = getDisplayRadius(booster);

        Shape[] s = EmptyShapes.getShapesBackWithSelectionSquare(transformation, radius);
        RocketComponentShape[] shapes = RocketComponentShape.toArray(s, component);

        // Set the color of the shapes
        for (int i = 0; i < shapes.length - 1; i++) {
            shapes[i].setColor(boosterColor);
        }
        shapes[shapes.length - 1].setColor(Color.INVISIBLE);

        return shapes;
    }

    private static double getDisplayRadius(ParallelStage booster) {
        return booster.getRocket().getBoundingRadius() * 0.03;
    }
}
