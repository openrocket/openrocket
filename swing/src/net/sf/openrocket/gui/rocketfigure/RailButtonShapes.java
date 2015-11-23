package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;


public class RailButtonShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate componentAbsoluteLocation) {
	
		net.sf.openrocket.rocketcomponent.RailButton btn = (net.sf.openrocket.rocketcomponent.RailButton)component;

		
		final double outerDiameter = btn.getOuterDiameter();
		final double outerRadius = outerDiameter/2;
		final double baseHeight = btn.getStandoff();
		final double flangeHeight = btn.getFlangeHeight();
		final double innerDiameter = btn.getInnerDiameter();
		final double innerRadius = innerDiameter/2;
		final double innerHeight = btn.getTotalHeight();
		final double rotation_rad = btn.getAngularOffset();
		Coordinate[] inst = transformation.transform( btn.getLocations());
		
		//if( MathUtil.EPSILON < Math.abs(rotation)){
			Shape[] s = new Shape[inst.length*3];
			for (int i=0; i < inst.length; i+=3 ) {
				// needs MUCH debugging. :P
//				System.err.println("?? Drawing RailButton shapes at: "+inst[i].toString());
//				System.err.println("    heights = "+baseHeight+", "+innerHeight+", "+flangeHeight);
//				System.err.println("    dias = "+outerDiameter+", "+innerDiameter);
				
				{// base 
					s[i] = new Rectangle2D.Double(
							(inst[i].x-outerRadius)*S,(inst[i].y)*S,
							(outerDiameter)*S, baseHeight*S);
				}
				{// inner
					s[i+1] = new Rectangle2D.Double(
							(inst[i].x-innerRadius)*S,(inst[i].y+baseHeight)*S,
							(innerDiameter)*S, innerHeight*S);
				}
				{ // outer flange
					s[i+2] = new Rectangle2D.Double(
							(inst[i].x-outerRadius)*S,(inst[i].y+baseHeight+innerHeight)*S,
							(outerDiameter)*S, flangeHeight*S);
				}
				
			}
//		}else{
//			Shape[] s = new Shape[inst.length];
//			for (int i=0; i < inst.length; i++) {
//				s[i] = new Rectangle2D.Double(inst[i].x*S,(inst[i].y-radius)*S,
//						length*S,2*radius*S);
//			}
//		}
		
		return RocketComponentShape.toArray(s, component);
	}
	

	public static RocketComponentShape[] getShapesBack(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate instanceOffset) {
	
		net.sf.openrocket.rocketcomponent.RailButton lug = (net.sf.openrocket.rocketcomponent.RailButton)component;
//		
//		double or = lug.getOuterRadius();
//		
//		Coordinate[] start = transformation.transform( lug.getLocations());
//
//		Shape[] s = new Shape[start.length];
//		for (int i=0; i < start.length; i++) {
//			s[i] = new Ellipse2D.Double((start[i].z-or)*S,(start[i].y-or)*S,2*or*S,2*or*S);
//		}
//		
		Shape[] s = new Shape[0];
		return RocketComponentShape.toArray(s, component);
	}
}
