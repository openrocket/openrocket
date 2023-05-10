package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class ParachuteShapes extends RocketComponentShape {

	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {
		final MassObject massObj = (MassObject)component;
		
		final double length = massObj.getLength();
		final double radius = massObj.getRadius(); // radius of the object, itself
		// magic number, but it's only cosmetic -- it just has to look pretty
		final double arc = Math.min(length, 2*radius) * 0.7;
		final double radialDistance = massObj.getRadialPosition();
		final double radialAngleRadians = massObj.getRadialDirection();
		
		final Coordinate localPosition = new Coordinate(0,
														radialDistance * Math.cos(radialAngleRadians),
														radialDistance * Math.sin(radialAngleRadians));
		final Coordinate renderPosition = transformation.transform(localPosition);
		
		Shape[] s = {new RoundRectangle2D.Double(renderPosition.x, renderPosition.y - radius, length, 2*radius, arc, arc)};
		
		return RocketComponentShape.toArray( addSymbol(s), component);
	}
	

	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {
		final MassObject massObj = (MassObject)component;
		
		final double radius = massObj.getRadius(); // radius of the object, itself
		final double diameter = 2*radius;
		final double radialDistance = massObj.getRadialPosition();
		final double radialAngleRadians = massObj.getRadialDirection();
		
		final Coordinate localPosition = new Coordinate(0,
														radialDistance * Math.cos(radialAngleRadians),
														radialDistance * Math.sin(radialAngleRadians));
		final Coordinate renderPosition = transformation.transform(localPosition);
		
		final Shape[] s = {new Ellipse2D.Double(renderPosition.z - radius, renderPosition.y - radius, diameter, diameter)};
		
		return RocketComponentShape.toArray( s, component);
	}
	
	private static Shape[] addSymbol(Shape[] baseShape){
		int offset=baseShape.length;
		Shape[] newShape = new Shape[baseShape.length+2];
		System.arraycopy(baseShape, 0, newShape, 0, baseShape.length);
			
		Rectangle2D bounds = baseShape[0].getBounds2D();

		Double chuteDiameter = bounds.getHeight()/2;
		if(chuteDiameter>0.75*bounds.getWidth())
			chuteDiameter=0.75*bounds.getWidth();
		
		newShape[offset]=new Arc2D.Double(bounds.getCenterX()-chuteDiameter/2, bounds.getCenterY()-chuteDiameter/4,
											chuteDiameter,chuteDiameter,180.0,180.0,Arc2D.PIE);
		Path2D.Double shrouds = new Path2D.Double();
		shrouds.moveTo(bounds.getCenterX()-chuteDiameter/2, bounds.getCenterY()+chuteDiameter/4);
		shrouds.lineTo(bounds.getCenterX(), bounds.getCenterY()-3*chuteDiameter/4);
		shrouds.lineTo(bounds.getCenterX()+chuteDiameter/2, bounds.getCenterY()+chuteDiameter/4);

		shrouds.moveTo(bounds.getCenterX()-chuteDiameter/4, bounds.getCenterY()+chuteDiameter/4);
		shrouds.lineTo(bounds.getCenterX(), bounds.getCenterY()-3*chuteDiameter/4);
		shrouds.lineTo(bounds.getCenterX()+chuteDiameter/4, bounds.getCenterY()+chuteDiameter/4);

		shrouds.moveTo(bounds.getCenterX(), bounds.getCenterY()+chuteDiameter/4);
		shrouds.lineTo(bounds.getCenterX(), bounds.getCenterY()-3*chuteDiameter/4);

		newShape[offset+1]=shrouds;
		return newShape;
	}
}
