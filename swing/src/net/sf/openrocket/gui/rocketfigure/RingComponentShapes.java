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
			Coordinate instanceAbsoluteLocation) {

		net.sf.openrocket.rocketcomponent.RingComponent tube = (net.sf.openrocket.rocketcomponent.RingComponent)component;
		Shape[] s;
		
		double length = tube.getLength();
		double outerRadius = tube.getOuterRadius();
		double innerRadius = tube.getInnerRadius();
		
		if ((outerRadius-innerRadius >= 0.0012) && (innerRadius > 0)) {
			// Draw outer and inner
			s = new Shape[] {
			        TubeShapes.getShapesSide(transformation, instanceAbsoluteLocation, length, outerRadius),
			        TubeShapes.getShapesSide(transformation, instanceAbsoluteLocation, length, innerRadius)
			};			        
		} else {
			// Draw only outer
			s = new Shape[] {
			        TubeShapes.getShapesSide(transformation, instanceAbsoluteLocation, length, outerRadius)
		    };                  
		}
		return RocketComponentShape.toArray( s, component);
	}
	

	public static RocketComponentShape[] getShapesBack(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate instanceAbsoluteLocation) {
		net.sf.openrocket.rocketcomponent.RingComponent tube = (net.sf.openrocket.rocketcomponent.RingComponent)component;
		Shape[] s;
		
		double outerRadius = tube.getOuterRadius();
        double innerRadius = tube.getInnerRadius();
        
        if ((outerRadius-innerRadius >= 0.0012) && (innerRadius > 0)) {
            s = new Shape[] {
                    TubeShapes.getShapesBack(transformation, instanceAbsoluteLocation, outerRadius),
                    TubeShapes.getShapesBack(transformation, instanceAbsoluteLocation, innerRadius)
            };
        }else {
            s = new Shape[] {
                    TubeShapes.getShapesBack(transformation, instanceAbsoluteLocation, outerRadius)
            };
        }
		
		return RocketComponentShape.toArray( s, component);
	}
	
}
