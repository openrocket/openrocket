package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;

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
