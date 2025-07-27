package info.openrocket.swing.gui.rocketfigure;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;


public class TransitionShapes extends RocketComponentShapes {
	@Override
	public Class<? extends RocketComponent> getShapeClass() {
		return Transition.class;
	}

	@Override
    public RocketComponentShapes[] getShapesSide(final RocketComponent component, final Transformation transformation) {
    	return getShapesSide(component, transformation, 1.0);
    }

    public static RocketComponentShapes[] getShapesSide(
    		 final RocketComponent component, 
    		 final Transformation transformation,
             final double scaleFactor) {
    

		Transition transition = (Transition)component;

        final Coordinate instanceAbsoluteLocation = transformation.transform(Coordinate.ZERO);
        
		RocketComponentShapes[] mainShapes;
		
		// Simpler shape for conical transition, others use the method from SymmetricComponent
		if (transition.getShapeType() == Transition.Shape.CONICAL) {
		    final Coordinate frontCenter = instanceAbsoluteLocation;
	        
			double length = transition.getLength();
			double r1 = transition.getForeRadius();
			double r2 = transition.getAftRadius();
					
			Path2D.Float path = new Path2D.Float();
			path.moveTo( (frontCenter.x) * scaleFactor, (frontCenter.y+ r1) * scaleFactor);
			path.lineTo( (frontCenter.x+length) * scaleFactor, (frontCenter.y+r2) * scaleFactor);
			path.lineTo( (frontCenter.x+length) * scaleFactor, (frontCenter.y-r2) * scaleFactor);
			path.lineTo( (frontCenter.x) * scaleFactor, (frontCenter.y-r1) * scaleFactor);
			path.closePath();
			
			mainShapes = new RocketComponentShapes[] { new RocketComponentShapes( path, component) };
		} else {
			mainShapes = SymmetricComponentShapes.getShapesSide(component, transformation, scaleFactor);
		}
		
		Shape foreShoulder=null, aftShoulder=null;
		int arrayLength = mainShapes.length;
		
		if (transition.getForeShoulderLength() > 0.0005) {
			final double shoulderLength = transition.getForeShoulderLength();
            final double shoulderRadius = transition.getForeShoulderRadius();
            final Transformation offsetTransform = Transformation.getTranslationTransform(-transition.getForeShoulderLength(), 0, 0);
            final Transformation foreShoulderTransform = transformation.applyTransformation(offsetTransform);
            
			foreShoulder = TubeShapes.getShapesSide( foreShoulderTransform, shoulderLength, shoulderRadius, scaleFactor);
			arrayLength++;
		}
		if (transition.getAftShoulderLength() > 0.0005) {
			final double shoulderLength = transition.getAftShoulderLength();
            final double shoulderRadius = transition.getAftShoulderRadius();
            final Transformation offsetTransform = Transformation.getTranslationTransform(transition.getLength(), 0, 0);
            final Transformation aftShoulderTransform = transformation.applyTransformation(offsetTransform);
            		
			aftShoulder = TubeShapes.getShapesSide(aftShoulderTransform, shoulderLength, shoulderRadius, scaleFactor);
			arrayLength++;
		}
		if (foreShoulder==null && aftShoulder==null)
			return mainShapes;
		
		Shape[] shapes = new Shape[arrayLength];
		int i;
		
		for (i=0; i < mainShapes.length; i++) {
			shapes[i] = mainShapes[i].shape;
		}
		if (foreShoulder != null) {
			shapes[i] = foreShoulder;
			i++;
		}
		if (aftShoulder != null) {
			shapes[i] = aftShoulder;
		}
		return RocketComponentShapes.toArray( shapes, component);
	}


	@Override
	public RocketComponentShapes[] getShapesBack(final RocketComponent component, final Transformation transformation) {

		Transition transition = (Transition) component;
		
		double r1 = transition.getForeRadius();
		double r2 = transition.getAftRadius();

		final Coordinate center = transformation.transform(Coordinate.ZERO);
		
		Shape[] s = new Shape[2];
		s[0] = new Ellipse2D.Double((center.z-r1),(center.y-r1),2*r1,2*r1);
		s[1] = new Ellipse2D.Double((center.z-r2),(center.y-r2),2*r2,2*r2);
		return RocketComponentShapes.toArray(s, component);
	}
	
	
}
