package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class LaunchLugShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide(
			RocketComponent component, 
			Transformation transformation,
			Coordinate instanceAbsoluteLocation) {
	
	    LaunchLug lug = (LaunchLug)component;
	 	double length = lug.getLength();
		double radius = lug.getOuterRadius();
		
		Shape[] s = new Shape[]{
		        TubeShapes.getShapesSide( transformation, instanceAbsoluteLocation, length, radius )
        };

        return RocketComponentShape.toArray(s, component);
	}
	

	public static RocketComponentShape[] getShapesBack(
			RocketComponent component, 
			Transformation transformation,
			Coordinate instanceAbsoluteLocation) {
	
	    LaunchLug lug = (LaunchLug)component;
	    double radius = lug.getOuterRadius();
        
        Shape[] s = new Shape[]{TubeShapes.getShapesBack( transformation, instanceAbsoluteLocation, radius)};
        
		return RocketComponentShape.toArray(s, component);
	}
}
