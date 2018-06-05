package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;


public class TransitionShapes extends RocketComponentShape {

	// TODO: LOW: Uses only first component of cluster (not currently clusterable).

    public static RocketComponentShape[] getShapesSide(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate instanceLocation) {
		return getShapesSide(component, transformation, instanceLocation, S);
    }

    public static RocketComponentShape[] getShapesSide(
                       RocketComponent component,
                       Transformation transformation,
                       Coordinate instanceAbsoluteLocation,
                       final double scaleFactor) {
        
		Transition transition = (Transition)component;

		RocketComponentShape[] mainShapes;
		
		// Simpler shape for conical transition, others use the method from SymmetricComponent
		if (transition.getType() == Transition.Shape.CONICAL) {
		    final Coordinate frontCenter = instanceAbsoluteLocation;
	        
			double length = transition.getLength();
			double r1 = transition.getForeRadius();
			double r2 = transition.getAftRadius();
					
			Path2D.Float path = new Path2D.Float();
			path.moveTo( (frontCenter.x)* scaleFactor, (frontCenter.y+ r1)* scaleFactor);
			path.lineTo( (frontCenter.x+length)* scaleFactor, (frontCenter.y+r2)* scaleFactor);
			path.lineTo( (frontCenter.x+length)* scaleFactor, (frontCenter.y-r2)* scaleFactor);
			path.lineTo( (frontCenter.x)* scaleFactor, (frontCenter.y-r1)* scaleFactor);
			path.closePath();
			
			mainShapes = new RocketComponentShape[] { new RocketComponentShape( path, component) };
		} else {
			mainShapes = SymmetricComponentShapes.getShapesSide(component, transformation, instanceAbsoluteLocation, scaleFactor);
		}
		
		Shape foreShoulder=null, aftShoulder=null;
		int arrayLength = mainShapes.length;
		
		if (transition.getForeShoulderLength() > 0.0005) {
			Coordinate foreTransitionShoulderCenter = instanceAbsoluteLocation.sub( transition.getForeShoulderLength(), 0, 0);
			final Coordinate frontCenter = foreTransitionShoulderCenter; //transformation.transform( foreTransitionShoulderCenter);
			final double length = transition.getForeShoulderLength();
			final double radius = transition.getForeShoulderRadius();
            
			foreShoulder = TubeShapes.getShapesSide( transformation, frontCenter, length, radius);
			arrayLength++;
		}
		if (transition.getAftShoulderLength() > 0.0005) {
			Coordinate aftTransitionShoulderCenter = instanceAbsoluteLocation.add( transition.getLength(), 0, 0);
			final Coordinate frontCenter = aftTransitionShoulderCenter; //transformation.transform( aftTransitionShoulderCenter );
            final double length = transition.getAftShoulderLength();
            final double radius = transition.getAftShoulderRadius();
			aftShoulder = TubeShapes.getShapesSide(transformation, frontCenter, length, radius);
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
		return RocketComponentShape.toArray( shapes, component);
	}
	

	public static RocketComponentShape[] getShapesBack(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate componentAbsoluteLocation) {

		net.sf.openrocket.rocketcomponent.Transition transition = (net.sf.openrocket.rocketcomponent.Transition)component;
		
		double r1 = transition.getForeRadius();
		double r2 = transition.getAftRadius();

		Coordinate center = componentAbsoluteLocation;
		
		Shape[] s = new Shape[2];
		s[0] = new Ellipse2D.Double((center.z-r1)*S,(center.y-r1)*S,2*r1*S,2*r1*S);
		s[1] = new Ellipse2D.Double((center.z-r2)*S,(center.y-r2)*S,2*r2*S,2*r2*S);
		return RocketComponentShape.toArray(s, component);
	}
	
	
}
