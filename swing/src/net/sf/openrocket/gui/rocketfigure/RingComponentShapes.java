package net.sf.openrocket.gui.rocketfigure;


import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;


public class RingComponentShapes extends RocketComponentShape {

	public static RocketComponentShape[] getShapesSide(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate componentAbsoluteLocation) {

		net.sf.openrocket.rocketcomponent.RingComponent tube = (net.sf.openrocket.rocketcomponent.RingComponent)component;
		Shape[] s;
		
		double length = tube.getLength();
		double or = tube.getOuterRadius();
		double ir = tube.getInnerRadius();
		
		// old version
		//Coordinate[] instanceOffsets = new Coordinate[]{ transformation.transform( componentAbsoluteLocation )};
		//instanceOffsets = component.shiftCoordinates(instanceOffsets);
		
		// new version
		Coordinate[] instanceOffsets = transformation.transform( component.getLocations());


		if ((or-ir >= 0.0012) && (ir > 0)) {
			// Draw outer and inner
			s = new Shape[instanceOffsets.length*2];
			for (int i=0; i < instanceOffsets.length; i++) {
				s[2*i] = new Rectangle2D.Double(instanceOffsets[i].x*S,(instanceOffsets[i].y-or)*S,
						length*S,2*or*S);
				s[2*i+1] = new Rectangle2D.Double(instanceOffsets[i].x*S,(instanceOffsets[i].y-ir)*S,
						length*S,2*ir*S);
			}
		} else {
			// Draw only outer
			s = new Shape[instanceOffsets.length];
			for (int i=0; i < instanceOffsets.length; i++) {
				s[i] = new Rectangle2D.Double(instanceOffsets[i].x*S,(instanceOffsets[i].y-or)*S,
						length*S,2*or*S);
			}
		}
		return RocketComponentShape.toArray( s, component);
	}
	

	public static RocketComponentShape[] getShapesBack(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate componentAbsoluteLocation) {
		net.sf.openrocket.rocketcomponent.RingComponent tube = (net.sf.openrocket.rocketcomponent.RingComponent)component;
		Shape[] s;
		
		double or = tube.getOuterRadius();
		double ir = tube.getInnerRadius();

		Coordinate[] instanceOffsets = new Coordinate[]{ transformation.transform( componentAbsoluteLocation )};
		
		// old version 
		//instanceOffsets = component.shiftCoordinates(instanceOffsets);
		
		// new version
		instanceOffsets = component.getLocations();

		if ((ir < or) && (ir > 0)) {
			// Draw inner and outer
			s = new Shape[instanceOffsets.length*2];
			for (int i=0; i < instanceOffsets.length; i++) {
				s[2*i]   = new Ellipse2D.Double((instanceOffsets[i].z-or)*S, (instanceOffsets[i].y-or)*S,
						2*or*S, 2*or*S);
				s[2*i+1] = new Ellipse2D.Double((instanceOffsets[i].z-ir)*S, (instanceOffsets[i].y-ir)*S,
						2*ir*S, 2*ir*S);
			}
		} else {
			// Draw only outer
			s = new Shape[instanceOffsets.length];
			for (int i=0; i < instanceOffsets.length; i++) {
				s[i] = new Ellipse2D.Double((instanceOffsets[i].z-or)*S,(instanceOffsets[i].y-or)*S,2*or*S,2*or*S);
			}
		}
		return RocketComponentShape.toArray( s, component);
	}
	
}
