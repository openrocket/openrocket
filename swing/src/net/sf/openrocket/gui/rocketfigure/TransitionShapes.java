package net.sf.openrocket.gui.rocketfigure;

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
			Coordinate instanceOffset) {
        return getShapesSide(component, transformation, instanceOffset, S);
    }

    public static RocketComponentShape[] getShapesSide(
    		net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate instanceOffset,
			final double scaleFactor) {
		net.sf.openrocket.rocketcomponent.Transition transition = (net.sf.openrocket.rocketcomponent.Transition)component;

		RocketComponentShape[] mainShapes;
		
		// Simpler shape for conical transition, others use the method from SymmetricComponent
		if (transition.getType() == Transition.Shape.CONICAL) {
			double length = transition.getLength();
			double r1 = transition.getForeRadius();
			double r2 = transition.getAftRadius();
			Coordinate start = transformation.transform(transition.
					toAbsolute(instanceOffset)[0]);
			
			Path2D.Float path = new Path2D.Float();
			path.moveTo(start.x* scaleFactor, r1* scaleFactor);
			path.lineTo((start.x+length)* scaleFactor, r2* scaleFactor);
			path.lineTo((start.x+length)* scaleFactor, -r2* scaleFactor);
			path.lineTo(start.x* scaleFactor, -r1* scaleFactor);
			path.closePath();
			
			mainShapes = new RocketComponentShape[] { new RocketComponentShape( path, component) };
		} else {
			mainShapes = SymmetricComponentShapes.getShapesSide(component, transformation, instanceOffset, scaleFactor);
		}
		
		Rectangle2D.Double shoulder1=null, shoulder2=null;
		int arrayLength = mainShapes.length;
		
		if (transition.getForeShoulderLength() > 0.0005) {
			Coordinate start = transformation.transform(transition.
					toAbsolute(Coordinate.NUL)[0]);
			double r = transition.getForeShoulderRadius();
			double l = transition.getForeShoulderLength();
			shoulder1 = new Rectangle2D.Double((start.x-l)* scaleFactor, -r* scaleFactor, l* scaleFactor, 2*r* scaleFactor);
			arrayLength++;
		}
		if (transition.getAftShoulderLength() > 0.0005) {
			Coordinate start = transformation.transform(transition.
					toAbsolute(new Coordinate(transition.getLength()))[0]);
			double r = transition.getAftShoulderRadius();
			double l = transition.getAftShoulderLength();
			shoulder2 = new Rectangle2D.Double(start.x* scaleFactor, -r* scaleFactor, l* scaleFactor, 2*r* scaleFactor);
			arrayLength++;
		}
		if (shoulder1==null && shoulder2==null)
			return mainShapes;
		
		Shape[] shapes = new Shape[arrayLength];
		int i;
		
		for (i=0; i < mainShapes.length; i++) {
			shapes[i] = mainShapes[i].shape;
		}
		if (shoulder1 != null) {
			shapes[i] = shoulder1;
			i++;
		}
		if (shoulder2 != null) {
			shapes[i] = shoulder2;
		}
		return RocketComponentShape.toArray( shapes, component);
	}
	

	public static RocketComponentShape[] getShapesBack(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate instanceOffset) {

		net.sf.openrocket.rocketcomponent.Transition transition = (net.sf.openrocket.rocketcomponent.Transition)component;
		
		double r1 = transition.getForeRadius();
		double r2 = transition.getAftRadius();

		Coordinate center = instanceOffset;
		// adjust center heree... somehow
		
		Shape[] s = new Shape[2];
		s[0] = new Ellipse2D.Double(-r1*S,-r1*S,2*r1*S,2*r1*S);
		s[1] = new Ellipse2D.Double(-r2*S,-r2*S,2*r2*S,2*r2*S);
		return RocketComponentShape.toArray(s, component);
	}
	
	
}
