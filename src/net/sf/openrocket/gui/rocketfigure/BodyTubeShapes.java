package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;


public class BodyTubeShapes extends RocketComponentShapes {
	
	public static Shape[] getShapesSide(net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation) {
		net.sf.openrocket.rocketcomponent.BodyTube tube = (net.sf.openrocket.rocketcomponent.BodyTube)component;

		double length = tube.getLength();
		double radius = tube.getOuterRadius();
		Coordinate[] start = transformation.transform(tube.toAbsolute(new Coordinate(0,0,0)));

		Shape[] s = new Shape[start.length];
		for (int i=0; i < start.length; i++) {
			s[i] = new Rectangle2D.Double(start[i].x*S,(start[i].y-radius)*S,
					length*S,2*radius*S);
		}
		return s;
	}
	

	public static Shape[] getShapesBack(net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation) {
		net.sf.openrocket.rocketcomponent.BodyTube tube = (net.sf.openrocket.rocketcomponent.BodyTube)component;
		
		double or = tube.getOuterRadius();
		
		Coordinate[] start = transformation.transform(tube.toAbsolute(new Coordinate(0,0,0)));

		Shape[] s = new Shape[start.length];
		for (int i=0; i < start.length; i++) {
			s[i] = new Ellipse2D.Double((start[i].z-or)*S,(start[i].y-or)*S,2*or*S,2*or*S);
		}
		return s;
	}
	
	
}
