package info.openrocket.swing.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Tube;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.Transformation;


public class TubeShapes extends RocketComponentShapes {
	public Class<? extends RocketComponent> getShapeClass() {
		return Tube.class;
	}
	

	public static Shape getShapesSide( final Transformation transformation, final double length, final double radius ){
		return getShapesSide(transformation, length, radius, 1.0d);
	}
	
	public static Shape getShapesSide( final Transformation transformation, final double length, final double radius, final double scaleFactor ){
	    
		final Coordinate instanceAbsoluteLocation = transformation.transform(Coordinate.ZERO);
		
	    return new Rectangle2D.Double((instanceAbsoluteLocation.x) * scaleFactor,    //x - the X coordinate of the upper-left corner of the newly constructed Rectangle2D
			(instanceAbsoluteLocation.y-radius) * scaleFactor, // y - the Y coordinate of the upper-left corner of the newly constructed Rectangle2D
			length * scaleFactor, // w - the width of the newly constructed Rectangle2D
			2*radius * scaleFactor); //  h - the height of the newly constructed Rectangle2D
	}
	
	public static Shape getShapesBack( final Transformation transformation, final double radius ) {
		
		final Coordinate instanceAbsoluteLocation = transformation.transform(Coordinate.ZERO);
		
		return new Ellipse2D.Double((instanceAbsoluteLocation.z-radius), (instanceAbsoluteLocation.y-radius), 2*radius, 2*radius);
	}
	
	
}
