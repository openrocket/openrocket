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
	
	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {

		RailButton btn = (RailButton)component;

		final Coordinate instanceAbsoluteLocation = transformation.transform(Coordinate.ZERO);
		final Coordinate unitOrientation = transformation.transform(new Coordinate(0,1,0));

		final double view_rotation_rad = -Math.atan2(unitOrientation.y, unitOrientation.z) + Math.PI/2;
		final double angle_offset_rad = btn.getAngleOffset();
		final double baseHeight = btn.getStandoff();
		final double innerHeight = btn.getInnerHeight();
		final double flangeHeight = btn.getFlangeHeight();
		final double outerDiameter = btn.getOuterDiameter();
		final double outerRadius = outerDiameter/2;
		final double innerDiameter = btn.getInnerDiameter();
		final double innerRadius = innerDiameter/2;
	
		final double sinr = Math.abs(Math.sin(angle_offset_rad + view_rotation_rad));
		final double cosr = Math.cos(angle_offset_rad + view_rotation_rad);
		final double baseHeightcos = baseHeight*cosr;
		final double innerHeightcos = innerHeight*cosr;
		final double flangeHeightcos = flangeHeight*cosr;

		Path2D.Double path = new Path2D.Double();
		{// central pillar
			final double drawWidth = outerDiameter;
			final double drawHeight = outerDiameter*sinr;
			final Point2D.Double center = new Point2D.Double( instanceAbsoluteLocation.x, instanceAbsoluteLocation.y );
			Point2D.Double lowerLeft = new Point2D.Double( center.x - outerRadius, center.y-outerRadius*sinr);
			path.append( new Ellipse2D.Double( lowerLeft.x, lowerLeft.y, drawWidth, drawHeight), false);
			
			path.append( new Line2D.Double( lowerLeft.x,  center.y, lowerLeft.x, (center.y+baseHeightcos) ), false);
			path.append( new Line2D.Double( (center.x+outerRadius),  center.y, (center.x+outerRadius), (center.y+baseHeightcos) ), false);
			
			path.append( new Ellipse2D.Double( lowerLeft.x, (lowerLeft.y+baseHeightcos), drawWidth, drawHeight), false);
		}
		
		{// inner flange
			final double drawWidth = innerDiameter;
			final double drawHeight = innerDiameter*sinr;
			final Point2D.Double center = new Point2D.Double( instanceAbsoluteLocation.x, instanceAbsoluteLocation.y + baseHeightcos);
			final Point2D.Double lowerLeft = new Point2D.Double( center.x - innerRadius, center.y-innerRadius*sinr);
			path.append( new Ellipse2D.Double( lowerLeft.x, lowerLeft.y, drawWidth, drawHeight), false);
			
			path.append( new Line2D.Double( lowerLeft.x,  center.y, lowerLeft.x, (center.y+innerHeightcos) ), false);
			path.append( new Line2D.Double( (center.x+innerRadius),  center.y, (center.x+innerRadius), (center.y+innerHeightcos) ), false);
			
			path.append( new Ellipse2D.Double( lowerLeft.x, (lowerLeft.y+innerHeightcos), drawWidth, drawHeight), false);
		}
		{// outer flange
			final double drawWidth = outerDiameter;
			final double drawHeight = outerDiameter*sinr;
			final Point2D.Double center = new Point2D.Double( instanceAbsoluteLocation.x, instanceAbsoluteLocation.y+baseHeightcos+innerHeightcos);
			final Point2D.Double lowerLeft = new Point2D.Double( center.x - outerRadius, center.y-outerRadius*sinr);
			path.append( new Ellipse2D.Double( lowerLeft.x, lowerLeft.y, drawWidth, drawHeight), false);
			
			path.append( new Line2D.Double( lowerLeft.x,  center.y, lowerLeft.x, (center.y+flangeHeightcos) ), false);
			path.append( new Line2D.Double( (center.x+outerRadius),  center.y, (center.x+outerRadius), (center.y+flangeHeightcos) ), false);
			
			path.append( new Ellipse2D.Double( lowerLeft.x, (lowerLeft.y+flangeHeightcos), drawWidth, drawHeight), false);
		}
	
		return RocketComponentShape.toArray( new Shape[]{ path }, component );
	}
	

	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {
	
		RailButton btn = (RailButton)component;

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
		
		Coordinate[] inst = {transformation.transform(Coordinate.ZERO)};
		
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
		
		rect.moveTo( (x-radius*cosr), (y+radius*sinr));
		rect.lineTo( (x-radius*cosr+height*sinr), (y+radius*sinr+height*cosr));
		rect.lineTo( (x+radius*cosr+height*sinr), (y-radius*sinr+height*cosr));
		rect.lineTo( (x+radius*cosr), (y-radius*sinr));
		rect.closePath();
		
		return rect;
	}
}
