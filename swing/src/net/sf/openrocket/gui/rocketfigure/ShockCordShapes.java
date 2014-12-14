package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class ShockCordShapes extends RocketComponentShapes {

	public static Shape[] getShapesSide(net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation) {
		net.sf.openrocket.rocketcomponent.MassObject tube = (net.sf.openrocket.rocketcomponent.MassObject)component;
		
		double length = tube.getLength();
		double radius = tube.getRadius();
		double arc = Math.min(length, 2*radius) * 0.7;
		Coordinate[] start = transformation.transform(tube.toAbsolute(new Coordinate(0,0,0)));

		Shape[] s = new Shape[start.length];
		for (int i=0; i < start.length; i++) {
			s[i] = new RoundRectangle2D.Double(start[i].x*S,(start[i].y-radius)*S,
					length*S,2*radius*S,arc*S,arc*S);
		}
		return addSymbol(s);
	}
	

	public static Shape[] getShapesBack(net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation) {
		net.sf.openrocket.rocketcomponent.MassObject tube = (net.sf.openrocket.rocketcomponent.MassObject)component;
		
		double or = tube.getRadius();
		
		Coordinate[] start = transformation.transform(tube.toAbsolute(new Coordinate(0,0,0)));

		Shape[] s = new Shape[start.length];
		for (int i=0; i < start.length; i++) {
			s[i] = new Ellipse2D.Double((start[i].z-or)*S,(start[i].y-or)*S,2*or*S,2*or*S);
		}
		return s;
	}
	
	private static Shape[] addSymbol(Shape[] baseShape){
		int offset=baseShape.length;
		Shape[] newShape = new Shape[baseShape.length+1];
		System.arraycopy(baseShape, 0, newShape, 0, baseShape.length);
			
		Rectangle2D bounds = baseShape[0].getBounds2D();

		Double left=bounds.getX()+bounds.getWidth()/4;
		Double cordWidth=bounds.getWidth()/2;
		Double top=bounds.getCenterY();
		Double flutterHeight=bounds.getHeight()/4;
		Double flutterWidth=cordWidth/4;
		
		Path2D.Double streamer= new Path2D.Double();
		streamer.moveTo(left, bounds.getCenterY()); 

		for(int i=0; i<4; i++){
			streamer.curveTo(left+(4*i+1)*flutterWidth/4, top+flutterHeight, left+(4*i+1)*flutterWidth/4, top+flutterHeight, left+(4*i+2)*flutterWidth/4, top);
			streamer.curveTo(left+(4*i+3)*flutterWidth/4, top-flutterHeight, left+(4*i+3)*flutterWidth/4, top-flutterHeight, left+(4*i+4)*flutterWidth/4, top);
		}
		
		newShape[offset]=streamer;
		return newShape;
	}
}
