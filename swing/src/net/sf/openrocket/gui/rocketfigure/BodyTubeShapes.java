package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;


public class BodyTubeShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate componentAbsoluteLocation) {
		net.sf.openrocket.rocketcomponent.BodyTube tube = (net.sf.openrocket.rocketcomponent.BodyTube)component;

		double length = tube.getLength();
		double radius = tube.getOuterRadius();
		Coordinate[] instanceOffsets = new Coordinate[]{ transformation.transform( componentAbsoluteLocation )};
		instanceOffsets = component.shiftCoordinates(instanceOffsets);
		
		Shape[] s = new Shape[instanceOffsets.length];
		for (int i=0; i < instanceOffsets.length; i++) {
			s[i] = new Rectangle2D.Double((instanceOffsets[i].x-length/2)*S,    //x - the X coordinate of the upper-left corner of the newly constructed Rectangle2D
					(instanceOffsets[i].y-radius)*S, // y - the Y coordinate of the upper-left corner of the newly constructed Rectangle2D
					length*S, // w - the width of the newly constructed Rectangle2D
					2*radius*S); //  h - the height of the newly constructed Rectangle2D
		}
		
		return RocketComponentShape.toArray(s, component);
	}
	
	public static RocketComponentShape[] getShapesBack(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate componentAbsoluteLocation) {
		net.sf.openrocket.rocketcomponent.BodyTube tube = (net.sf.openrocket.rocketcomponent.BodyTube)component;
		
		double or = tube.getOuterRadius();
		
		Coordinate[] instanceOffsets = new Coordinate[]{ transformation.transform( componentAbsoluteLocation )};
		instanceOffsets = component.shiftCoordinates(instanceOffsets);
		
		Shape[] s = new Shape[instanceOffsets.length];
		for (int i=0; i < instanceOffsets.length; i++) {
			s[i] = new Ellipse2D.Double((instanceOffsets[i].z-or)*S,(instanceOffsets[i].y-or)*S,2*or*S,2*or*S);
		}
		
		return RocketComponentShape.toArray(s, component);
	}
	
	
}
