package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;
import net.sf.openrocket.util.ORColor;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;

public class ComponentAssemblyShapes extends RocketComponentShape {

    public static RocketComponentShape[] getShapesSide(final RocketComponent component, final Transformation transformation) {
        // Ignore normal stages
        if (component instanceof AxialStage && !(component instanceof ParallelStage)) {
            return null;
        }

        ComponentAssembly assembly = (ComponentAssembly) component;

        // Update the marker location based on the axial method. The axial method changes the "reference point" of the component.
        Transformation correctedTransform = transformation;
        if (assembly.getAxialMethod() == AxialMethod.BOTTOM) {
            correctedTransform = transformation.applyTransformation(new Transformation(assembly.getLength(), 0, 0));
        } else if (assembly.getAxialMethod() == AxialMethod.MIDDLE) {
            correctedTransform = transformation.applyTransformation(new Transformation(assembly.getLength() / 2, 0, 0));
        }

        // Correct the radius to be at the "reference point" dictated by the component's radius offset.
        if (assembly.getRadiusMethod() == RadiusMethod.RELATIVE) {
            double boundingRadius = assembly.getBoundingRadius();
            correctedTransform = correctedTransform.applyTransformation(new Transformation(0, -boundingRadius, 0));
        }

        double markerRadius = getDisplayRadius(component);
        Shape[] s = EmptyShapes.getShapesSideWithSelectionSquare(correctedTransform, markerRadius);
        RocketComponentShape[] shapes = RocketComponentShape.toArray(s, component);

        shapes[shapes.length - 1].setColor(ORColor.INVISIBLE);

        return shapes;
    }

    public static RocketComponentShape[] getShapesBack(final RocketComponent component, final Transformation transformation) {
        // Ignore normal stages
        if (component instanceof AxialStage && !(component instanceof ParallelStage)) {
            return null;
        }

        ComponentAssembly assembly = (ComponentAssembly) component;

        // Correct the radius to be at the "reference point" dictated by the component's radius offset.
        Transformation correctedTransform = transformation;
        if (assembly.getRadiusMethod() == RadiusMethod.RELATIVE) {
            double boundingRadius = assembly.getBoundingRadius();
            correctedTransform = correctedTransform.applyTransformation(new Transformation(0, -boundingRadius, 0));
        }

        double markerRadius = getDisplayRadius(component);
        Shape[] s = EmptyShapes.getShapesBackWithSelectionSquare(correctedTransform, markerRadius);
        RocketComponentShape[] shapes = RocketComponentShape.toArray(s, component);

        shapes[shapes.length - 1].setColor(ORColor.INVISIBLE);

        return shapes;
    }

    /**
     * Returns the radius of the marker (i.e. the marker size), based on the rocket size.
     * @param component this component
     * @return the radius to draw the marker with
     */
    private static double getDisplayRadius(RocketComponent component) {
        return component.getRocket().getBoundingRadius() * 0.03;
    }
}
