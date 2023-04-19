package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;

import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Transformation;

public class BodyTubeShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {

	    
		BodyTube tube = (BodyTube)component;
		
		double length = tube.getLength();
		double radius = tube.getOuterRadius();
		
		Shape[] s = new Shape[1];
        s[0] = TubeShapes.getShapesSide( transformation, length, radius );

        return RocketComponentShape.toArray(s, component);
	}
	
	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {

	    BodyTube tube = (BodyTube)component;
	    
	    double radius = tube.getOuterRadius();

        Shape[] s = new Shape[1];
        s[0] = TubeShapes.getShapesBack( transformation, radius);
		
		return RocketComponentShape.toArray(s, component);
	}
	
	
}
