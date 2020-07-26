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
	 * @return an array of RocketComponentShapes that are used to draw the TubeFinSet from the side.
	 */
	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {
		final TubeFinSet finSet = (TubeFinSet) component;
		final double outerRadius = finSet.getOuterRadius();
		final double length = finSet.getLength();
		final Coordinate location = transformation.transform(new Coordinate(0, outerRadius, 0));

		final Shape[] shapes = new Shape[] {
				new Rectangle2D.Double(location.x, (location.y-outerRadius), length, 2*outerRadius)
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
		final TubeFinSet finSet = (TubeFinSet) component;
		final double outerRadius = finSet.getOuterRadius();
		final Coordinate location = transformation.transform(new Coordinate(0, outerRadius, 0));

		final Shape[] shapes = new Shape[] {
				new Ellipse2D.Double((location.z - outerRadius), (location.y - outerRadius), (2 * outerRadius), (2 * outerRadius))
			};
		
		return RocketComponentShape.toArray(shapes, component);
	}
}
