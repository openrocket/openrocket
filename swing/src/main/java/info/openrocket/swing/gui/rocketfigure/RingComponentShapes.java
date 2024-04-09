package info.openrocket.swing.gui.rocketfigure;


import java.awt.Shape;

import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Transformation;


public class RingComponentShapes extends RocketComponentShapes {
	@Override
	public Class<? extends RocketComponent> getShapeClass() {
		return RingComponent.class;
	}

	@Override
	public RocketComponentShapes[] getShapesSide(final RocketComponent component, final Transformation transformation) {


		RingComponent tube = (RingComponent) component;
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
		return RocketComponentShapes.toArray( s, component);
	}


	@Override
	public RocketComponentShapes[] getShapesBack(final RocketComponent component, final Transformation transformation) {
		RingComponent tube = (RingComponent) component;
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
		
		return RocketComponentShapes.toArray( s, component);
	}
	
}
