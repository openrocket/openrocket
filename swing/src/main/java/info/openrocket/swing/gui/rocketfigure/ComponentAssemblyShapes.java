package info.openrocket.swing.gui.rocketfigure;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.rocketcomponent.position.RadiusMethod;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.Transformation;

import java.awt.Shape;

public class ComponentAssemblyShapes extends RocketComponentShapes {
    @Override
    public Class<? extends RocketComponent> getShapeClass() {
        return ComponentAssembly.class;
    }

    @Override
    public RocketComponentShapes[] getShapesSide(final RocketComponent component, final Transformation transformation) {
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
        RocketComponentShapes[] shapes = RocketComponentShapes.toArray(s, component);

        shapes[shapes.length - 1].setColor(ORColor.INVISIBLE);

        return shapes;
    }

    @Override
    public RocketComponentShapes[] getShapesBack(final RocketComponent component, final Transformation transformation) {
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
        RocketComponentShapes[] shapes = RocketComponentShapes.toArray(s, component);

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
