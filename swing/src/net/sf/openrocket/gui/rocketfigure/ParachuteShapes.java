package net.sf.openrocket.gui.rocketfigure;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class ParachuteShapes extends RocketComponentShapes {

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
