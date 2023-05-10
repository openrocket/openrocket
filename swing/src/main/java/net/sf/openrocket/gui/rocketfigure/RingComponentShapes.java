package net.sf.openrocket.gui.rocketfigure;


import java.awt.Shape;

import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Transformation;


public class RingComponentShapes extends RocketComponentShape {

	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {


		net.sf.openrocket.rocketcomponent.RingComponent tube = (net.sf.openrocket.rocketcomponent.RingComponent)component;
		Shape[] s;
		
		double length = tube.getLength();
		double outerRadius = tube.getOuterRadius();
		double innerRadius = tube.getInnerRadius();
		
		if ((outerRadius-innerRadius >= 0.0012) && (innerRadius > 0)) {
			// Draw outer and inner
			s = new Shape[] {
			        TubeShapes.getShapesSide(transformation, length, outerRadius),
			        TubeShapes.getShapesSide(transformation, length, innerRadius)
			};			        
		} else {
			// Draw only outer
			s = new Shape[] {
			        TubeShapes.getShapesSide(transformation, length, outerRadius)
		    };                  
		}
		return RocketComponentShape.toArray( s, component);
	}
	

	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {

		RingComponent tube = (net.sf.openrocket.rocketcomponent.RingComponent)component;
		Shape[] s;
		
		double outerRadius = tube.getOuterRadius();
        double innerRadius = tube.getInnerRadius();
        
        if ((outerRadius-innerRadius >= 0.0012) && (innerRadius > 0)) {
            s = new Shape[] {
                    TubeShapes.getShapesBack(transformation, outerRadius),
                    TubeShapes.getShapesBack(transformation, innerRadius)
            };
        }else {
            s = new Shape[] {
                    TubeShapes.getShapesBack(transformation, outerRadius)
            };
        }
		
		return RocketComponentShape.toArray( s, component);
	}
	
}
