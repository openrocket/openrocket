package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;


public class MassObjectShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {

	MassObject tube = (MassObject)component;
		
		double length = tube.getLength();
		double radius = tube.getRadius();
		double arc = Math.min(length, 2*radius) * 0.7;
		
		Coordinate start = transformation.transform(Coordinate.ZERO);
		
		Shape[] s = {new RoundRectangle2D.Double(start.x, (start.y-radius), length, 2*radius, arc, arc)};
				
		return RocketComponentShape.toArray(s, component);
	}
	

	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {

		MassObject tube = (MassObject)component;
		
		double or = tube.getRadius();
		
		final Coordinate start = transformation.transform(Coordinate.ZERO);

		Shape[] s = {new Ellipse2D.Double((start.z-or), (start.y-or), 2*or, 2*or)};
		
		return RocketComponentShape.toArray(s, component);
	}

}
