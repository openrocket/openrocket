package info.openrocket.swing.gui.rocketfigure;

import java.awt.Shape;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Transformation;

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
