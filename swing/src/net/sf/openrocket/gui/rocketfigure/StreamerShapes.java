package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class StreamerShapes extends RocketComponentShape {

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
		
		return RocketComponentShape.toArray(addSymbol(s), component);
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
		
		return RocketComponentShape.toArray(s, component);
	}
	
	private static Shape[] addSymbol(Shape[] baseShape){
		int offset=baseShape.length;
		Shape[] newShape = new Shape[baseShape.length+1];
		System.arraycopy(baseShape, 0, newShape, 0, baseShape.length);
			
		Rectangle2D bounds = baseShape[0].getBounds2D();

		Double left=bounds.getX()+bounds.getWidth()/4;
		Double streamerWidth=bounds.getWidth()/2;
		Double streamerHeight=bounds.getHeight()/2;
		Double top=bounds.getCenterY()+streamerHeight/2;
		Double bottom=bounds.getCenterY()-streamerHeight/2;
		Double flutterHeight=bounds.getHeight()/16;
		Double flutterWidth=streamerWidth/4;
		
		Path2D.Double streamer= new Path2D.Double();
		streamer.moveTo(left, bottom); //bottom left
		streamer.lineTo(left, top); //upper left
		for(int i=0; i<4; i++){
			streamer.curveTo(left+(4*i+1)*flutterWidth/4, top+flutterHeight, left+(4*i+1)*flutterWidth/4, top+flutterHeight, left+(4*i+2)*flutterWidth/4, top);
			streamer.curveTo(left+(4*i+3)*flutterWidth/4, top-flutterHeight, left+(4*i+3)*flutterWidth/4, top-flutterHeight, left+(4*i+4)*flutterWidth/4, top);
		}
		streamer.lineTo(left+streamerWidth, bottom);
		streamer.moveTo(left, bottom); //bottom left
		for(int i=0; i<4; i++){
			streamer.curveTo(left+(4*i+1)*flutterWidth/4, bottom+flutterHeight, left+(4*i+1)*flutterWidth/4, bottom+flutterHeight, left+(4*i+2)*flutterWidth/4, bottom);
			streamer.curveTo(left+(4*i+3)*flutterWidth/4, bottom-flutterHeight, left+(4*i+3)*flutterWidth/4, bottom-flutterHeight, left+(4*i+4)*flutterWidth/4, bottom);
		}
		
		newShape[offset]=streamer;
		return newShape;
	}
}
