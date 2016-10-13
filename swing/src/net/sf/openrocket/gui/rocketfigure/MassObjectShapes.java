package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;


public class MassObjectShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate instanceOffset) {
	net.sf.openrocket.rocketcomponent.MassObject tube = (net.sf.openrocket.rocketcomponent.MassObject)component;
		
		double length = tube.getLength();
		double radius = tube.getRadius();
		double arc = Math.min(length, 2*radius) * 0.7;
		Coordinate[] start = transformation.transform(tube.toAbsolute(instanceOffset));

		Shape[] s = new Shape[start.length];
		for (int i=0; i < start.length; i++) {
			s[i] = new RoundRectangle2D.Double(start[i].x*S,(start[i].y-radius)*S,
					length*S,2*radius*S,arc*S,arc*S);
		}
		
		return RocketComponentShape.toArray(s, component);
	}
	

	public static RocketComponentShape[] getShapesBack(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate instanceOffset) {

		net.sf.openrocket.rocketcomponent.MassObject tube = (net.sf.openrocket.rocketcomponent.MassObject)component;
		
		double or = tube.getRadius();
		
		Coordinate[] start = transformation.transform(tube.toAbsolute(instanceOffset));

		Shape[] s = new Shape[start.length];
		for (int i=0; i < start.length; i++) {
			s[i] = new Ellipse2D.Double((start[i].z-or)*S,(start[i].y-or)*S,2*or*S,2*or*S);
		}
		return RocketComponentShape.toArray(s, component);
	}

}
