package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;


public class RailButtonShapes extends RocketComponentShape {
	
	public static RocketComponentShape[] getShapesSide(
			RocketComponent component, 
			Transformation transformation,
			Coordinate instanceAbsoluteLocation) {
	
		RailButton btn = (RailButton)component;

		final double rotation_rad = btn.getAngleOffset();
		final double baseHeight = btn.getStandoff();
		final double innerHeight = btn.getInnerHeight();
		final double flangeHeight = btn.getFlangeHeight();
		final double outerDiameter = btn.getOuterDiameter();
		final double outerRadius = outerDiameter/2;
		final double innerDiameter = btn.getInnerDiameter();
		final double innerRadius = innerDiameter/2;
	
		final double sinr = Math.abs(Math.sin(rotation_rad));
		final double cosr = Math.cos(rotation_rad);
		final double baseHeightcos = baseHeight*cosr;
		final double innerHeightcos = innerHeight*cosr;
		final double flangeHeightcos = flangeHeight*cosr;

		
		Path2D.Double path = new Path2D.Double();
		{// central pillar
			final double drawWidth = outerDiameter;
			final double drawHeight = outerDiameter*sinr;
			final Point2D.Double center = new Point2D.Double( instanceAbsoluteLocation.x, instanceAbsoluteLocation.y );
			Point2D.Double lowerLeft = new Point2D.Double( center.x - outerRadius, center.y-outerRadius*sinr);
			path.append( new Ellipse2D.Double( lowerLeft.x*S, lowerLeft.y*S, drawWidth*S, drawHeight*S), false);
			
			path.append( new Line2D.Double( lowerLeft.x*S,  center.y*S, lowerLeft.x*S, (center.y+baseHeightcos)*S ), false);
			path.append( new Line2D.Double( (center.x+outerRadius)*S,  center.y*S, (center.x+outerRadius)*S, (center.y+baseHeightcos)*S ), false);
			
			path.append( new Ellipse2D.Double( lowerLeft.x*S, (lowerLeft.y+baseHeightcos)*S, drawWidth*S, drawHeight*S), false);
		}
		
		{// inner
			final double drawWidth = innerDiameter;
			final double drawHeight = innerDiameter*sinr;
			final Point2D.Double center = new Point2D.Double( instanceAbsoluteLocation.x, instanceAbsoluteLocation.y + baseHeightcos);
			final Point2D.Double lowerLeft = new Point2D.Double( center.x - innerRadius, center.y-innerRadius*sinr);
			path.append( new Ellipse2D.Double( lowerLeft.x*S, lowerLeft.y*S, drawWidth*S, drawHeight*S), false);
			
			path.append( new Line2D.Double( lowerLeft.x*S,  center.y*S, lowerLeft.x*S, (center.y+innerHeightcos)*S ), false);
			path.append( new Line2D.Double( (center.x+innerRadius)*S,  center.y*S, (center.x+innerRadius)*S, (center.y+innerHeightcos)*S ), false);
			
			path.append( new Ellipse2D.Double( lowerLeft.x*S, (lowerLeft.y+innerHeightcos)*S, drawWidth*S, drawHeight*S), false);
		}
		{// outer flange
			final double drawWidth = outerDiameter;
			final double drawHeight = outerDiameter*sinr;
			final Point2D.Double center = new Point2D.Double( instanceAbsoluteLocation.x, instanceAbsoluteLocation.y+baseHeightcos+innerHeightcos);
			final Point2D.Double lowerLeft = new Point2D.Double( center.x - outerRadius, center.y-outerRadius*sinr);
			path.append( new Ellipse2D.Double( lowerLeft.x*S, lowerLeft.y*S, drawWidth*S, drawHeight*S), false);
			
			path.append( new Line2D.Double( lowerLeft.x*S,  center.y*S, lowerLeft.x*S, (center.y+flangeHeightcos)*S ), false);
			path.append( new Line2D.Double( (center.x+outerRadius)*S,  center.y*S, (center.x+outerRadius)*S, (center.y+flangeHeightcos)*S ), false);
			
			path.append( new Ellipse2D.Double( lowerLeft.x*S, (lowerLeft.y+flangeHeightcos)*S, drawWidth*S, drawHeight*S), false);
		}
	
		return RocketComponentShape.toArray( new Shape[]{ path }, component );
	}
	

	public static RocketComponentShape[] getShapesBack(
			net.sf.openrocket.rocketcomponent.RocketComponent component, 
			Transformation transformation,
			Coordinate instanceOffset) {
	
		net.sf.openrocket.rocketcomponent.RailButton btn = (net.sf.openrocket.rocketcomponent.RailButton)component;

		final double rotation_rad = btn.getAngleOffset();
		final double sinr = Math.sin(rotation_rad);
		final double cosr = Math.cos(rotation_rad);
		final double baseHeight = btn.getStandoff();
		final double innerHeight = btn.getInnerHeight();
		final double flangeHeight = btn.getFlangeHeight();

		final double outerDiameter = btn.getOuterDiameter();
		final double outerRadius = outerDiameter/2;
		final double innerDiameter = btn.getInnerDiameter();
		final double innerRadius = innerDiameter/2;
		Coordinate[] inst = transformation.transform( btn.getLocations());
		
		Shape[] s = new Shape[inst.length];
		for (int i=0; i < inst.length; i++) {
			Path2D.Double compound = new Path2D.Double();
			s[i] = compound;
			// base
			compound.append( getRotatedRectangle( inst[i].z, inst[i].y, outerRadius, baseHeight, rotation_rad), false );
			
			{// inner
				final double delta_r = baseHeight;
				final double delta_y = delta_r*cosr;
				final double delta_z = delta_r*sinr;
				compound.append( getRotatedRectangle( inst[i].z+delta_z, inst[i].y+delta_y, innerRadius, innerHeight, rotation_rad ), false);
			}
			{// outer flange
				final double delta_r = baseHeight + innerHeight;
				final double delta_y = delta_r*cosr;
				final double delta_z = delta_r*sinr;
				compound.append( getRotatedRectangle( inst[i].z+delta_z, inst[i].y+delta_y, outerRadius, flangeHeight, rotation_rad ), false);
			}
		}

		return RocketComponentShape.toArray(s, component);
	}
	
	
	
	public static Shape getRotatedRectangle( final double x, final double y, final double radius, final double height, final double angle_rad ){
		Path2D.Double rect = new Path2D.Double();
		final double sinr = Math.sin(angle_rad);
		final double cosr = Math.cos(angle_rad);
		
		rect.moveTo( (x-radius*cosr)*S, (y+radius*sinr)*S);
		rect.lineTo( (x-radius*cosr+height*sinr)*S, (y+radius*sinr+height*cosr)*S);
		rect.lineTo( (x+radius*cosr+height*sinr)*S, (y-radius*sinr+height*cosr)*S);
		rect.lineTo( (x+radius*cosr)*S, (y-radius*sinr)*S);
		rect.closePath();
		// add points
		
		
		return rect;
	}
}
