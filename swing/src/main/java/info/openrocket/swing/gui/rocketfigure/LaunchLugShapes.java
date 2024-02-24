package info.openrocket.swing.gui.rocketfigure;

import java.awt.Shape;

import info.openrocket.core.util.Transformation;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class LaunchLugShapes extends RocketComponentShapes {
	@Override
	public Class<? extends RocketComponent> getShapeClass() {
		return LaunchLug.class;
	}

	@Override
	public RocketComponentShapes[] getShapesSide(final RocketComponent component, final Transformation transformation) {

	    LaunchLug lug = (LaunchLug)component;
	 	double length = lug.getLength();
		double radius = lug.getOuterRadius();
		
		Shape[] s = new Shape[]{
		        TubeShapes.getShapesSide( transformation, length, radius )
        };

        return RocketComponentShapes.toArray(s, component);
	}

	@Override
	public RocketComponentShapes[] getShapesBack(final RocketComponent component, final Transformation transformation) {
	
	    LaunchLug lug = (LaunchLug)component;
	    double radius = lug.getOuterRadius();
        
        Shape[] s = new Shape[]{TubeShapes.getShapesBack( transformation, radius)};
        
		return RocketComponentShapes.toArray(s, component);
	}
}
