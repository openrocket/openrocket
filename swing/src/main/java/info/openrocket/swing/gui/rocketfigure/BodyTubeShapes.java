package info.openrocket.swing.gui.rocketfigure;

import java.awt.Shape;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Transformation;

public class BodyTubeShapes extends RocketComponentShapes {
	@Override
	public Class<? extends RocketComponent> getShapeClass() {
		return BodyTube.class;
	}

	@Override
	public RocketComponentShapes[] getShapesSide(final RocketComponent component, final Transformation transformation) {
		BodyTube tube = (BodyTube)component;
		
		double length = tube.getLength();
		double radius = tube.getOuterRadius();
		
		Shape[] s = new Shape[1];
        s[0] = TubeShapes.getShapesSide( transformation, length, radius );

        return RocketComponentShapes.toArray(s, component);
	}

	@Override
	public RocketComponentShapes[] getShapesBack(final RocketComponent component, final Transformation transformation) {
	    BodyTube tube = (BodyTube)component;
	    
	    double radius = tube.getOuterRadius();

        Shape[] s = new Shape[1];
        s[0] = TubeShapes.getShapesBack( transformation, radius);
		
		return RocketComponentShapes.toArray(s, component);
	}
	
	
}
