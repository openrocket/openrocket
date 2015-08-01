package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;


public class TubeFinSetShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate componentAbsoluteLocation) {

		net.sf.openrocket.rocketcomponent.TubeFinSet finset = (net.sf.openrocket.rocketcomponent.TubeFinSet)component;

		int fins = finset.getFinCount();
		double length = finset.getLength();
		double outerRadius = finset.getOuterRadius();
		double bodyRadius = finset.getBodyRadius();

		Coordinate[] start = new Coordinate[]{ transformation.transform( componentAbsoluteLocation )};
		start = component.shiftCoordinates( start);

		Transformation baseRotation = finset.getBaseRotationTransformation();
		Transformation finRotation = finset.getFinRotationTransformation();

		// Translate & rotate the coordinates
		for (int i=0; i<start.length; i++) {
			start[i] = baseRotation.transform(transformation.transform(start[i].add(0,bodyRadius+outerRadius,0)));
		}

		//start = baseRotation.transform(start);
		
		Shape[] s = new Shape[fins];
		for (int i=0; i<fins; i++) {
			s[i] = new Rectangle2D.Double(start[0].x*S,(start[0].y-outerRadius)*S,length*S,2*outerRadius*S);
			start = finRotation.transform(start);
		}
		return RocketComponentShape.toArray(s, component);
	}
	

	public static RocketComponentShape[] getShapesBack(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate componentAbsoluteLocation) {
			
		net.sf.openrocket.rocketcomponent.TubeFinSet finset = (net.sf.openrocket.rocketcomponent.TubeFinSet)component;
		
		int fins = finset.getFinCount();
		double outerradius = finset.getOuterRadius();
		double bodyradius = finset.getBodyRadius();
		
		Coordinate[] start = new Coordinate[]{ transformation.transform( componentAbsoluteLocation.sub( 0, 0, 0) )};
		start = component.shiftCoordinates( start);

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
		return RocketComponentShape.toArray(s, component);
	}
	
	
}
