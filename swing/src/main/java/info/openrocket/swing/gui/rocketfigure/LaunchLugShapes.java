package info.openrocket.swing.gui.rocketfigure;

import java.awt.Shape;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.Transformation;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class LaunchLugShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {

	    LaunchLug lug = (LaunchLug)component;
	 	double length = lug.getLength();
		double radius = lug.getOuterRadius();
		
		Shape[] s = new Shape[]{
		        TubeShapes.getShapesSide( transformation, length, radius )
        };

        return RocketComponentShape.toArray(s, component);
	}
	

	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {
	
	    LaunchLug lug = (LaunchLug)component;
	    double radius = lug.getOuterRadius();
        
        Shape[] s = new Shape[]{TubeShapes.getShapesBack( transformation, radius)};
        
		return RocketComponentShape.toArray(s, component);
	}
}
