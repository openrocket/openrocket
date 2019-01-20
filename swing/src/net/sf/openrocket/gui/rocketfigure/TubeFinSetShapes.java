package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;


public class TubeFinSetShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {

		TubeFinSet finset = (net.sf.openrocket.rocketcomponent.TubeFinSet)component;

		int fins = finset.getFinCount();
		double length = finset.getLength();
		double outerRadius = finset.getOuterRadius();
		double bodyRadius = finset.getBodyRadius();
		// old version - Oct, 19 2015
		//Coordinate[] instanceOffsets = new Coordinate[]{ transformation.transform( componentAbsoluteLocation )};
		//instanceOffsets = component.shiftCoordinates(instanceOffsets);
		
		// new version
		Coordinate[] start = transformation.transform( component.getLocations());

		Transformation baseRotation = finset.getBaseRotationTransformation();
		Transformation finRotation = finset.getFinRotationTransformation();

		// Translate & rotate the coordinates
		for (int i=0; i<start.length; i++) {
			start[i] = baseRotation.transform(transformation.transform(start[i].add(0,bodyRadius+outerRadius,0)));
		}

		//start = baseRotation.transform(start);
		
		Shape[] s = new Shape[fins];
		for (int i=0; i<fins; i++) {
			s[i] = new Rectangle2D.Double(start[0].x,(start[0].y-outerRadius),length,2*outerRadius);
			start = finRotation.transform(start);
		}
		return RocketComponentShape.toArray(s, component);
	}
	

	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {
			
		TubeFinSet finset = (net.sf.openrocket.rocketcomponent.TubeFinSet)component;
		
		int fins = finset.getFinCount();
		double outerradius = finset.getOuterRadius();
		double bodyradius = finset.getBodyRadius();
		
		// old version - Oct, 19 2015
		//Coordinate[] instanceOffsets = new Coordinate[]{ transformation.transform( componentAbsoluteLocation )};
		//instanceOffsets = component.shiftCoordinates(instanceOffsets);
		
		// new version
		Coordinate[] start = transformation.transform( component.getLocations());

		Transformation baseRotation = finset.getBaseRotationTransformation();
		Transformation finRotation = finset.getFinRotationTransformation();
		
		// Translate & rotate the coordinates
		for (int i=0; i<start.length; i++) {
			start[i] = baseRotation.transform(transformation.transform(start[i].add(0,bodyradius+outerradius,0)));
		}

		Shape[] s = new Shape[fins];
		for (int i=0; i < fins; i++) {
			s[i] = new Ellipse2D.Double((start[0].z-outerradius),(start[0].y-outerradius),2*outerradius,2*outerradius);
			start = finRotation.transform(start);
		}
		return RocketComponentShape.toArray(s, component);
	}
	
	
}
