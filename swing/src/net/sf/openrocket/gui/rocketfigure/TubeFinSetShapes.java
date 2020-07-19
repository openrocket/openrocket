package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;


/**
 * The TubeFinSetShapes is used for retrieving shapes of the TubeFins on a
 * Rocket from multiple view points. The returned shapes will be translated
 * and transformed to the correct locations for rendering the rocket in the
 * 2D view space.
 */
public class TubeFinSetShapes extends RocketComponentShape {
	
	/**
	 * Returns an array of RocketcomponentShapes that describe the shape of
	 * the TubeFinSet when viewed from the side of the rocket. TubeFins will
	 * appear as a Rectangle from the side view
	 * 
	 * @param component the TubeFinSet to get the shapes for
	 * @param transformation the transformation to apply to the shapes
	 * @return an array of RocketComponentShapes that are used to draw the
	 *         TubeFinSet from the side.
	 */
	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {

		TubeFinSet finSet = (TubeFinSet) component;

		final double outerRadius = finSet.getOuterRadius();
		final double length = finSet.getLength();
		Coordinate[] locations = transformLocations(finSet, transformation);

		Shape[] shapes = new Shape[] {
				new Rectangle2D.Double(locations[0].x, (locations[0].y-outerRadius), length, 2*outerRadius)
			};
		
		return RocketComponentShape.toArray(shapes, component);
	}

	/**
	 * Returns an array of RocketComponentShapes that describe the shape of
	 * the TubeFinSet when viewed from the rear or the rocket. TubeFins will
	 * appear as an Ellipse/Circle from the back view.
	 * 
	 * @param component the TubeFinSet to get the shapes for
	 * @param transformation the transformation to apply to the shapes
	 * @return an array of RocketComponentShapes that are used to draw the
	 *         TubeFinSet from the back
	 */
	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {
			
		TubeFinSet finSet = (TubeFinSet) component;
		
		final double outerRadius = finSet.getOuterRadius();
		Coordinate[] locations = transformLocations(finSet, transformation);

		Shape[] shapes = new Shape[] {
				new Ellipse2D.Double((locations[0].z - outerRadius), (locations[0].y - outerRadius), (2 * outerRadius), (2 * outerRadius))
			};
		
		return RocketComponentShape.toArray(shapes, component);
	}
	
	/**
	 * Translates and rotates the coordinates as follows:
	 * 
	 * 1. Ensure the coordinate accounts for the body and outer radius. This
	 *    adjusts the Y value of the coordinate to place it in the correct
	 *    position relative to the body tube.
	 * 
	 * 2. Perform a linear transformation of the coordinate using the supplied
	 *    transform. Using the linear transform ensures the coordinate is
	 *    rotated per the view, but avoids applying an offset translation
	 *    since that is already applied in the locations retrieved from the
	 *    TubeFinSet.
	 * 
	 * 3. Apply the base rotational transform described by the TubeFinSet
	 *    component itself.
	 *    
	 * @param finSet the TubeFinSet to apply the transformation to
	 * @param transformation the Transformation to apply to the TubeFinSet
	 */
	private static Coordinate[] transformLocations(final TubeFinSet finSet, final Transformation transformation) {
		final double outerRadius = finSet.getOuterRadius();
		final double bodyRadius = finSet.getBodyRadius();
		Coordinate[] locations = finSet.getInstanceLocations();
		
		for (int i=0; i < locations.length; i++) {
			Coordinate c = locations[i].setX(0.);
			c = c.sub(0, (bodyRadius - outerRadius), 0);
			c = transformation.transform(c);
			locations[i] = c;
		}
		
		return locations;
	}
	
}
