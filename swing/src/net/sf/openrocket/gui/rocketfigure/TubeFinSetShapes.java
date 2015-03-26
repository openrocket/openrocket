package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;


public class TubeFinSetShapes extends RocketComponentShapes {
	
	public static Shape[] getShapesSide(net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation) {
		net.sf.openrocket.rocketcomponent.TubeFinSet finset = (net.sf.openrocket.rocketcomponent.TubeFinSet)component;

		int fins = finset.getFinCount();
		double length = finset.getLength();
		double outerradius = finset.getOuterRadius();
		double bodyradius = finset.getBodyRadius();

		Coordinate[] start = finset.toAbsolute(new Coordinate(0,0,0));

		Transformation baseRotation = finset.getBaseRotationTransformation();
		Transformation finRotation = finset.getFinRotationTransformation();

		// Translate & rotate the coordinates
		for (int i=0; i<start.length; i++) {
			start[i] = baseRotation.transform(transformation.transform(start[i].add(0,bodyradius+outerradius,0)));
		}

		//start = baseRotation.transform(start);
		
		Shape[] s = new Shape[fins];
		for (int i=0; i<fins; i++) {
			s[i] = new Rectangle2D.Double(start[0].x*S,(start[0].y-outerradius)*S,length*S,2*outerradius*S);
			start = finRotation.transform(start);
		}
		return s;
	}
	

	public static Shape[] getShapesBack(net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation) {
		net.sf.openrocket.rocketcomponent.TubeFinSet finset = (net.sf.openrocket.rocketcomponent.TubeFinSet)component;
		
		int fins = finset.getFinCount();
		double outerradius = finset.getOuterRadius();
		double bodyradius = finset.getBodyRadius();
		
		Coordinate[] start = finset.toAbsolute(new Coordinate(0,0,0));

		Transformation baseRotation = finset.getBaseRotationTransformation();
		Transformation finRotation = finset.getFinRotationTransformation();
		
		// Translate & rotate the coordinates
		for (int i=0; i<start.length; i++) {
			start[i] = baseRotation.transform(transformation.transform(start[i].add(0,bodyradius+outerradius,0)));
		}

		Shape[] s = new Shape[fins];
		for (int i=0; i < fins; i++) {
			s[i] = new Ellipse2D.Double((start[0].z-outerradius)*S,(start[0].y-outerradius)*S,2*outerradius*S,2*outerradius*S);
			start = finRotation.transform(start);
		}
		return s;
	}
	
	
}
