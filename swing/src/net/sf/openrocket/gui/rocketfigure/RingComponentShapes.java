package net.sf.openrocket.gui.rocketfigure;


import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;


public class RingComponentShapes extends RocketComponentShapes {

	public static Shape[] getShapesSide(net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation) {
		net.sf.openrocket.rocketcomponent.RingComponent tube = (net.sf.openrocket.rocketcomponent.RingComponent)component;
		Shape[] s;
		
		double length = tube.getLength();
		double or = tube.getOuterRadius();
		double ir = tube.getInnerRadius();
		

		Coordinate[] start = transformation.transform(tube.toAbsolute(new Coordinate(0,0,0)));

		if ((or-ir >= 0.0012) && (ir > 0)) {
			// Draw outer and inner
			s = new Shape[start.length*2];
			for (int i=0; i < start.length; i++) {
				s[2*i] = new Rectangle2D.Double(start[i].x*S,(start[i].y-or)*S,
						length*S,2*or*S);
				s[2*i+1] = new Rectangle2D.Double(start[i].x*S,(start[i].y-ir)*S,
						length*S,2*ir*S);
			}
		} else {
			// Draw only outer
			s = new Shape[start.length];
			for (int i=0; i < start.length; i++) {
				s[i] = new Rectangle2D.Double(start[i].x*S,(start[i].y-or)*S,
						length*S,2*or*S);
			}
		}
		return s;
	}
	

	public static Shape[] getShapesBack(net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation) {
		net.sf.openrocket.rocketcomponent.RingComponent tube = (net.sf.openrocket.rocketcomponent.RingComponent)component;
		Shape[] s;
		
		double or = tube.getOuterRadius();
		double ir = tube.getInnerRadius();
		

		Coordinate[] start = transformation.transform(tube.toAbsolute(new Coordinate(0,0,0)));

		if ((ir < or) && (ir > 0)) {
			// Draw inner and outer
			s = new Shape[start.length*2];
			for (int i=0; i < start.length; i++) {
				s[2*i]   = new Ellipse2D.Double((start[i].z-or)*S, (start[i].y-or)*S,
						2*or*S, 2*or*S);
				s[2*i+1] = new Ellipse2D.Double((start[i].z-ir)*S, (start[i].y-ir)*S,
						2*ir*S, 2*ir*S);
			}
		} else {
			// Draw only outer
			s = new Shape[start.length];
			for (int i=0; i < start.length; i++) {
				s[i] = new Ellipse2D.Double((start[i].z-or)*S,(start[i].y-or)*S,2*or*S,2*or*S);
			}
		}
		return s;
	}
	
}
