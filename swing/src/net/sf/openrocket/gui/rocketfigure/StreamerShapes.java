package net.sf.openrocket.gui.rocketfigure;

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

		net.sf.openrocket.rocketcomponent.MassObject massObj = (net.sf.openrocket.rocketcomponent.MassObject)component;
		
		double length = massObj.getLength();
		double radius = massObj.getRadius();
		double arc = Math.min(length, 2*radius) * 0.7;
		
		Shape[] s = new Shape[1];
		Coordinate frontCenter = transformation.transform(Coordinate.ZERO);
		s[0] = new RoundRectangle2D.Double((frontCenter.x),(frontCenter.y-radius),
					length,2*radius,arc,arc);
					
//		Coordinate[] start = transformation.transform(tube.toAbsolute(instanceOffset));
//		Shape[] s = new Shape[start.length];
//		for (int i=0; i < start.length; i++) {
//			s[i] = new RoundRectangle2D.Double(start[i].x,(start[i].y-radius),
//					length,2*radius,arc,arc);
//		}
		return RocketComponentShape.toArray(addSymbol(s), component);
	}
	

	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {

		net.sf.openrocket.rocketcomponent.MassObject tube = (net.sf.openrocket.rocketcomponent.MassObject)component;
		
		double or = tube.getRadius();
		Shape[] s = new Shape[1];
		Coordinate center = transformation.transform(Coordinate.ZERO);
		
		s[0] = new Ellipse2D.Double((center.z-or),(center.y-or),2*or,2*or);
		 
//		Coordinate[] start = transformation.transform(tube.toAbsolute(instanceOffset));
//
//		Shape[] s = new Shape[start.length];
//		for (int i=0; i < start.length; i++) {
//			s[i] = new Ellipse2D.Double((start[i].z-or),(start[i].y-or),2*or,2*or);
//		}
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
